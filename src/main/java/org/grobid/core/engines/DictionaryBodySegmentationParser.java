package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import org.grobid.core.data.LabeledLexicalInformation;
import org.grobid.core.engines.label.LexicalEntryLabels;
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
import org.grobid.core.engines.label.DictionaryBodySegmentationLabels;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.engines.label.TaggingLabel;
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
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;

import static org.grobid.core.document.TEIDictionaryFormatter.createMyXMLString;
import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_SENSE_LABEL;
import static org.grobid.core.engines.label.TaggingLabels.OTHER_LABEL;
import static org.grobid.service.DictionaryPaths.PATH_DICTIONARY_BODY_SEGMENTATATION;
import static org.grobid.service.DictionaryPaths.PATH_FULL_DICTIONARY;
import static org.grobid.service.DictionaryPaths.PATH_LEXICAL_ENTRY;

/**
 * Created by med on 02.08.16.
 */
public class DictionaryBodySegmentationParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionarySegmentationParser.class);
    private static volatile DictionaryBodySegmentationParser instance;

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


            List<Pair<List<LayoutToken>, String>> structuredBody = null;
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


    public static List<Pair<List<LayoutToken>, String>> extractBodyComponents(LayoutTokenization layoutTokenization, String contentFeatured) {
        //Extract the lexical entries in a clusters of tokens for each lexical entry, ponctuation and other parts
        List<LayoutToken> tokenizations = layoutTokenization.getTokenization();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.DICTIONARY_BODY_SEGMENTATION, contentFeatured, tokenizations);

        List<TaggingTokenCluster> clusters = clusteror.cluster();
        List<Pair<List<LayoutToken>, String>> list1 = new ArrayList<>();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);
            String tagLabel = clusterLabel.getLabel();


            if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                list1.add(new Pair(cluster.concatTokens(), tagLabel));
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.OTHER_LABEL)) {
                list1.add(new Pair(cluster.concatTokens(), tagLabel));
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                list1.add(new Pair(cluster.concatTokens(), tagLabel));
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

        SortedSet<DocumentPiece> headNotesOfAllPages = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL);
        SortedSet<DocumentPiece> footNotesOfAllPages = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL);
        SortedSet<DocumentPiece> otherOfAllPages = doc.getDocumentDictionaryPart(OTHER_LABEL);


        int pagesNumber = doc.getPages().size();
        int currentHeadIndex = 1;
        int currentFootIndex = 0;
        int currentOtherIndex = 0;
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
        List<Pair<List<LayoutToken>, String>> bodyComponents = doc.getBodyComponents();
        int lexicalEntriesNumber = bodyComponents.size();

        for (int i = 0; i < lexicalEntriesNumber; i++) {
            int beginLEOffSet = bodyComponents.get(i).getA().get(0).getOffset();
            lexicalEntriesOffsetArray.add(beginLEOffSet);
        }

        Boolean bigEntryIsInsideDetected = false;
        List<Pair<List<LayoutToken>, String>> lexicalEntriesSubList = new ArrayList<>();

        if (modelToRun.equals(PATH_DICTIONARY_BODY_SEGMENTATATION)) {
            if (lexicalEntriesNumber > pagesNumber) {
                tei.append("\t\t<fw " + "type=\"header\">");
                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(headNotesOfAllPages.first())));
                tei.append("</fw>\n");


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
                        lexicalEntriesSubList = bodyComponents.subList(lexicalEntryBeginIndex, k);
                        int subListSize = lexicalEntriesSubList.size();
                        Pair<List<LayoutToken>, String> lastEntryInSublist = lexicalEntriesSubList.get(subListSize - 1);

                        //Check if the last entry in the page is cut by the footer and header
                        if (lastEntryInSublist.getA().get(lastEntryInSublist.getA().size() - 1).getOffset() <= newPageOffset) {
                            for (Pair<List<LayoutToken>, String> bodyComponent : lexicalEntriesSubList) {
                                List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                                String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                produceXmlNode(tei, clusterContent, bodyComponent.getB());
                            }


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
                                tei.append("\t\t<other>");
                                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))));
                                currentOtherIndex++;
                                tei.append("</other>");
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


                        } else {
                            List<LayoutToken> textToShowInTokens = new ArrayList<>();
                            int indexOfLastTokenInThePage = 0;
                            List<LayoutToken> lexicalEntry = bodyComponents.get(k).getA();

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
                                produceXmlNode(tei, clusterContent, tag);
                            }

                            List<LayoutToken> firstPartOfLastLexicalEntry = lastEntryInSublist.getA().subList(0, indexOfLastTokenInThePage);
                            List<LayoutToken> restOfLexicalEntryTokens = lastEntryInSublist.getA().subList(indexOfLastTokenInThePage, lastEntryInSublist.getA().size());
                            String tagLabel = lastEntryInSublist.getB();
                            //Compound the last entry in tokens and insert the oher page blocks
                            textToShowInTokens.addAll(firstPartOfLastLexicalEntry);


                            if (currentFootIndex < footNotesOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex))) != "") {
                                textToShowInTokens.add(new LayoutToken("\t\t<fw type=\"footer\">"));
                                textToShowInTokens.add(new LayoutToken(doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex))));
                                currentFootIndex++;
                                textToShowInTokens.add(new LayoutToken("</fw>"));
                                textToShowInTokens.add(new LayoutToken("\n"));

                            }


                            if (currentOtherIndex < otherOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))) != "") {
                                textToShowInTokens.add(new LayoutToken("\t\t<other>"));
                                textToShowInTokens.add(new LayoutToken(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))));
                                currentOtherIndex++;
                                textToShowInTokens.add(new LayoutToken("</other>"));
                                textToShowInTokens.add(new LayoutToken("\n"));
                            }


                            textToShowInTokens.add(new LayoutToken("\t\t<pb/>"));

                            if (currentHeadIndex < headNotesOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(headNotesOfAllPages, currentHeadIndex))) != "") {

                                textToShowInTokens.add(new LayoutToken("\t\t<fw type=\"header\">"));
                                textToShowInTokens.add(new LayoutToken(doc.getDocumentPieceText(Iterables.get(headNotesOfAllPages, currentHeadIndex))));
                                currentHeadIndex++;
                                textToShowInTokens.add(new LayoutToken("</fw>"));
                            }


                            textToShowInTokens.addAll(restOfLexicalEntryTokens);
                            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(textToShowInTokens));
                            produceXmlNode(tei, clusterContent, tagLabel);


                        }
                        if (pageOffsetIndex == pagesOffsetArray.size() - 1) {
                            lexicalEntriesSubList = bodyComponents.subList(k, bodyComponents.size());


                            for (Pair<List<LayoutToken>, String> bodyComponent : lexicalEntriesSubList) {
                                List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                                String tagLabel = bodyComponent.getB();
                                String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                produceXmlNode(tei, clusterContent, tagLabel);
                            }


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
                                tei.append("\t\t<other>");
                                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))));
                                currentOtherIndex++;
                                tei.append("</other>");
                                tei.append("\n");
                            }


                        }
                    }

                } else {
                    // In this case, the input file has just one page

                    for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents) {
                        List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                        String clusterContent = LayoutTokensUtil.normalizeText(allTokensOfaLE);
                        String tagLabel = bodyComponent.getB();
                        produceXmlNode(tei, clusterContent, tagLabel);
                    }

                    for (DocumentPiece footer : footNotesOfAllPages) {

                        tei.append("\t\t<fw type=\"footer\">");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(footer)));
                        currentFootIndex++;
                        tei.append("</fw>");
                        tei.append("\n");
                    }


                    for (DocumentPiece other : otherOfAllPages) {
                        tei.append("\t\t<other>");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(other)));
                        currentOtherIndex++;
                        tei.append("</other>");
                        tei.append("\n");
                    }

                }
                if (bigEntryIsInsideDetected) {
                    tei = headerTEI;
                    for (DocumentPiece header : headNotesOfAllPages) {
                        tei.append("\t\t<fw type=\"header\">");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(header)));
                        tei.append("</fw>");
                    }

                    for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents) {
                        List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                        String clusterContent = LayoutTokensUtil.normalizeText(allTokensOfaLE);
                        String tagLabel = bodyComponent.getB();
                        if (bodyComponent.getB().equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                            produceXmlNode(tei, clusterContent, tagLabel);
                        }

                    }

                    for (DocumentPiece footer : footNotesOfAllPages) {

                        tei.append("\t\t<fw type=\"footer\">");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(footer)));
                        currentFootIndex++;
                        tei.append("</fw>");
                        tei.append("\n");
                    }
                    for (DocumentPiece other : otherOfAllPages) {
                        tei.append("\t\t<other>");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(other)));
                        currentOtherIndex++;
                        tei.append("</other>");
                        tei.append("\n");
                    }
                }
            } else {
                // This is caused probably by a lack of training. So just try to show what is already recognized in a consistent way


                for (DocumentPiece header : headNotesOfAllPages) {
                    tei.append("\t\t<fw type=\"header\">");
                    tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(header)));
                    tei.append("</fw>");
                }

                for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents) {
                    List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                    String clusterContent = LayoutTokensUtil.normalizeText(allTokensOfaLE);
                    String tagLabel = bodyComponent.getB();
                    produceXmlNode(tei, clusterContent, tagLabel);
                }

                for (DocumentPiece footer : footNotesOfAllPages) {

                    tei.append("\t\t<fw type=\"footer\">");
                    tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(footer)));
                    currentFootIndex++;
                    tei.append("</fw>");
                    tei.append("\n");
                }
                for (DocumentPiece other : otherOfAllPages) {
                    tei.append("\t\t<other>");
                    tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(other)));
                    currentOtherIndex++;
                    tei.append("</other>");
                    tei.append("\n");
                }


            }

        } else if (modelToRun.equals(PATH_LEXICAL_ENTRY)) {
            LexicalEntryParser lexicalEntryParser = new LexicalEntryParser();
            if (lexicalEntriesNumber > pagesNumber) {
                tei.append("\t\t<fw " + "type=\"header\">");
                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(headNotesOfAllPages.first())));
                tei.append("</fw>\n");


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
                        lexicalEntriesSubList = bodyComponents.subList(lexicalEntryBeginIndex, k);
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

                                produceXmlNode(tei, clusterContent, bodyComponent.getB());
                            }


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
                                tei.append("\t\t<other>");
                                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))));
                                currentOtherIndex++;
                                tei.append("</other>");
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


                        } else {
                            List<LayoutToken> textToShowInTokens = new ArrayList<>();
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
                                produceXmlNode(tei, clusterContent, tag);
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
                                    textToShowInTokens.add(new LayoutToken(tagOfSplitComponent));
                                    textToShowInTokens.addAll(firstPartOfSplitComponent);


                                    if (currentFootIndex < footNotesOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex))) != "") {
                                        textToShowInTokens.add(new LayoutToken("\t\t<fw type=\"footer\">"));
                                        textToShowInTokens.add(new LayoutToken(doc.getDocumentPieceText(Iterables.get(footNotesOfAllPages, currentFootIndex))));
                                        currentFootIndex++;
                                        textToShowInTokens.add(new LayoutToken("</fw>"));
                                        textToShowInTokens.add(new LayoutToken("\n"));

                                    }


                                    if (currentOtherIndex < otherOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))) != "") {
                                        textToShowInTokens.add(new LayoutToken("\t\t<other>"));
                                        textToShowInTokens.add(new LayoutToken(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))));
                                        currentOtherIndex++;
                                        textToShowInTokens.add(new LayoutToken("</other>"));
                                        textToShowInTokens.add(new LayoutToken("\n"));
                                    }
                                    textToShowInTokens.add(new LayoutToken("\t\t<pb/>"));

                                    if (currentHeadIndex < headNotesOfAllPages.size() && LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(headNotesOfAllPages, currentHeadIndex))) != "") {

                                        textToShowInTokens.add(new LayoutToken("\t\t<fw type=\"header\">"));
                                        textToShowInTokens.add(new LayoutToken(doc.getDocumentPieceText(Iterables.get(headNotesOfAllPages, currentHeadIndex))));
                                        currentHeadIndex++;
                                        textToShowInTokens.add(new LayoutToken("</fw>"));
                                    }


                                    textToShowInTokens.addAll(restOfSplitComponentTokens);
                                    textToShowInTokens.add(new LayoutToken(tagOfSplitComponent.replace("<", "</")));

                                } else {
                                    String tag = lastEntryInSublist.getLabels().get(h).getB().replace("<", "").replace(">", "");
                                    textToShowInTokens.add(new LayoutToken(createMyXMLString(tag, LayoutTokensUtil.toText(componentOfLastLexicalEntry))));
                                }

                            }
                            String tagLabel = lexicalEntriesSubList.get(lexicalEntriesSubList.size() - 1).getB();
                            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(textToShowInTokens));
                            produceXmlNode(tei, clusterContent, tagLabel);

                        }
                        if (pageOffsetIndex == pagesOffsetArray.size() - 1) {
                            lexicalEntriesSubList = bodyComponents.subList(k, bodyComponents.size());

                            for (Pair<List<LayoutToken>, String> bodyComponent : lexicalEntriesSubList) {
                                List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                                String tagLabel = bodyComponent.getB();
                                String clusterContent;
                                if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                                    clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                                } else {
                                    clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                                }
                                produceXmlNode(tei, clusterContent, tagLabel);
                            }


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
                                tei.append("\t\t<other>");
                                tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(Iterables.get(otherOfAllPages, currentOtherIndex))));
                                currentOtherIndex++;
                                tei.append("</other>");
                                tei.append("\n");
                            }


                        }
                    }

                } else {
                    // In this case, the input file has just one page

                    for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents) {
                        List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                        String clusterContent;
                        String tagLabel = bodyComponent.getB();
                        if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                            clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                        } else {
                            clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                        }
                        produceXmlNode(tei, clusterContent, tagLabel);
                    }

                    for (DocumentPiece footer : footNotesOfAllPages) {

                        tei.append("\t\t<fw type=\"footer\">");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(footer)));
                        currentFootIndex++;
                        tei.append("</fw>");
                        tei.append("\n");
                    }


                    for (DocumentPiece other : otherOfAllPages) {
                        tei.append("\t\t<other>");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(other)));
                        currentOtherIndex++;
                        tei.append("</other>");
                        tei.append("\n");
                    }

                }
                if (bigEntryIsInsideDetected) {
                    tei = headerTEI;
                    for (DocumentPiece header : headNotesOfAllPages) {
                        tei.append("\t\t<fw type=\"header\">");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(header)));
                        tei.append("</fw>");
                    }

                    for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents) {
                        List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                        String clusterContent;
                        String tagLabel = bodyComponent.getB();

                        if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                            clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                        } else {
                            clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                        }
