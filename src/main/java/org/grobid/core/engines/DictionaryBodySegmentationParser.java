package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.util.IOUtils;
import org.grobid.core.document.*;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.layout.LayoutTokenization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.SortedSet;

/**
 * Created by med on 02.08.16.
 */
public class DictionaryBodySegmentationParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryBodySegmentationParser.class);
    private static volatile DictionaryBodySegmentationParser instance;
    private String lexEntries;

    //Might be needed to have several LEXICALENTRIES_XYZ models, based on the function,
    // depending how many sub models will be created.
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

    public String process(File originFile) {
//        //Prepare
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionarySegmentationParser parser = new DictionarySegmentationParser();
        DictionaryDocument doc = parser.initiateProcessing(originFile, config);
        //Get Body
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);
        //Get tokens from the body
        LayoutTokenization tokens = DocumentUtils.getLayoutTokenizations(doc, documentBodyParts);

        String segmentedBody = null;

        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(tokens).toString();
        String labeledFeatures = null;

//        // if featSeg is null, it usually means that no body segment is found in the
        if ((featSeg != null) && (featSeg.trim().length() > 0)) {
            labeledFeatures = label(featSeg);
            segmentedBody = new TEIDictionaryFormatter(doc).toTEIFormatDictionaryBodySegmentation(config, null, labeledFeatures, DocumentUtils.getLayoutTokenizations(doc, documentBodyParts)).toString();
        }

        return segmentedBody;
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
                    String featuresFile = outputDirectory + "/" + fileEntry.getName().substring(0, fileEntry.getName().length() - 4) + ".training.dictionaryBodySegmentation";
                    Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                    writer.write(FeatureVectorLexicalEntry.createFeaturesFromPDF(fileEntry).toString());
                    IOUtils.closeWhileHandlingException(writer);
                    createTrainingDictionaryBody(fileEntry, outputDirectory);
                    n++;
                }

            } else {
                String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation";
                Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                writer.write(FeatureVectorLexicalEntry.createFeaturesFromPDF(path).toString());
                IOUtils.closeWhileHandlingException(writer);
                n++;

                createTrainingDictionaryBody(path, outputDirectory);
            }


            System.out.println(n + " files to be processed.");

            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingDictionaryBody(File path, String outputDirectory) throws IOException {

        //Using the existing model of the parser to generate a pre-annotate tei file to be corrected


        //Get the segmented dictionary
        String segmentedDictionary = process(path);

//        //Naive method to prepare wrap the body
//        StringBuilder bodytxt = DocumentUtils.getDictionarySegmentationTEIToAnnotate(null,doc);


        String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation.tei.xml";
        FileUtils.writeStringToFile(new File(outTei), segmentedDictionary, "UTF-8");

        // also write the raw text as seen before segmentation
        // Prepare first
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionarySegmentationParser parser = new DictionarySegmentationParser();
        DictionaryDocument doc = parser.initiateProcessing(path, config);
        //Write rawtext
        StringBuffer rawtxt = DocumentUtils.getRawTextFromDoc(doc);
        String outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation.rawtxt";
        FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");
    }


}
