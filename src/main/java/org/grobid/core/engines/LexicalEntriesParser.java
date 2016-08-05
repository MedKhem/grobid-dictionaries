package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.data.Figure;
import org.grobid.core.data.Table;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.data.LexicalEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Consumer;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by med on 02.08.16.
 */
public class LexicalEntriesParser extends AbstractParser {
    private static final Logger logger = LoggerFactory.getLogger(LexicalEntriesParser.class);

    private String lexEntries;

    public LexicalEntriesParser() {
        super(GrobidModels.FULLTEXT);
      //  FULLTEXT to be replaced by LEXICALENTRIES model;


    }


    public String processing(File originFile, GrobidAnalysisConfig config) throws Exception {
        // Segmenter to identify the document's block
        lexEntries = new Engine().fullTextToTEI(originFile,config);
      /*  DocumentSource documentSource = DocumentSource.fromPdf(originFile, config.getStartPage(), config.getEndPage(), config.getPdfAssetPath() != null);
        Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, config);

        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);
        FullTextParser parser = new EngineParsers().getFullTextParser();

        documentBodyParts.forEach(
                new Consumer<DocumentPiece>() {
                    @Override
                    public void accept(DocumentPiece documentPiece) {
                        lexEntries = doc.getDocumentPieceText(documentPiece);
                        lexEntries = TextUtilities.dehyphenize(lexEntries);

                        // full text processing
                        Pair<String, LayoutTokenization> featSeg = parser.getBodyTextFeatured(doc, documentBodyParts);
                        String rese = null;
                        LayoutTokenization layoutTokenization = null;
                        List<Figure> figures = null;
                        List<Table> tables = null;
                        if (featSeg != null) {
                            // if featSeg is null, it usually means that no body segment is found in the
                            // document segmentation
                            String bodytext = featSeg.getA();
                            layoutTokenization = featSeg.getB();
                            //tokenizationsBody = featSeg.getB().getTokenization();
                            //layoutTokensBody = featSeg.getB().getLayoutTokens();
                            if ((bodytext != null) && (bodytext.trim().length() > 0)) {
                                rese = label(bodytext);
                            }

                        }
                    }
                }
        );*/



        return lexEntries;
    }


    /**
     * Extract all occurrences of measurement/quantities from a simple piece of text.
     */
    public Response extractLexicalEntries(File inputFile, GrobidAnalysisConfig config) throws Exception {

        Response ob = null;
        return ob;

    }

}
