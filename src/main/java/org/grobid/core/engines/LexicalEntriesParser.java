package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentPointer;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * Created by med on 02.08.16.
 */
public class LexicalEntriesParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LexicalEntriesParser.class);

    private String lexEntries;

    //Might be needed to have several LEXICALENTRIES_XYZ models, based on the function,
    // depending how many sub models will be created.
    public LexicalEntriesParser() {
        super(GrobidModels.SEGMENTATION);
    }


    public String process(File originFile) {
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().generateTeiIds(true).build();

        // Segment the document to identify the document's block
        DocumentSource documentSource = DocumentSource.fromPdf(originFile, config.getStartPage(), config.getEndPage(), config.getPdfAssetPath() != null);
        Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, config);

        //only body please :)
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);

        LayoutTokenization tokens = getLayoutTokenizations(doc, documentBodyParts);
        String text = tokens.getTokenization().stream().map(LayoutToken::getText).collect(Collectors.joining());

        return text;
    }

    LayoutTokenization getLayoutTokenizations(Document doc, SortedSet<DocumentPiece> documentBodyParts) {
        String currentFont = null;
        int currentFontSize = -1;
        boolean endblock;
        boolean endPage = true;
        boolean newPage = true;
        //boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        double lineStartX = Double.NaN;
        boolean indented = false;
        int fulltextLength = 0;
        int pageLength = 0; // length of the current page
        double lowestPos = 0.0;
        double spacingPreviousBlock = 0.0;
        int currentPage = 0;


        List<LayoutToken> layoutTokens = new ArrayList<>();

        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            //TODO: should we return null?
            return null;
        }

        for (DocumentPiece docPiece : documentBodyParts) {
            DocumentPointer dp1 = docPiece.a;
            DocumentPointer dp2 = docPiece.b;

            //int blockPos = dp1.getBlockPtr();
            for (int blockIndex = dp1.getBlockPtr(); blockIndex <= dp2.getBlockPtr(); blockIndex++) {
                boolean graphicVector = false;
                boolean graphicBitmap = false;
                Block block = blocks.get(blockIndex);
                // length of the page where the current block is
                double pageHeight = block.getPage().getHeight();
                int localPage = block.getPage().getNumber();
                if (localPage != currentPage) {
                    newPage = true;
                    currentPage = localPage;
                    mm = 0;
                    lowestPos = 0.0;
                    spacingPreviousBlock = 0.0;
                }

	            /*if (start) {
                    newPage = true;
	                start = false;
	            }*/

                boolean newline;
                boolean previousNewline = false;
                endblock = false;

	            /*if (endPage) {
                    newPage = true;
	                mm = 0;
					lowestPos = 0.0;
	            }*/

                if (lowestPos > block.getY()) {
                    // we have a vertical shift, which can be due to a change of column or other particular layout formatting
                    spacingPreviousBlock = doc.getMaxBlockSpacing() / 5.0; // default
                } else
                    spacingPreviousBlock = block.getY() - lowestPos;

                String localText = block.getText();
                if (TextUtilities.filterLine(localText)) {
                    continue;
                }
                /*if (localText != null) {
                    if (localText.contains("@PAGE")) {
	                    mm = 0;
	                    // pageLength = 0;
	                    endPage = true;
	                    newPage = false;
	                } else {
	                    endPage = false;
	                }
	            }*/

                // character density of the block
                double density = 0.0;
                if ((block.getHeight() != 0.0) && (block.getWidth() != 0.0) &&
                        (localText != null) && (!localText.contains("@PAGE")) &&
                        (!localText.contains("@IMAGE")))
                    density = (double) localText.length() / (block.getHeight() * block.getWidth());


                List<LayoutToken> tokens = block.getTokens();
                if (tokens == null) {
                    continue;
                }

                int n = 0;// token position in current block
                if (blockIndex == dp1.getBlockPtr()) {
//					n = dp1.getTokenDocPos() - block.getStartToken();
                    n = dp1.getTokenBlockPos();
                }
                int lastPos = tokens.size();
                // if it's a last block from a document piece, it may end earlier
                if (blockIndex == dp2.getBlockPtr()) {
                    lastPos = dp2.getTokenBlockPos();
                    if (lastPos >= tokens.size()) {
                        LOGGER.error("DocumentPointer for block " + blockIndex + " points to " +
                                dp2.getTokenBlockPos() + " token, but block token size is " +
                                tokens.size());
                        lastPos = tokens.size();
                    }
                }

                while (n < lastPos) {
                    if (blockIndex == dp2.getBlockPtr()) {
                        //if (n > block.getEndToken()) {
                        if (n > dp2.getTokenDocPos() - block.getStartToken()) {
                            break;
                        }
                    }

                    LayoutToken token = tokens.get(n);
                    layoutTokens.add(token);

                    double coordinateLineY = token.getY();

                    String text = token.getText();
                    if ((text == null) || (text.length() == 0)) {
                        n++;
                        mm++;
                        nn++;
                        continue;
                    }
                    text = text.replace(" ", "");
                    if (text.length() == 0) {
                        n++;
                        mm++;
                        nn++;
                        continue;
                    }

                    if (text.equals("\n") || text.equals("\r")) {
                        newline = true;
                        previousNewline = true;
                        n++;
                        mm++;
                        nn++;
                        continue;
                    } else
                        newline = false;

                    if (TextUtilities.filterLine(text)) {
                        n++;
                        continue;
                    }

                    if (previousNewline) {
                        newline = true;
                        previousNewline = false;
                        if ((token != null)) {
                            double previousLineStartX = lineStartX;
                            lineStartX = token.getX();
                            double characterWidth = token.width / text.length();
                            if (!Double.isNaN(previousLineStartX)) {
                                if (previousLineStartX - lineStartX > characterWidth)
                                    indented = false;
                                else if (lineStartX - previousLineStartX > characterWidth)
                                    indented = true;
                                // Indentation ends if line start is > 1 character width to the left of previous line start
                                // Indentation starts if line start is > 1 character width to the right of previous line start
                                // Otherwise indentation is unchanged
                            }
                        }
                    }


                    if (n == 0) {
                        if (token != null)
                            lineStartX = token.getX();
                    } else if (n == tokens.size() - 1) {
                        previousNewline = true;
                        endblock = true;
                    } else {
                        // look ahead...
                        boolean endline = false;

                        int ii = 1;
                        boolean endloop = false;
                        while ((n + ii < tokens.size()) && (!endloop)) {
                            LayoutToken tok = tokens.get(n + ii);
                            if (tok != null) {
                                String toto = tok.getText();
                                if (toto != null) {
                                    if (toto.equals("\n")) {
                                        endline = true;
                                        endloop = true;
                                    } else {
                                        if ((toto.length() != 0)
                                                && (!(toto.startsWith("@IMAGE")))
                                                && (!(toto.startsWith("@PAGE")))
                                                && (!text.contains(".pbm"))
                                                && (!text.contains(".vec"))
                                                && (!text.contains(".jpg"))) {
                                            endloop = true;
                                        }
                                    }
                                }
                            }

                            if (n + ii == tokens.size() - 1) {
//                                endblock = true;
                                endline = true;
                            }

                            ii++;
                        }

                    }

                    n++;
//                    mm += text.length();
//                    nn += text.length();
                }
                // lowest position of the block
//                lowestPos = block.getY() + block.getHeight();

                //blockPos++;
            }
        }
        return new LayoutTokenization(layoutTokens);
    }


    public void createTrainingData(String inputFile,
                                   String pathFullText) {

        Writer writer = null;
        DocumentSource documentSource = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            File file = new File(inputFile);
            String PDFFileName = file.getName();

            documentSource = DocumentSource.fromPdf(file);
            Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, GrobidAnalysisConfig.defaultInstance());
            SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);

            LayoutTokenization tokens = getLayoutTokenizations(doc, documentBodyParts);
            String previousFont = null;
            String fontStatus = null;
            String previousLine = null;
            String lineStatus = null;

            for(LayoutToken layoutToken : tokens.getTokenization()) {

                if (previousFont == null) {
                    previousFont = layoutToken.getFont();
                    fontStatus = "NEWFONT";
                } else if (!previousFont.equals(layoutToken.getFont())) {
                    previousFont = layoutToken.getFont();
                    fontStatus = "NEWFONT";
                } else{
                    fontStatus = "SAMEFONT";
                }

                if (previousLine == null){
                    lineStatus = "LINESTART";
                    previousLine  = "LINESTART";
                }
                else if(layoutToken.getText().equals("\n")){
                    lineStatus = "LINEEND";
                    previousLine = "LINEEND";
                }else if (!layoutToken.getText().equals("\n")&&(previousLine=="LINEEND")){
                    lineStatus= "LINESTART";
                    previousLine = "LINESTART";
                }else if ((!layoutToken.getText().equals("\n"))&&((previousLine=="LINESTART")||(previousLine=="LINEIN"))){
                    lineStatus= "LINEIN";
                    previousLine = "LINEIN";
                }

                FeatureVectorLexicalEntry vector = FeatureVectorLexicalEntry.addFeaturesLexicalEntries(layoutToken, "", lineStatus, fontStatus);
                stringBuilder.append(vector.printVector());
            }

            // we write the features
            String outPathFulltext = pathFullText + File.separator +
                    PDFFileName.replace(".pdf", ".training.segmentation");
            writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");

            writer.write(stringBuilder + "\n");
        } catch (Exception e) {
           //TODO: something
        } finally {
            try {
                writer.close();
            } catch (IOException e) {

            }
        }
    }

}
