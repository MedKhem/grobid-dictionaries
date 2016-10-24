package org.grobid.core.document;

import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.features.FeaturesUtils;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

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
                DocumentSource.close(documentSource, false);
            } else {
                DocumentSource.close(documentSource, true);
            }
        }
        return doc;
    }

    public static LayoutTokenization getLayoutTokenizations(Document doc, SortedSet<DocumentPiece> documentBodyParts) {


        List<LayoutToken> layoutTokens = new ArrayList<>();

        if (documentBodyParts != null) {


            for (DocumentPiece docPiece : documentBodyParts) {
                //Every document Piece contains two Parts
                DocumentPointer dp1 = docPiece.a;
                DocumentPointer dp2 = docPiece.b;
                //The first part is identified by its first token. The second part is identified by its final token
                int tokenStart = dp1.getTokenDocPos();
                int tokenEnd = dp2.getTokenDocPos();
                LayoutToken token = null;
                Boolean isNewline = true;
                for (int i = tokenStart; i <= tokenEnd; i++) {
                    token = doc.getTokenizations().get(i);
                    if ((token.getText() == null) || (token.getText().trim().equals("\n")) ||  (token.getText().trim().equals("\r")) ||  (token.getText().trim().equals("\n\r"))){
                        isNewline = true;
                        continue;
                    }
                    if ( isNewline ) {
                        // We use the token property "new line after" as it was a property for the actual token representing a new line and for the token that follows
                        // Carefull with the understanding of this method
                        token.setNewLineAfter(true);
                        isNewline = false;
                            }
                    else{
                        token.setNewLineAfter(false);
                    }
                    layoutTokens.add(token);
                }
            }
        }

        // the old mothod to get te layoutTokenization thatdoesn't use the docPart
//            List<LayoutToken> layoutTokens = new ArrayList<>();
//        for (Page page : doc.getPages()) {
//            //get the blocks
//            if ((page.getBlocks() == null) || (page.getBlocks().size() == 0))
//                continue;
//
//            for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
//                Block block = page.getBlocks().get(blockIndex);
//
//                String localText = block.getText();
//                List<LayoutToken> tokens = block.getTokens();
//
//                if (localText != null) {
//                    if (tokens != null) {
//
//                        String[] lines = localText.split("[\\n\\r]");
//
////                        //For each line of the block
////                        for (int li = 0; li < lines.length; li++) {
////                            String line = lines[li].trim();
////
//
//                        // For each token of the block
//                        for (int k = 0; k < tokens.size(); k++) {
//                            //catch the line status here: now it's based on the position of the token in the block. Should be modified for the position in the line
//                            LayoutToken token = null;
//
//                            token = tokens.get(k);
//                            if ((token.getText() == null) ||
//                                    (token.getText().trim().length() == 0) ||
//                                    (token.getText().trim().equals("\n")) ||
//                                    (token.getText().trim().equals("\r")) ||
//                                    (token.getText().trim().equals("\n\r")))
//                                continue;
//                            else if (token.isNewLineAfter() || (block.getEndToken() == k)) {
//                                token.setNewLineAfter(true);
//                            }
//
//                            layoutTokens.add(token);
//
//
//                        }
//
//                    }
//
//
//                }
//            }
//        }

        return new LayoutTokenization(layoutTokens);
    }
}
