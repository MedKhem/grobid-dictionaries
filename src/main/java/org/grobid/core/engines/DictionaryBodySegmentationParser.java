package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import org.grobid.core.data.LabeledLexicalInformation;
import org.grobid.core.engines.label.*;
import org.grobid.core.layout.Page;
import org.grobid.core.utilities.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.IOUtils;
import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentUtils;
import org.grobid.core.document.TEIDictionaryFormatter;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL;
import static org.grobid.core.engines.label.EtymQuoteLabels.QUOTE__ETYMQUOTE_LABEL;
import static org.grobid.core.engines.label.EtymQuoteLabels.SEG_ETYMQUOTE_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_ETYM_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_FORM_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_SENSE_LABEL;
import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_DICTSCRAP_LABEL;
import static org.grobid.service.DictionaryPaths.*;

/**
 * Created by med on 02.08.16.
 */
public class DictionaryBodySegmentationParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionarySegmentationParser.class);
    private static volatile DictionaryBodySegmentationParser instance;
    private SortedSet<DocumentPiece> headNotesOfAllPages = new TreeSet();
    private SortedSet<DocumentPiece> footNotesOfAllPages= new TreeSet();
    private SortedSet<DocumentPiece> otherOfAllPages= new TreeSet();


    int pagesNumber;
    int currentHeadIndex;
    int currentFootIndex;
    int currentOtherIndex;

    public DictionaryBodySegmentationParser() {

        super(DictionaryModels.DICTIONARY_BODY_SEGMENTATION);
    }

    public static DictionaryBodySegmentationParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Create a new instance.
     */
    private static synchronized void getNewInstance() {
        instance = new DictionaryBodySegmentationParser();
    }

    public String processToTEI(File originFile, String modelToRun) {
        //This method is used by the service mode to display the segmentation result as text in tei-xml format
        //Prepare
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionaryDocument doc = null;

        try {
            doc = processing(originFile);
        } catch (GrobidException e) {
            throw e;
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }

        String segmentedBody = toTEIFormatDictionaryBodySegmentation(config, null, doc, modelToRun).toString();

        return segmentedBody;
    }

    public DictionaryDocument processing(File originFile) {
        // This method is to be called by the following parser
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionarySegmentationParser dictionaryParser = new DictionarySegmentationParser();
        DictionaryDocument doc = dictionaryParser.initiateProcessing(originFile, config);
        try {
            //Get Body
            SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

            //Get tokens from the body
            LayoutTokenization layoutTokenization = DocumentUtils.getLayoutTokenizations(doc, documentBodyParts);

            String bodytextFeatured = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization()).toString();
            String labeledFeatures = null;


            LabeledLexicalInformation structuredBody = new LabeledLexicalInformation();
            if (bodytextFeatured != null) {
                // if bodytextFeatured is null, it usually means that no body segment is found in the
                // document segmentation

                if ((bodytextFeatured != null) && (bodytextFeatured.trim().length() > 0)) {
                    labeledFeatures = label(bodytextFeatured);
                }

                structuredBody = extractBodyComponents(layoutTokenization, labeledFeatures);

                doc.setBodyComponents(structuredBody);
            }

            return doc;
        } catch (GrobidException e) {
            throw e;
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
    }


    public static LabeledLexicalInformation extractBodyComponents(LayoutTokenization layoutTokenization, String contentFeatured) {
        //Extract the lexical entries in a clusters of tokens for each lexical entry, ponctuation and other parts
        List<LayoutToken> tokenizations = layoutTokenization.getTokenization();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.DICTIONARY_BODY_SEGMENTATION, contentFeatured, tokenizations);

        List<TaggingTokenCluster> clusters = clusteror.cluster();
        LabeledLexicalInformation list1 = new LabeledLexicalInformation();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);
            String tagLabel = clusterLabel.getLabel();


            if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                list1.addLabel(new Pair(cluster.concatTokens(), tagLabel));
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_DICTSCRAP_LABEL)) {
                list1.addLabel(new Pair(cluster.concatTokens(), tagLabel));
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                list1.addLabel(new Pair(cluster.concatTokens(), tagLabel));
            } else {
                throw new IllegalArgumentException(tagLabel + " is not a valid possible tag");
            }


        }

        return list1;
    }

    public StringBuilder formatHeader(GrobidAnalysisConfig config,
                                      TEIDictionaryFormatter.SchemaDeclaration schemaDeclaration, DictionaryDocument doc) {
        StringBuilder headerTEI = new StringBuilder();
        StringBuilder tei = new StringBuilder();
        headerTEI.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        if (config.isWithXslStylesheet()) {
            headerTEI.append("<?xml-stylesheet type=\"text/xsl\" href=\"../jsp/xmlverbatimwrapper.xsl\"?> \n");
        }
        if (schemaDeclaration != null) {
            if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.DTD)) {
                headerTEI.append("<!DOCTYPE TEI SYSTEM \"" + GrobidProperties.get_GROBID_HOME_PATH()
                        + "/schemas/dtd/Grobid.dtd" + "\">\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.XSD)) {
                // XML schema
                headerTEI.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" \n" +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                        //"\n xsi:noNamespaceSchemaLocation=\"" +
                        //GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\""	+
                        "xsi:schemaLocation=\"http://www.tei-c.org/ns/1.0 " +
                        GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\"" +
                        "\n xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
//				"\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.RNG)) {
                // standard RelaxNG
                headerTEI.append("<?xml-model href=\"file://" +
                        GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rng" +
                        "\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.RNC)) {
                // compact RelaxNG
                headerTEI.append("<?xml-model href=\"file://" +
                        GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rnc" +
                        "\" type=\"application/relax-ng-compact-syntax\"?>\n");
            }

            // by default there is no schema association
            if (!schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.XSD)) {
                headerTEI.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
            }
        } else {
            headerTEI.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
        }

        if (doc.getLanguage() != null) {
            headerTEI.append("\t<teiHeader xml:lang=\"" + doc.getLanguage() + "\">");
        } else {
            headerTEI.append("\t<teiHeader>");
        }

        // encodingDesc gives info about the producer of the file
        headerTEI.append("\n\t\t<encodingDesc>\n");
        headerTEI.append("\t\t\t<appInfo>\n");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        headerTEI.append("\t\t\t\t<application version=\"" + GrobidProperties.getVersion() +
                "\" ident=\"GROBID\" when=\"" + dateISOString + "\">\n");
        headerTEI.append("\t\t\t\t\t<ref target=\"https://github.com/MedKhem/grobid-dictionaries\">GROBID_Dictionaries - A machine learning software for structuring digitized dictionaries</ref>\n");
        headerTEI.append("\t\t\t\t</application>\n");
        headerTEI.append("\t\t\t</appInfo>\n");
        headerTEI.append("\t\t</encodingDesc>");

        headerTEI.append("\n\t\t<fileDesc>\n\t\t\t<titleStmt>\n\t\t\t\t<title level=\"a\" type=\"main\"");
        if (config.isGenerateTeiIds()) {
            String divID = KeyGen.getKey().substring(0, 7);
            headerTEI.append(" xml:id=\"_" + divID + "\"");
        }
        headerTEI.append("/>");
        headerTEI.append("\n\t\t\t</titleStmt>\n");
        headerTEI.append("\n\t\t</fileDesc>\n");
        headerTEI.append("\t</teiHeader>\n");

        if (doc.getLanguage() != null) {
            headerTEI.append("\t<text xml:lang=\"").append(doc.getLanguage()).append("\">\n");
        } else {
            headerTEI.append("\t<text>\n");
        }
        headerTEI.append("\t\t<body>\n");

        return headerTEI;

    }

    public StringBuilder toTEIFormatDictionaryBodySegmentation(GrobidAnalysisConfig config,
                                                               TEIDictionaryFormatter.SchemaDeclaration schemaDeclaration, DictionaryDocument doc, String modelToRun) {
        StringBuilder headerTEI = new StringBuilder();
        StringBuilder tei = formatHeader(config, schemaDeclaration, doc);
        tei.append(headerTEI);
        if (doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL) != null){
            headNotesOfAllPages = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL);

        }
        if (doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL) != null){
            footNotesOfAllPages = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL);

        }
        if (doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_DICTSCRAP_LABEL) != null){
            otherOfAllPages = doc.getDocumentDictionaryPart(DICTIONARY_DICTSCRAP_LABEL);
        }





        pagesNumber = doc.getPages().size();
        currentHeadIndex = 1;
        currentFootIndex = 0;
        currentOtherIndex = 0;
        LayoutToken lastVisitedLayoutToken = new LayoutToken();
        lastVisitedLayoutToken.setPage(1);


        // Prepare an offset based index for pages
        List<Integer> pagesOffsetArray = new ArrayList<Integer>();

        for (Page page : doc.getPages()) {
            if (page.getBlocks() != null) {
                int beginOffSet = page.getBlocks().get(0).getTokens().get(0).getOffset();

                pagesOffsetArray.add(beginOffSet);
            }

        }


        // Prepare an offset based index for LEs
        List<Integer> lexicalEntriesOffsetArray = new ArrayList<Integer>();
        LabeledLexicalInformation bodyComponents = doc.getBodyComponents();
        int lexicalEntriesNumber = bodyComponents.getLabels().size();

        for (int i = 0; i < lexicalEntriesNumber; i++) {
            int beginLEOffSet = bodyComponents.getLabels().get(i).getA().get(0).getOffset();
            lexicalEntriesOffsetArray.add(beginLEOffSet);
        }

        Boolean bigEntryIsInsideDetected = false;
        List<Pair<List<LayoutToken>, String>> lexicalEntriesSubList = new ArrayList<>();

        if (modelToRun.equals(PATH_DICTIONARY_BODY_SEGMENTATATION)) {
            if (lexicalEntriesNumber > pagesNumber) {

                if(headNotesOfAllPages.size() != 0){
                    tei.append("\t\t<fw " + "type=\"header\">");
                    tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(headNotesOfAllPages.first())));
                    tei.append("</fw>\n");
                }




                if (pagesOffsetArray.size() > 1) {
                    int k = 0;
                    int lexicalEntryBeginIndex;
                    for (int pageOffsetIndex = 1; pageOffsetIndex <= pagesOffsetArray.size() - 1; pageOffsetIndex++) {

                        int newPageOffset = pagesOffsetArray.get(pageOffsetIndex);
                        lexicalEntryBeginIndex = k;
                        // Check if the lexical entries are recognized (exist)
                        if (k < lexicalEntriesOffsetArray.size()) {
                            while (lexicalEntriesOffsetArray.get(k) < newPageOffset) {
                                k++;
                            }
                        }
                        if (lexicalEntryBeginIndex >= k) {
                            // When entry is on more than one page, this is considered as anomaly for the moment. So quit and show in second form of the output
                            bigEntryIsInsideDetected = true;
                            break;
                        }
                        lexicalEntriesSubList = bodyComponents.getLabels().subList(lexicalEntryBeginIndex, k);
                        int subListSize = lexicalEntriesSubList.size();
                        Pair<List<LayoutToken>, String> lastEntryInSublist = lexicalEntriesSubList.get(subListSize - 1);

                        //Check if the last entry in the page is cut by the footer and header
                        if (lastEntryInSublist.getA().get(lastEntryInSublist.getA().size() - 1).getOffset() < newPageOffset) {
                            for (Pair<List<LayoutToken>, String> bodyComponent : lexicalEntriesSubList) {
                                List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                                String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                produceXmlNode(tei, clusterContent, bodyComponent.getB(),false);
                            }


                            treatEndOfPageAndBeginingOfSecondPage(tei, doc);


                        } else {
                            String textToShowInTokens;
                            int indexOfLastTokenInThePage = 0;
                            List<LayoutToken> lexicalEntry = bodyComponents.getLabels().get(k).getA();

                            for (LayoutToken token : lastEntryInSublist.getA()) {
                                //Check offset of each token in the LE to insert the header and footer blocks
                                if (token.getOffset() < newPageOffset) {

                                    indexOfLastTokenInThePage++;
                                } else {
//                            indexOfLastTokenInThePage--;
                                    break;
                                }

                            }


                            for (int h = 0; h < lexicalEntriesSubList.size() - 2; h++) {
                                List<LayoutToken> allTokensOfaLE = lexicalEntriesSubList.get(h).getA();
                                String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                String tag = lexicalEntriesSubList.get(h).getB();
                                produceXmlNode(tei, clusterContent, tag, false);
                            }

                            List<LayoutToken> firstPartOfLastLexicalEntry = lastEntryInSublist.getA().subList(0, indexOfLastTokenInThePage);
                            List<LayoutToken> restOfLexicalEntryTokens = lastEntryInSublist.getA().subList(indexOfLastTokenInThePage, lastEntryInSublist.getA().size());
                            String tagLabel = lastEntryInSublist.getB();
                            //Compound the last entry in tokens and insert the oher page blocks
                            textToShowInTokens = DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(firstPartOfLastLexicalEntry));


                            textToShowInTokens = treatEndOfSplitPage(textToShowInTokens, doc);


                            textToShowInTokens += DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(restOfLexicalEntryTokens));
                            String clusterContent = LayoutTokensUtil.normalizeText(textToShowInTokens);
                            produceXmlNodeWithSplitInside(tei, clusterContent, tagLabel, false);


                        }
                        if (pageOffsetIndex == pagesOffsetArray.size() - 1) {
                            lexicalEntriesSubList = bodyComponents.getLabels().subList(k, bodyComponents.getLabels().size());


                            for (Pair<List<LayoutToken>, String> bodyComponent : lexicalEntriesSubList) {
                                List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                                String tagLabel = bodyComponent.getB();
                                String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                produceXmlNode(tei, clusterContent, tagLabel,false);
                            }


                            treatEndOfLastPage(tei, doc);


                        }
                    }

                } else {
                    // In this case, the input file has just one page

                    for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents.getLabels()) {
                        List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                        String clusterContent = LayoutTokensUtil.normalizeText(allTokensOfaLE);
                        String tagLabel = bodyComponent.getB();
                        produceXmlNode(tei, clusterContent, tagLabel, false);
                    }

                    simpleDisplayEndOfPage(tei, doc);


                }
                if (bigEntryIsInsideDetected) {
                    tei = headerTEI;
                    if(headNotesOfAllPages.size() != 0){
                        for (DocumentPiece header : headNotesOfAllPages) {
                            tei.append("\t\t<fw type=\"header\">");
                            tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(header)));
                            tei.append("</fw>");
                        }
                    }


                    for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents.getLabels()) {
                        List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                        String clusterContent = LayoutTokensUtil.normalizeText(allTokensOfaLE);
                        String tagLabel = bodyComponent.getB();
                        if (bodyComponent.getB().equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                            produceXmlNode(tei, clusterContent, tagLabel,false);
                        }

                    }

                    simpleDisplayEndOfPage(tei, doc);


                }
            } else {
                // This is caused probably by a lack of training. So just try to show what is already recognized in a consistent way

                if(headNotesOfAllPages.size() != 0){
                    for (DocumentPiece header : headNotesOfAllPages) {
                        tei.append("\t\t<fw type=\"header\">");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(header)));
                        tei.append("</fw>");
                    }
                }


                for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents.getLabels()) {
                    List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                    String clusterContent = LayoutTokensUtil.normalizeText(allTokensOfaLE);
                    String tagLabel = bodyComponent.getB();
                    produceXmlNode(tei, clusterContent, tagLabel,false);
                }

                simpleDisplayEndOfPage(tei, doc);


            }

        } else if (modelToRun.equals(PATH_LEXICAL_ENTRY)) {
            LexicalEntryParser lexicalEntryParser = new LexicalEntryParser();
            if (lexicalEntriesNumber > pagesNumber) {
                if(headNotesOfAllPages.size() != 0) {
                    tei.append("\t\t<fw " + "type=\"header\">");
                    tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(headNotesOfAllPages.first())));
                    tei.append("</fw>\n");

                }
                if (pagesOffsetArray.size() > 1) {
                    int k = 0;
                    int lexicalEntryBeginIndex;
                    for (int pageOffsetIndex = 1; pageOffsetIndex <= pagesOffsetArray.size() - 1; pageOffsetIndex++) {

                        int newPageOffset = pagesOffsetArray.get(pageOffsetIndex);
                        lexicalEntryBeginIndex = k;
                        // Check if the lexical entries are recognized (exist)
                        if (k < lexicalEntriesOffsetArray.size()) {
                            while (lexicalEntriesOffsetArray.get(k) < newPageOffset) {
                                k++;
                            }
                        }
                        if (lexicalEntryBeginIndex >= k) {
                            // When entry is on more than one page, this is considered as anomaly for the moment. So quit and show in second form of the output
                            bigEntryIsInsideDetected = true;
                            break;
                        }
                        lexicalEntriesSubList = bodyComponents.getLabels().subList(lexicalEntryBeginIndex, k);
                        int subListSize = lexicalEntriesSubList.size();
                        LabeledLexicalInformation lastEntryInSublist = lexicalEntryParser.process(lexicalEntriesSubList.get(subListSize - 1).getA(), DICTIONARY_ENTRY_LABEL);

                        //Check if the last entry in the page is cut by the footer and header
                        List<LayoutToken> lastComponent = lastEntryInSublist.getLabels().get(lastEntryInSublist.getLabels().size() - 1).getA();

                        if (lastComponent.get(lastComponent.size() - 1).getOffset() < newPageOffset) {
                            for (Pair<List<LayoutToken>, String> bodyComponent : lexicalEntriesSubList) {
                                List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                                String clusterContent;
                                if (bodyComponent.getB().equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                                    clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                } else {
                                    clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                                }

                                produceXmlNode(tei, clusterContent, bodyComponent.getB(), true);
                            }


                            treatEndOfPageAndBeginingOfSecondPage(tei, doc);


                        } else {
                            String textToShowInTokens ="";
                            int indexOfLastTokenInThePage = 0;

                            //Find first the token just before the split
                            for (Pair<List<LayoutToken>, String> entryComponent : lastEntryInSublist.getLabels()) {
                                //Check offset of each token in the LE to insert the header and footer blocks
                                LayoutToken lastTokenOfTheEntryComponent = entryComponent.getA().get(entryComponent.getA().size() - 1);

                                if (lastTokenOfTheEntryComponent.getOffset() > newPageOffset) {
                                    for (LayoutToken token : entryComponent.getA()) {
                                        if (token.getOffset() < newPageOffset) {
                                            indexOfLastTokenInThePage++;
                                        } else {
                                            break;
                                        }

                                    }
                                    break;
                                }

                            }


                            for (int h = 0; h < lexicalEntriesSubList.size() - 1; h++) {
                                List<LayoutToken> allTokensOfaLE = lexicalEntriesSubList.get(h).getA();
                                String tag = lexicalEntriesSubList.get(h).getB();
                                String clusterContent;
                                if (tag.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                                    clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                } else {
                                    clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                                }
                                produceXmlNode(tei, clusterContent, tag,true);
                            }

                            boolean splitProcessed = false;
                            for (int h = 0; h < lastEntryInSublist.getLabels().size(); h++) {

                                String tagOfSplitComponent = lastEntryInSublist.getLabels().get(h).getB();
                                List<LayoutToken> componentOfLastLexicalEntry = lastEntryInSublist.getLabels().get(h).getA();
                                //if the last token of the component is on the second page && the split is not yet processed, then split
                                if ((componentOfLastLexicalEntry.get(componentOfLastLexicalEntry.size() - 1).getOffset() > newPageOffset) && (splitProcessed == false)) {
                                    splitProcessed = true;
                                    List<LayoutToken> firstPartOfSplitComponent = componentOfLastLexicalEntry.subList(0, indexOfLastTokenInThePage);
                                    List<LayoutToken> restOfSplitComponentTokens = componentOfLastLexicalEntry.subList(indexOfLastTokenInThePage, componentOfLastLexicalEntry.size());

                                    //Compound the element to split in tokens and insert the oher page blocks
                                    textToShowInTokens= tagOfSplitComponent;
                                    textToShowInTokens += DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(firstPartOfSplitComponent));


                                    textToShowInTokens = treatEndOfSplitPage(textToShowInTokens, doc);


                                    textToShowInTokens += DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(restOfSplitComponentTokens));
                                    textToShowInTokens += tagOfSplitComponent.replace("<", "</");

                                } else {
                                    String tag = lastEntryInSublist.getLabels().get(h).getB().replace("<", "").replace(">", "");
                                    textToShowInTokens += createMyXMLString(tag, DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(componentOfLastLexicalEntry)));
                                }

                            }
                            String tagLabel = lexicalEntriesSubList.get(lexicalEntriesSubList.size() - 1).getB();
                            String clusterContent = LayoutTokensUtil.normalizeText(textToShowInTokens);
                            produceXmlNodeWithSplitInside(tei, clusterContent, tagLabel, true);

                        }
                        if (pageOffsetIndex == pagesOffsetArray.size() - 1) {
                            lexicalEntriesSubList = bodyComponents.getLabels().subList(k, bodyComponents.getLabels().size());

                            for (Pair<List<LayoutToken>, String> bodyComponent : lexicalEntriesSubList) {
                                List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                                String tagLabel = bodyComponent.getB();
                                String clusterContent;
                                if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                                    clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                } else {
                                    clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                                }
                                produceXmlNode(tei, clusterContent, tagLabel, true);
                            }


                            treatEndOfLastPage(tei, doc);


                        }
                    }

                } else {
                    // In this case, the input file has just one page

                    for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents.getLabels()) {
                        List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                        String clusterContent;
                        String tagLabel = bodyComponent.getB();
                        if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                            clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                        } else {
                            clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                        }
                        produceXmlNode(tei, clusterContent, tagLabel, true);
                    }

                    simpleDisplayEndOfPage(tei, doc);


                }
                if (bigEntryIsInsideDetected) {
                    tei = headerTEI;
                    tei = bigEntryFormat(modelToRun, tei, bodyComponents, doc);

                }
            } else {
                // This is caused probably by a lack of training. So just try to show what is already recognized in a consistent way
                if(headNotesOfAllPages.size() != 0) {

                    for (DocumentPiece header : headNotesOfAllPages) {
                        tei.append("\t\t<fw type=\"header\">");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(header)));
                        tei.append("</fw>");
                    }
                }

                for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents.getLabels()) {
                    List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                    String clusterContent;
                    String tagLabel = bodyComponent.getB();
                    if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                        clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                    } else {
                        clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                    }
                    produceXmlNode(tei, clusterContent, tagLabel, true);
                }

                simpleDisplayEndOfPage(tei, doc);


            }

        } else if (modelToRun.equals(PATH_FULL_DICTIONARY)) {
            LexicalEntryParser lexicalEntryParser = new LexicalEntryParser();
            FormParser formParser = new FormParser();
            SenseParser senseParser = new SenseParser();
            if (lexicalEntriesNumber > pagesNumber) {
                if(headNotesOfAllPages.size() != 0) {
                    tei.append("\t\t<fw " + "type=\"header\">");
                    tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(headNotesOfAllPages.first())));
                    tei.append("</fw>\n");
                }

                if (pagesOffsetArray.size() > 1) {
                    int k = 0;
                    int lexicalEntryBeginIndex;
                    for (int pageOffsetIndex = 1; pageOffsetIndex <= pagesOffsetArray.size() - 1; pageOffsetIndex++) {

                        int newPageOffset = pagesOffsetArray.get(pageOffsetIndex);
                        lexicalEntryBeginIndex = k;
                        // Check if the lexical entries are recognized (exist)
                        if (k < lexicalEntriesOffsetArray.size()) {
                            while (lexicalEntriesOffsetArray.get(k) < newPageOffset) {
                                k++;
                            }
                        }
                        if (lexicalEntryBeginIndex >= k) {
                            // When entry is on more than one page, this is considered as anomaly for the moment. So quit and show in second form of the output
                            bigEntryIsInsideDetected = true;
                            break;
                        }
                        lexicalEntriesSubList = bodyComponents.getLabels().subList(lexicalEntryBeginIndex, k);
                        int subListSize = lexicalEntriesSubList.size();
                        LabeledLexicalInformation lastEntryInSublist = lexicalEntryParser.process(lexicalEntriesSubList.get(subListSize - 1).getA(), DICTIONARY_ENTRY_LABEL);

                        //Check if the last entry in the page is cut by the footer and header
                        List<LayoutToken> lastComponent = lastEntryInSublist.getLabels().get(lastEntryInSublist.getLabels().size() - 1).getA();

                        if (lastComponent.get(lastComponent.size() - 1).getOffset() < newPageOffset) {
                            for (Pair<List<LayoutToken>, String> bodyComponent : lexicalEntriesSubList) {
                                List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                                String clusterContent = "";
                                String tagLabel = bodyComponent.getB();
                                if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                                    clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                } else {
                                    LabeledLexicalInformation parsedLexicalEntry = lexicalEntryParser.process(allTokensOfaLE, modelToRun);
                                    for (Pair<List<LayoutToken>, String> segmentedEntryComponent : parsedLexicalEntry.getLabels()) {
                                        if (segmentedEntryComponent.getB().equals(LEXICAL_ENTRY_FORM_LABEL)) {
                                            clusterContent = clusterContent + formParser.processToTEI(segmentedEntryComponent.getA()).toString();

                                        } else if (segmentedEntryComponent.getB().equals(LEXICAL_ENTRY_SENSE_LABEL)) {

                                            clusterContent = clusterContent + senseParser.processToTEI(segmentedEntryComponent.getA()).toString();
                                        } else {
                                            String xmlTag = segmentedEntryComponent.getB().replace("<", "").replace(">", "");
                                            clusterContent = clusterContent + createMyXMLString(xmlTag, LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(segmentedEntryComponent.getA())));


                                        }

                                    }

                                }
                                produceXmlNode(tei, clusterContent, tagLabel, true);
                            }


                            treatEndOfPageAndBeginingOfSecondPage(tei, doc);


                        } else {
                            String textToShowInTokens = "";


                            //Parse all components of the lexical entries, which come just before the last LE
                            for (int h = 0; h < lexicalEntriesSubList.size() - 1; h++) {
                                Pair<List<LayoutToken>, String> bodyComponent = lexicalEntriesSubList.get(h);
                                processFullABodyComponentToTEI(bodyComponent, tei, modelToRun);

                            }
                            //Take care of the components of the last LE to wrap the split element
                            boolean splitProcessed = false;
                            int indexOfLastTokenInThePage;


                            for (int h = 0; h < lastEntryInSublist.getLabels().size(); h++) {
                                LabeledLexicalInformation segmentedEntryComponent = new LabeledLexicalInformation();
                                String tagOfSplitEntryComponent = lastEntryInSublist.getLabels().get(h).getB();
                                List<LayoutToken> componentOfLastLexicalEntry = lastEntryInSublist.getLabels().get(h).getA();
                                //Gather all segmented forms, senses..
                                if (tagOfSplitEntryComponent.equals(LEXICAL_ENTRY_FORM_LABEL) || tagOfSplitEntryComponent.equals(LEXICAL_ENTRY_SENSE_LABEL)) {
                                    if (tagOfSplitEntryComponent.equals(LEXICAL_ENTRY_FORM_LABEL)) {
                                        segmentedEntryComponent.setLabels(formParser.process(componentOfLastLexicalEntry).getLabels());
                                    }
                                    if (tagOfSplitEntryComponent.equals(LEXICAL_ENTRY_SENSE_LABEL)) {
                                        segmentedEntryComponent.setLabels(senseParser.process(componentOfLastLexicalEntry).getLabels());
                                    }
                                    // lastSecondLevelComponent could be, for example, a component of form or sense
                                    List<LayoutToken> lastSecondLevelComponent = segmentedEntryComponent.getLabels().get(segmentedEntryComponent.getLabels().size() - 1).getA();
                                    if (lastSecondLevelComponent.get(lastSecondLevelComponent.size() - 1).getOffset() >= newPageOffset) {
                                        //Parse all the components in form, sense.., to find the split token or just to add the component to the textToShowInTokens

                                        //Insert first the segmentedEntry label in textToShowInTokens
                                        textToShowInTokens += tagOfSplitEntryComponent;
                                        for (Pair<List<LayoutToken>, String> secondLevelComponent : segmentedEntryComponent.getLabels()) {
                                            indexOfLastTokenInThePage = 0;
                                            LayoutToken lastTokenOfTheSecondLevelComponent = secondLevelComponent.getA().get(secondLevelComponent.getA().size() - 1);

                                            if ((lastTokenOfTheSecondLevelComponent.getOffset() >= newPageOffset) && (splitProcessed == false)) {
                                                splitProcessed = true;
                                                for (LayoutToken token : secondLevelComponent.getA()) {
                                                    if (token.getOffset() < newPageOffset) {
                                                        //Increment until the split token is found
                                                        indexOfLastTokenInThePage++;

                                                    } else {
                                                        //The split token is found, so wrap the current component
                                                        List<LayoutToken> firstPartOfSplitComponent = lastSecondLevelComponent.subList(0, indexOfLastTokenInThePage);
                                                        List<LayoutToken> restOfSplitComponentTokens = lastSecondLevelComponent.subList(indexOfLastTokenInThePage, lastSecondLevelComponent.size());

                                                        //Wrap the second level element
                                                        textToShowInTokens += secondLevelComponent.getB();
                                                        textToShowInTokens += DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(firstPartOfSplitComponent));


                                                        textToShowInTokens = treatEndOfSplitPage(textToShowInTokens, doc);


                                                        textToShowInTokens += DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(restOfSplitComponentTokens));
                                                        textToShowInTokens += secondLevelComponent.getB().replace("<", "</");
                                                        break;
                                                    }

                                                }

                                            } else {
                                                //if the split component is not the current or if it's already processed
                                                textToShowInTokens += secondLevelComponent.getB();
                                                textToShowInTokens += DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(secondLevelComponent.getA()));
                                                textToShowInTokens += secondLevelComponent.getB().replace("<", "</");
                                            }
                                        }

                                        textToShowInTokens +=tagOfSplitEntryComponent.replace("<", "</");


                                    } else {
                                        //Case where there is nothing to split
                                        String tag = lastEntryInSublist.getLabels().get(h).getB().replace("<", "").replace(">", "");
                                        String formattedSecondLevelComponent = checkFullABodyComponentToTEI(tag, componentOfLastLexicalEntry, modelToRun);
                                        textToShowInTokens += createMyXMLString(tag, formattedSecondLevelComponent);
                                    }


                                } else {
                                    // Elements not yet segmented (like other or pc) add them as well
                                    //Find first the split token (like in the lexical entry level)


                                    //if the last token of the component is on the second page && the split is not yet processed, then split
                                    if ((componentOfLastLexicalEntry.get(componentOfLastLexicalEntry.size() - 1).getOffset() > newPageOffset) && (splitProcessed == false)) {
                                        indexOfLastTokenInThePage = 0;
                                        textToShowInTokens = "";

                                        //Find first the token just before the split
                                        for (Pair<List<LayoutToken>, String> entryComponent : lastEntryInSublist.getLabels()) {
                                            //Check offset of each token in the LE to insert the header and footer blocks
                                            LayoutToken lastTokenOfTheEntryComponent = entryComponent.getA().get(entryComponent.getA().size() - 1);

                                            if (lastTokenOfTheEntryComponent.getOffset() > newPageOffset) {
                                                for (LayoutToken token : entryComponent.getA()) {
                                                    if (token.getOffset() < newPageOffset) {
                                                        indexOfLastTokenInThePage++;
                                                    } else {
                                                        break;
                                                    }

                                                }
                                                break;
                                            }

                                        }
                                        splitProcessed = true;
                                        List<LayoutToken> firstPartOfSplitComponent = componentOfLastLexicalEntry.subList(0, indexOfLastTokenInThePage);
                                        List<LayoutToken> restOfSplitComponentTokens = componentOfLastLexicalEntry.subList(indexOfLastTokenInThePage, componentOfLastLexicalEntry.size());

                                        //Compound the element to split in tokens and insert the oher page blocks
                                        textToShowInTokens += tagOfSplitEntryComponent;
                                        textToShowInTokens += DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(firstPartOfSplitComponent));


                                        textToShowInTokens = treatEndOfSplitPage(textToShowInTokens, doc);


                                        textToShowInTokens += DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(restOfSplitComponentTokens));
                                        textToShowInTokens += tagOfSplitEntryComponent.replace("<", "</");

                                    } else {
                                        String tag = lastEntryInSublist.getLabels().get(h).getB().replace("<", "").replace(">", "");
                                        textToShowInTokens += createMyXMLString(tag, DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(componentOfLastLexicalEntry)));
                                    }


                                }


                            }
                            String tagLabel = lexicalEntriesSubList.get(lexicalEntriesSubList.size() - 1).getB();
                            String clusterContent = LayoutTokensUtil.normalizeText(textToShowInTokens);
                            produceXmlNodeWithSplitInside(tei, clusterContent, tagLabel, true);


                        }
                        if (pageOffsetIndex == pagesOffsetArray.size() - 1) {
                            lexicalEntriesSubList = bodyComponents.getLabels().subList(k, bodyComponents.getLabels().size());

                            for (Pair<List<LayoutToken>, String> bodyComponent : lexicalEntriesSubList) {
                                processFullABodyComponentToTEI(bodyComponent, tei, modelToRun);
                            }


                            treatEndOfLastPage(tei, doc);


                        }
                    }

                } else {
                    // In this case, the input file has just one page

                    for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents.getLabels()) {
                        processFullABodyComponentToTEI(bodyComponent, tei, modelToRun);
                    }

                    simpleDisplayEndOfPage(tei, doc);


                }
                if (bigEntryIsInsideDetected) {
                    tei = headerTEI;
                    tei = bigEntryFormat(modelToRun, tei, bodyComponents, doc);

                }
            } else {
                // This is caused probably by a lack of training. So just try to show what is already recognized in a consistent way

                if(headNotesOfAllPages.size() != 0) {
                    for (DocumentPiece header : headNotesOfAllPages) {
                        tei.append("\t\t<fw type=\"header\">");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(header)));
                        tei.append("</fw>");
                    }
                }

                for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents.getLabels()) {
                    processFullABodyComponentToTEI(bodyComponent, tei, modelToRun);
                }

                simpleDisplayEndOfPage(tei, doc);


            }
        } else {

        }


        tei.append("\t\t</body>\n");
        tei.append("\t</text>\n");
        tei.append("</TEI>\n");

        return tei;
    }


    @SuppressWarnings({"UnusedParameters"})
    public int createTrainingBatch(String inputDirectory, String outputDirectory) throws IOException {
        // This method is to create feature matrix and create pre-annotated data using the existing model
        try {
            File path = new File(inputDirectory);
            if (!path.exists()) {
                throw new GrobidException("Cannot create training data because input directory can not be accessed: " + inputDirectory);
            }

            File pathOut = new File(outputDirectory);
            if (!pathOut.exists()) {
                throw new GrobidException("Cannot create training data because ouput directory can not be accessed: " + outputDirectory);
            }

            int n = 0;
            // we process all pdf files in the directory
            if (path.isDirectory()) {
                for (File fileEntry : path.listFiles()) {
                    // Create the pre-annotated file and the raw text
                    createTrainingDictionaryBody(fileEntry, outputDirectory, false);
                    n++;
                }

            } else {
                createTrainingDictionaryBody(path, outputDirectory, false);
                n++;

            }


            System.out.println(n + " files to be processed.");

            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    public int createAnnotatedTrainingBatch(String inputDirectory, String outputDirectory) throws IOException {
        // This method is to create feature matrix and create pre-annotated data using the existing model
        try {
            File path = new File(inputDirectory);
            if (!path.exists()) {
                throw new GrobidException("Cannot create training data because input directory can not be accessed: " + inputDirectory);
            }

            File pathOut = new File(outputDirectory);
            if (!pathOut.exists()) {
                throw new GrobidException("Cannot create training data because ouput directory can not be accessed: " + outputDirectory);
            }

            int n = 0;
            // we process all pdf files in the directory
            if (path.isDirectory()) {
                for (File fileEntry : path.listFiles()) {
                    // Create the pre-annotated file and the raw text
                    createTrainingDictionaryBody(fileEntry, outputDirectory, true);
                    n++;
                }

            } else {
                createTrainingDictionaryBody(path, outputDirectory, true);
                n++;

            }


            System.out.println(n + " files to be processed.");

            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingDictionaryBody(File path, String outputDirectory, boolean isAnnotated) throws Exception {

        // Segment the doc
        DictionaryDocument doc = processing(path);
        //Get Body
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

        //Get tokens from the body
        LayoutTokenization tokenizations = DocumentUtils.getLayoutTokenizations(doc, documentBodyParts);

        String bodyTextFeatured = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(tokenizations.getTokenization()).toString();
        //Write the features file
        String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation";
        Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
        writer.write(bodyTextFeatured);
        IOUtils.closeWhileHandlingException(writer);

        // also write the raw text as seen before segmentation
        StringBuffer rawtxt = new StringBuffer();
        for (LayoutToken txtline : tokenizations.getTokenization()) {
            rawtxt.append(txtline.getText());
        }
        String outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation.rawtxt";
        FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");

        //Create rng and css files for guiding the annotation
        File existingRngFile = new File("templates/dictionaryBodySegmentation.rng");
        File newRngFile = new File(outputDirectory + "/" + "dictionaryBodySegmentation.rng");
        copyFileUsingStream(existingRngFile, newRngFile);

        File existingCssFile = new File("templates/dictionaryBodySegmentation.css");
        File newCssFile = new File(outputDirectory + "/" + "dictionaryBodySegmentation.css");
        copyFileUsingStream(existingCssFile, newCssFile);

        StringBuffer bufferFulltext = new StringBuffer();

        if (isAnnotated) {

            String rese = label(bodyTextFeatured);
            bufferFulltext.append(trainingExtraction(doc, rese, tokenizations));

        } else {
            bufferFulltext.append(DocumentUtils.replaceLinebreaksWithTags(DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(tokenizations.getTokenization()))));
        }


        //Using the existing model of the parser to generate a pre-annotate tei file to be corrected


        // write the TEI file to reflect the exact layout of the text as extracted from the pdf
        String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation.tei.xml";
        writer = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<?xml-model href=\"dictionaryBodySegmentation.rng\" type=\"application/xml\" schematypens=\"http://relaxng.org/ns/structure/1.0\"\n" +
                "?>\n" + "<?xml-stylesheet type=\"text/css\" href=\"dictionaryBodySegmentation.css\"?>\n" +
                "<tei xml:space=\"preserve\">\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                "\"/>\n\t</teiHeader>\n\t<text>");

        writer.write("\n\t\t<body>");
        writer.write(bufferFulltext.toString().replaceAll("&","&amp;"));
        writer.write("</body>");
        writer.write("\n\t</text>\n</tei>\n");
        writer.close();

    }

    /**
     * Extract results from a labelled full text in the training format without any string modification.
     *
     * @param result        reult
     * @param tokenizations toks
     * @return extraction
     */
    private StringBuilder trainingExtraction(DictionaryDocument doc, String result, LayoutTokenization tokenizations) {

        StringBuilder buffer = new TEIDictionaryFormatter(doc).toTEIDictionaryBodySegmentation(result, tokenizations);
        return buffer;
    }

    private void produceXmlNode(StringBuilder buffer, String clusterContent, String tagLabel, Boolean clusterContentIsEscaped) {

    if(!clusterContentIsEscaped){
        clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
        clusterContent = DocumentUtils.escapeHTMLCharac(clusterContent);
    }

        if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
            buffer.append(createMyXMLString("entry", clusterContent));
        } else if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_DICTSCRAP_LABEL)) {
            buffer.append(createMyXMLString("dictScrap", clusterContent));
        } else if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
            buffer.append(createMyXMLString("pc", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_FORM_LABEL)) {
            buffer.append(createMyXMLString("form", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_ETYM_LABEL)) {
            buffer.append(createMyXMLString("etym", clusterContent));
        } else if (tagLabel.equals(LEXICAL_ENTRY_SENSE_LABEL)) {
            buffer.append(createMyXMLString("sense", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_RE_LABEL)) {
            buffer.append(createMyXMLString("re", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_OTHER_LABEL)) {
            buffer.append(createMyXMLString("dictScrap", clusterContent));
        } else if (tagLabel.equals(EtymQuoteLabels.ETYM_QUOTE_SEG)) {
            buffer.append(createMyXMLString("quote", clusterContent));
        } else if (tagLabel.equals(EtymQuoteLabels.ETYM_QUOTE_SEG)) {
            buffer.append(createMyXMLString("seg", clusterContent));
        } else if (tagLabel.equals(EtymLabels.SEG_ETYM_LABEL)) {
            buffer.append(createMyXMLString("seg", clusterContent));
        } else if (tagLabel.equals(EtymLabels.BIBL_ETYM_LABEL)) {
            buffer.append(createMyXMLString("bibl", clusterContent));
        } else if (tagLabel.equals(EtymLabels.DEF_ETYM_LABEL)) {
            buffer.append(createMyXMLString("def", clusterContent));
        } else if (tagLabel.equals(EtymLabels.MENTIONED_ETYM_LABEL)) {
            buffer.append(createMyXMLString("mentioned", clusterContent));
        } else if (tagLabel.equals(EtymLabels.LANG_ETYM_LABEL)) {
            buffer.append(createMyXMLString("lang", clusterContent));
        } else {
            throw new IllegalArgumentException(tagLabel + " is not a valid possible tag");
        }
    }

    private void produceXmlNodeWithSplitInside(StringBuilder buffer, String clusterContent, String tagLabel, Boolean clusterContentIsEscaped) {

        if(!clusterContentIsEscaped){
            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");

        }

        if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
            buffer.append(createMyXMLString("entry", clusterContent));
        } else if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_DICTSCRAP_LABEL)) {
            buffer.append(createMyXMLString("dictScrap", clusterContent));
        } else if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
            buffer.append(createMyXMLString("pc", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_FORM_LABEL)) {
            buffer.append(createMyXMLString("form", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_ETYM_LABEL)) {
            buffer.append(createMyXMLString("etym", clusterContent));
        } else if (tagLabel.equals(LEXICAL_ENTRY_SENSE_LABEL)) {
            buffer.append(createMyXMLString("sense", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_RE_LABEL)) {
            buffer.append(createMyXMLString("re", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_OTHER_LABEL)) {
            buffer.append(createMyXMLString("dictScrap", clusterContent));
        } else if (tagLabel.equals(EtymQuoteLabels.ETYM_QUOTE_SEG)) {
            buffer.append(createMyXMLString("quote", clusterContent));
        } else if (tagLabel.equals(EtymQuoteLabels.ETYM_QUOTE_SEG)) {
            buffer.append(createMyXMLString("seg", clusterContent));
        } else if (tagLabel.equals(EtymLabels.SEG_ETYM_LABEL)) {
            buffer.append(createMyXMLString("seg", clusterContent));
        } else if (tagLabel.equals(EtymLabels.BIBL_ETYM_LABEL)) {
            buffer.append(createMyXMLString("bibl", clusterContent));
        } else if (tagLabel.equals(EtymLabels.DEF_ETYM_LABEL)) {
            buffer.append(createMyXMLString("def", clusterContent));
        } else if (tagLabel.equals(EtymLabels.MENTIONED_ETYM_LABEL)) {
            buffer.append(createMyXMLString("mentioned", clusterContent));
        } else if (tagLabel.equals(EtymLabels.LANG_ETYM_LABEL)) {
            buffer.append(createMyXMLString("lang", clusterContent));
        } else {
            throw new IllegalArgumentException(tagLabel + " is not a valid possible tag");
        }
    }

    public static String createMyXMLString(String elementName, String elementContent) {
        StringBuilder xmlStringElement = new StringBuilder();
        xmlStringElement.append("<");
        xmlStringElement.append(elementName);
        xmlStringElement.append(">");
        xmlStringElement.append(elementContent);
        xmlStringElement.append("</");
        xmlStringElement.append(elementName);
        xmlStringElement.append(">");
        xmlStringElement.append("\n");

        return xmlStringElement.toString();
    }

    public StringBuilder bigEntryFormat(String modelToRun, StringBuilder tei, LabeledLexicalInformation bodyComponents, DictionaryDocument doc) {


        LexicalEntryParser lexicalEntryParser = new LexicalEntryParser();
        FormParser formParser = new FormParser();
        SenseParser senseParser = new SenseParser();
        if(headNotesOfAllPages.size() != 0) {
            for (DocumentPiece header : headNotesOfAllPages) {
                tei.append("\t\t<fw type=\"header\">");
                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(header)));
                tei.append("</fw>");
            }
        }

        for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents.getLabels()) {
            List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
            String clusterContent = "";
            String tagLabel = bodyComponent.getB();
            if (modelToRun.equals(PATH_DICTIONARY_BODY_SEGMENTATATION)) {

            } else if (modelToRun.equals(PATH_LEXICAL_ENTRY)) {

                if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                    clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));

                } else {
                    clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                }

            } else if (modelToRun.equals(PATH_FULL_DICTIONARY)) {
                LabeledLexicalInformation segmentedEntry = lexicalEntryParser.process(allTokensOfaLE, modelToRun);
                for (Pair<List<LayoutToken>, String> entryComoonent : segmentedEntry.getLabels()) {


                }

            }


