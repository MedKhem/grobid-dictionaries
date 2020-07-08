package org.grobid.core.document;

import com.google.common.collect.Iterables;
import org.grobid.core.engines.DictionaryModels;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.DictionaryBodySegmentationLabels;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.engines.label.LexicalEntryLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.layout.Page;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.*;

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
    private DocumentUtils formatter = new DocumentUtils();

    public TEIDictionaryFormatter(DictionaryDocument document) {
        doc = document;
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

            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            clusterContent = DocumentUtils.escapeHTMLCharac(clusterContent);
            clusterContent = DocumentUtils.replaceLinebreaksWithTags(clusterContent);
            String tagLabel = clusterLabel.getLabel();

            // Note in the following, that the text that is not belonging to the entry tags is kept
            // since we are not sure that the model will perform perfectly and the tag could be moved afterwards to contains this extra-text, if needed

            //For the result data (shown as result of applying the second model) any the text that is not contained between tags is removed (not like the following)
            if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                buffer.append(formatter.createMyXMLString("entry", null, clusterContent));
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_DICTSCRAP_LABEL)) {
                buffer.append(formatter.createMyXMLString("dictScrap", null, clusterContent));
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.PUNCTUATION_LABEL)) {
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
                buffer.append(formatter.createMyXMLString("body", null, clusterContent));
            } else if (tagLabel.equals(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL)) {
                buffer.append(formatter.createMyXMLString("headnote", null, clusterContent));
            } else if (tagLabel.equals(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL)) {
                buffer.append(formatter.createMyXMLString("footnote", null,  clusterContent));
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
                tei.append("<!DOCTYPE TEI SYSTEM \"" + GrobidDictionaryProperties.get_GROBID_HOME_PATH()
                        + "/schemas/dtd/Grobid.dtd" + "\">\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.XSD)) {
                // XML schema
                tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\" \n" +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                        //"\n xsi:noNamespaceSchemaLocation=\"" +
                        //GrobidDictionaryProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\""	+
                        "xsi:schemaLocation=\"http://www.tei-c.org/ns/1.0 " +
                        GrobidDictionaryProperties.get_GROBID_HOME_PATH() + "/schemas/xsd/Grobid.xsd\"" +
                        "\n xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
//				"\n xmlns:mml=\"http://www.w3.org/1998/Math/MathML\">\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.RNG)) {
                // standard RelaxNG
                tei.append("<?xml-model href=\"file://" +
                        GrobidDictionaryProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rng" +
                        "\" schematypens=\"http://relaxng.org/ns/structure/1.0\"?>\n");
            } else if (schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.RNC)) {
                // compact RelaxNG
                tei.append("<?xml-model href=\"file://" +
                        GrobidDictionaryProperties.get_GROBID_HOME_PATH() + "/schemas/rng/Grobid.rnc" +
                        "\" type=\"application/relax-ng-compact-syntax\"?>\n");
            }

            // by default there is no schema association
            if (!schemaDeclaration.equals(org.grobid.core.document.TEIFormatter.SchemaDeclaration.XSD)) {
                tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
            }
        } else {
            tei.append("<TEI xmlns=\"http://www.tei-c.org/ns/1.0\">\n");
        }

        if (doc != null && doc.getLanguage() != null) {
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

        tei.append("\t\t\t\t<application version=\"" + GrobidDictionaryProperties.getVersion() +
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




    // possible association to Grobid customised TEI schemas: DTD, XML schema, RelaxNG or compact RelaxNG
    // DEFAULT means no schema association in the generated XML documents
    public enum SchemaDeclaration {
        DEFAULT, DTD, XSD, RNG, RNC
    }



}
