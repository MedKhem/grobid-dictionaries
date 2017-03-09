package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.util.IOUtils;
import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentUtils;
import org.grobid.core.document.TEIDictionaryFormatter;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.DictionaryBodySegmentationLabels;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * Created by med on 02.08.16.
 */
public class DictionaryBodySegmentationParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionarySegmentationParser.class);
    private static volatile DictionaryBodySegmentationParser instance;

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

    public static List<List<LayoutToken>> processLexicalEntriesLayoutTokens(LayoutTokenization layoutTokenization, String contentFeatured) {
        //Extract the lexical entries in a clusters of tokens for each lexical entry
        StringBuilder buffer = new StringBuilder();
        TaggingLabel lastClusterLabel = null;
        List<LayoutToken> tokenizations = layoutTokenization.getTokenization();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.DICTIONARY_BODY_SEGMENTATION, contentFeatured, tokenizations);

        String tokenLabel = null;
        List<TaggingTokenCluster> clusters = clusteror.cluster();
        List<List<LayoutToken>> list1 = new ArrayList<List<LayoutToken>>();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);
            String tagLabel = clusterLabel.getLabel();


            if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                list1.add(cluster.concatTokens());
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_BODY_OTHER_LABEL)) {
//                list1.add(cluster.concatTokens());
            } else if (tagLabel.equals(DictionaryBodySegmentationLabels.DICTIONARY_BODY_PC_LABEL)) {
//                list1.add(cluster.concatTokens());
            } else {
                throw new IllegalArgumentException(tagLabel + " is not a valid possible tag");
            }


        }

        return list1;
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

    public String process(File originFile) throws Exception {
        //This method is used by the service mode to display the segmentation result as text in tei-xml format
        //Prepare
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionaryDocument doc = null;

        try {
            doc = processing(originFile);
        } catch (GrobidException e) {
            throw e;
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }

        String segmentedBody = new TEIDictionaryFormatter(doc).toTEIFormatDictionaryBodySegmentation(config, null).toString();


        return segmentedBody;
    }

    public DictionaryDocument processing(File originFile) throws Exception {
        // This method is to be called by the following parser
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionarySegmentationParser parser = new DictionarySegmentationParser();
        DictionaryDocument doc = parser.initiateProcessing(originFile, config);
        try {
            //Get Body
            SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

            //Get tokens from the body
            LayoutTokenization layoutTokenization = DocumentUtils.getLayoutTokenizations(doc, documentBodyParts);

            String bodytextFeatured = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization).toString();
            String labeledFeatures = null;


            List<List<LayoutToken>> structuredBody = null;
            if (bodytextFeatured != null) {
                // if bodytextFeatured is null, it usually means that no body segment is found in the
                // document segmentation

                if ((bodytextFeatured != null) && (bodytextFeatured.trim().length() > 0)) {
                    labeledFeatures = label(bodytextFeatured);
                }

                structuredBody = processLexicalEntriesLayoutTokens(layoutTokenization, labeledFeatures);


                doc.setLexicalEntries(structuredBody);
            }

            return doc;
        } catch (GrobidException e) {
            throw e;
        } catch (Exception e) {
            throw new GrobidException("An exception occurred while running Grobid.", e);
        }
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

            int n = 0;
            // we process all pdf files in the directory
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

        // Segment the doc
        DictionaryDocument doc = processing(path);
        //Get Body
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

        //Get tokens from the body
        LayoutTokenization tokenizations = DocumentUtils.getLayoutTokenizations(doc, documentBodyParts);

        String bodytextFeatured = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(tokenizations).toString();

        if (bodytextFeatured != null) {
            // if featSeg is null, it usually means that no body segment is found in the
            // document segmentation


            if ((bodytextFeatured != null) && (bodytextFeatured.trim().length() > 0)) {
                //Write the features file
                String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation";
                Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                writer.write(bodytextFeatured);
                IOUtils.closeWhileHandlingException(writer);

                // also write the raw text as seen before segmentation
                StringBuffer rawtxt = new StringBuffer();
                for (LayoutToken txtline : tokenizations.getTokenization()) {
                    rawtxt.append(txtline.getText());
                }
                String outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation.rawtxt";
                FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");

                //Using the existing model of the parser to generate a pre-annotate tei file to be corrected
                if (bodytextFeatured.length() > 0) {
                    String rese = label(bodytextFeatured);
                    StringBuilder bufferFulltext = trainingExtraction(doc, rese, tokenizations);

                    // write the TEI file to reflect the extact layout of the text as extracted from the pdf
                    String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.dictionaryBodySegmentation.tei.xml";
                    writer = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
                    writer.write("<?xml version=\"1.0\" ?>\n<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                                         "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">");
                    writer.write("\n\t\t<headnote>");
                    writer.write(DocumentUtils.replaceLinebreaksWithTags(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL).toString()));
                    writer.write("</headnote>");
                    writer.write("\n\t\t<body>");
                    writer.write(bufferFulltext.toString());
                    writer.write("</body>");
                    writer.write("\n\t\t<footnote>");
                    writer.write(DocumentUtils.replaceLinebreaksWithTags(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL).toString()));
                    writer.write("</footnote>");
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
    private StringBuilder trainingExtraction(DictionaryDocument doc, String result, LayoutTokenization tokenizations) {

        StringBuilder buffer = new TEIDictionaryFormatter(doc).toTEIDictionaryBodySegmentation(result, tokenizations);
        return buffer;
    }


    @Override
    public void close() throws IOException {
        super.close();
        // ...
    }


}
