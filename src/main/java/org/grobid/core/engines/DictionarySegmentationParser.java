package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import eugfc.imageio.plugins.PNMRegistry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.io.IOUtils;
import org.grobid.core.data.LabeledLexicalInformation;
import org.grobid.core.document.*;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorSegmentation;
import org.grobid.core.layout.*;
import org.grobid.core.utilities.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.grobid.core.engines.label.DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL;

/**
 * Created by med on 02.08.16.
 */
public class DictionarySegmentationParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionarySegmentationParser.class);
    // default bins for relative position
    private static final int NBBINS_POSITION = 12;
    // default bins for inter-block spacing
    private static final int NBBINS_SPACE = 5;
    // default bins for block character density
    private static final int NBBINS_DENSITY = 5;
    // projection scale for line length
    private static final int LINESCALE = 10;
    private static volatile DictionarySegmentationParser instance;
    private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
    private FeatureFactory featureFactory = FeatureFactory.getInstance();

    public DictionarySegmentationParser() {
        super(DictionaryModels.DICTIONARY_SEGMENTATION);
    }

    public static DictionarySegmentationParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Create a new instance.
     */
    private static synchronized void getNewInstance() {
        instance = new DictionarySegmentationParser();
    }

    static public DictionaryDocument generalResultSegmentation(DictionaryDocument doc, String labeledResult, List<LayoutToken> documentTokens) {

        List<Pair<String, String>> labeledTokens = GenericTaggerUtils.getTokensAndLabels(labeledResult);

        SortedSetMultimap<String, DocumentPiece> labeledBlocks = TreeMultimap.create();
        doc.setLabeledBlocks(labeledBlocks);
        List<Block> docBlocks = doc.getBlocks();
        int indexLine = 0;
        int blockIndex = 0;
        int p = 0; // position in the labeled result
        int currentLineEndPos = 0; // position in the global doc. tokenization of the last
        // token of the current line
        int currentLineStartPos = 0; // position in the global doc.
        // tokenization of the first token of the current line
        String line = null;

        //DocumentPointer pointerA = DocumentPointer.START_DOCUMENT_POINTER;
        // the default first block might not contain tokens but only bitmap - in this case we move
        // to the first block containing some LayoutToken objects
        while (docBlocks.get(blockIndex).getTokens() == null
            //TODO: make things right
//                || docBlocks.get(blockIndex).getStartToken() == -1
                ) {
            blockIndex++;
        }
        DocumentPointer pointerA = new DocumentPointer(doc, blockIndex, docBlocks.get(blockIndex).getStartToken());

        DocumentPointer currentPointer = null;
        DocumentPointer lastPointer = null;

        String curLabel;
        String curPlainLabel = null;
        String lastPlainLabel = null;

        int lastTokenInd = -1;
        for (int i = docBlocks.size() - 1; i >= 0; i--) {
            int endToken = docBlocks.get(i).getEndToken();
            if (endToken != -1) {
                lastTokenInd = endToken;
                break;
            }
        }

        // we do this concatenation trick so that we don't have to process stuff after the main loop
        // no copying of lists happens because of this, so it's ok to concatenate
        String ignoredLabel = "@IGNORED_LABEL@";
        for (Pair<String, String> labeledTokenPair :
                Iterables.concat(labeledTokens,
                        Collections.singleton(Pair.of("IgnoredToken", ignoredLabel)))) {
            if (labeledTokenPair == null) {
                p++;
                continue;
            }

            // as we process the document segmentation line by line, we don't use the usual
            // tokenization to rebuild the text flow, but we get each line again from the
            // text stored in the document blocks (similarly as when generating the features)
            line = null;
            while ((line == null) && (blockIndex < docBlocks.size())) {
                Block block = docBlocks.get(blockIndex);
                List<LayoutToken> tokens = block.getTokens();
                String localText = block.getText();
                if ((tokens == null) || (localText == null) || (localText.trim().length() == 0)) {
                    blockIndex++;
                    indexLine = 0;
                    if (blockIndex < docBlocks.size()) {
                        block = docBlocks.get(blockIndex);
                        currentLineStartPos = block.getStartToken();
                    }
                    continue;
                }
                String[] lines = localText.split("[\\n\\r]");
                if ((lines.length == 0) || (indexLine >= lines.length)) {
                    blockIndex++;
                    indexLine = 0;
                    if (blockIndex < docBlocks.size()) {
                        block = docBlocks.get(blockIndex);
                        currentLineStartPos = block.getStartToken();
                    }
                    continue;
                } else {
                    line = lines[indexLine];
                    indexLine++;
                    if ((line.trim().length() == 0) || (TextUtilities.filterLine(line))) {
                        line = null;
                        continue;
                    }

                    if (currentLineStartPos > lastTokenInd)
                        continue;

                    // adjust the start token position in documentTokens to this non trivial line
                    // first skip possible space characters and tabs at the beginning of the line
                    while ((documentTokens.get(currentLineStartPos).t().equals(" ") ||
                            documentTokens.get(currentLineStartPos).t().equals("\t"))
                            && (currentLineStartPos != lastTokenInd)) {
                        currentLineStartPos++;
                    }
                    if (!labeledTokenPair.getLeft().startsWith(documentTokens.get(currentLineStartPos).getText())) {
                        while (currentLineStartPos < block.getEndToken()) {
                            if (documentTokens.get(currentLineStartPos).t().equals("\n")
                                    || documentTokens.get(currentLineStartPos).t().equals("\r")) {
                                // move to the start of the next line, but ignore space characters and tabs
                                currentLineStartPos++;
                                while ((documentTokens.get(currentLineStartPos).t().equals(" ") ||
                                        documentTokens.get(currentLineStartPos).t().equals("\t"))
                                        && (currentLineStartPos != lastTokenInd)) {
                                    currentLineStartPos++;
                                }
                                if ((currentLineStartPos != lastTokenInd) &&
                                        labeledTokenPair.getLeft().startsWith(documentTokens.get(currentLineStartPos).getText())) {
                                    break;
                                }
                            }
                            currentLineStartPos++;
                        }
                    }

                    // what is then the position of the last token of this line?
                    currentLineEndPos = currentLineStartPos;
                    while (currentLineEndPos < block.getEndToken()) {
                        if (documentTokens.get(currentLineEndPos).t().equals("\n")
                                || documentTokens.get(currentLineEndPos).t().equals("\r")) {
                            currentLineEndPos--;
                            break;
                        }
                        currentLineEndPos++;
                    }
                }
            }
            curLabel = labeledTokenPair.getRight();
            curPlainLabel = GenericTaggerUtils.getPlainLabel(curLabel);


            if (blockIndex == docBlocks.size()) {
                break;
            }

            currentPointer = new DocumentPointer(doc, blockIndex, currentLineEndPos);

            // either a new entity starts or a new beginning of the same type of entity
            if ((!curPlainLabel.equals(lastPlainLabel)) && (lastPlainLabel != null)) {
                if ((pointerA.getTokenDocPos() <= lastPointer.getTokenDocPos()) &&
                        (pointerA.getTokenDocPos() != -1)) {
                    labeledBlocks.put(lastPlainLabel, new DocumentPiece(pointerA, lastPointer));
                }
                pointerA = new DocumentPointer(doc, blockIndex, currentLineStartPos);
                //System.out.println("add segment for: " + lastPlainLabel + ", until " + (currentLineStartPos-2));
            }

            //updating stuff for next iteration
            lastPlainLabel = curPlainLabel;
            lastPointer = currentPointer;
            currentLineStartPos = currentLineEndPos + 2; // one shift for the EOL, one for the next line
            p++;
        }

        if (blockIndex == docBlocks.size()) {
            // the last labelled piece has still to be added
            if ((!curPlainLabel.equals(lastPlainLabel)) && (lastPlainLabel != null)) {
                if ((pointerA.getTokenDocPos() <= lastPointer.getTokenDocPos()) &&
                        (pointerA.getTokenDocPos() != -1)) {
                    labeledBlocks.put(lastPlainLabel, new DocumentPiece(pointerA, lastPointer));
                    //System.out.println("add segment for: " + lastPlainLabel + ", until " + (currentLineStartPos-2));
                }
            }
        }

        return doc;
    }

    public String processToTEI(File originFile) {
        // GrobidConfig needs to be always initiated before calling initiateProcessing()
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().generateTeiIds(true).build();
        DictionaryDocument doc = initiateProcessing(originFile, config);

        LayoutTokenization tokens = new LayoutTokenization(doc.getTokenizations());
        String segmentedDictionary = null;

        String featSeg = getAllLinesFeatured(doc);
        String labeledFeatures = null;

        // if featSeg is null, it usually means that no body segment is found in the

        if ((featSeg != null) && (featSeg.trim().length() > 0)) {
            labeledFeatures = label(featSeg);
            segmentedDictionary = toTEIFormatDictionarySegmentation(config, null, labeledFeatures, doc).toString();
        }

        return segmentedDictionary;
    }

    public DictionaryDocument initiateProcessing(File originFile, GrobidAnalysisConfig config) {
        // This method is to be called by any parser to perform first level segmentation: Headnote, Body and Footnote
        DocumentSource documentSource = DocumentSource.fromPdf(originFile, config.getStartPage(), config.getEndPage());
        try {
            Document document = new Document(documentSource);

            //Transform the doc to a dictionary doc
            DictionaryDocument doc = new DictionaryDocument(document);
            doc.addTokenizedDocument(config);
            doc.produceStatistics();
            doc = prepareDocument(doc);

            // if assets is true, the images are still there under directory pathXML+"_data"
            // we copy them to the assetPath directory

            File assetFile = config.getPdfAssetPath();
            if (assetFile != null) {
                dealWithImages(documentSource, doc, assetFile, config);
            }
            return doc;
        } finally {
            // keep it clean when leaving...
            if (config.getPdfAssetPath() == null) {
                // remove the pdf2xml tmp file
                DocumentSource.close(documentSource, false, true,true);
            } else {
                // remove the pdf2xml tmp files, including the sub-directories
                DocumentSource.close(documentSource, true, true,true);
            }
        }
    }

    private void dealWithImages(DocumentSource documentSource, Document doc, File assetFile, GrobidAnalysisConfig config) {
        if (assetFile != null) {
            // copy the files under the directory pathXML+"_data"
            // we copy the asset files into the path specified by assetPath

            if (!assetFile.exists()) {
                // we create it
                if (assetFile.mkdir()) {
                    LOGGER.debug("Directory created: " + assetFile.getPath());
                } else {
                    LOGGER.error("Failed to create directory: " + assetFile.getPath());
                }
            }
            PNMRegistry.registerAllServicesProviders();

            // filter all .jpg and .png files
            File directoryPath = new File(documentSource.getXmlFile().getAbsolutePath() + "_data");
            if (directoryPath.exists()) {
                File[] files = directoryPath.listFiles();
                if (files != null) {
                    for (final File currFile : files) {
                        String toLowerCaseName = currFile.getName().toLowerCase();
                        if (toLowerCaseName.endsWith(".png") || !config.isPreprocessImages()) {
                            try {
                                if (toLowerCaseName.endsWith(".vec")) {
                                    continue;
                                }
                                FileUtils.copyFileToDirectory(currFile, assetFile);
                            } catch (IOException e) {
                                LOGGER.error("Cannot copy file " + currFile.getAbsolutePath() + " to " + assetFile.getAbsolutePath(), e);
                            }
                        } else if (toLowerCaseName.endsWith(".jpg")
                                || toLowerCaseName.endsWith(".ppm")
                            //	|| currFile.getName().toLowerCase().endsWith(".pbm")
                                ) {

                            String outputFilePath = "";
                            try {
                                final BufferedImage bi = ImageIO.read(currFile);

                                if (toLowerCaseName.endsWith(".jpg")) {
                                    outputFilePath = assetFile.getPath() + File.separator +
                                            toLowerCaseName.replace(".jpg", ".png");
                                }
                                /*else if (currFile.getName().toLowerCase().endsWith(".pbm")) {
                                    outputFilePath = assetFile.getPath() + File.separator +
                                         currFile.getName().toLowerCase().replace(".pbm",".png");
                                }*/
                                else {
                                    outputFilePath = assetFile.getPath() + File.separator +
                                            toLowerCaseName.replace(".ppm", ".png");
                                }
                                ImageIO.write(bi, "png", new File(outputFilePath));
                            } catch (IOException e) {
                                LOGGER.error("Cannot convert file " + currFile.getAbsolutePath() + " to " + outputFilePath, e);
                            }
                        }
                    }
                }
            }
            // update the path of the image description stored in Document

            if (config.isPreprocessImages()) {
                List<GraphicObject> images = doc.getImages();
                if (images != null) {
                    String subPath = assetFile.getPath();
                    int ind = subPath.lastIndexOf("/");
                    if (ind != -1)
                        subPath = subPath.substring(ind + 1, subPath.length());
                    for (GraphicObject image : images) {
                        String fileImage = image.getFilePath();
                        if (fileImage == null) {
                            continue;
                        }
                        fileImage = fileImage.replace(".ppm", ".png")
                                .replace(".jpg", ".png");
                        ind = fileImage.indexOf("/");
                        image.setFilePath(subPath + fileImage.substring(ind, fileImage.length()));
                    }
                }
            }
        }
    }

    public DictionaryDocument prepareDocument(DictionaryDocument doc) {

        //This method is used to tokenize and set the diffrent sections of the document (labelled blocks)
        List<LayoutToken> tokenizations = doc.getTokenizations();
//        if (tokenizations.size() > GrobidProperties.getPdfTokensMax()) {
//            throw new GrobidException("The document has " + tokenizations.size() + " tokens, but the limit is " + GrobidProperties.getPdfTokensMax(),
//                    GrobidExceptionStatus.TOO_MANY_TOKENS);
//        }

        doc.produceStatistics();
        String content = getAllLinesFeatured(doc);
        if (isNotEmpty(trim(content))) {
            String labelledResult = label(content);
            // set the different sections of the Document object
            // the method is originally implemented in BasicStructureBuilder class but it is
            // reimplemented here to be able to use DictionaryDocument object
            doc = generalResultSegmentation(doc, labelledResult, tokenizations);


        }
        return doc;
    }

    public void optimise(DictionaryDocument doc, String dictionaryPartLabel) {
        //Optimise Headnotes
        List<DocumentPiece> aDocumentPartOfAllPages = new ArrayList<>(doc.getDocumentDictionaryPart(dictionaryPartLabel));
        if (aDocumentPartOfAllPages.size() > 0) {
            LabeledLexicalInformation headnotesOptimised = new LabeledLexicalInformation();
            List<LayoutToken> currentHeadnote = new ArrayList<>(doc.getDocumentPieceTokenization(aDocumentPartOfAllPages.get(0)));
            int previousHeadnotePageNumber = 0;
            currentHeadnote.get(currentHeadnote.size() - 1).getPage();
            int currentHeadnotePageNumber = 0;
            int i = 1;
            //
            while (i < aDocumentPartOfAllPages.size() - 1) {
                previousHeadnotePageNumber = currentHeadnote.get(0).getPage();
                List<DocumentPiece> restOfHeads = aDocumentPartOfAllPages.subList(i, aDocumentPartOfAllPages.size() - 1);
                for (int j = 0; j < restOfHeads.size(); j++) {
                    List<LayoutToken> aHeadnote = new ArrayList<>(doc.getDocumentPieceTokenization(restOfHeads.get(j)));
                    currentHeadnotePageNumber = aHeadnote.get(aHeadnote.size() - 1).getPage();

                    if (currentHeadnotePageNumber > previousHeadnotePageNumber) {

                        headnotesOptimised.addLabel(Pair.of(currentHeadnote, dictionaryPartLabel));

                        currentHeadnote = doc.getDocumentPieceTokenization(aDocumentPartOfAllPages.get(i));
                        i++;
                        break;
                    } else {
                        List<LayoutToken> sameHeadnote = doc.getDocumentPieceTokenization(restOfHeads.get(j));
                        currentHeadnote.addAll(sameHeadnote);

                    }

                    i++;
                }


            }
            List<LayoutToken> lastHeadnote = new ArrayList<>(doc.getDocumentPieceTokenization(aDocumentPartOfAllPages.get(aDocumentPartOfAllPages.size() - 1)));
            // Careful with page number of first tokens which could have the same number as the previous page. So take the last one
            currentHeadnotePageNumber = lastHeadnote.get(lastHeadnote.size() - 1).getPage();
            if (currentHeadnotePageNumber == previousHeadnotePageNumber) {

                List<LayoutToken> newHeadnote = lastHeadnote;
                currentHeadnote.addAll(newHeadnote);

            }
            if (aDocumentPartOfAllPages.size() !=1 ){
                headnotesOptimised.addLabel(Pair.of(currentHeadnote, dictionaryPartLabel));
            }

            headnotesOptimised.addLabel(Pair.of(lastHeadnote, dictionaryPartLabel));
            doc.setDictionaryPagePartOptimised(headnotesOptimised,dictionaryPartLabel);

        }

    }

    /**
     * Addition of the features at line level for the complete document.
     * <p/>
     * This is an alternative to the token level, where the unit for labeling is the line - so allowing faster
     * processing and involving less features.
     * Lexical features becomes line prefix and suffix, the feature text unit is the first 10 characters of the
     * line without space.
     * The dictionary flags are at line level (i.e. the line contains a name mention, a place mention, a year, etc.)
     * Regarding layout features: font, size and style are the one associated to the first token of the line.
     */
    public String getAllLinesFeatured(Document doc) {

        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }

        //guaranteeing quality of service. Otherwise, there are some PDF that may contain 300k blocks and thousands of extracted "images" that ruins the performance
        if (blocks.size() > GrobidProperties.getPdfBlocksMax()) {
            throw new GrobidException("Postprocessed document is too big, contains: " + blocks.size(), GrobidExceptionStatus.TOO_MANY_BLOCKS);
        }

        boolean graphicVector = false;
        boolean graphicBitmap = false;

        // list of textual patterns at the head and foot of pages which can be re-occur on several pages
        // (typically indicating a publisher foot or head notes)
        Map<String, Integer> patterns = new TreeMap<String, Integer>();
        Map<String, Boolean> firstTimePattern = new TreeMap<String, Boolean>();

        for (Page page : doc.getPages()) {
            // we just look at the two first and last blocks of the page
            if ((page.getBlocks() != null) && (page.getBlocks().size() > 0)) {
                for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                    if ((blockIndex < 2) || (blockIndex > page.getBlocks().size() - 2)) {
                        Block block = page.getBlocks().get(blockIndex);
                        String localText = block.getText();
                        if ((localText != null) && (localText.length() > 0)) {
                            String[] lines = localText.split("[\\n\\r]");
                            if (lines.length > 0) {
                                String line = lines[0];
                                String pattern = featureFactory.getPattern(line);
                                if (pattern.length() > 8) {
                                    Integer nb = patterns.get(pattern);
                                    if (nb == null) {
                                        patterns.put(pattern, new Integer(1));
                                        firstTimePattern.put(pattern, false);
                                    } else
                                        patterns.put(pattern, new Integer(nb + 1));
                                }
                            }
                        }
                    }
                }
            }
        }

        String featuresAsString = getFeatureVectorsAsString(doc, graphicVector, graphicBitmap, patterns, firstTimePattern);

        return featuresAsString;
    }

    private String getFeatureVectorsAsString(Document doc, boolean graphicVector,
                                             boolean graphicBitmap, Map<String, Integer> patterns,
                                             Map<String, Boolean> firstTimePattern) {
        StringBuilder fulltext = new StringBuilder();
        int documentLength = doc.getDocumentLenghtChar();

        String currentFont = null;
        int currentFontSize = -1;

        boolean newPage;
        boolean start = true;
        int mm = 0; // page position
        int nn = 0; // document position
        int pageLength = 0; // length of the current page
        double pageHeight = 0.0;

        // vector for features
        FeaturesVectorSegmentation features;
        FeaturesVectorSegmentation previousFeatures = null;

        for (Page page : doc.getPages()) {
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

            for (int blockIndex = 0; blockIndex < page.getBlocks().size(); blockIndex++) {
                Block block = page.getBlocks().get(blockIndex);
                /*if (start) {
                    newPage = true;
                    start = false;
                }*/
                boolean lastPageBlock = false;
                boolean firstPageBlock = false;
                if (blockIndex == page.getBlocks().size() - 1) {
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
                    for (GraphicObject localImage : localImages) {
                        if (localImage.getType() == GraphicObjectType.BITMAP)
                            graphicVector = true;
                        if (localImage.getType() == GraphicObjectType.VECTOR)
                            graphicBitmap = true;
                    }
                }

                if (lowestPos > block.getY()) {
                    // we have a vertical shift, which can be due to a change of column or other particular layout formatting
                    spacingPreviousBlock = doc.getMaxBlockSpacing() / 5.0; // default
                } else
                    spacingPreviousBlock = block.getY() - lowestPos;

                String localText = block.getText();
                if (localText == null)
                    continue;

                // character density of the block
                double density = 0.0;
                if ((block.getHeight() != 0.0) && (block.getWidth() != 0.0) &&
                        (block.getText() != null) && (!block.getText().contains("@PAGE")) &&
                        (!block.getText().contains("@IMAGE")))
                    density = (double) block.getText().length() / (block.getHeight() * block.getWidth());

                // is the current block in the main area of the page or not?
                boolean inPageMainArea = true;
                BoundingBox blockBoundingBox = BoundingBox.fromPointAndDimensions(page.getNumber(),
                        block.getX(), block.getY(), block.getWidth(), block.getHeight());
                if (pageBoundingBox == null || (!pageBoundingBox.contains(blockBoundingBox) && !pageBoundingBox.intersect(blockBoundingBox)))
                    inPageMainArea = false;

                String[] lines = localText.split("[\\n\\r]");
                // set the max length of the lines in the block, in number of characters
                int maxLineLength = 0;
                for (int p = 0; p < lines.length; p++) {
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

                    if ((blockIndex < 2) || (blockIndex > page.getBlocks().size() - 2)) {
                        String pattern = featureFactory.getPattern(line);
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

                    Matcher m2 = featureFactory.year.matcher(text);
                    if (m2.find()) {
                        features.year = true;
                    }

                    Matcher m3 = featureFactory.email.matcher(text);
                    if (m3.find()) {
                        features.email = true;
                    }

                    Matcher m4 = featureFactory.http.matcher(text);
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
                                .linearScaling(spacingPreviousBlock - doc.getMinBlockSpacing(), doc.getMaxBlockSpacing() - doc.getMinBlockSpacing(), NBBINS_SPACE);
                    }

                    features.inMainArea = inPageMainArea;

                    if (density != -1.0) {
                        features.characterDensity = featureFactory
                                .linearScaling(density - doc.getMinCharacterDensity(), doc.getMaxCharacterDensity() - doc.getMinCharacterDensity(), NBBINS_DENSITY);
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

            // we process all pdf files in the directory

            int n = 0;

            if (path.isDirectory()) {
                for (File fileEntry : path.listFiles()) {

                    // Create the pre-annotated file and the raw text
                    createTrainingDictionary(fileEntry, outputDirectory, false);
                    n++;
                }

            } else {

                createTrainingDictionary(path, outputDirectory, false);
                n++;

            }

            System.out.println(n + " files to be processed.");

            return n;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid training" +
                    " data generation for segmentation model.", e);
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    public int createAnnotatedTrainingBatch(String inputDirectory, String outputDirectory) throws IOException {
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

            // we process all pdf files in the directory

            int n = 0;

            if (path.isDirectory()) {
                for (File fileEntry : path.listFiles()) {

                    // Create the pre-annotated file and the raw text
                    createTrainingDictionary(fileEntry, outputDirectory, true);
                    n++;
                }

            } else {

                createTrainingDictionary(path, outputDirectory, true);
                n++;

            }

            System.out.println(n + " files to be processed.");

            return n;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid training" +
                    " data generation for segmentation model.", e);
        }
    }

    public void createTrainingDictionary(File path, String outputDirectory, Boolean isAnnotated) throws Exception {

        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().generateTeiIds(true).build();
        DictionaryDocument doc = initiateProcessing(path, config);
        if (doc.getBlocks() == null) {
            throw new Exception("PDF parsing resulted in empty content");
        }
        List<LayoutToken> tokenizations = doc.getTokenizations();

        //Write the features file
        String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionarySegmentation";
        Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
        String featuredText = getAllLinesFeatured(doc);
        writer.write(featuredText);
        IOUtils.closeQuietly(writer);

        //Create rng and css files for guiding the annotation
        File existingRngFile = new File("templates/dictionarySegmentation.rng");
        File newRngFile = new File(outputDirectory + "/" + "dictionarySegmentation.rng");
        copyFileUsingStream(existingRngFile, newRngFile);

        File existingCssFile = new File("templates/dictionarySegmentation.css");
        File newCssFile = new File(outputDirectory + "/" + "dictionarySegmentation.css");
//        Files.copy(Gui.getClass().getResourceAsStream("templates/lexicalEntry.css"), Paths.get("new_project","css","lexicalEntry.css"))
        copyFileUsingStream(existingCssFile, newCssFile);

        // also write the raw text as seen before segmentation

        StringBuffer rawtxt = new StringBuffer();
        for (LayoutToken txtline : tokenizations) {
            rawtxt.append(txtline.getText());
        }
        String outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionarySegmentation.rawtxt";
        FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");

        StringBuffer bufferFulltext = new StringBuffer();


        if (isAnnotated) {
            if ((featuredText != null) && (featuredText.length() > 0)) {
                String rese = label(featuredText);
                bufferFulltext.append(trainingExtraction(rese, doc));
            }

        } else {
            bufferFulltext.append(DocumentUtils.replaceLinebreaksWithTags(DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(tokenizations))));
        }
        //Using the existing model of the parser to generate a pre-annotate tei file to be corrected


        // write the TEI file to reflect the exact layout of the text as extracted from the pdf
        String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionarySegmentation.tei.xml";
        writer = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
        writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<?xml-model href=\"dictionarySegmentation.rng\" type=\"application/xml\" schematypens=\"http://relaxng.org/ns/structure/1.0\"\n" +
                "?>\n" + "<?xml-stylesheet type=\"text/css\" href=\"dictionarySegmentation.css\"?>\n" +
                "<tei xml:space=\"preserve\">\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                "\"/>\n\t</teiHeader>\n\t<text>");

        writer.write(bufferFulltext.toString().replaceAll("&", "&amp;"));
        writer.write("\n\t</text>\n</tei>\n");
        writer.close();

    }

    /**
     * Extract results from a labelled full text in the training format without any string modification.
     *
     * @param result reult
     * @return extraction
     */
    private StringBuffer trainingExtraction(String result,
                                            Document doc) {
        // this is the main buffer for the whole full text
        StringBuffer buffer = new StringBuffer();
        try {
            List<Block> blocks = doc.getBlocks();
            int currentBlockIndex = 0;
            int indexLine = 0;

            StringTokenizer st = new StringTokenizer(result, "\n");
            String s1 = null; // current label/tag
            String s2 = null; // current lexical token
            String s3 = null; // current second lexical token
            String lastTag = null;

            // current token position
            int p = 0;
            boolean start = true;

            while (st.hasMoreTokens()) {
                boolean addSpace = false;
                String tok = st.nextToken().trim();
                String line = null; // current line

                if (tok.length() == 0) {
                    continue;
                }
                StringTokenizer stt = new StringTokenizer(tok, " \t");
                List<String> localFeatures = new ArrayList<String>();
                int i = 0;

                boolean newLine = true;
                int ll = stt.countTokens();
                while (stt.hasMoreTokens()) {
                    String s = stt.nextToken().trim();
                    if (i == 0) {
                        //     s2 = TextUtilities.HTMLEncode(s); // lexical token
                    } else if (i == 1) {
                        //     s3 = TextUtilities.HTMLEncode(s); // second lexical token
                    } else if (i == ll - 1) {
                        s1 = s; // current label
                    } else {
                        localFeatures.add(s); // we keep the feature values in case they appear useful
                    }
                    i++;
                }

                // as we process the document segmentation line by line, we don't use the usual
                // tokenization to rebuild the text flow, but we get each line again from the
                // text stored in the document blocks (similarly as when generating the features)
                line = null;
                while ((line == null) && (currentBlockIndex < blocks.size())) {
                    Block block = blocks.get(currentBlockIndex);
                    List<LayoutToken> tokens = block.getTokens();
                    if (tokens == null) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    }
                    String localText = block.getText();
                    if ((localText == null) || (localText.trim().length() == 0)) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    }
                    //String[] lines = localText.split("\n");
                    String[] lines = localText.split("[\\n\\r]");
                    if ((lines.length == 0) || (indexLine >= lines.length)) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    } else {
                        line = lines[indexLine];
                        indexLine++;
                        if (line.trim().length() == 0) {
                            line = null;
                            continue;
                        }

                        if (TextUtilities.filterLine(line)) {
                            line = null;
                            continue;
                        }
                    }
                }

                line = DocumentUtils.escapeHTMLCharac(line);

                if (newLine && !start) {
                    buffer.append("<lb/>");
                }

                String lastTag0 = null;
                if (lastTag != null) {
                    if (lastTag.startsWith("I-")) {
                        lastTag0 = lastTag.substring(2, lastTag.length());
                    } else {
                        lastTag0 = lastTag;
                    }
                }
                String currentTag0 = null;
                if (s1 != null) {
                    if (s1.startsWith("I-")) {
                        currentTag0 = s1.substring(2, s1.length());
                    } else {
                        currentTag0 = s1;
                    }
                }

                //boolean closeParagraph = false;
                if (lastTag != null) {
                    //closeParagraph =
                    testClosingTagForTrainingData(buffer, currentTag0, lastTag0, s1);
                }

                boolean output;

                output = writeFieldForTrainingData(buffer, line, s1, lastTag0, s2, "<headnote>", "<headnote>", addSpace, 3);
                if (!output) {
                    output = writeFieldForTrainingData(buffer, line, s1, lastTag0, s2, "<body>", "<body>", addSpace, 3);
                }

                if (!output) {
                    output = writeFieldForTrainingData(buffer, line, s1, lastTag0, s2, "<footnote>", "<footnote>", addSpace, 3);
                }
                if (!output) {
                    output = writeFieldForTrainingData(buffer, line, s1, lastTag0, s2, "<pc>", "<pc>", addSpace, 3);
                }
                if (!output) {
                    output = writeFieldForTrainingData(buffer, line, s1, lastTag0, s2, "<dictScrap>", "<dictScrap>", addSpace, 3);
                }

                lastTag = s1;

                if (!st.hasMoreTokens()) {
                    if (lastTag != null) {
                        buffer = new StringBuffer(LayoutTokensUtil.normalizeText(buffer.toString()));
                        testClosingTagForTrainingData(buffer, "", currentTag0, s1);
                    }
                }
                if (start) {
                    start = false;
                }
            }

            return buffer;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    /**
     * Extract results from a labelled full text in the training format with string modification.
     *
     * @param result result
     * @param doc    doc
     * @return extraction
     */
    private StringBuffer outputTextExtraction(String result,
                                              Document doc) {
        // this is the main buffer for the whole full text
        StringBuffer buffer = new StringBuffer();
        try {
            List<Block> blocks = doc.getBlocks();
            int currentBlockIndex = 0;
            int indexLine = 0;

            StringTokenizer st = new StringTokenizer(result, "\n");
            String s1 = null; // current label/tag
            String s2 = null; // current lexical token
            String s3 = null; // current second lexical token
            String lastTag = null;

            // current token position
            int p = 0;
            boolean start = true;

            while (st.hasMoreTokens()) {
                boolean addSpace = false;
                String tok = st.nextToken().trim();
                String line = null; // current line

                if (tok.length() == 0) {
                    continue;
                }
                StringTokenizer stt = new StringTokenizer(tok, " \t");
                List<String> localFeatures = new ArrayList<String>();
                int i = 0;

                boolean newLine = true;
                int ll = stt.countTokens();
                while (stt.hasMoreTokens()) {
                    String s = stt.nextToken().trim();
                    if (i == 0) {
//                        s2 = TextUtilities.HTMLEncode(s); // lexical token
                    } else if (i == 1) {
//                        s3 = TextUtilities.HTMLEncode(s); // second lexical token
                    } else if (i == ll - 1) {
                        s1 = s; // current label
                    } else {
                        localFeatures.add(s); // we keep the feature values in case they appear useful
                    }
                    i++;
                }

                // as we process the document segmentation line by line, we don't use the usual
                // tokenization to rebuild the text flow, but we get each line again from the
                // text stored in the document blocks (similarly as when generating the features)
                line = null;
                while ((line == null) && (currentBlockIndex < blocks.size())) {
                    Block block = blocks.get(currentBlockIndex);
                    List<LayoutToken> tokens = block.getTokens();
                    if (tokens == null) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    }
                    String localText = block.getText();
                    if ((localText == null) || (localText.trim().length() == 0)) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    }
                    //String[] lines = localText.split("\n");
                    String[] lines = localText.split("[\\n\\r]");
                    if ((lines.length == 0) || (indexLine >= lines.length)) {
                        currentBlockIndex++;
                        indexLine = 0;
                        continue;
                    } else {
                        line = lines[indexLine];
                        indexLine++;
                        if (line.trim().length() == 0) {
                            line = null;
                            continue;
                        }

                        if (TextUtilities.filterLine(line)) {
                            line = null;
                            continue;
                        }
                    }
                }

                line = DocumentUtils.escapeHTMLCharac(line);

                String lastTag0 = null;
                if (lastTag != null) {
                    if (lastTag.startsWith("I-")) {
                        lastTag0 = lastTag.substring(2, lastTag.length());
                    } else {
                        lastTag0 = lastTag;
                    }
                }
                String currentTag0 = null;
                if (s1 != null) {
                    if (s1.startsWith("I-")) {
                        currentTag0 = s1.substring(2, s1.length());
                    } else {
                        currentTag0 = s1;
                    }
                }

                //boolean closeParagraph = false;
                if (lastTag != null) {

                    testClosingTag(buffer, currentTag0, lastTag0, s1);
                }

                boolean output;


                output = writeField(buffer, line, s1, lastTag0, s2, "<headnote>", "<fw>", addSpace, 3);
                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<body>", "<ab>", addSpace, 3);
                }

                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<footnote>", "<fw>", addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<pc>", "<pc>", addSpace, 3);
                }
                if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<dictScrap>", "<dictScrap>", addSpace, 3);
                }

                lastTag = s1;

                if (!st.hasMoreTokens()) {
                    if (lastTag != null) {
                        buffer = new StringBuffer(LayoutTokensUtil.normalizeText(buffer.toString()));
                        testClosingTag(buffer, "", currentTag0, s1);
                    }
                }
                if (start) {
                    start = false;
                }
            }

            return buffer;
        } catch (Exception e) {
            throw new GrobidException("An exception occured while running Grobid.", e);
        }
    }

    /**
     * TODO some documentation...
     *
     * @param buffer
     * @param s1
     * @param lastTag0
     * @param s2
     * @param field
     * @param outField
     * @param addSpace
     * @param nbIndent
     * @return
     */
    private boolean writeField(StringBuffer buffer,
                               String line,
                               String s1,
                               String lastTag0,
                               String s2,
                               String field,
                               String outField,
                               boolean addSpace,
                               int nbIndent) {
        boolean result = false;
        // filter the output path
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
            line = line.replace("@BULLET", "\u2022");
            // if previous and current tag are the same, we output the token
            if (s1.equals(lastTag0) || s1.equals("I-" + lastTag0)) {

                buffer.append(line);
            } else if (field.equals("<headnote>")) {
                outField = outField.substring(0, outField.length() - 1) + " type=\"header\">";
                buffer.append(outField).append(line);
            } else if (field.equals("<footnote>")) {
                outField = outField.substring(0, outField.length() - 1) + " type=\"footer\">";
                buffer.append(outField).append(line);
            } else if (field.equals("<body>")) {
                outField = outField.substring(0, outField.length() - 1) + ">";
                buffer.append(outField).append(line);
            } else if (lastTag0 == null) {
                buffer.append(outField).append(line);
            } else if (!lastTag0.equals("<titlePage>")) {
                buffer.append(outField).append(line);
            } else {
                // otherwise we continue by ouputting the token
                buffer.append(line);
            }
        }
        return result;
    }

    private boolean writeFieldForTrainingData(StringBuffer buffer,
                                              String line,
                                              String s1,
                                              String lastTag0,
                                              String s2,
                                              String field,
                                              String outField,
                                              boolean addSpace,
                                              int nbIndent) {
        boolean result = false;
        // filter the output path
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
            line = line.replace("@BULLET", "\u2022");
            // if previous and current tag are the same, we output the token
            if (s1.equals(lastTag0) || s1.equals("I-" + lastTag0)) {

                buffer.append(line);
            } else if (lastTag0 == null) {
                buffer.append(outField).append(line);
            } else if (!lastTag0.equals("<titlePage>")) {
                buffer.append(outField).append(line);
            } else {
                // otherwise we continue by ouputting the token
                buffer.append(line);
            }
        }
        return result;
    }

    /**
     * TODO some documentation
     *
     * @param buffer
     * @param currentTag0
     * @param lastTag0
     * @param currentTag
     * @return
     */
    private boolean testClosingTag(StringBuffer buffer,
                                   String currentTag0,
                                   String lastTag0,
                                   String currentTag) {
        boolean res = false;
        // reference_marker and citation_marker are two exceptions because they can be embedded

        if (!currentTag0.equals(lastTag0)) {
            /*if (currentTag0.equals("<citation_marker>") || currentTag0.equals("<figure_marker>")) {
                return res;
            }*/

            res = false;
            // we close the current tag
            if (lastTag0.equals("<headnote>")) {
                buffer.append("</fw>");
            } else if (lastTag0.equals("<body>")) {
                buffer.append("</ab>");
            } else if (lastTag0.equals("<footnote>")) {
                buffer.append("</fw>");
            } else if (lastTag0.equals("<dictScrap>")) {
                buffer.append("</dictScrap>");
            } else if (lastTag0.equals("<pc>")) {
                buffer.append("</pc>");
            } else {
                res = false;
            }

        }
        return res;
    }

    private boolean testClosingTagForTrainingData(StringBuffer buffer,
                                                  String currentTag0,
                                                  String lastTag0,
                                                  String currentTag) {
        boolean res = false;
        // reference_marker and citation_marker are two exceptions because they can be embedded

        if (!currentTag0.equals(lastTag0)) {
            /*if (currentTag0.equals("<citation_marker>") || currentTag0.equals("<figure_marker>")) {
                return res;
            }*/

            res = false;
            // we close the current tag
            if (lastTag0.equals("<headnote>")) {
                buffer.append("</headnote>");
            } else if (lastTag0.equals("<body>")) {
                buffer.append("</body>");
            } else if (lastTag0.equals("<footnote>")) {
                buffer.append("</footnote>");
            } else if (lastTag0.equals("<dictScrap>")) {
                buffer.append("</dictScrap>");
            } else if (lastTag0.equals("<pc>")) {
                buffer.append("</pc>");
            } else {
                res = false;
            }

        }
        return res;
    }

    @Override
    public void close() throws IOException {
        super.close();
        // ...
    }

    public StringBuilder toTEIFormatDictionarySegmentation(GrobidAnalysisConfig config,
                                                           TEIDictionaryFormatter.SchemaDeclaration schemaDeclaration,
                                                           String segmentedDictionary, DictionaryDocument doc) {
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
        tei.append(outputTextExtraction(segmentedDictionary, doc));
        tei.append("\n\t\t</body>");
        tei.append("\n\t</text>\n</TEI>\n");

        return tei;
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

}
