package org.grobid.core.document;

import com.google.common.collect.Iterables;
import org.grobid.core.engines.DictionaryModels;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.DictionaryBodySegmentationLabels;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.layout.Page;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.TextUtilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;

/**
 * Created by med on 24.09.16.
 */
public class TEIDictionaryFormatter {

    private DictionaryDocument doc = null;

    public TEIDictionaryFormatter(DictionaryDocument document) {
        doc = document;
    }


    public StringBuilder toTEIFormatDictionaryBodySegmentation(GrobidAnalysisConfig config,
                                                               SchemaDeclaration schemaDeclaration) {
        StringBuilder tei = new StringBuilder();
        tei.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        if (config.isWithXslStylesheet()) {
            tei.append("<?xml-stylesheet type=\"text/xsl\" href=\"../jsp/xmlverbatimwrapper.xsl\"?> \n");
        }
        if (schemaDeclaration != null) {
            if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.DTD)) {
                tei.append("<!DOCTYPE TEI SYSTEM \"" + GrobidProperties.get_GROBID_HOME_PATH()
                        + "/schemas/dtd/Grobid.dtd" + "\">\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.XSD)) {
                // XML schema
                tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" \n" +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                        //"\n xsi:noNamespaceSchemaLocation=\"" +
                        //GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\""	+
                        "xsi:schemaLocation=\"http://www.tei-c.org/ns/1.0 " +
                        GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\"" +
                        "\n xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
//				"\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.RNG)) {
                // standard RelaxNG
                tei.append("<?xml-model href=\"file://" +
                        GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rng" +
                        "\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.RNC)) {
                // compact RelaxNG
                tei.append("<?xml-model href=\"file://" +
                        GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rnc" +
                        "\" type=\"application/relax-ng-compact-syntax\"?>\n");
            }

            // by default there is no schema association
            if (!schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.XSD)) {
                tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
            }
        } else {
            tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
        }

        if (doc.getLanguage() != null) {
            tei.append("\t<teiHeader xml:lang=\"" + doc.getLanguage() + "\">");
        } else {
            tei.append("\t<teiHeader>");
        }

        // encodingDesc gives info about the producer of the file
        tei.append("\n\t\t<encodingDesc>\n");
        tei.append("\t\t\t<appInfo>\n");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        tei.append("\t\t\t\t<application version=\"" + GrobidProperties.getVersion() +
                "\" ident=\"GROBID\" when=\"" + dateISOString + "\">\n");
        tei.append("\t\t\t\t\t<ref target=\"https://github.com/MedKhem/grobid-dictionaries\">GROBID_Dictionaries - A machine learning software for structuring digitized dictionaries</ref>\n");
        tei.append("\t\t\t\t</application>\n");
        tei.append("\t\t\t</appInfo>\n");
        tei.append("\t\t</encodingDesc>");

        tei.append("\n\t\t<fileDesc>\n\t\t\t<titleStmt>\n\t\t\t\t<title level=\"a\" type=\"main\"");
        if (config.isGenerateTeiIds()) {
            String divID = KeyGen.getKey().substring(0, 7);
            tei.append(" xml:id=\"_" + divID + "\"");
        }
        tei.append("/>");
        tei.append("\n\t\t\t</titleStmt>\n");
        tei.append("\n\t\t</fileDesc>\n");
        tei.append("\t</teiHeader>\n");

        if (doc.getLanguage() != null) {
            tei.append("\t<text xml:lang=\"").append(doc.getLanguage()).append("\">\n");
        } else {
            tei.append("\t<text>\n");
        }
        tei.append("\t\t<body>\n");

