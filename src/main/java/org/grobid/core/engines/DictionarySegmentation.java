package org.grobid.core.engines;

import eugfc.imageio.plugins.PNMRegistry;
import org.apache.commons.io.FileUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.document.BasicStructureBuilder;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.exceptions.GrobidResourceException;
import org.grobid.core.features.FeatureFactory;
import org.grobid.core.features.FeaturesVectorSegmentation;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.GraphicObjectType;
import org.grobid.core.layout.Page;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.LanguageUtilities;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;

import static org.grobid.core.analyzers.GrobidLineAnalyser.getAllLinesFeatured;

/**
 * Created by med on 27.07.16.
 */
public class DictionarySegmentation extends AbstractParser {

	/*
        10 labels for this model:
	 		cover page <cover>,
			document header <header>,
			page footer <footnote>,
			page header <headnote>,
			document body <body>,
			bibliographical section <references>,
			page number <page>,
			annexes <annex>,
		    acknowledgement <acknowledgement>,
		    toc <toc> -> not yet used because not yet training data for this
	*/

private static final Logger LOGGER = LoggerFactory.getLogger(Segmentation.class);

private LanguageUtilities languageUtilities = LanguageUtilities.getInstance();

// default bins for relative position
private static final int NBBINS_POSITION = 12;

// default bins for inter-block spacing
private static final int NBBINS_SPACE = 5;

// default bins for block character density
private static final int NBBINS_DENSITY = 5;

// projection scale for line length
private static final int LINESCALE = 10;

/**
 * TODO some documentation...
 */
public DictionarySegmentation() {
        super(GrobidModels.SEGMENTATION);
        }

public Document processing(File input, GrobidAnalysisConfig config) {
        if (input == null) {
        throw new GrobidResourceException("Cannot process pdf file, because input file was null.");
        }
        if (!input.exists()) {
        throw new GrobidResourceException("Cannot process pdf file, because input file '" +
        input.getAbsolutePath() + "' does not exist.");
        }
        DocumentSource documentSource = DocumentSource.fromPdf(input, config.getStartPage(), config.getEndPage());
        return processing(documentSource, config);
        }
/**
 * Segment a PDF document into high level zones: cover page, document header,
 * page footer, page header, body, page numbers, biblio section and annexes.
 *
 * @param documentSource     document source
 * @return Document object with segmentation information
 */
public Document processing(DocumentSource documentSource, GrobidAnalysisConfig config) {
        try {
        Document doc = new Document(documentSource);
        doc.addTokenizedDocument(config);
        doc = prepareDocument(doc);

        // if assets is true, the images are still there under directory pathXML+"_data"
        // we copy them to the assetPath directory

        File assetFile = config.getPdfAssetPath();
        dealWithImages(documentSource, doc, assetFile, config);
        return doc;
        } finally {
        // keep it clean when leaving...
        if (config.getPdfAssetPath() == null) {
        // remove the pdf2xml tmp file
        //DocumentSource.close(documentSource, false);
        DocumentSource.close(documentSource, true);
        } else {
        // remove the pdf2xml tmp files, including the sub-directories
        DocumentSource.close(documentSource, true);
        }
        }
        }

public Document processing(String text) {
        Document doc = Document.createFromText(text);
        return prepareDocument(doc);
        }

public Document prepareDocument(Document doc) {

        List<LayoutToken> tokenizations = doc.getTokenizations();
        if (tokenizations.size() > GrobidProperties.getPdfTokensMax()) {
        throw new GrobidException("The document has " + tokenizations.size() + " tokens, but the limit is " + GrobidProperties.getPdfTokensMax(),
        GrobidExceptionStatus.TOO_MANY_TOKENS);
        }

        doc.produceStatistics();
        String content = //getAllTextFeatured(doc, headerMode);
        getAllLinesFeatured(doc);
        if ((content != null) && (content.trim().length() > 0)) {
        String labelledResult = label(content);
        // set the different sections of the Document object
        doc = BasicStructureBuilder.generalResultSegmentation(doc, labelledResult, tokenizations);
        }
        return doc;
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
        e.printStackTrace();
        }
        } else if (toLowerCaseName.endsWith(".jpg")
        || toLowerCaseName.endsWith(".ppm")
        //	|| currFile.getName().toLowerCase().endsWith(".pbm")
        ) {
        try {
final BufferedImage bi = ImageIO.read(currFile);
        String outputfilePath;
        if (toLowerCaseName.endsWith(".jpg")) {
        outputfilePath = assetFile.getPath() + File.separator +
        toLowerCaseName.replace(".jpg", ".png");
        }
                                /*else if (currFile.getName().toLowerCase().endsWith(".pbm")) {
                                    outputfilePath = assetFile.getPath() + File.separator +
                                         currFile.getName().toLowerCase().replace(".pbm",".png");
                                }*/
        else {
        outputfilePath = assetFile.getPath() + File.separator +
        toLowerCaseName.replace(".ppm", ".png");
        }
        ImageIO.write(bi, "png", new File(outputfilePath));
        } catch (IOException e) {
        e.printStackTrace();
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





/**
 * Process the content of the specified pdf and format the result as training data.
 *
 * @param inputFile    input file
 * @param pathFullText path to fulltext
 * @param pathTEI      path to TEI
 * @param id           id
 */
public void createTrainingSegmentation(String inputFile,
        String pathFullText,
        String pathTEI,
        int id) {
        DocumentSource documentSource = null;
        try {
        File file = new File(inputFile);

        documentSource = DocumentSource.fromPdf(file);
        Document doc = new Document(documentSource);

        String PDFFileName = file.getName();
        doc.addTokenizedDocument(GrobidAnalysisConfig.defaultInstance());

        if (doc.getBlocks() == null) {
        throw new Exception("PDF parsing resulted in empty content");
        }
        doc.produceStatistics();

        String fulltext = //getAllTextFeatured(doc, false);
        getAllLinesFeatured(doc);
        List<LayoutToken> tokenizations = doc.getTokenizationsFulltext();

        // we write the full text untagged (but featurized)
        String outPathFulltext = pathFullText + File.separator +
        PDFFileName.replace(".pdf", ".training.segmentation");
        Writer writer = new OutputStreamWriter(new FileOutputStream(new File(outPathFulltext), false), "UTF-8");
        writer.write(fulltext + "\n");
        writer.close();

        // also write the raw text as seen before segmentation
        StringBuffer rawtxt = new StringBuffer();
        for(LayoutToken txtline : tokenizations) {
        rawtxt.append(txtline.getText());
        }
        String outPathRawtext = pathFullText + File.separator +
        PDFFileName.replace(".pdf", ".training.segmentation.rawtxt");
        FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");

        if ((fulltext != null) && (fulltext.length() > 0)) {
        String rese = label(fulltext);
        StringBuffer bufferFulltext = trainingExtraction(rese, tokenizations, doc);

        // write the TEI file to reflect the extact layout of the text as extracted from the pdf
        writer = new OutputStreamWriter(new FileOutputStream(new File(pathTEI +
        File.separator +
        PDFFileName.replace(".pdf", ".training.segmentation.tei.xml")), false), "UTF-8");
        writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" + id +
        "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">\n");

        writer.write(bufferFulltext.toString());
        writer.write("\n\t</text>\n</tei>\n");
        writer.close();
        }

        } catch (Exception e) {
        e.printStackTrace();
        throw new GrobidException("An exception occured while running Grobid training" +
        " data generation for segmentation model.", e);
        } finally {
        DocumentSource.close(documentSource, true);
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

        output = writeField(buffer, line, s1, lastTag0, s2, "<header>", "<front>", addSpace, 3);
                /*if (!output) {
                    output = writeField(buffer, line, s1, lastTag0, s2, "<other>", "<note type=\"other\">", addSpace, 3);
                }*/
        if (!output) {
        output = writeField(buffer, line, s1, lastTag0, s2, "<headnote>", "<note place=\"headnote\">",
        addSpace, 3);
        }
        if (!output) {
        output = writeField(buffer, line, s1, lastTag0, s2, "<footnote>", "<note place=\"footnote\">",
        addSpace, 3);
        }
        if (!output) {
        output = writeField(buffer, line, s1, lastTag0, s2, "<page>", "<page>", addSpace, 3);
        }
        if (!output) {
        //output = writeFieldBeginEnd(buffer, s1, lastTag0, s2, "<reference>", "<listBibl>", addSpace, 3);
        output = writeField(buffer, line, s1, lastTag0, s2, "<references>", "<listBibl>", addSpace, 3);
        }
        if (!output) {
        //output = writeFieldBeginEnd(buffer, s1, lastTag0, s2, "<body>", "<body>", addSpace, 3);
        output = writeField(buffer, line, s1, lastTag0, s2, "<body>", "<body>", addSpace, 3);
        }
        if (!output) {
        output = writeField(buffer, line, s1, lastTag0, s2, "<cover>", "<titlePage>", addSpace, 3);
        }
        if (!output) {
        output = writeField(buffer, line, s1, lastTag0, s2, "<annex>", "<div type=\"annex\">", addSpace, 3);
        }
        if (!output) {
        output = writeField(buffer, line, s1, lastTag0, s2, "<acknowledgement>", "<div type=\"acknowledgement\">", addSpace, 3);
        }
                /*if (!output) {
                    if (closeParagraph) {
                        output = writeField(buffer, s1, "", s2, "<reference_marker>", "<label>", addSpace, 3);
                    } else
                        output = writeField(buffer, s1, lastTag0, s2, "<reference_marker>", "<label>", addSpace, 3);
                }*/
                /*if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<citation_marker>", "<ref type=\"biblio\">",
                            addSpace, 3);
                }*/
                /*if (!output) {
                    output = writeField(buffer, s1, lastTag0, s2, "<figure_marker>", "<ref type=\"figure\">",
                            addSpace, 3);
                }*/
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
            /*else if (lastTag0 == null) {
                   for(int i=0; i<nbIndent; i++) {
                       buffer.append("\t");
                   }
                     buffer.append(outField+s2);
               }*/
            /*else if (field.equals("<citation_marker>")) {
                if (addSpace)
                    buffer.append(" " + outField + s2);
                else
                    buffer.append(outField + s2);
            } else if (field.equals("<figure_marker>")) {
                if (addSpace)
                    buffer.append(" " + outField + s2);
                else
                    buffer.append(outField + s2);
            } else if (field.equals("<reference_marker>")) {
                if (!lastTag0.equals("<references>") && !lastTag0.equals("<reference_marker>")) {
                    for (int i = 0; i < nbIndent; i++) {
                        buffer.append("\t");
                    }
                    buffer.append("<bibl>");
                }
                if (addSpace)
                    buffer.append(" " + outField + s2);
                else
                    buffer.append(outField + s2);
            } */
        else if (lastTag0 == null) {
        // if previous tagname is null, we output the opening xml tag
        for (int i = 0; i < nbIndent; i++) {
        buffer.append("\t");
        }
        buffer.append(outField).append(line);
        } else if (!lastTag0.equals("<titlePage>")) {
        // if the previous tagname is not titlePage, we output the opening xml tag
        for (int i = 0; i < nbIndent; i++) {
        buffer.append("\t");
        }
        buffer.append(outField).append(line);
        } else {
        // otherwise we continue by ouputting the token
        buffer.append(line);
        }
        }
        return result;
        }

/**
 * This is for writing fields for fields where begin and end of field matter, like paragraph or item
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
    /*private boolean writeFieldBeginEnd(StringBuffer buffer,
                                       String s1,
                                       String lastTag0,
                                       String s2,
                                       String field,
                                       String outField,
                                       boolean addSpace,
                                       int nbIndent) {
        boolean result = false;
        if ((s1.equals(field)) || (s1.equals("I-" + field))) {
            result = true;
            if (lastTag0.equals("I-" + field)) {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            } /*else if (lastTag0.equals(field) && s1.equals(field)) {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            } else if (!lastTag0.equals("<citation_marker>") && !lastTag0.equals("<figure_marker>")
                    && !lastTag0.equals("<figure>") && !lastTag0.equals("<reference_marker>")) {
                for (int i = 0; i < nbIndent; i++) {
                    buffer.append("\t");
                }
                buffer.append(outField + s2);
            }
			else {
                if (addSpace)
                    buffer.append(" " + s2);
                else
                    buffer.append(s2);
            }
        }
        return result;
    }*/

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
        if (lastTag0.equals("<header>")) {
        buffer.append("</front>\n\n");
        } else if (lastTag0.equals("<body>")) {
        buffer.append("</body>\n\n");
        } else if (lastTag0.equals("<headnote>")) {
        buffer.append("</note>\n\n");
        } else if (lastTag0.equals("<footnote>")) {
        buffer.append("</note>\n\n");
        } else if (lastTag0.equals("<references>")) {
        buffer.append("</listBibl>\n\n");
        res = true;
        } else if (lastTag0.equals("<page>")) {
        buffer.append("</page>\n\n");
        } else if (lastTag0.equals("<cover>")) {
        buffer.append("</titlePage>\n\n");
        } else if (lastTag0.equals("<annex>")) {
        buffer.append("</div>\n\n");
        } else if (lastTag0.equals("<acknowledgement>")) {
        buffer.append("</div>\n\n");
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
