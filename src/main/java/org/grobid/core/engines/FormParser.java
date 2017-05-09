package org.grobid.core.engines;

import com.google.common.collect.ForwardingMapEntry;
import org.apache.commons.io.IOUtils;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.data.LabeledForm;
import org.grobid.core.data.LabeledLexicalEntry;
import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.document.TEIDictionaryFormatter;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorForm;
import org.grobid.core.features.FeaturesUtils;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.layout.BoundingBox;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_FORM_LABEL;

/**
 * Created by lfoppiano on 05/05/2017.
 */
public class FormParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FormParser.class);
    private static volatile FormParser instance;

    public FormParser() {
        super(DictionaryModels.FORM);
    }


    public static FormParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new FormParser();
    }

    public LabeledForm process(List<LayoutToken> formEntry) {

        return process(formEntry, LEXICAL_ENTRY_FORM_LABEL);
    }

    public LabeledForm process(List<LayoutToken> formEntry, String parentTag) {

        StringBuilder sb = new StringBuilder();
        String previousFont = null;
        String fontStatus = null;
        String lineStatus = null;

        int counter = 0;
        int nbToken = formEntry.size();
        for (LayoutToken token : formEntry) {
            String text = token.getText();
            text = text.replace(" ", "");

            if (TextUtilities.filterLine(text) || isBlank(text)) {
                counter++;
                continue;
            }
            if (text.equals("\n") || text.equals("\r") || (text.equals("\n\r"))) {
                counter++;
                continue;
            }

            // First token
            if (counter - 1 < 0) {
                lineStatus = LineStatus.LINE_START.toString();
            } else if (counter + 1 == nbToken) {
                // Last token
                lineStatus = LineStatus.LINE_END.toString();
            } else {
                String previousTokenText;
                Boolean previousTokenIsNewLineAfter;
                String nextTokenText;
                Boolean nextTokenIsNewLineAfter;
                Boolean afterNextTokenIsNewLineAfter = false;

                //The existence of the previousToken and nextToken is already check.
                previousTokenText = formEntry.get(counter - 1).getText();
                previousTokenIsNewLineAfter = formEntry.get(counter - 1).isNewLineAfter();
                nextTokenText = formEntry.get(counter + 1).getText();
                nextTokenIsNewLineAfter = formEntry.get(counter + 1).isNewLineAfter();

                // Check the existence of the afterNextToken
                if ((nbToken > counter + 2) && (formEntry.get(counter + 2) != null)) {
                    afterNextTokenIsNewLineAfter = formEntry.get(counter + 2).isNewLineAfter();
                }

                lineStatus = FeaturesUtils.checkLineStatus(text, previousTokenIsNewLineAfter, previousTokenText, nextTokenIsNewLineAfter, nextTokenText, afterNextTokenIsNewLineAfter);

            }
            counter++;

            String[] returnedFont = FeaturesUtils.checkFontStatus(token.getFont(), previousFont);
            previousFont = returnedFont[0];
            fontStatus = returnedFont[1];


            FeatureVectorForm featureVectorForm = FeatureVectorForm.addFeaturesForm(token, "",
                    lineStatus, fontStatus, parentTag);

            sb.append(featureVectorForm.printVector() + "\n");
        }

        String features = sb.toString();
        String output = label(features);


        LabeledForm labeledForm = transformResponse(output, formEntry);

        return labeledForm;

    }

    public LabeledForm transformResponse(String modelOutput, List<LayoutToken> layoutTokens) {
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.FORM,
                modelOutput, layoutTokens);

        List<TaggingTokenCluster> clusters = clusteror.cluster();
        LabeledForm labeledForm = new LabeledForm();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);

            List<LayoutToken> concatenatedTokens = cluster.concatTokens();
            String text = LayoutTokensUtil.toText(concatenatedTokens);
            String tagLabel = clusterLabel.getLabel();

            labeledForm.addLabel(new Pair(text, tagLabel));
        }

        return labeledForm;

    }

    public String process(String text) {
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        StringBuffer output = new StringBuffer();

        List<LayoutToken> tokens = null;
        try {
            tokens = GrobidAnalyzer.getInstance().tokenizeWithLayoutToken(text);
        } catch (Exception e) {
            LOGGER.error("Fail to tokenize:, " + text, e);
        }

        if ((tokens == null) || (tokens.size() == 0)) {
            return null;
        }

        try {
            LayoutTokenization layoutTokenization;

            String ress = null;
            List<String> texts = new ArrayList<>();
            for (LayoutToken token : tokens) {
                if (!token.getText().equals(" ") && !token.getText().equals("\t") && !token.getText().equals("\u00A0")) {
                    texts.add(token.getText());
                }
            }

            String response;
            try {
                response = label(ress);
            } catch (Exception e) {
                throw new GrobidException("CRF labeling for form parsing failed.", e);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public String process(File originFile) {
        //Prepare
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        LexicalEntryParser lexicalEntriesParser = new LexicalEntryParser();
        DictionaryDocument doc = null;
        StringBuffer lexicalEntries = new StringBuffer();
        try {
            doc = lexicalEntriesParser.process(originFile);
            List<LabeledLexicalEntry> labeledLexicalEntries = doc.getLabeledLexicalEntries();

            for (LabeledLexicalEntry lexicalEntry : labeledLexicalEntries) {

                final StringBuilder sb = new StringBuilder();

                for (Pair<List<LayoutToken>, String> entry : lexicalEntry.getLabels()) {
                    String token = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(entry.getA()));
                    String label = entry.getB();

//                    produceXmlNode(sb, token, label);
                }

//                lexicalEntry.getLabels().forEach();


//                String featSeg = FeatureVectorForm.addFeaturesForm(token, null,
//                        lineStatus, fontStatus);

//                String labeledFeatures = null;
//                if (StringUtils.isNotBlank(featSeg)) {
//                    labeledFeatures = label(featSeg);
//                }

                lexicalEntries.append("</entry>");

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String LEs = new TEIDictionaryFormatter(null).toTEIFormatLexicalEntry(config, null, lexicalEntries.toString()).toString();
        return LEs;
    }

    public String processResponse(String text, String result, List<LayoutToken> tokenizations) {

        List<LabeledForm> measurements = new ArrayList<>();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.FORM, result,
                tokenizations);
        List<TaggingTokenCluster> clusters = clusteror.cluster();

        LabeledForm form = new LabeledForm();

        int pos = 0; // position in term of characters for creating the offsets

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }

            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            List<LayoutToken> theTokens = cluster.concatTokens();
            String clusterContent = LayoutTokensUtil.toText(cluster.concatTokens()).trim();
            List<BoundingBox> boundingBoxes = null;

        }

        return null;
    }

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

            int n = 0;
            // we process all pdf files in the directory
            if (path.isDirectory()) {
                for (File fileEntry : path.listFiles()) {
                    // Create the pre-annotated file and the raw text
                    createTrainingData(fileEntry, outputDirectory);
                    n++;
                }

            } else {
                createTrainingData(path, outputDirectory);
                n++;

            }


            System.out.println(n + " files to be processed.");

            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingData(File path, String outputDirectory) throws Exception {

        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        FormParser parser = new FormParser();
        String content = parser.process(IOUtils.toString(new FileReader(path)));

        parser.process(content);

        /*if (bodytextFeatured != null) {
            // if featSeg is null, it usually means that no body segment is found in the
            // document segmentation


            if ((bodytextFeatured != null) && (bodytextFeatured.trim().length() > 0)) {
                //Write the features file
                String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.lexicalEntry";
                Writer writer = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");
                writer.write(bodytextFeatured);
                IOUtils.closeWhileHandlingException(writer);

                // also write the raw text as seen before segmentation
                StringBuffer rawtxt = new StringBuffer();
                for (LayoutToken txtline : tokenizations.getTokenization()) {
                    rawtxt.append(txtline.getText());
                }
                String outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.lexicalEntry.rawtxt";
                FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");

                //Using the existing model of the parser to generate a pre-annotate tei file to be corrected
                if (bodytextFeatured.length() > 0) {
                    DictionaryBodySegmentationParser bodySegmentationParser = new DictionaryBodySegmentationParser();
                    doc = bodySegmentationParser.processing(path);
                    StringBuffer LexicalEntries = new StringBuffer();
                    LayoutTokenization layoutTokenization;
                    for (List<LayoutToken> allLayoutokensOfALexicalEntry : doc.getLexicalEntries()) {
                        LexicalEntries.append("<entry>");
                        layoutTokenization = new LayoutTokenization(allLayoutokensOfALexicalEntry);
                        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization).toString();
                        String labeledFeatures = null;
                        // if featSeg is null, it usually means that no body segment is found in the

                        if ((featSeg != null) && (featSeg.trim().length() > 0)) {
                            labeledFeatures = label(featSeg);
                            LexicalEntries.append(toTEILexicalEntry(labeledFeatures, layoutTokenization, true));
                        }
                        LexicalEntries.append("</entry>");

                    }


                    // write the TEI file to reflect the extact layout of the text as extracted from the pdf
                    String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.lexicalEntry.tei.xml";
                    writer = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
                    writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                            "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">");
                    writer.write("\n\t\t<headnote>");
                    writer.write(DocumentUtils.replaceLinebreaksWithTags(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL).toString()));
                    writer.write("</headnote>");
                    writer.write("\n\t\t<body>");
                    writer.write(LexicalEntries.toString());
                    writer.write("</body>");
                    writer.write("\n\t\t<footnote>");
                    writer.write(DocumentUtils.replaceLinebreaksWithTags(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL).toString()));
                    writer.write("</footnote>");
                    writer.write("\n\t</text>\n</tei>\n");
                    writer.close();
                }
            }

        }*/
    }
}