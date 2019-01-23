package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import org.grobid.core.data.LabeledLexicalInformation;
import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.document.DocumentUtils;
import org.grobid.core.engines.label.*;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
//import org.grobid.core.utilities.Pair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

import static org.grobid.core.document.TEIDictionaryFormatter.createMyXMLString;
import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_ETYM_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_FORM_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_SENSE_LABEL;
import static org.grobid.service.DictionaryPaths.PATH_FULL_DICTIONARY;

/**
 * Created by Med on 26.08.17.
 */
public class EtymParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EtymParser.class);
    private static volatile EtymParser instance;

    public EtymParser() {
        super(DictionaryModels.ETYM);
    }


    public static EtymParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new EtymParser();
    }

    public StringBuilder processToTei(List<LayoutToken> etymEntry, String label) {
        LabeledLexicalInformation labeledSense = process(etymEntry, PATH_FULL_DICTIONARY);
        StringBuilder sb = new StringBuilder();

        if(label.equals("<quote>")) {
            sb.append("<quote>").append("\n");
        }
        //I apply the form also to the sense to recognise the grammatical group, if any!

        for (Pair<List<LayoutToken>, String> entrySense : labeledSense.getLabels()) {
            String tokenSense = LayoutTokensUtil.normalizeText(entrySense.getLeft());
            String labelSense = entrySense.getRight();

            String content = DocumentUtils.escapeHTMLCharac(tokenSense);
            content = content.replace("&lt;lb/&gt;", "<lb/>");

            if(labelSense.equals("<seg>")){
                sb.append(content);
            }
            else{
                sb.append(createMyXMLString(labelSense.replaceAll("[<>]", ""), content));
            }


        }
        if(label.equals("<quote>")) {
            sb.append("</quote>").append("\n");
        }
        return sb;

    }

    public LabeledLexicalInformation process(List<LayoutToken> etymEntry, String parentTag) {
        LabeledLexicalInformation labeledLexicalEntry = new LabeledLexicalInformation();

        LayoutTokenization layoutTokenization = new LayoutTokenization(etymEntry);

        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization(), DICTIONARY_ENTRY_LABEL).toString();

        if (StringUtils.isNotBlank(featSeg)) {
            // Run the lexical entry model to label the features
            String modelOutput = label(featSeg);
            TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.ETYM, modelOutput, etymEntry);

            List<TaggingTokenCluster> clusters = clusteror.cluster();

            for (TaggingTokenCluster cluster : clusters) {
                if (cluster == null) {
                    continue;
                }
                TaggingLabel clusterLabel = cluster.getTaggingLabel();
                Engine.getCntManager().i((TaggingLabel) clusterLabel);

                List<LayoutToken> concatenatedTokens = cluster.concatTokens();
                String tagLabel = clusterLabel.getLabel();

                labeledLexicalEntry.addLabel(Pair.of(concatenatedTokens, tagLabel));
            }
        }


        return labeledLexicalEntry;
    }

    public StringBuilder toTEIEtym(String bodyContentFeatured, List<LayoutToken> layoutTokens,
                                   boolean isTrainingData) {
        StringBuilder buffer = new StringBuilder();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.ETYM,
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

        clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
        clusterContent = DocumentUtils.escapeHTMLCharac(clusterContent);

        if (tagLabel.equals(LexicalEntryLabels.LEXICAL_ENTRY_OTHER_LABEL)) {
            buffer.append(createMyXMLString("dictScrap", clusterContent));
        } else if (tagLabel.equals(EtymQuoteLabels.ETYM_QUOTE_SEG)) {
            buffer.append(createMyXMLString("quote", clusterContent));
        } else if (tagLabel.equals(EtymQuoteLabels.ETYM_QUOTE_SEG)) {
            buffer.append(createMyXMLString("seg", clusterContent));
        } else if (tagLabel.equals(EtymLabels.SEG_ETYM_LABEL)) {
            buffer.append(createMyXMLString("seg", clusterContent));
        } else if (tagLabel.equals(EtymLabels.BIBL_ETYM_LABEL)) {
            buffer.append(createMyXMLString("bibl", clusterContent));
        } else if (tagLabel.equals(EtymLabels.DEF_ETYM_LABEL)) {
            buffer.append(createMyXMLString("def", clusterContent));
        } else if (tagLabel.equals(EtymLabels.MENTIONED_ETYM_LABEL)) {
            buffer.append(createMyXMLString("mentioned", clusterContent));
        } else if (tagLabel.equals(EtymLabels.LANG_ETYM_LABEL)) {
            buffer.append(createMyXMLString("lang", clusterContent));
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
                    createTrainingEtym(fileEntry, outputDirectory, false);
                    n++;
                }

            } else {
                createTrainingEtym(path, outputDirectory, false);
                n++;

            }


            System.out.println(n + " files to be processed.");
            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    public int createAnnotatedTrainingBatch(String inputDirectory, String outputDirectory) throws IOException {
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
                    createTrainingEtym(fileEntry, outputDirectory, true);
                    n++;
                }

            } else {
                createTrainingEtym(path, outputDirectory, true);
                n++;

            }


            System.out.println(n + " files to be processed.");
            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingEtym(File path, String outputDirectory, Boolean isAnnotated) throws Exception {
        // Calling previous cascading model
        DictionaryBodySegmentationParser bodySegmentationParser = new DictionaryBodySegmentationParser();
        DictionaryDocument doc = bodySegmentationParser.processing(path);

        //Writing feature file
        String featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.etym";
        Writer featureWriter = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");

        //Create rng and css files for guiding the annotation
        File existingRngFile = new File("templates/etym.rng");
        File newRngFile = new File(outputDirectory + "/" + "etym.rng");
        copyFileUsingStream(existingRngFile, newRngFile);

        File existingCssFile = new File("templates/etym.css");
        File newCssFile = new File(outputDirectory + "/" + "etym.css");
//        Files.copy(Gui.getClass().getResourceAsStream("templates/lexicalEntry.css"), Paths.get("new_project","css","lexicalEntry.css"))
        copyFileUsingStream(existingCssFile, newCssFile);


        StringBuffer rawtxt = new StringBuffer();

        StringBuffer etyms = new StringBuffer();
        LexicalEntryParser lexicalEntryParser = new LexicalEntryParser();
        EtymQuoteParser etymQuoteParser = new EtymQuoteParser();
        for (Pair<List<LayoutToken>, String> lexicalEntryLayoutTokens : doc.getBodyComponents().getLabels()) {

            if (lexicalEntryLayoutTokens.getRight().equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                LabeledLexicalInformation lexicalEntryComponents = lexicalEntryParser.process(lexicalEntryLayoutTokens.getLeft(), DICTIONARY_ENTRY_LABEL);

                for (Pair<List<LayoutToken>, String> lexicalEntryComponent : lexicalEntryComponents.getLabels()) {
                    if (lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_ETYM_LABEL)) {
                        etyms.append("<etym>");
                        LabeledLexicalInformation segOrQuoteComponents = etymQuoteParser.process(lexicalEntryComponent.getLeft(), EtymQuoteLabels.QUOTE__ETYMQUOTE_LABEL);
                        for (Pair<List<LayoutToken>, String> segOrQuoteComponent : segOrQuoteComponents.getLabels()) {
                            if (segOrQuoteComponent.getRight().equals(EtymQuoteLabels.QUOTE__ETYMQUOTE_LABEL)) {
                                //Write raw text
                                for (LayoutToken txtline : segOrQuoteComponent.getLeft()) {
                                    rawtxt.append(txtline.getText());
                                }
                                etyms.append("<quote>");
                                LayoutTokenization layoutTokenization = new LayoutTokenization(segOrQuoteComponent.getLeft());
                                String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization()).toString();
                                featureWriter.write(featSeg + "\n");
                                if (isAnnotated) {
                                    String labeledFeatures = null;
                                    // if featSeg is null, it usually means that no body segment is found in the

                                    if ((featSeg != null) && (featSeg.trim().length() > 0)) {


                                        labeledFeatures = label(featSeg);
                                        etyms.append(toTEIEtym(labeledFeatures, layoutTokenization.getTokenization(), true));
                                    }
                                } else {
                                    etyms.append(DocumentUtils.replaceLinebreaksWithTags(DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(segOrQuoteComponent.getLeft()))));

                                }

                                etyms.append("</quote>");
                            } else if (segOrQuoteComponent.getRight().equals(EtymQuoteLabels.SEG_ETYMQUOTE_LABEL)) {
                                //Write raw text
                                for (LayoutToken txtline : segOrQuoteComponent.getLeft()) {
                                    rawtxt.append(txtline.getText());
                                }
                                etyms.append("<seg>");
                                LayoutTokenization layoutTokenization = new LayoutTokenization(segOrQuoteComponent.getLeft());
                                if (isAnnotated) {
                                    String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization()).toString();
                                    String labeledFeatures = null;
                                    // if featSeg is null, it usually means that no body segment is found in the

                                    if ((featSeg != null) && (featSeg.trim().length() > 0)) {
                                        featureWriter.write(featSeg + "\n");

                                        labeledFeatures = label(featSeg);
                                        etyms.append(toTEIEtym(labeledFeatures, layoutTokenization.getTokenization(), true));
                                    }
                                } else {
                                    etyms.append(DocumentUtils.replaceLinebreaksWithTags(LayoutTokensUtil.toText(segOrQuoteComponent.getLeft())));

                                }

                                etyms.append("</seg>");

                            }
                        }
                        etyms.append("</etym>");
                    }

                }


            }


        }

        //Writing RAW file (only text)
        String outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.etym.rawtxt";
        FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");


        // write the TEI file
        String outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + ".training.etym.tei.xml";
        Writer teiWriter = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
        teiWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<?xml-model href=\"etym.rng\" type=\"application/xml\" schematypens=\"http://relaxng.org/ns/structure/1.0\"\n" +
                "?>\n" + "<?xml-stylesheet type=\"text/css\" href=\"etym.css\"?>\n" +
                "<tei xml:space=\"preserve\">\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                "\"/>\n\t</teiHeader>\n\t<text>");
        teiWriter.write("\n\t\t<body>");
        teiWriter.write(etyms.toString().replaceAll("&","&amp;"));
        teiWriter.write("</body>");
        teiWriter.write("\n\t</text>\n</tei>\n");


        IOUtils.closeQuietly(featureWriter, teiWriter);
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