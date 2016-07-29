package org.grobid.core.analyzers;

import org.grobid.core.document.Document;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorSegmentation;
import org.grobid.core.layout.*;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.TextUtilities;

import java.util.*;
import java.util.regex.Matcher;

/**
 * TODO Tokenization on the line level
 * Created by med on 27.07.16.
 */
public class GrobidLineAnalyser {

    // default bins for relative position
    private static final int NBBINS_POSITION = 12;

    // default bins for inter-block spacing
    private static final int NBBINS_SPACE = 5;

    // default bins for block character density
    private static final int NBBINS_DENSITY = 5;

    // projection scale for line length
    private static final int LINESCALE = 10;

    public static String getAllLinesFeatured(Document doc) {
        FeatureFactory featureFactory = FeatureFactory.getInstance();
        StringBuilder fulltext = new StringBuilder();
        String currentFont = null;
        int currentFontSize = -1;

        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }

        //guaranteeing quality of service. Otherwise, there are some PDF that may contain 300k blocks and thousands of extracted "images" that ruins the performance
        if (blocks.size() > GrobidProperties.getPdfBlocksMax()) {
            throw new GrobidException("Postprocessed document is too big, contains: " + blocks.size(), GrobidExceptionStatus.TOO_MANY_BLOCKS);
        }

        List<Page> pages = doc.getPages();

        // vector for features
        FeaturesVectorSegmentation features;
        FeaturesVectorSegmentation previousFeatures = null;
        //boolean endblock = false;
        //boolean endPage = true;
        boolean newPage;
        boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        int pageLength = 0; // length of the current page

        List<LayoutToken> tokenizationsBody = new ArrayList<LayoutToken>();
        List<LayoutToken> tokenizations = doc.getTokenizations();

        int documentLength = doc.getDocumentLenghtChar();

        double pageHeight = 0.0;
        boolean graphicVector = false;
        boolean graphicBitmap = false;

        // list of textual patterns at the head and foot of pages which can be re-occur on several pages
        // (typically indicating a publisher foot or head notes)
        Map<String, Integer> patterns = new TreeMap<String, Integer>();
        Map<String, Boolean> firstTimePattern = new TreeMap<String, Boolean>();
        for(Page page : pages) {
            pageHeight = page.getHeight();
            // we just look at the two first and last blocks of the page
            if ((page.getBlocks() != null) && (page.getBlocks().size() > 0)) {
                for(int blockIndex=0; blockIndex < page.getBlocks().size(); blockIndex++) {
                    if ( (blockIndex < 2) || (blockIndex > page.getBlocks().size()-2)) {
                        Block block = page.getBlocks().get(blockIndex);
                        String localText = block.getText();
                        if ((localText != null) && (localText.length() > 0)) {
                            String[] lines = localText.split("[\\n\\r]");
                            if (lines.length > 0) {
                                String line = lines[0];
                                String pattern = FeatureFactory.getPattern(line);
                                if (pattern.length() > 8) {
                                    Integer nb = patterns.get(pattern);
                                    if (nb == null) {
                                        patterns.put(pattern, new Integer(1));
                                        firstTimePattern.put(pattern, false);
                                    }
                                    else
                                        patterns.put(pattern, new Integer(nb+1));
                                }
                            }
                        }
                    }
                }
            }
        }

