package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.util.IOUtils;
import org.grobid.core.data.LabeledLexicalEntry;
import org.grobid.core.data.SimpleLabeled;
import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.document.DocumentUtils;
import org.grobid.core.document.TEIDictionaryFormatter;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.engines.label.LexicalEntryLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static org.grobid.core.document.TEIDictionaryFormatter.createMyXMLString;
import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_SENSE_LABEL;

/**
 * Created by med on 18.10.16.
 */
public class LexicalEntryParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LexicalEntryParser.class);
    private static volatile LexicalEntryParser instance;

    public LexicalEntryParser() {
        super(DictionaryModels.LEXICAL_ENTRY);
    }

    public static LexicalEntryParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new LexicalEntryParser();
    }

    public String processToTei(File originFile, boolean onlyLexicalEntries) {
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionaryDocument doc = process(originFile);
        List<LabeledLexicalEntry> entries = doc.getLabeledLexicalEntries();

        StringBuilder lexicalEntries = new StringBuilder();

        for (LabeledLexicalEntry entry : entries) {
            lexicalEntries.append("<entry>");
            if (onlyLexicalEntries) {
                lexicalEntries.append(toTEILexicalEntry(entry));
            } else {
                lexicalEntries.append(toTEILexicalEntryAndBeyond(entry));
            }
            lexicalEntries.append("</entry>");
        }

        String LEs = new TEIDictionaryFormatter(doc)
                .toTEIFormatLexicalEntry(config, null, lexicalEntries.toString()).toString();
        return LEs;
    }

    public DictionaryDocument process(File originFile) {
        //Prepare
        DictionaryBodySegmentationParser bodySegmentationParser = new DictionaryBodySegmentationParser();
        DictionaryDocument doc = null;
        try {
            doc = bodySegmentationParser.processing(originFile);

            List<LabeledLexicalEntry> entries = new ArrayList<>();
            for (List<LayoutToken> allLayoutokensOfALexicalEntry : doc.getLexicalEntries()) {
                LabeledLexicalEntry entry = process(allLayoutokensOfALexicalEntry);
                entries.add(entry);
            }

            doc.setLabeledLexicalEntries(entries);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doc;
    }

    public LabeledLexicalEntry process(List<LayoutToken> entry) {
        return process(entry, DICTIONARY_ENTRY_LABEL);
    }

    public LabeledLexicalEntry process(List<LayoutToken> entry, String parentTag) {
        LayoutTokenization layoutTokenization = new LayoutTokenization(entry);
        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization(), parentTag).toString();

        // if featSeg is null, it usually means that no body segment is found in the dictionary
        LabeledLexicalEntry result = null;
        if (StringUtils.isNotBlank(featSeg)) {
            String labeledFeatures = label(featSeg);
            result = transformResponse(labeledFeatures, layoutTokenization.getTokenization());
        }

        return result;
    }

    public LabeledLexicalEntry transformResponse(String modelOutput, List<LayoutToken> layoutTokens) {
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.LEXICAL_ENTRY,
                modelOutput, layoutTokens);

        List<TaggingTokenCluster> clusters = clusteror.cluster();
        LabeledLexicalEntry labeledLexicalEntry = new LabeledLexicalEntry();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);

            List<LayoutToken> concatenatedTokens = cluster.concatTokens();
            String tagLabel = clusterLabel.getLabel();

            labeledLexicalEntry.addLabel(new Pair(concatenatedTokens, tagLabel));
        }

        return labeledLexicalEntry;
    }


    public String toTEILexicalEntry(LabeledLexicalEntry entries) {
        final StringBuilder sb = new StringBuilder();

        for (Pair<List<LayoutToken>, String> entry : entries.getLabels()) {
            String token = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(entry.getA()));
            String label = entry.getB();
            produceXmlNode(sb, token, label);
        }

        return sb.toString();
    }

    public String toTEILexicalEntryAndBeyond(LabeledLexicalEntry entries) {
        final StringBuilder sb = new StringBuilder();

        for (Pair<List<LayoutToken>, String> entry : entries.getLabels()) {
            String token = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(entry.getA()));
            String label = entry.getB();

            /*if (label.equals("<form>")) {
                sb.append("<form>").append("\n");
                SimpleLabeled form = new FormParser().process(entry.getA());
                StringBuilder gramGrp = new StringBuilder();
                for (Pair<String, String> entryForm : form.getLabels()) {
                    String tokenForm = LayoutTokensUtil.normalizeText(entryForm.getA());
                    String labelForm = entryForm.getB();

                    String content = TextUtilities.HTMLEncode(tokenForm);
                    content = content.replace("&lt;lb/&gt;", "<lb/>");
                    if (!labelForm.equals("<other>") && (!labelForm.equals("<gramGrp>"))) {
                        sb.append(createMyXMLString(labelForm.replaceAll("[<>]", ""), content));
                    } else if (labelForm.equals("<gramGrp>")) {
                        gramGrp.append(createMyXMLString(labelForm.replaceAll("[<>]", ""), content));
                    } else {
                        sb.append(content);
                    }
                }
                sb.append("</form>").append("\n");
                sb.append(gramGrp.toString());
            } else */if (label.equals("<sense>")) {
                sb.append("<sense>").append("\n");
                //I apply the form also to the sense to recognise the grammatical group, if any!
                SimpleLabeled sense = new SenseParser().process(entry.getA());
                for (Pair<String, String> entryForm : sense.getLabels()) {
                    String tokenSense = LayoutTokensUtil.normalizeText(entryForm.getA());
                    String labelSense = entryForm.getB();

                    String content = TextUtilities.HTMLEncode(tokenSense);
                    content = content.replace("&lt;lb/&gt;", "<lb/>");
                    if (labelSense.equals("<gramGrp>")) {
                        sb.append(createMyXMLString(labelSense.replaceAll("[<>]", ""), content));
                    } else if (labelSense.equals("<sense>")) {
                        sb.append(createMyXMLString(labelSense.replaceAll("[<>]", ""), content));
                    } else {
                        sb.append(content);
                    }
                }
                sb.append("</sense>").append("\n");
//            } else if (label.equals("<re>")) {
//                //I apply the same model recursively on the relative entry
//                sb.append("<re>").append("\n");
//                //I apply the form also to the sense to recognise the grammatical group, if any!
//                LabeledLexicalEntry labeledEntries = new LexicalEntryParser().process(entry.getA(), LEXICAL_ENTRY_RE_LABEL);
//                for (Pair<List<LayoutToken>, String> lexicalEntry : labeledEntries.getLabels()) {
//                    String tokenForm = LayoutTokensUtil.normalizeText(LayoutTokensUtil.toText(lexicalEntry.getA()));
//                    String labelForm = lexicalEntry.getB();
//
//                    String content = TextUtilities.HTMLEncode(tokenForm);
//                    content = content.replace("&lt;lb/&gt;", "<lb/>");
//                    if (!labelForm.equals("<other>")) {
//                        sb.append(createMyXMLString(labelForm.replaceAll("[<>]", ""), content));
//                    } else {
//                        sb.append(content);
//                    }
//                }
//                sb.append("</re>").append("\n");

            } else {
                produceXmlNode(sb, token, label);
            }
        }
        return sb.toString();
    }


    public StringBuilder toTEILexicalEntry(String bodyContentFeatured, List<LayoutToken> layoutTokens,
                                           boolean isTrainingData) {
        StringBuilder buffer = new StringBuilder();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.LEXICAL_ENTRY,
                bodyContentFeatured, layoutTokens);

        List<TaggingTokenCluster> clusters = clusteror.cluster();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);

            List<LayoutToken> list1 = cluster.concatTokens();
            String str1 = LayoutTokensUtil.toText(list1);
            String clusterContent;
            if (isTrainingData) {
                clusterContent = DocumentUtils.replaceLinebreaksWithTags(str1);
            } else {
                clusterContent = LayoutTokensUtil.normalizeText(str1);
            }

            String tagLabel = clusterLabel.getLabel();


            produceXmlNode(buffer, clusterContent, tagLabel);
        }

        return buffer;
    }

    private void produceXmlNode(StringBuilder buffer, String clusterContent, String tagLabel) {
        if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_FORM_LABEL)) {
            clusterContent = TextUtilities.HTMLEncode(clusterContent);
            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("form", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_ETYM_LABEL)) {
            clusterContent = TextUtilities.HTMLEncode(clusterContent);
            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("etym", clusterContent));
        } else if (tagLabel.equals(LEXICAL_ENTRY_SENSE_LABEL)) {
            clusterContent = TextUtilities.HTMLEncode(clusterContent);
            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("sense", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_RE_LABEL)) {
            clusterContent = TextUtilities.HTMLEncode(clusterContent);
            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("re", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_OTHER_LABEL)) {
            clusterContent = TextUtilities.HTMLEncode(clusterContent);
            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("other", clusterContent));
        } else if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_PC_LABEL)) {
            clusterContent = TextUtilities.HTMLEncode(clusterContent);
            clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
            buffer.append(createMyXMLString("pc", clusterContent));
        } else {
            throw new IllegalArgumentException(tagLabel + " is not a valid possible tag");
        }
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

            int n = 0;
            // we process all pdf files in the directory
            if (path.isDirectory()) {
                for (File fileEntry : path.listFiles()) {
                    // Create the pre-annotated file and the raw text
                    createTrainingLexicalEntries(fileEntry, outputDirectory);
                    n++;
                }

            } else {
                createTrainingLexicalEntries(path, outputDirectory);
                n++;

            }


            System.out.println(n + " files to be processed.");
            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingLexicalEntries(File path, String outputDirectory) throws Exception {
        // Calling previous cascading model 
        DictionaryBodySegmentationParser bodySegmentationParser = new DictionaryBodySegmentationParser();
        DictionaryDocument doc = bodySegmentationParser.processing(path);

        //Writing feature file
        String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.lexicalEntry";
        Writer featureWriter = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");

        StringBuffer rawtxt = new StringBuffer();

        StringBuffer lexicalEntries = new StringBuffer();
        for (List<LayoutToken> lexicalEntryLayoutTokens : doc.getLexicalEntries()) {

            for (LayoutToken txtline : lexicalEntryLayoutTokens) {
                rawtxt.append(txtline.getText());
            }

            lexicalEntries.append("<entry>");
            LayoutTokenization layoutTokenization = new LayoutTokenization(lexicalEntryLayoutTokens);
            String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization()).toString();
            String labeledFeatures = null;
            // if featSeg is null, it usually means that no body segment is found in the

            if ((featSeg != null) && (featSeg.trim().length() > 0)) {
                featureWriter.write(featSeg);

                labeledFeatures = label(featSeg);
                lexicalEntries.append(toTEILexicalEntry(labeledFeatures, layoutTokenization.getTokenization(), true));
            }
            lexicalEntries.append("</entry>");
        }

        //Writing RAW file (only text)
        String outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.lexicalEntry.rawtxt";
        FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");


        // write the TEI file
        String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.lexicalEntry.tei.xml";
        Writer teiWriter = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
        teiWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<tei>\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                "\"/>\n\t</teiHeader>\n\t<text xml:lang=\"en\">");
        teiWriter.write("\n\t\t<headnote>");
        teiWriter.write(DocumentUtils.replaceLinebreaksWithTags(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL).toString()));
        teiWriter.write("</headnote>");
        teiWriter.write("\n\t\t<body>");
        teiWriter.write(lexicalEntries.toString());
        teiWriter.write("</body>");
        teiWriter.write("\n\t\t<footnote>");
        teiWriter.write(DocumentUtils.replaceLinebreaksWithTags(doc.getDictionaryDocumentPartText(DictionarySegmentationLabels.DICTIONARY_FOOTNOTE_LABEL).toString()));
        teiWriter.write("</footnote>");
        teiWriter.write("\n\t</text>\n</tei>\n");


        IOUtils.closeWhileHandlingException(featureWriter, teiWriter);
    }
}
