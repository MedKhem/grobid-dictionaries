package org.grobid.core.document;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.TaggingLabel;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.enums.PossibleTags;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.KeyGen;
import org.grobid.core.utilities.LayoutTokensUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by med on 24.09.16.
 */
public class TEIDictionaryFormatter {

    private Document doc = null;

    public TEIDictionaryFormatter(Document document) {
        doc = document;
    }

    public StringBuilder toTEIFormat(GrobidAnalysisConfig config,
                                     SchemaDeclaration schemaDeclaration,
                                     String bodyContentFeatured, LayoutTokenization layoutTokenization) {
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
        tei.append("\t\t\t\t\t<ref target=\"https://github.com/kermitt2/grobid\">GROBID_Dictionaries - A machine learning software for structuring digitized dictionaries</ref>\n");
        tei.append("\t\t\t\t</application>\n");
        tei.append("\t\t\t</appInfo>\n");
        tei.append("\t\t</encodingDesc>");

        tei.append("\n\t\t<fileDesc>\n\t\t\t<titleStmt>\n\t\t\t\t<title level=\"a\" type=\"main\"");
        if (config.isGenerateTeiIds()) {
            String divID = KeyGen.getKey().substring(0, 7);
            tei.append(" xml:id=\"_" + divID + "\"");
        }
        tei.append(">");


        tei.append("\t</teiHeader>\n");

        if (doc.getLanguage() != null) {
            tei.append("\t<text xml:lang=\"").append(doc.getLanguage()).append("\">\n");
        } else {
            tei.append("\t<text>\n");
        }
        tei.append("\t<body>\n");
        tei.append(toTEIBodyLexicalEntries(bodyContentFeatured, layoutTokenization));
        tei.append("\t</body>\n");
        tei.append("\t</text>\n");

        return tei;
    }

    public StringBuilder toTEIBodyLexicalEntries(String bodyContentFeatured, LayoutTokenization layoutTokenization) {

        StringBuilder buffer = new StringBuilder();
        TaggingLabel lastClusterLabel = null;
        List<LayoutToken> tokenizations = layoutTokenization.getTokenization();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, bodyContentFeatured, tokenizations);

        String tokenLabel = null;
        List<TaggingTokenCluster> clusters = clusteror.cluster();


//        System.out.println(new TaggingTokenClusteror(GrobidModels.FULLTEXT, result, tokenizations).cluster());

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel)clusterLabel);

            // Problem with Grobid Normalisation
            List<LayoutToken> list1 = cluster.concatTokens();
            String str1 = LayoutTokensUtil.toText(list1);
            String clusterContent = LayoutTokensUtil.normalizeText(str1);
//            StringBuilder clusterContentBuilder = new StringBuilder();
//            String clusterContent;
//            List<LayoutToken> lisLayoutTokens = cluster.concatTokens();
//            for (LayoutToken layoutToken : lisLayoutTokens) {
//
//                clusterContentBuilder.append(" ").append(layoutToken.getText());
//            }
//            clusterContent = clusterContentBuilder.toString();

            String tagLabel = clusterLabel.getLabel();


            if (tagLabel.equals(PossibleTags.ENTRY.getTag())) {
                buffer.append(createMyXMLString("entry", clusterContent));
            } else if (tagLabel.equals(PossibleTags.FORM.getTag())) {
                buffer.append(createMyXMLString("form", clusterContent));
            } else if (tagLabel.equals(PossibleTags.SENSE.getTag())) {
                buffer.append(createMyXMLString("sense", clusterContent));
            } else if (tagLabel.equals(PossibleTags.METAMARK.getTag())) {
                buffer.append(createMyXMLString("metamark", clusterContent));
            } else if (tagLabel.equals(PossibleTags.ETYM.getTag())) {
                buffer.append(createMyXMLString("etym", clusterContent));
            } else if (tagLabel.equals(PossibleTags.RE.getTag())) {
                buffer.append(createMyXMLString("re", clusterContent));
            } else if (tagLabel.equals(PossibleTags.NOTE.getTag())) {
                buffer.append(createMyXMLString("note", clusterContent));
            } else {
                throw new IllegalArgumentException(tagLabel + " is not a valid possible tag");
            }


        }

        return buffer;
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
        xmlStringElement.append("\n");

        return xmlStringElement.toString();
    }

    // possible association to Grobid customised TEI schemas: DTD, XML schema, RelaxNG or compact RelaxNG
    // DEFAULT means no schema association in the generated XML documents
    public enum SchemaDeclaration {
        DEFAULT, DTD, XSD, RNG, RNC
    }


}
