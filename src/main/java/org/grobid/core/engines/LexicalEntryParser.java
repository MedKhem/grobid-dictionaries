package org.grobid.core.engines;

import org.apache.lucene.util.IOUtils;
import org.grobid.core.GrobidModels;
import org.grobid.core.document.*;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.layout.LayoutTokenization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.SortedSet;

/**
 * Created by med on 18.10.16.
 */
public class LexicalEntryParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LexicalEntryParser.class);
    private static volatile LexicalEntryParser instance;

    public LexicalEntryParser() {
        super(GrobidModels.LEXICAL_ENTRY);
    }

    public static LexicalEntryParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }
    /**
     * Create a new instance.
     */
    private static synchronized void getNewInstance() {
        instance = new LexicalEntryParser();
    }

    public String process(File originFile) {
        //Prepare
        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().generateTeiIds(true).build();
        DocumentSource documentSource = DocumentSource.fromPdf(originFile, config.getStartPage(), config.getEndPage(), config.getPdfAssetPath() != null);
        //Old BODY from document
        Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, config);
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);
        //New body from document
//        DictionaryDocument doc = (DictionaryDocument) new EngineParsers().getSegmentationParser().processing(documentSource, config);
//        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabel.BODY);

        LayoutTokenization tokens = DocumentUtils.getLayoutTokenizations(doc, documentBodyParts);
//        String text = tokens.getTokenization().stream().map(LayoutToken::getText).collect(Collectors.joining());
        String bodyLexicalEntry = null;
//        Document doc = getDocFromPDF(originFile);
        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(tokens).toString();
        String labeledFeatures = null;


        // if featSeg is null, it usually means that no body segment is found in the

        if ((featSeg != null) && (featSeg.trim().length() > 0)) {
            labeledFeatures = label(featSeg);
            bodyLexicalEntry = new TEIDictionaryFormatter(doc).toTEIFormat(config, null, labeledFeatures, DocumentUtils.getLayoutTokenizations(doc, documentBodyParts)).toString();
        }

        return bodyLexicalEntry;
    }


    @SuppressWarnings({"UnusedParameters"})
    public int createTrainingBatch(String inputDirectory, String outputDirectory) throws IOException {
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
                    String featuresFile = outputDirectory + "/" + fileEntry.getName().substring(0, fileEntry.getName().length() - 4) + ".training.dictionary";
                    Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                    writer.write(FeatureVectorLexicalEntry.createFeaturesFromPDF(fileEntry).toString());
                    IOUtils.closeWhileHandlingException(writer);
                    n++;
                }

            } else {
                String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionary";
                Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                writer.write(FeatureVectorLexicalEntry.createFeaturesFromPDF(path).toString());
                IOUtils.closeWhileHandlingException(writer);
                n++;
            }

            System.out.println(n + " files to be processed.");

            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }



}
