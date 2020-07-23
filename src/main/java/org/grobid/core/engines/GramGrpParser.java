package org.grobid.core.engines;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.data.LabeledLexicalInformation;
import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.document.DocumentUtils;
import org.grobid.core.engines.label.DictionaryBodySegmentationLabels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.features.FeaturesUtils;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL;
import static org.grobid.core.engines.label.FormLabels.GRAMMATICAL_GROUP_FORM_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.*;
import static org.grobid.core.engines.label.SenseLabels.GRAMMATICAL_GROUP_SENSE_LABEL;
import static org.grobid.core.engines.label.SenseLabels.SUBSENSE_SENSE_LABEL;
import static org.grobid.core.engines.label.SubSenseLabels.SUB_SENSE_XR_LABEL;

/**
 * Created by Med on 30.04.19.
 */
public class GramGrpParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GramGrpParser.class);
    private static volatile GramGrpParser instance;
    private DocumentUtils formatter = new DocumentUtils();

    public GramGrpParser() {
        super(DictionaryModels.GRAMMATICAL_GROUP);
    }


    public static GramGrpParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new GramGrpParser();
    }

    public StringBuilder processToTEI(List<LayoutToken> aGramGrp) {
        //This method is used by the parent parser to get the TEI to include the general TEI output


        LabeledLexicalInformation gramGrpComponents = process(aGramGrp);

        StringBuilder sb = new StringBuilder();


        sb.append("<gramGrp>").append("\n");
        for (Pair<List<LayoutToken>, String> gramGrpComponent : gramGrpComponents.getLabels()) {
            String gramGrpComponentText = LayoutTokensUtil.normalizeText(gramGrpComponent.getLeft());
            String gramGrpComponentLabel = gramGrpComponent.getRight();

            String content = DocumentUtils.escapeHTMLCharac(gramGrpComponentText);

                sb.append(formatter.createMyXMLString(gramGrpComponentLabel, null, content));

        }


        sb.append("</gramGrp>").append("\n");
//        if (ref.length() > 0) {
//            sb.append(ref.toString()).append("\n");
//        }


        return sb;

    }

    public LabeledLexicalInformation process(List<LayoutToken> layoutTokens) {
        //This method is used by the parent parser to feed a following parser with a cluster of layout tokens

        StringBuilder featureMatrix = new StringBuilder();
        String previousFont = null;
        String fontStatus = null;
        String lineStatus = null;

        int counter = 0;
        int nbToken = layoutTokens.size();
        for (LayoutToken token : layoutTokens) {
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
                previousTokenText = layoutTokens.get(counter - 1).getText();
                previousTokenIsNewLineAfter = layoutTokens.get(counter - 1).isNewLineAfter();
                nextTokenText = layoutTokens.get(counter + 1).getText();
                nextTokenIsNewLineAfter = layoutTokens.get(counter + 1).isNewLineAfter();

                // Check the existence of the afterNextToken
                if ((nbToken > counter + 2) && (layoutTokens.get(counter + 2) != null)) {
                    afterNextTokenIsNewLineAfter = layoutTokens.get(counter + 2).isNewLineAfter();
                }

                lineStatus = FeaturesUtils.checkLineStatus(text, previousTokenIsNewLineAfter, previousTokenText, nextTokenIsNewLineAfter, nextTokenText, afterNextTokenIsNewLineAfter);

            }
            counter++;

            String[] returnedFont = FeaturesUtils.checkFontStatus(token.getFont(), previousFont);
            previousFont = returnedFont[0];
            fontStatus = returnedFont[1];

            FeatureVectorLexicalEntry featureVectorForm = FeatureVectorLexicalEntry.addFeaturesLexicalEntries(token, "",
                    lineStatus, fontStatus);

            featureMatrix.append(featureVectorForm.printVector() + "\n");
        }

        String features = featureMatrix.toString();
        String output = label(features);

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.GRAMMATICAL_GROUP,
                output, layoutTokens);

        List<TaggingTokenCluster> clusters = clusteror.cluster();

        LabeledLexicalInformation labelledLayoutTokens = new LabeledLexicalInformation();
        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);
            String tagLabel = clusterLabel.getLabel();
            List<LayoutToken> concatenatedTokens = cluster.concatTokens();

            labelledLayoutTokens.addLabel(Pair.of(concatenatedTokens, tagLabel));

        }

        return labelledLayoutTokens;

    }

    public StringBuilder toTEIGramGrp(String bodyContentFeatured, List<LayoutToken> layoutTokens,
                                      boolean isTrainingData) {
        StringBuilder buffer = new StringBuilder();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.GRAMMATICAL_GROUP,
                bodyContentFeatured, layoutTokens);

        List<TaggingTokenCluster> clusters = clusteror.cluster();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);

            List<LayoutToken> list1 = cluster.concatTokens();
            String tagLabel = clusterLabel.getLabel();

            String clusterContent = LayoutTokensUtil.toText(list1);

            if (isTrainingData) {
                clusterContent = clusterContent.replace("&lt;lb/&gt;", "<lb/>");
                clusterContent = DocumentUtils.escapeHTMLCharac(clusterContent);
                clusterContent = DocumentUtils.replaceLinebreaksWithTags(clusterContent);
                formatter.produceXmlNodeForAnnotation(buffer, clusterContent, tagLabel, null);

            } else {
                clusterContent = LayoutTokensUtil.normalizeText(clusterContent);
                formatter.produceXmlNode(buffer, clusterContent, tagLabel, null);
            }


        }

        return buffer;
    }


    @SuppressWarnings({"UnusedParameters"})
    public int createTrainingBatch(String inputDirectory, String outputDirectory, String calledBy, Boolean isPDF) throws IOException {
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
                    createTrainingGramGrp(fileEntry, outputDirectory, false, calledBy, isPDF);
                    n++;
                }

            } else {
                createTrainingGramGrp(path, outputDirectory, false, calledBy, isPDF);
                n++;

            }


            System.out.println(n + " files to be processed.");
            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    public int createAnnotatedTrainingBatch(String inputDirectory, String outputDirectory, String calledBy, Boolean isPDF) throws IOException {
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
                    createTrainingGramGrp(fileEntry, outputDirectory, true, calledBy, isPDF);
                    n++;
                }

            } else {
                createTrainingGramGrp(path, outputDirectory, true, calledBy, isPDF);
                n++;

            }


            System.out.println(n + " files to be processed.");
            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingGramGrp(File path, String outputDirectory, Boolean isAnnotated, String calledBy, Boolean isPDF) throws Exception {
        // Calling previous cascading model
        DictionaryBodySegmentationParser bodySegmentationParser = new DictionaryBodySegmentationParser();
        DictionaryDocument doc = bodySegmentationParser.processing(path, isPDF);

        //Writing feature file
        String featuresFile="";
        if(calledBy.equals("lexical entry")){
            featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-LE.training.gramGrp";
        }else if(calledBy.equals("sense")){
            featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-sense.training.gramGrp";
        }else if(calledBy.equals("form")){
            featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-form.training.gramGrp";
        }

         Writer featureWriter = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");

        //Create rng and css files for guiding the annotation
        File existingRngFile = new File("templates/gramGrp.rng");
        File newRngFile = new File(outputDirectory + "/" + "gramGrp.rng");
        copyFileUsingStream(existingRngFile, newRngFile);

        File existingCssFile = new File("templates/gramGrp.css");
        File newCssFile = new File(outputDirectory + "/" + "gramGrp.css");
//        Files.copy(Gui.getClass().getResourceAsStream("templates/lexicalEntry.css"), Paths.get("new_project","css","lexicalEntry.css"))
        copyFileUsingStream(existingCssFile, newCssFile);


        StringBuffer rawtxt = new StringBuffer();

        StringBuffer gramGrps = new StringBuffer();
        LexicalEntryParser lexicalEntryParser = new LexicalEntryParser();
        for (Pair<List<LayoutToken>, String> lexicalEntryLayoutTokens : doc.getBodyComponents().getLabels()) {

            if (lexicalEntryLayoutTokens.getRight().equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                LabeledLexicalInformation lexicalEntryComponents = lexicalEntryParser.process(lexicalEntryLayoutTokens.getLeft(), DICTIONARY_ENTRY_LABEL);

                for (Pair<List<LayoutToken>, String> lexicalEntryComponent : lexicalEntryComponents.getLabels()) {
                    if ((lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_FROM_GRAMGRP_LABEL) ||
                            lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_FROM_GRAMGRP_LABEL) ) && calledBy.equals("lexical entry")) {
                        //Write raw text
                        for (LayoutToken txtline : lexicalEntryComponent.getLeft()) {
                            rawtxt.append(txtline.getText());
                        }
                        gramGrps.append("<gramGrp>");
                        LayoutTokenization layoutTokenization = new LayoutTokenization(lexicalEntryComponent.getLeft());
                        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization()).toString();
                        featureWriter.write(featSeg + "\n");
                        if (isAnnotated) {

                            String labeledFeatures = null;
                            // if featSeg is null, it usually means that no body segment is found in the

                            if ((featSeg != null) && (featSeg.trim().length() > 0)) {


                                labeledFeatures = label(featSeg);
                                gramGrps.append(toTEIGramGrp(labeledFeatures, layoutTokenization.getTokenization(), true));
                            }
                        } else {
                            gramGrps.append(DocumentUtils.replaceLinebreaksWithTags(DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(lexicalEntryComponent.getLeft()))));

                        }

                        gramGrps.append("</gramGrp>");
                    }
                    if ((lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_LEMMA_LABEL) ||
                            lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_INFLECTED_LABEL) ||
                            lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_ENDING_LABEL) ||
                            lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_VARIANT_LABEL)) && calledBy.equals("form")) {
                        FormParser formParser = new FormParser();

                        LabeledLexicalInformation formComponents = formParser.process(lexicalEntryComponent.getLeft());
                        for (Pair<List<LayoutToken>, String> formComponent : formComponents.getLabels()) {
                            if (formComponent.getRight().equals(GRAMMATICAL_GROUP_FORM_LABEL)) {


                                for (LayoutToken txtline : formComponent.getLeft()) {
                                    rawtxt.append(txtline.getText());
                                }
                                gramGrps.append("<gramGrp>");
                                LayoutTokenization layoutTokenization = new LayoutTokenization(formComponent.getLeft());
                                String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization()).toString();
                                featureWriter.write(featSeg + "\n");
                                if (isAnnotated) {

                                    String labeledFeatures = null;
                                    // if featSeg is null, it usually means that no body segment is found in the

                                    if ((featSeg != null) && (featSeg.trim().length() > 0)) {


                                        labeledFeatures = label(featSeg);
                                        gramGrps.append(toTEIGramGrp(labeledFeatures, layoutTokenization.getTokenization(), true));
                                    }
                                } else {
                                    gramGrps.append(DocumentUtils.replaceLinebreaksWithTags(DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(formComponent.getLeft()))));

                                }

                                gramGrps.append("</gramGrp>");


                            }


                        }


                    }
                    if (lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_SENSE_LABEL) && calledBy.equals("sense")) {
                        SenseParser senseParser = new SenseParser();

                        LabeledLexicalInformation senseComponents = senseParser.process(lexicalEntryComponent.getLeft());
                        for (Pair<List<LayoutToken>, String> senseComponent : senseComponents.getLabels()) {
                            if (senseComponent.getRight().equals(GRAMMATICAL_GROUP_SENSE_LABEL)) {


                                for (LayoutToken txtline : senseComponent.getLeft()) {
                                    rawtxt.append(txtline.getText());
                                }
                                gramGrps.append("<gramGrp>");
                                LayoutTokenization layoutTokenization = new LayoutTokenization(senseComponent.getLeft());
                                String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization()).toString();
                                featureWriter.write(featSeg + "\n");
                                if (isAnnotated) {

                                    String labeledFeatures = null;
                                    // if featSeg is null, it usually means that no body segment is found in the

                                    if ((featSeg != null) && (featSeg.trim().length() > 0)) {


                                        labeledFeatures = label(featSeg);
                                        gramGrps.append(toTEIGramGrp(labeledFeatures, layoutTokenization.getTokenization(), true));
                                    }
                                } else {
                                    gramGrps.append(DocumentUtils.replaceLinebreaksWithTags(DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(senseComponent.getLeft()))));

                                }

                                gramGrps.append("</gramGrp>");


                            }


                        }


                    }
                }


            }


        }

        //Writing RAW file (only text)

        String outPathRawtext="";
        if(calledBy.equals("lexical entry")){
            outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-LE.training.gramGrp.rawtxt";
        }else if(calledBy.equals("sense")){
            outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-sense.training.gramGrp.rawtxt";
        }else if(calledBy.equals("form")){
            outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-form.training.gramGrp.rawtxt";
        }



        FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");


        // write the TEI file
        String outTei="";
        if(calledBy.equals("lexical entry")){
            outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-LE.training.gramGrp.tei.xml";}else if(calledBy.equals("sense")){
            outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-sense.training.gramGrp.tei.xml";}else if(calledBy.equals("form")){
            outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-form.training.gramGrp.tei.xml";
        }
        Writer teiWriter = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
        teiWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<?xml-model href=\"gramGrp.rng\" type=\"application/xml\" schematypens=\"http://relaxng.org/ns/structure/1.0\"\n" +
                "?>\n" + "<?xml-stylesheet type=\"text/css\" href=\"gramGrp.css\"?>\n" +
                "<tei xml:space=\"preserve\">\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                "\"/>\n\t</teiHeader>\n\t<text>");
        teiWriter.write("\n\t\t<body>");
        teiWriter.write(gramGrps.toString().replaceAll("&", "&amp;"));
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

