package org.grobid.core.document;

import org.grobid.core.engines.EngineParsers;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.layout.Page;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.KeyGen;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;

/**
 * Created by med on 20.10.16.
 */
public class DocumentUtils {

    public DocumentUtils() {

    }

    public static Document getDocFromPDF(File originFile) {
        DocumentSource documentSource = null;


        documentSource = DocumentSource.fromPdf(originFile);
        Document doc;
        try {
            doc = new Document(documentSource);
            doc.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());

        } finally {
            if (GrobidAnalysisConfig.defaultInstance().getPdfAssetPath() == null) {
                documentSource.close(true, true);
            } else {
                documentSource.close(false, true);
            }
        }
        return doc;
    }

    public static LayoutTokenization getLayoutTokenizations(DictionaryDocument doc, SortedSet<DocumentPiece> documentParts) {


        List<LayoutToken> layoutTokens = new ArrayList<>();

        if (documentParts != null) {


            for (DocumentPiece docPiece : documentParts) {
                //Every document Piece contains two Parts
                DocumentPointer dp1 = docPiece.a;
                DocumentPointer dp2 = docPiece.b;
                //The first part is identified by its first token. The second part is identified by its final token
                int tokenStart = dp1.getTokenDocPos();
                int tokenEnd = dp2.getTokenDocPos();
                LayoutToken token;

                for (int i = tokenStart; i <= tokenEnd; i++) {
                    token = doc.getTokenizations().get(i);
                    if ((token.getText() == null) || (token.getText().equals("\n")) || (token.getText().equals("\r")) || (token.getText().equals("\n\r"))) {
                        token.setNewLineAfter(true);

                    }

                    layoutTokens.add(token);
                }
            }
        }


        return new LayoutTokenization(layoutTokens);
    }

    public static LayoutTokenization getLayoutTokenizationsIndiferrentFromBodyPart(DictionaryDocument doc) {

        // Method to create layoutTokneizations for the whole document. Useful for the DictionarySegmentation model
        List<LayoutToken> layoutTokens = new ArrayList<>();

        for (Page page : doc.getPages()) {
            //get the blocks
            if ((page.getBlocks() == null) || (page.getBlocks().size() == 0))
                continue;

            for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                Block block = page.getBlocks().get(blockIndex);

                String localText = block.getText();
                List<LayoutToken> tokens = block.getTokens();

                if (localText != null) {
                    if (tokens != null) {
                        // For each token of the block
                        for (int k = 0; k < tokens.size(); k++) {
                            LayoutToken token = tokens.get(k);
                            layoutTokens.add(token);

                        }
                    }
                }
            }
        }

        return new LayoutTokenization(layoutTokens);
    }
    public static StringBuffer getRawTextFromDoc(DictionaryDocument doc) {

        // Method to create raw for the whole document. Useful for the DictionarySegmentation model
        List<LayoutToken> layoutTokens = new ArrayList<>();
        StringBuffer rawtxt = new StringBuffer();
        for (Page page : doc.getPages()) {
            //get the blocks
            if ((page.getBlocks() == null) || (page.getBlocks().size() == 0))
                continue;

            for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                Block block = page.getBlocks().get(blockIndex);

                String localText = block.getText();
                List<LayoutToken> tokens = block.getTokens();

                if (localText != null) {
                    if (tokens != null) {
                        // For each token of the block
                        for (int k = 0; k < tokens.size(); k++) {
                            LayoutToken token = tokens.get(k);
                            rawtxt.append(token.getText());

                        }
                    }
                }
            }
        }

        return rawtxt;
    }
    public static Document docPrepare(File originFile) {

        // Method to create raw for the whole document. Useful for the DictionarySegmentation model
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().generateTeiIds(true).build();
        DocumentSource documentSource = DocumentSource.fromPdf(originFile, config.getStartPage(), config.getEndPage());
        //Old BODY from document
        Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, config);

        return doc;
    }

    public static StringBuilder getBodyTEI(TEIDictionaryFormatter.SchemaDeclaration schemaDeclaration, DictionaryDocument doc, String bodytext) {

        // Method to create raw for the whole document. Useful for the DictionarySegmentation model
        List<LayoutToken> layoutTokens = new ArrayList<>();
        StringBuilder tei = new StringBuilder();
        tei.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
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
        // Not useful for the moment
//        if (config.isGenerateTeiIds()) {
//            String divID = KeyGen.getKey().substring(0, 7);
//            tei.append(" xml:id=\"_" + divID + "\"");
//        }
        tei.append("/>");
        tei.append("\n\t\t\t</titleStmt>\n");
        tei.append("\n\t\t</fileDesc>\n");
        tei.append("\t</teiHeader>\n");

        if (doc.getLanguage() != null) {
            tei.append("\t<text xml:lang=\"").append(doc.getLanguage()).append("\">\n");
        } else {
            tei.append("\t<text>\n");
        }
        tei.append("\t\t<body>");
        tei.append(getRawTextFromDoc(doc));
        tei.append("</body>\n");
        tei.append("\t</text>\n");
        tei.append("</TEI>\n");

        return tei;

    }

    public static List<Block> getBlocksFromDocumentPart(SortedSet<DocumentPiece> documentBodyParts, DictionaryDocument doc){

        // Get all the blocks from the document
        List<Block> blocks = doc.getBlocks();
        // Extract just the blocks from the specific document part
        List<Block> documentPartBlocks = null;

        for (DocumentPiece docPiece : documentBodyParts) {
            DocumentPointer dp1 = docPiece.a;
            DocumentPointer dp2 = docPiece.b;

            //int blockPos = dp1.getBlockPtr();
            for (int blockIndex = dp1.getBlockPtr(); blockIndex <= dp2.getBlockPtr(); blockIndex++) {
                documentPartBlocks.add(blocks.get(blockIndex));

            }
        }

        return   documentPartBlocks;
    }

    public static String replaceLinebreaksWithTags (String text){
        // This method is useful to have a pre-annotated training data. The line breaks, for this purpose, should be transformed to tags
        text = text.replace("\n","<lb/>");
        while (text.indexOf("<lb/><lb/>") != -1){
            text = text.replace("<lb/><lb/>","<lb/>");
        }
        return text;
    }

    public static String escapeHTMLCharac (String text){

        text = text.replace("&","&amp;");
        text = text.replace("<","&lt;");
        text = text.replace(">","&gt;");

//        text = text.replace("\'","&apos;");
//        text = text.replace("\"","&quot;");


        return text;
    }
}