//                        if (bodyComponent.getB().equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
            produceXmlNode(tei, clusterContent, tagLabel, true);
//                        }

        }

        simpleDisplayEndOfPage(tei, doc);


        return tei;
    }

    private void treatEndOfPageAndBeginingOfSecondPage(StringBuilder tei, DictionaryDocument doc) {
        if (currentFootIndex < footNotesOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex))) != "") {
            // With this check, just one foot note that doesn't correspond to the right page could stop showing the rest of the footnotes in the Stack.
            // This is caused by the forced check of the footnote'a index and its supposed page
            // Need to choose between showing things in their right spots or show them and it  doesn't matter if they are correctly labelled
//                                if (lastVisitedLayoutToken.getPage() == currentFootIndex+1) {
            tei.append("\t\t<fw type=\"footer\">");
            tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex))));
            currentFootIndex++;
            tei.append("</fw>");
            tei.append("\n");
//                                }
        }


        if (currentOtherIndex < otherOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))) != "") {
            // Same logic as the footnote
//                                if (lastVisitedLayoutToken.getPage() == currentFootIndex+1) {
            tei.append("\t\t<dictScrap>");
            tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))));
            currentOtherIndex++;
            tei.append("</dictScrap>");
            tei.append("\n");
//                                }
        }

        tei.append("<pb/>");

            if (currentHeadIndex < headNotesOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(headNotesOfAllPages, currentHeadIndex))) != "") {
                tei.append("\t\t<fw type=\"header\">");
                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(headNotesOfAllPages, currentHeadIndex))));
                currentHeadIndex++;
                tei.append("</fw>");

            }
    }

    private void simpleDisplayEndOfPage(StringBuilder tei, DictionaryDocument doc) {
        if(footNotesOfAllPages.size() != 0) {
            for (DocumentPiece footer : footNotesOfAllPages) {

                tei.append("\t\t<fw type=\"footer\">");
                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(footer)));
                currentFootIndex++;
                tei.append("</fw>");
                tei.append("\n");
            }
        }
        if(otherOfAllPages.size() != 0) {
            for (DocumentPiece other : otherOfAllPages) {
                tei.append("\t\t<dictScrap>");
                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(other)));
                currentOtherIndex++;
                tei.append("</dictScrap>");
                tei.append("\n");
            }
        }

    }

    private void treatEndOfLastPage(StringBuilder tei, DictionaryDocument doc) {
        if (currentFootIndex < footNotesOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex))) != "") {
            // With this check, just one foot note that doesn't correspond to the right page could stop showing the rest of the footnotes in the Stack.
            // This is caused by the forced check of the footnote'a index and its supposed page
            // Need to choose between showing things in their right spots or show them and it  doesn't matter if they are correctly labelled
            tei.append("\t\t<fw type=\"footer\">");
            tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex))));
            currentFootIndex++;
            tei.append("</fw>");
            tei.append("\n");

        }


        if (currentOtherIndex < otherOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))) != "") {
            // Same logic as the footnote
            tei.append("\t\t<dictScrap>");
            tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))));
            currentOtherIndex++;
            tei.append("</dictScrap>");
            tei.append("\n");
        }
    }

    private String treatEndOfSplitPage(String textToShowInTokens, DictionaryDocument doc) {
        if (currentFootIndex < footNotesOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex))) != "") {
            textToShowInTokens += "\t\t<fw type=\"footer\">";
            textToShowInTokens += doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex));
            currentFootIndex++;
            textToShowInTokens += "</fw>";
            textToShowInTokens += "\n";

        }


        if (currentOtherIndex < otherOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))) != "") {
            textToShowInTokens += "\t\t<dictScrap>";
            textToShowInTokens += doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex));
            currentOtherIndex++;
            textToShowInTokens += "</dictScrap>";
            textToShowInTokens += "\n";
        }
        textToShowInTokens += "\t\t<pb/>";


            if (currentHeadIndex < headNotesOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(headNotesOfAllPages, currentHeadIndex))) != "") {

                textToShowInTokens += "\t\t<fw type=\"header\">";
                textToShowInTokens += doc.getDocumentPieceText(Iterables.get(headNotesOfAllPages, currentHeadIndex));
                currentHeadIndex++;
                textToShowInTokens += "</fw>";
            }

    return textToShowInTokens;
    }

    private void processFullABodyComponentToTEI(Pair<List<LayoutToken>, String> bodyComponent, StringBuilder tei, String modelToRun) {
        String clusterContent = "";
        List<LayoutToken> allTokensOfaLE = bodyComponent.getA();

        String tagLabel = bodyComponent.getB();
        clusterContent = checkFullABodyComponentToTEI(tagLabel, allTokensOfaLE, modelToRun);

        produceXmlNode(tei, clusterContent, tagLabel, true);
    }

    private String checkFullABodyComponentToTEI(String tagLabel, List<LayoutToken> allTokensOfaLE, String modelToRun) {
        StringBuilder clusterContent = new StringBuilder();
        LexicalEntryParser lexicalEntryParser = new LexicalEntryParser();
        FormParser formParser = new FormParser();
        SenseParser senseParser = new SenseParser();
        EtymQuoteParser etymQuoteParser = new EtymQuoteParser();
        EtymParser etymParser = new EtymParser();
        if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
            clusterContent.append(LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE)));
        } else {
            LabeledLexicalInformation parsedLexicalEntry = lexicalEntryParser.process(allTokensOfaLE, modelToRun);
            for (Pair<List<LayoutToken>, String> segmentedEntryComponent : parsedLexicalEntry.getLabels()) {
                if (segmentedEntryComponent.getB().equals(LEXICAL_ENTRY_FORM_LABEL)) {
                    clusterContent.append(formParser.processToTEI(segmentedEntryComponent.getA()).toString());

                } else if (segmentedEntryComponent.getB().equals(LEXICAL_ENTRY_SENSE_LABEL)) {

                    clusterContent.append(senseParser.processToTEI(segmentedEntryComponent.getA()).toString());
                } else if (segmentedEntryComponent.getB().equals(LEXICAL_ENTRY_ETYM_LABEL)) {
                    // Get the result of the first level Etym parsing
                    LabeledLexicalInformation parsedEtymSegOrQuote = etymQuoteParser.process(segmentedEntryComponent.getA(), modelToRun);
                    // For each <seg> or <quote> segment parse the etym information
                    String etymTEIString = "";
                    for (Pair<List<LayoutToken>, String> segmentedEtym : parsedEtymSegOrQuote.getLabels()) {

                        etymTEIString = etymTEIString + etymParser.processToTei(segmentedEtym.getA(), segmentedEtym.getB()).toString();


                    }
                    produceXmlNode(clusterContent, etymTEIString, LEXICAL_ENTRY_ETYM_LABEL, true);
                    //clusterContent.append(clusterContent + etymTEIString);

                    //clusterContent = clusterContent + etymQuoteParser.processToTei(segmentedEntryComponent.getA()).toString();
                } else {
                    String xmlTag = segmentedEntryComponent.getB().replace("<", "").replace(">", "");
                    clusterContent.append(createMyXMLString(xmlTag, LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(segmentedEntryComponent.getA()))));


                }

            }

        }
        return clusterContent.toString();
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }
}
