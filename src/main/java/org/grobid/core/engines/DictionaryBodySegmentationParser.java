package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.util.IOUtils;
import org.grobid.core.data.Figure;
import org.grobid.core.data.Table;
import org.grobid.core.document.*;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.DictionaryBodySegmentationLabels;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorFulltext;
import org.grobid.core.layout.*;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

/**
 * Created by med on 02.08.16.
 */
public class DictionaryBodySegmentationParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionarySegmentationParser.class);
    // default bins for relative position
    private static final int NBBINS_POSITION = 12;
    // default bins for inter-block spacing
    private static final int NBBINS_SPACE = 5;
    // default bins for block character density
    private static final int NBBINS_DENSITY = 5;
    // projection scale for line length
    private static final int LINESCALE = 10;
    private static volatile DictionaryBodySegmentationParser instance;
    private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();
    private FeatureFactory featureFactory = FeatureFactory.getInstance();

    public DictionaryBodySegmentationParser() {

        super(DictionaryModels.DICTIONARY_BODY_SEGMENTATION);
    }

    public static DictionaryBodySegmentationParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Create a new instance.
     */
    private static synchronized void getNewInstance() {
        instance = new DictionaryBodySegmentationParser();
    }

    public String process(File originFile) throws Exception {
//        //Prepare
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionaryDocument doc = null;

        try {
            doc= processing(originFile);
        }catch (GrobidException e) {
            throw e;
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }

        String segmentedBody = new TEIDictionaryFormatter(doc).toTEIFormatDictionaryBodySegmentation(config, null).toString();



        return segmentedBody;
    }



    public DictionaryDocument processing(File originFile) throws Exception {

        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionarySegmentationParser parser = new DictionarySegmentationParser();
        DictionaryDocument doc = parser.initiateProcessing(originFile, config);
        try {
            //Get Body
            SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

            // full text processing
            Pair<String, LayoutTokenization> featSeg = getBodyTextFeatured(doc, documentBodyParts);
            String rese = null;
            LayoutTokenization layoutTokenization = null;
            List<Figure> figures = null;
            List<Table> tables = null;
            String structeredBody = null;
            if (featSeg != null) {
                // if featSeg is null, it usually means that no body segment is found in the
                // document segmentation
                String bodytextFeatured = featSeg.getA();
                layoutTokenization = featSeg.getB();

                if ( (bodytextFeatured != null) && (bodytextFeatured.trim().length() > 0) ) {
                    rese = label(bodytextFeatured);
                }

                structeredBody = processLexicalEntries(layoutTokenization,rese);
                doc.setLexicalEntries(structeredBody);
            }

            return doc;
        } catch (GrobidException e) {
            throw e;
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
    }
    public static String processLexicalEntries (LayoutTokenization layoutTokenization, String contentFeatured){
        //Extract the lexical entries
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
            Engine.getCntManager().i((TaggingLabel)clusterLabel);

            // Problem with Grobid Normalisation
            List<LayoutToken> list1 = cluster.concatTokens();
            String clusterContent = LayoutTokensUtil.toText(list1);
//            String clusterContent = LayoutTokensUtil.normalizeText(str1);

            String tagLabel = clusterLabel.getLabel();


            if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                buffer.append(createMyXMLString("entry", clusterContent));
            }else if (tagLabel.equals(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL)){
                System.out.println("body:+"+ createMyXMLString("entry", clusterContent));
            }
            else {
                throw new IllegalArgumentException(tagLabel + " is not a valid possible tag");
            }

        }

        return buffer.toString();
    }
    public static String createMyXMLString(String elementName, String elementContent) {
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

    static public Pair<String, LayoutTokenization> getBodyTextFeatured(Document doc,
                                                                       SortedSet<DocumentPiece> documentBodyParts) {
        if ((documentBodyParts == null) || (documentBodyParts.size() == 0)) {
            return null;
        }
        FeatureFactory featureFactory = FeatureFactory.getInstance();
        StringBuilder fulltext = new StringBuilder();
        String currentFont = null;
        int currentFontSize = -1;

        List<Block> blocks = doc.getBlocks();
        if ((blocks == null) || blocks.size() == 0) {
            return null;
        }

        // vector for features
        FeaturesVectorFulltext features;
        FeaturesVectorFulltext previousFeatures = null;

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

        List<LayoutToken> layoutTokens = new ArrayList<LayoutToken>();
        fulltextLength = getFulltextLength(doc, documentBodyParts, fulltextLength);

        // System.out.println("fulltextLength: " + fulltextLength);

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

                    features = new FeaturesVectorFulltext();
                    features.token = token;

                    double coordinateLineY = token.getY();

                    String text = token.getText();
                    if ((text == null) || (text.length() == 0)) {
                        n++;
                        //mm++;
                        //nn++;
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
                        if ((token != null) && (previousFeatures != null)) {
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
//System.out.println(text + "\t" + token.getX() + "\t" + lineStartX + "\t" + indented);
                    features.string = text;

                    if (graphicBitmap) {
                        features.bitmapAround = true;
                    }
                    if (graphicVector) {
                        features.vectorAround = true;
                    }

                    if (newline) {
                        features.lineStatus = "LINESTART";
                        if (token != null)
                            lineStartX = token.getX();
                    }
                    Matcher m0 = featureFactory.isPunct.matcher(text);
                    if (m0.find()) {
                        features.punctType = "PUNCT";
                    }
                    if (text.equals("(") || text.equals("[")) {
                        features.punctType = "OPENBRACKET";

                    } else if (text.equals(")") || text.equals("]")) {
                        features.punctType = "ENDBRACKET";

                    } else if (text.equals(".")) {
                        features.punctType = "DOT";

                    } else if (text.equals(",")) {
                        features.punctType = "COMMA";

                    } else if (text.equals("-")) {
                        features.punctType = "HYPHEN";

                    } else if (text.equals("\"") || text.equals("\'") || text.equals("`")) {
                        features.punctType = "QUOTE";
                    }

                    if (indented) {
                        features.alignmentStatus = "LINEINDENT";
                    } else {
                        features.alignmentStatus = "ALIGNEDLEFT";
                    }

                    if (n == 0) {
                        features.lineStatus = "LINESTART";
                        if (token != null)
                            lineStartX = token.getX();
                        features.blockStatus = "BLOCKSTART";
                    } else if (n == tokens.size() - 1) {
                        features.lineStatus = "LINEEND";
                        previousNewline = true;
                        features.blockStatus = "BLOCKEND";
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
                                endblock = true;
                                endline = true;
                            }

                            ii++;
                        }

                        if ((!endline) && !(newline)) {
                            features.lineStatus = "LINEIN";
                        } else if (!newline) {
                            features.lineStatus = "LINEEND";
                            previousNewline = true;
                        }

                        if ((!endblock) && (features.blockStatus == null))
                            features.blockStatus = "BLOCKIN";
                        else if (features.blockStatus == null) {
                            features.blockStatus = "BLOCKEND";
                            //endblock = true;
                        }
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

	                /*if (featureFactory.test_common(text)) {
	                    features.commonName = true;
	                }

	                if (featureFactory.test_names(text)) {
	                    features.properName = true;
	                }

	                if (featureFactory.test_month(text)) {
	                    features.month = true;
	                }*/

                    Matcher m = featureFactory.isDigit.matcher(text);
                    if (m.find()) {
                        features.digit = "ALLDIGIT";
                    }

	                /*Matcher m2 = featureFactory.YEAR.matcher(text);
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
					*/
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

                    if (features.capitalisation == null)
                        features.capitalisation = "NOCAPS";

                    if (features.digit == null)
                        features.digit = "NODIGIT";

                    if (features.punctType == null)
                        features.punctType = "NOPUNCT";

                    features.relativeDocumentPosition = featureFactory
                            .linearScaling(nn, fulltextLength, NBBINS_POSITION);
                    // System.out.println(mm + " / " + pageLength);
                    features.relativePagePositionChar = featureFactory
                            .linearScaling(mm, pageLength, NBBINS_POSITION);

                    int pagePos = featureFactory
                            .linearScaling(coordinateLineY, pageHeight, NBBINS_POSITION);
                    if (pagePos > NBBINS_POSITION)
                        pagePos = NBBINS_POSITION;
                    features.relativePagePosition = pagePos;
//System.out.println((coordinateLineY) + " " + (pageHeight) + " " + NBBINS_POSITION + " " + pagePos);

                    if (spacingPreviousBlock != 0.0) {
                        features.spacingWithPreviousBlock = featureFactory
                                .linearScaling(spacingPreviousBlock - doc.getMinBlockSpacing(),
                                        doc.getMaxBlockSpacing() - doc.getMinBlockSpacing(), NBBINS_SPACE);
                    }

                    if (density != -1.0) {
                        features.characterDensity = featureFactory
                                .linearScaling(density - doc.getMinCharacterDensity(), doc.getMaxCharacterDensity() - doc.getMinCharacterDensity(), NBBINS_DENSITY);
//System.out.println((density-doc.getMinCharacterDensity()) + " " + (doc.getMaxCharacterDensity()-doc.getMinCharacterDensity()) + " " + NBBINS_DENSITY + " " + features.characterDensity);
                    }

                    // fulltext.append(features.printVector());
                    if (previousFeatures != null) {
                        if (features.blockStatus.equals("BLOCKSTART") &&
                                previousFeatures.blockStatus.equals("BLOCKIN")) {
                            // this is a post-correction due to the fact that the last character of a block
                            // can be a space or EOL character
                            previousFeatures.blockStatus = "BLOCKEND";
                            previousFeatures.lineStatus = "LINEEND";
                        }
                        fulltext.append(previousFeatures.printVector());
                    }
                    n++;
                    mm += text.length();
                    nn += text.length();
                    previousFeatures = features;
                }
                // lowest position of the block
                lowestPos = block.getY() + block.getHeight();

                //blockPos++;
            }
        }
        if (previousFeatures != null) {
            fulltext.append(previousFeatures.printVector());

        }

        return new Pair<String, LayoutTokenization>(fulltext.toString(),
                new LayoutTokenization(layoutTokens));
    }

    /**
     * Evaluate the length of the fulltext
     */
    private static int getFulltextLength(Document doc, SortedSet<DocumentPiece> documentBodyParts, int fulltextLength) {
        for(DocumentPiece docPiece : documentBodyParts) {
            DocumentPointer dp1 = docPiece.a;
            DocumentPointer dp2 = docPiece.b;

            int tokenStart = dp1.getTokenDocPos();
            int tokenEnd = dp2.getTokenDocPos();
            for (int i = tokenStart; i <= tokenEnd; i++) {
                //tokenizationsBody.add(tokenizations.get(i));
                fulltextLength += doc.getTokenizations().get(i).getText().length();
            }
        }
        return fulltextLength;
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
                    createTrainingDictionaryBody(fileEntry, outputDirectory);
                    n++;
                }

            } else {
                createTrainingDictionaryBody(path, outputDirectory);
                n++;

            }


            System.out.println(n + " files to be processed.");

            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingDictionaryBody(File path, String outputDirectory) throws Exception {


        DictionaryDocument doc = processing(path);
        //Get Body
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

        // full text processing
        Pair<String, LayoutTokenization> featSeg = getBodyTextFeatured(doc, documentBodyParts);

        String structeredBody = null;
        if (featSeg != null) {
            // if featSeg is null, it usually means that no body segment is found in the
            // document segmentation
            String bodytextFeatured = featSeg.getA();
            List<LayoutToken> tokenizations = featSeg.getB().getTokenization();

            if ( (bodytextFeatured != null) && (bodytextFeatured.trim().length() > 0) ) {
                               //Write the features file
                String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation";
                Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                writer.write(bodytextFeatured);
                IOUtils.closeWhileHandlingException(writer);

                // also write the raw text as seen before segmentation
                StringBuffer rawtxt = new StringBuffer();
                for(LayoutToken txtline : tokenizations) {
                    rawtxt.append(txtline.getText());
                }
                String outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation.rawtxt";
                FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");

                //Using the existing model of the parser to generate a pre-annotate tei file to be corrected
                if (bodytextFeatured.length() > 0) {
                    String rese = label(bodytextFeatured);
                    StringBuffer bufferFulltext = trainingExtraction(rese, tokenizations, doc);

                    // write the TEI file to reflect the extact layout of the text as extracted from the pdf
                    String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation.tei.xml";
                    writer = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
                    writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                                         "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">\n");
                    writer.write("\n\t\t<headnote>\n");
                    writer.write(doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL).toString());
                    writer.write("\n\t\t</headnote>\n");
                    writer.write("\n\t\t<body>\n");
                    writer.write(bufferFulltext.toString());
                    writer.write("\n\t\t</body>\n");
                    writer.write("\n\t\t<footnote>\n");
                    writer.write(doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL).toString());
                    writer.write("\n\t\t</footnote>\n");
                    writer.write("\n\t</text>\n</tei>\n");
                    writer.close();
                }
            }

        }


    }

    /**
     * Extract results from a labelled full text in the training format without any string modification.
     *
     * @param result        reult
     * @param tokenizations toks
     * @return extraction
     */
    private StringBuffer trainingExtraction(String result,
                                            List<LayoutToken> tokenizations,
                                            DictionaryDocument doc) {
        // this is the main buffer for the whole full text
        StringBuffer buffer = new StringBuffer();
        try {
            List<Block> blocks = DocumentUtils.getBlocksFromDocumentPart(doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL), doc);
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
                        s2 = TextUtilities.HTMLEncode(s); // lexical token
                    } else if (i == 1) {
                        s3 = TextUtilities.HTMLEncode(s); // second lexical token
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

                line = TextUtilities.HTMLEncode(line);

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
                    testClosingTag(buffer, currentTag0, lastTag0, s1);
                }

                boolean output;

                output = writeField(buffer, line, s1, lastTag0, s2, "<entry>", "<entry>", addSpace, 3);


                lastTag = s1;

                if (!st.hasMoreTokens()) {
                    if (lastTag != null) {
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
            }
            else if (lastTag0 == null) {
//                // if previous tagname is null, we output the opening xml tag
//                for (int i = 0; i < nbIndent; i++) {
//                    buffer.append("\t");
//                }
                buffer.append(outField).append(line);
            } else if (!lastTag0.equals("<titlePage>")) {
//                // if the previous tagname is not titlePage, we output the opening xml tag
//                for (int i = 0; i < nbIndent; i++) {
//                    buffer.append("\t");
//                }
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
            if (lastTag0.equals("<entry>")) {
                buffer.append("</entry>");
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


}