//                        if (bodyComponent.getB().equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                        produceXmlNode(tei, clusterContent, tagLabel);
//                        }

                    }

                    for (DocumentPiece footer : footNotesOfAllPages) {

                        tei.append("\t\t<fw type=\"footer\">");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(footer)));
                        currentFootIndex++;
                        tei.append("</fw>");
                        tei.append("\n");
                    }
                    for (DocumentPiece other : otherOfAllPages) {
                        tei.append("\t\t<other>");
                        tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(other)));
                        currentOtherIndex++;
                        tei.append("</other>");
                        tei.append("\n");
                    }
                }
            } else {
                // This is caused probably by a lack of training. So just try to show what is already recognized in a consistent way


                for (DocumentPiece header : headNotesOfAllPages) {
                    tei.append("\t\t<fw type=\"header\">");
                    tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(header)));
                    tei.append("</fw>");
                }

                for (Pair<List<LayoutToken>, String> bodyComponent : bodyComponents) {
                    List<LayoutToken> allTokensOfaLE = bodyComponent.getA();
                    String clusterContent;
                    String tagLabel = bodyComponent.getB();
                    if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
                        clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                    } else {
                        clusterContent = lexicalEntryParser.processToTei(allTokensOfaLE, modelToRun);
                    }
                    produceXmlNode(tei, clusterContent, tagLabel);
                }

                for (DocumentPiece footer : footNotesOfAllPages) {

                    tei.append("\t\t<fw type=\"footer\">");
                    tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(footer)));
                    currentFootIndex++;
                    tei.append("</fw>");
                    tei.append("\n");
                }
                for (DocumentPiece other : otherOfAllPages) {
                    tei.append("\t\t<other>");
                    tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(other)));
                    currentOtherIndex++;
                    tei.append("</other>");
                    tei.append("\n");
                }


            }

        } else if (modelToRun.equals(PATH_FULL_DICTIONARY)) {

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
                    createTrainingDictionaryBody(fileEntry, outputDirectory);
                    n++;
                }

            } else {
                createTrainingDictionaryBody(path, outputDirectory);
                n++;

            }


            System.out.println(n + " files to be processed.");

            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingDictionaryBody(File path, String outputDirectory) throws Exception {

        // Segment the doc
        DictionaryDocument doc = processing(path);
        //Get Body
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

        //Get tokens from the body
        LayoutTokenization tokenizations = DocumentUtils.getLayoutTokenizations(doc, documentBodyParts);

        String bodyTextFeatured = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(tokenizations.getTokenization()).toString();

        if (StringUtils.isNotBlank(bodyTextFeatured)) {
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

            //Using the existing model of the parser to generate a pre-annotate tei file to be corrected
            if (bodyTextFeatured.length() > 0) {
                String rese = label(bodyTextFeatured);
                StringBuilder bufferFulltext = trainingExtraction(doc, rese, tokenizations);

                // write the TEI file to reflect the extact layout of the text as extracted from the pdf
                String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation.tei.xml";
                writer = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                        "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">");
                writer.write("\n\t\t<headnote>");
                writer.write(DocumentUtils.replaceLinebreaksWithTags(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL).toString()));
                writer.write("</headnote>");
                writer.write("\n\t\t<body>");
                writer.write(bufferFulltext.toString());
                writer.write("</body>");
                writer.write("\n\t\t<footnote>");
                writer.write(DocumentUtils.replaceLinebreaksWithTags(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL).toString()));
                writer.write("</footnote>");
                writer.write("\n\t</text>\n</tei>\n");
                writer.close();
            }
        }
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

    private void produceXmlNode(StringBuilder buffer, String clusterContent, String tagLabel) {
        if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
//            clusterContent = TextUtilities.HTMLEncode(clusterContent);
//            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("entry", clusterContent));
        } else if (tagLabel.equals(DictionaryBodySegmentationLabels.OTHER_LABEL)) {
//            clusterContent = TextUtilities.HTMLEncode(clusterContent);
//            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("other", clusterContent));
        } else if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
//            clusterContent = TextUtilities.HTMLEncode(clusterContent);
//            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("pc", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_FORM_LABEL)) {
//            clusterContent = TextUtilities.HTMLEncode(clusterContent);
//            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("form", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_ETYM_LABEL)) {
//            clusterContent = TextUtilities.HTMLEncode(clusterContent);
//            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("etym", clusterContent));
        } else if (tagLabel.equals(LEXICAL_ENTRY_SENSE_LABEL)) {
//            clusterContent = TextUtilities.HTMLEncode(clusterContent);
//            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("sense", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_RE_LABEL)) {
//            clusterContent = TextUtilities.HTMLEncode(clusterContent);
//            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("re", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_OTHER_LABEL)) {
//            clusterContent = TextUtilities.HTMLEncode(clusterContent);
//            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("other", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_PC_LABEL)) {
//            clusterContent = TextUtilities.HTMLEncode(clusterContent);
//            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("pc", clusterContent));
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

}
