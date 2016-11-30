package org.grobid.core.engines;

import com.google.common.collect.Iterables;
import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.util.IOUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.document.*;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.tagging.GenericTaggerUtils;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.exceptions.GrobidExceptionStatus;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.layout.Block;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Created by med on 02.08.16.
 */
public class DictionarySegmentationParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionarySegmentationParser.class);
    private static volatile DictionarySegmentationParser instance;

    public DictionarySegmentationParser() {
        super(GrobidModels.DICTIONARY_SEGMENTATION);
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

    public String process(File originFile) {
        // GrobidConfig needs to be always initiated before calling initiateProcessing()
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().generateTeiIds(true).build();
        DictionaryDocument doc = initiateProcessing(originFile, config);

        LayoutTokenization tokens = new LayoutTokenization(doc.getTokenizations());
        String segmentedDictionary = null;

        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(tokens).toString();
        String labeledFeatures = null;

        // if featSeg is null, it usually means that no body segment is found in the

        if ((featSeg != null) && (featSeg.trim().length() > 0)) {
            labeledFeatures = label(featSeg);
            segmentedDictionary = new TEIDictionaryFormatter(doc).toTEIFormatDictionarySegmentation(config, null, labeledFeatures, tokens).toString();
        }

        return segmentedDictionary;
    }

    public DictionaryDocument initiateProcessing(File originFile, GrobidAnalysisConfig config) {
        // This method is to be called by an following parser to perform first level segmentation: Headnote, Body and Footnote
        DocumentSource documentSource = DocumentSource.fromPdf(originFile, config.getStartPage(), config.getEndPage());
        try {

            //Prepare
            Document document = new Document(documentSource);
            document.addTokenizedDocument(config);
            document.produceStatistics();
            //Transform the doc to a dictionary doc
            DictionaryDocument doc = new DictionaryDocument(document);
            doc.addTokenizedDocument(config);
            doc = prepareDocument(doc);

            return doc;
        } finally {
            // keep it clean when leaving...
            if (config.getPdfAssetPath() == null) {
                // remove the pdf2xml tmp file
                DocumentSource.close(documentSource, false, true);
            } else {
                // remove the pdf2xml tmp files, including the sub-directories
                DocumentSource.close(documentSource, true, true);
            }
        }
    }

    public DictionaryDocument prepareDocument(DictionaryDocument doc) {

        //This method is used to tokenize and set the diffrent sections of the document (labelled blocks)
        LayoutTokenization tokens = new LayoutTokenization(doc.getTokenizations());
        List<LayoutToken> tokenizations = doc.getTokenizations();
        if (tokenizations.size() > GrobidProperties.getPdfTokensMax()) {
            throw new GrobidException("The document has " + tokenizations.size() + " tokens, but the limit is " + GrobidProperties.getPdfTokensMax(),
                                      GrobidExceptionStatus.TOO_MANY_TOKENS);
        }
        String content = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(tokens).toString();
        if (isNotEmpty(trim(content))) {
            String labelledResult = label(content);
            // set the different sections of the Document object
            doc = generalResultSegmentation(doc, labelledResult, tokenizations);

        }

        return doc;
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
                                 Collections.singleton(new Pair<String, String>("IgnoredToken", ignoredLabel)))) {
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
                    if (!labeledTokenPair.a.startsWith(documentTokens.get(currentLineStartPos).getText())) {
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
                                        labeledTokenPair.a.startsWith(documentTokens.get(currentLineStartPos).getText())) {
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
            curLabel = labeledTokenPair.b;
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
                    String featuresFile = outputDirectory + "/" + fileEntry.getName().substring(0, fileEntry.getName().length() - 4) + ".training.dictionarySegmentation";
                    Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                    writer.write(FeatureVectorLexicalEntry.createFeaturesFromPDF(fileEntry).toString());
                    IOUtils.closeWhileHandlingException(writer);
                    createTrainingDictionary(fileEntry, outputDirectory);
                    n++;
                }

            } else {
                String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionarySegmentation";
                Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                writer.write(FeatureVectorLexicalEntry.createFeaturesFromPDF(path).toString());
                IOUtils.closeWhileHandlingException(writer);
                n++;

                createTrainingDictionary(path, outputDirectory);
            }


            System.out.println(n + " files to be processed.");

            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingDictionary(File path, String outputDirectory) throws IOException {

        //Using the existing model of the parser to generate a pre-annotate tei file to be corrected


        //Get the segmented dictionary
        String segmentedDictionary = process(path);

//        //Naive method to prepare wrap the body
//        StringBuilder bodytxt = DocumentUtils.getDictionarySegmentationTEIToAnnotate(null,doc);


        String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionarySegmentation.tei.xml";
        FileUtils.writeStringToFile(new File(outTei), segmentedDictionary, "UTF-8");

        // also write the raw text as seen before segmentation
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().generateTeiIds(true).build();
        DictionaryDocument doc = initiateProcessing(path, config);
        StringBuffer rawtxt = DocumentUtils.getRawTextFromDoc(doc);
        String outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionarySegmentation.rawtxt";
        FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");
    }

}