        SortedSet<DocumentPiece> headNotesOfAllPages = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL);
        SortedSet<DocumentPiece> footNotesOfAllPages = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL);
        SortedSet<DocumentPiece> otherOfAllPages = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_OTHER_LABEL);


        int pagesNumber = doc.getPages().size();
        int currentHeadIndex = 1;
        int currentFootIndex = 0;
        int currentOtherIndex = 0;
        LayoutToken lastVisitedLayoutToken = new LayoutToken();
        lastVisitedLayoutToken.setPage(1);


        // Prepare an offset based index for pages
        List<Integer> pagesOffsetArray = new ArrayList<Integer>();

        for (Page page : doc.getPages()) {
            int beginOffSet = page.getBlocks().get(0).getTokens().get(0).getOffset();

            pagesOffsetArray.add(beginOffSet);
        }

        // Prepare an offset based index for LEs
        List<Integer> lexicalEntriesOffsetArray = new ArrayList<Integer>();
        List<List<LayoutToken>> listOfLEs = doc.getLexicalEntries();
        int lexicalEntriesNumber = listOfLEs.size();

        for (int i = 0; i < lexicalEntriesNumber; i++) {
            int beginLEOffSet = listOfLEs.get(i).get(0).getOffset();
            lexicalEntriesOffsetArray.add(beginLEOffSet);
        }


        List<List<LayoutToken>> lexicalEntriesSubList = new ArrayList<>();
        if (lexicalEntriesNumber > 1) {
            tei.append("\t\t<fw " + "type=\"header\">");
            tei.append(LayoutTokensUtil.normalizeText(doc.getDocumentPieceText(headNotesOfAllPages.first())));
            tei.append("</fw>\n");


            if (pagesOffsetArray.size() > 1) {
                int k = 0;
                int lexicalEntryBeginIndex;
                for (int pageOffsetIndex = 1; pageOffsetIndex <= pagesOffsetArray.size() - 1; pageOffsetIndex++) {
                    List<LayoutToken> textToShowInTokens = new ArrayList<>();
                    int newPageOffset = pagesOffsetArray.get(pageOffsetIndex);
                    lexicalEntryBeginIndex = k;
                    // Check if the lexical entries are recognized (exist)
                    if (k < lexicalEntriesOffsetArray.size()) {
                        while (lexicalEntriesOffsetArray.get(k) < newPageOffset) {
                            k++;
                        }
                    }

                    lexicalEntriesSubList = listOfLEs.subList(lexicalEntryBeginIndex, k);
                    int subListSize = lexicalEntriesSubList.size();
                    List<LayoutToken> lastEntryInSublist = lexicalEntriesSubList.get(subListSize - 1);

                    //Check if the last entry in the page is cut by the footer and header
                    if (lastEntryInSublist.get(lastEntryInSublist.size() - 1).getOffset() <= newPageOffset) {
                        for (List<LayoutToken> allTokensOfaLE : lexicalEntriesSubList) {
                            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                            tei.append(createMyXMLString("entry", clusterContent));
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
                        int indexOfLastTokenInThePage = 0;
                        List<LayoutToken> lexicalEntry = listOfLEs.get(k);

                        for (LayoutToken token : lastEntryInSublist) {
                            //Check offset of each token in the LE to insert the header and footer blocks
                            if (token.getOffset() < newPageOffset) {

                                indexOfLastTokenInThePage++;
                            } else {
//                            indexOfLastTokenInThePage--;
                                break;
                            }

                        }


                        for (int h = 0; h < lexicalEntriesSubList.size() - 2; h++) {
                            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(lexicalEntriesSubList.get(h)));
                            tei.append(createMyXMLString("entry", clusterContent));
                        }

                        List<LayoutToken> firstPartOfLastLexicalEntry = lastEntryInSublist.subList(0, indexOfLastTokenInThePage);
                        List<LayoutToken> restOfLexicalEntryTokens = lastEntryInSublist.subList(indexOfLastTokenInThePage, lastEntryInSublist.size());
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
                        tei.append(createMyXMLString("entry", clusterContent));


                    }
                    if (pageOffsetIndex == pagesOffsetArray.size() - 1) {
                        lexicalEntriesSubList = listOfLEs.subList(k, listOfLEs.size());


                        for (List<LayoutToken> allTokensOfaLE : lexicalEntriesSubList) {
                            String clusterContent = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(allTokensOfaLE));
                            tei.append(createMyXMLString("entry", clusterContent));
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


                for (DocumentPiece footer : footNotesOfAllPages) {


                    for (List<LayoutToken> lexialEntry : listOfLEs) {
                        String clusterContent = LayoutTokensUtil.normalizeText(lexialEntry);
                        tei.append(createMyXMLString("entry", clusterContent));
                    }


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

            tei.append(tei.append(LayoutTokensUtil.normalizeText(listOfLEs.get(0))));

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


//        tei.append("\t\t</body>\n");
//        tei.append("\t\t<fw "+"type=\"footer\">" );
//        tei.append(LayoutTokensUtil.normalizeText(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL)));
//        tei.append("</fw>\n");
        tei.append("\t\t</body>\n");
        tei.append("\t</text>\n");
        tei.append("</TEI>\n");

        return tei;
    }

    public StringBuilder toTEIDictionaryBodySegmentation(String contentFeatured, LayoutTokenization layoutTokenization) {

        StringBuilder buffer = new StringBuilder();
        TaggingLabel lastClusterLabel = null;
        List<LayoutToken> tokenizations = layoutTokenization.getTokenization();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.DICTIONARY_BODY_SEGMENTATION, contentFeatured, tokenizations);

        String tokenLabel = null;
        List<TaggingTokenCluster> clusters = clusteror.cluster();


        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);

            List<LayoutToken> list1 = cluster.concatTokens();
            String clusterContent = LayoutTokensUtil.toText(list1);
            clusterContent = TextUtilities.HTMLEncode(clusterContent);
            clusterContent = DocumentUtils.replaceLinebreaksWithTags(clusterContent);
            String tagLabel = clusterLabel.getLabel();

            // Note in the following, that the text that is not belonging to the entry tags is kept
            // since we are not sure that the model will perform perfectly and the tag could be moved afterwards to contains this extra-text, if needed

            //For the result data (shown as result of applying the second model) any the text that is not contained between tags is removed (not like the following)
            if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                buffer.append(createMyXMLString("entry", clusterContent));
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_BODY_OTHER_LABEL)) {
                buffer.append(clusterContent);
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_BODY_PC_LABEL)) {
                buffer.append(clusterContent);
            } else {
                throw new IllegalArgumentException(tagLabel + " is not a valid possible tag");
            }

        }
        return buffer;
    }

    public StringBuilder toTEIDictionarySegmentation(String contentFeatured, LayoutTokenization layoutTokenization) {

        StringBuilder buffer = new StringBuilder();
        TaggingLabel lastClusterLabel = null;
        List<LayoutToken> tokenizations = layoutTokenization.getTokenization();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.DICTIONARY_SEGMENTATION, contentFeatured, tokenizations);

        String tokenLabel = null;
        List<TaggingTokenCluster> clusters = clusteror.cluster();


        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);

            // Problem with Grobid Normalisation
            List<LayoutToken> list1 = cluster.concatTokens();
            String str1 = LayoutTokensUtil.toText(list1);
            String clusterContent = LayoutTokensUtil.normalizeText(str1);

            String tagLabel = clusterLabel.getLabel();


            if (tagLabel.equals(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL)) {
                buffer.append(createMyXMLString("body", clusterContent));
            } else if (tagLabel.equals(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL)) {
                buffer.append(createMyXMLString("headnote", clusterContent));
            } else if (tagLabel.equals(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL)) {
                buffer.append(createMyXMLString("footnote", clusterContent));
            } else {
                throw new IllegalArgumentException(tagLabel + " is not a valid possible tag");
            }


        }

        return buffer;
    }

    public StringBuilder toTEIFormatLexicalEntry(GrobidAnalysisConfig config,
                                                 SchemaDeclaration schemaDeclaration,
                                                 String structuredLE) {
        StringBuilder tei = new StringBuilder();
        tei.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        if (config.isWithXslStylesheet()) {
            tei.append("<?xml-stylesheet type=\"text/xsl\" href=\"../jsp/xmlverbatimwrapper.xsl\"?> \n");
        }
        if (schemaDeclaration != null) {
            if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.DTD)) {
                tei.append("<!DOCTYPE TEI SYSTEM \"" + GrobidProperties.get_GROBID_HOME_PATH()
                        + "/schemas/dtd/Grobid.dtd" + "\">\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.XSD)) {
                // XML schema
                tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" \n" +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                        //"\n xsi:noNamespaceSchemaLocation=\"" +
                        //GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\""	+
                        "xsi:schemaLocation=\"http://www.tei-c.org/ns/1.0 " +
                        GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\"" +
                        "\n xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
//				"\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.RNG)) {
                // standard RelaxNG
                tei.append("<?xml-model href=\"file://" +
                        GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rng" +
                        "\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.RNC)) {
                // compact RelaxNG
                tei.append("<?xml-model href=\"file://" +
                        GrobidProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rnc" +
                        "\" type=\"application/relax-ng-compact-syntax\"?>\n");
            }

            // by default there is no schema association
            if (!schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.XSD)) {
                tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
            }
        } else {
            tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
        }

        if (doc.getLanguage() != null) {
            tei.append("\t<teiHeader xml:lang=\"" + doc.getLanguage() + "\">");
        } else {
            tei.append("\t<teiHeader>");
        }

        // encodingDesc gives info about the producer of the file
        tei.append("\n\t\t<encodingDesc>\n");
        tei.append("\t\t\t<appInfo>\n");

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        String dateISOString = df.format(new java.util.Date());

        tei.append("\t\t\t\t<application version=\"" + GrobidProperties.getVersion() +
                "\" ident=\"GROBID\" when=\"" + dateISOString + "\">\n");
        tei.append("\t\t\t\t\t<ref target=\"https://github.com/MedKhem/grobid-dictionaries\">GROBID_Dictionaries - A machine learning software for structuring digitized dictionaries</ref>\n");
        tei.append("\t\t\t\t</application>\n");
        tei.append("\t\t\t</appInfo>\n");
        tei.append("\t\t</encodingDesc>");

        tei.append("\n\t\t<fileDesc>\n\t\t\t<titleStmt>\n\t\t\t\t<title level=\"a\" type=\"main\"");
        if (config.isGenerateTeiIds()) {
            String divID = KeyGen.getKey().substring(0, 7);
            tei.append(" xml:id=\"_" + divID + "\"");
        }
        tei.append("/>");
        tei.append("\n\t\t\t</titleStmt>\n");
        tei.append("\n\t\t</fileDesc>\n");
        tei.append("\t</teiHeader>\n");

        if (doc.getLanguage() != null) {
            tei.append("\t<text xml:lang=\"").append(doc.getLanguage()).append("\">\n");
        } else {
            tei.append("\t<text>\n");
        }
        tei.append("\t\t<fw " + "type=\"header\">");
        tei.append(LayoutTokensUtil.normalizeText(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL)));
        tei.append("</fw>\n");
        tei.append("\n\t\t<body>");
        tei.append(structuredLE);
        tei.append("\n\t\t</body>");
        tei.append("\t\t<fw " + "type=\"footer\">");
        tei.append(LayoutTokensUtil.normalizeText(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL)));
        tei.append("</fw>\n");
        tei.append("\n\t</text>\n");
        tei.append("</TEI>\n");

        return tei;
    }


    public String createMyXMLString(String elementName, String elementContent) {
        StringBuilder xmlStringElement = new StringBuilder();
        xmlStringElement.append("<");
        xmlStringElement.append(elementName);
        xmlStringElement.append(">");
        xmlStringElement.append(elementContent);
        xmlStringElement.append("</");
        xmlStringElement.append(elementName);
        xmlStringElement.append(">");

        return xmlStringElement.toString();
    }

    // possible association to Grobid customised TEI schemas: DTD, XML schema, RelaxNG or compact RelaxNG
    // DEFAULT means no schema association in the generated XML documents
    public enum SchemaDeclaration {
        DEFAULT, DTD, XSD, RNG, RNC
    }


}
