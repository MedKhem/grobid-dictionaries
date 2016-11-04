package org.grobid.core.document;

import org.grobid.core.engines.config.GrobidAnalysisConfig;
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
}