        for(Page page : pages) {
            pageHeight = page.getHeight();
            newPage = true;
            double spacingPreviousBlock = 0.0; // discretized
            double lowestPos = 0.0;
            pageLength = page.getPageLengthChar();
            BoundingBox pageBoundingBox = page.getMainArea();
            mm = 0;
            //endPage = true;

            if ((page.getBlocks() == null) || (page.getBlocks().size() == 0))
                continue;

            for(int blockIndex=0; blockIndex < page.getBlocks().size(); blockIndex++) {
                Block block = page.getBlocks().get(blockIndex);
                /*if (start) {
                    newPage = true;
                    start = false;
                }*/
                boolean lastPageBlock = false;
                boolean firstPageBlock = false;
                if (blockIndex == page.getBlocks().size()-1) {
                    lastPageBlock = true;
                }

                if (blockIndex == 0) {
                    firstPageBlock = true;
                }

                //endblock = false;

                /*if (endPage) {
                    newPage = true;
                    mm = 0;
                }*/

                // check if we have a graphical object connected to the current block
                List<GraphicObject> localImages = Document.getConnectedGraphics(block, doc);
                if (localImages != null) {
                    for(GraphicObject localImage : localImages) {
                        if (localImage.getType() == GraphicObjectType.BITMAP)
                            graphicVector = true;
                        if (localImage.getType() == GraphicObjectType.VECTOR)
                            graphicBitmap = true;
                    }
                }

                if (lowestPos >  block.getY()) {
                    // we have a vertical shift, which can be due to a change of column or other particular layout formatting
                    spacingPreviousBlock = doc.getMaxBlockSpacing() / 5.0; // default
                }
                else
                    spacingPreviousBlock = block.getY() - lowestPos;

                String localText = block.getText();
                if (localText == null)
                    continue;

                // character density of the block
                double density = 0.0;
                if ( (block.getHeight() != 0.0) && (block.getWidth() != 0.0) &&
                        (block.getText() != null) && (!block.getText().contains("@PAGE")) &&
                        (!block.getText().contains("@IMAGE")) )
                    density = (double)block.getText().length() / (block.getHeight() * block.getWidth());

                // is the current block in the main area of the page or not?
                boolean inPageMainArea = true;
                BoundingBox blockBoundingBox = BoundingBox.fromPointAndDimensions(page.getNumber(),
                        block.getX(), block.getY(), block.getWidth(), block.getHeight());
                if (pageBoundingBox == null || (!pageBoundingBox.contains(blockBoundingBox) && !pageBoundingBox.intersect(blockBoundingBox)))
                    inPageMainArea = false;

                String[] lines = localText.split("[\\n\\r]");
                // set the max length of the lines in the block, in number of characters
                int maxLineLength = 0;
                for(int p=0; p<lines.length; p++) {
                    if (lines[p].length() > maxLineLength)
                        maxLineLength = lines[p].length();
                }
                List<LayoutToken> tokens = block.getTokens();
                if ((tokens == null) || (tokens.size() == 0)) {
                    continue;
                }
                for (int li = 0; li < lines.length; li++) {
                    String line = lines[li];
                    /*boolean firstPageBlock = false;
                    boolean lastPageBlock = false;

                    if (newPage)
                        firstPageBlock = true;
                    if (endPage)
                        lastPageBlock = true;
                    */

                    // for the layout information of the block, we take simply the first layout token
                    LayoutToken token = null;
                    if (tokens.size() > 0)
                        token = tokens.get(0);

                    double coordinateLineY = token.getY();

                    features = new FeaturesVectorSegmentation();
                    features.token = token;
                    features.line = line;

                    if ( (blockIndex < 2) || (blockIndex > page.getBlocks().size()-2)) {
                        String pattern = FeatureFactory.getPattern(line);
                        Integer nb = patterns.get(pattern);
                        if ((nb != null) && (nb > 1)) {
                            features.repetitivePattern = true;

                            Boolean firstTimeDone = firstTimePattern.get(pattern);
                            if ((firstTimeDone != null) && !firstTimeDone) {
                                features.firstRepetitivePattern = true;
                                firstTimePattern.put(pattern, true);
                            }
                        }
                    }

                    StringTokenizer st2 = new StringTokenizer(line, " \t");
                    String text = null;
                    String text2 = null;
                    if (st2.hasMoreTokens())
                        text = st2.nextToken();
                    if (st2.hasMoreTokens())
                        text2 = st2.nextToken();
                    if ((text == null) ||
                            (text.trim().length() == 0) ||
                            (text.trim().equals("\n")) ||
                            (text.trim().equals("\r")) ||
                            (text.trim().equals("\n\r")) ||
                            (TextUtilities.filterLine(line))) {
                        continue;
                    }

                    text = text.trim();

                    features.string = text;
                    features.secondString = text2;

                    features.firstPageBlock = firstPageBlock;
                    features.lastPageBlock = lastPageBlock;
                    //features.lineLength = line.length() / LINESCALE;
                    features.lineLength = featureFactory
                            .linearScaling(line.length(), maxLineLength, LINESCALE);

                    features.punctuationProfile = TextUtilities.punctuationProfile(line);

                    if (graphicBitmap) {
                        features.bitmapAround = true;
                    }
                    if (graphicVector) {
                        features.vectorAround = true;
                    }

                    features.lineStatus = null;
                    features.punctType = null;

                    if ((li == 0) ||
                            ((previousFeatures != null) && previousFeatures.blockStatus.equals("BLOCKEND"))) {
                        features.blockStatus = "BLOCKSTART";
                    } else if (li == lines.length - 1) {
                        features.blockStatus = "BLOCKEND";
                        //endblock = true;
                    } else if (features.blockStatus == null) {
                        features.blockStatus = "BLOCKIN";
                    }

                    if (newPage) {
                        features.pageStatus = "PAGESTART";
                        newPage = false;
                        //endPage = false;
                        if (previousFeatures != null)
                            previousFeatures.pageStatus = "PAGEEND";
                    } else {
                        features.pageStatus = "PAGEIN";
                        newPage = false;
                        //endPage = false;
                    }

                    if (text.length() == 1) {
                        features.singleChar = true;
                    }

                    if (Character.isUpperCase(text.charAt(0))) {
                        features.capitalisation = "INITCAP";
                    }

                    if (featureFactory.test_all_capital(text)) {
                        features.capitalisation = "ALLCAP";
                    }

                    if (featureFactory.test_digit(text)) {
                        features.digit = "CONTAINSDIGITS";
                    }

                    if (featureFactory.test_common(text)) {
                        features.commonName = true;
                    }

                    if (featureFactory.test_names(text)) {
                        features.properName = true;
                    }

                    if (featureFactory.test_month(text)) {
                        features.month = true;
                    }

                    Matcher m = featureFactory.isDigit.matcher(text);
                    if (m.find()) {
                        features.digit = "ALLDIGIT";
                    }

                    Matcher m2 = featureFactory.YEAR.matcher(text);
                    if (m2.find()) {
                        features.year = true;
                    }

                    Matcher m3 = featureFactory.EMAIL.matcher(text);
                    if (m3.find()) {
                        features.email = true;
                    }

                    Matcher m4 = featureFactory.HTTP.matcher(text);
                    if (m4.find()) {
                        features.http = true;
                    }

                    if (currentFont == null) {
                        currentFont = token.getFont();
                        features.fontStatus = "NEWFONT";
                    } else if (!currentFont.equals(token.getFont())) {
                        currentFont = token.getFont();
                        features.fontStatus = "NEWFONT";
                    } else
                        features.fontStatus = "SAMEFONT";

                    int newFontSize = (int) token.getFontSize();
                    if (currentFontSize == -1) {
                        currentFontSize = newFontSize;
                        features.fontSize = "HIGHERFONT";
                    } else if (currentFontSize == newFontSize) {
                        features.fontSize = "SAMEFONTSIZE";
                    } else if (currentFontSize < newFontSize) {
                        features.fontSize = "HIGHERFONT";
                        currentFontSize = newFontSize;
                    } else if (currentFontSize > newFontSize) {
                        features.fontSize = "LOWERFONT";
                        currentFontSize = newFontSize;
                    }

                    if (token.getBold())
                        features.bold = true;

                    if (token.getItalic())
                        features.italic = true;

                    // HERE horizontal information
                    // CENTERED
                    // LEFTAJUSTED
                    // CENTERED

                    if (features.capitalisation == null)
                        features.capitalisation = "NOCAPS";

                    if (features.digit == null)
                        features.digit = "NODIGIT";

                    //if (features.punctType == null)
                    //    features.punctType = "NOPUNCT";

                    features.relativeDocumentPosition = featureFactory
                            .linearScaling(nn, documentLength, NBBINS_POSITION);
//System.out.println(nn + " " + documentLength + " " + NBBINS_POSITION + " " + features.relativeDocumentPosition);
                    features.relativePagePositionChar = featureFactory
                            .linearScaling(mm, pageLength, NBBINS_POSITION);
//System.out.println(mm + " " + pageLength + " " + NBBINS_POSITION + " " + features.relativePagePositionChar);
                    int pagePos = featureFactory
                            .linearScaling(coordinateLineY, pageHeight, NBBINS_POSITION);
//System.out.println(coordinateLineY + " " + pageHeight + " " + NBBINS_POSITION + " " + pagePos);
                    if (pagePos > NBBINS_POSITION)
                        pagePos = NBBINS_POSITION;
                    features.relativePagePosition = pagePos;
//System.out.println(coordinateLineY + "\t" + pageHeight);

                    if (spacingPreviousBlock != 0.0) {
                        features.spacingWithPreviousBlock = featureFactory
                                .linearScaling(spacingPreviousBlock-doc.getMinBlockSpacing(), doc.getMaxBlockSpacing()-doc.getMinBlockSpacing(), NBBINS_SPACE);
                    }

                    features.inMainArea = inPageMainArea;

                    if (density != -1.0) {
                        features.characterDensity = featureFactory
                                .linearScaling(density-doc.getMinCharacterDensity(), doc.getMaxCharacterDensity()-doc.getMinCharacterDensity(), NBBINS_DENSITY);
//System.out.println((density-doc.getMinCharacterDensity()) + " " + (doc.getMaxCharacterDensity()-doc.getMinCharacterDensity()) + " " + NBBINS_DENSITY + " " + features.characterDensity);
                    }

                    if (previousFeatures != null) {
                        String vector = previousFeatures.printVector();
                        fulltext.append(vector);
                    }
                    previousFeatures = features;
                }

//System.out.println((spacingPreviousBlock-doc.getMinBlockSpacing()) + " " + (doc.getMaxBlockSpacing()-doc.getMinBlockSpacing()) + " " + NBBINS_SPACE + " "
//    + featureFactory.linearScaling(spacingPreviousBlock-doc.getMinBlockSpacing(), doc.getMaxBlockSpacing()-doc.getMinBlockSpacing(), NBBINS_SPACE));

                // lowest position of the block
                lowestPos = block.getY() + block.getHeight();

                // update page-level and document-level positions
                if (tokens != null) {
                    mm += tokens.size();
                    nn += tokens.size();
                }
            }
        }
        if (previousFeatures != null)
            fulltext.append(previousFeatures.printVector());

        return fulltext.toString();
    }
}
