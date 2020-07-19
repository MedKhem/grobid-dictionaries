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
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_SENSE_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_SUB_ENTRY_LABEL;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_XR_LABEL;
import static org.grobid.core.engines.label.SenseLabels.SUBSENSE_SENSE_LABEL;
import static org.grobid.core.engines.label.SubSenseLabels.SUB_SENSE_XR_LABEL;

/**
 * Created by Med on 26.04.19.
 */
public class CrossRefParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrossRefParser.class);
    private static volatile CrossRefParser instance;
    private DocumentUtils formatter = new DocumentUtils();

    public CrossRefParser() {
        super(DictionaryModels.CROSS_REF);
    }


    public static CrossRefParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new CrossRefParser();
    }

    public StringBuilder processToTEI(List<LayoutToken> crossRefSequence ) {
        //This method is used by the parent parser to get the TEI to include the general TEI output


        LabeledLexicalInformation labeledForm = process(crossRefSequence);

        StringBuilder sb = new StringBuilder();
        StringBuilder ref = new StringBuilder();
        boolean relationshipSpecified = false;
        String relation="";


        for (Pair<List<LayoutToken>, String> entryForm : labeledForm.getLabels()) {
            String tokenForm = LayoutTokensUtil.normalizeText(entryForm.getLeft());
            String labeledCrossRefComponent = entryForm.getRight();

            String content = DocumentUtils.escapeHTMLCharac(tokenForm);
//            if (labelForm.equals("<relation>") && !relationshipSpecified) {
//                relation = content;
//                relationshipSpecified = true;
//
//            }else {
                sb.append(formatter.createMyXMLString(labeledCrossRefComponent, null, content));
//            }
        }
//        String attributes="";
//        if (relationshipSpecified){
//             attributes= "type-"+relation;
//            sb.append(formatter.createMyXMLString("<xr>", attributes, ref.toString()));
//        }else {
//            sb.append(formatter.createMyXMLString("<xr>", attributes, ref.toString()));
//        }

//        ref.append("</form>").append("\n");
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

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.CROSS_REF,
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

    public StringBuilder toTEICrossRef(String bodyContentFeatured, List<LayoutToken> layoutTokens,
                                       boolean isTrainingData) {
        StringBuilder buffer = new StringBuilder();

        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.CROSS_REF,
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
                formatter.produceXmlNode(buffer, clusterContent, tagLabel,null);
            }





        }

        return buffer;
    }




    @SuppressWarnings({"UnusedParameters"})
    public int createTrainingBatch(String inputDirectory, String outputDirectory, String calledBy) throws IOException {
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
                    createTrainingCrossRef(fileEntry, outputDirectory, false, calledBy);
                    n++;
                }

            } else {
                createTrainingCrossRef(path, outputDirectory, false, calledBy);
                n++;

            }


            System.out.println(n + " files to be processed.");
            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    @SuppressWarnings({"UnusedParameters"})
    public int createAnnotatedTrainingBatch(String inputDirectory, String outputDirectory, String calledBy) throws IOException {
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
                    createTrainingCrossRef(fileEntry, outputDirectory, true, calledBy);
                    n++;
                }

            } else {
                createTrainingCrossRef(path, outputDirectory, true, calledBy);
                n++;

            }


            System.out.println(n + " files to be processed.");
            return n;
        } catch (final Exception exp) {
            throw new GrobidException("An exception occurred while running Grobid batch.", exp);
        }
    }

    public void createTrainingCrossRef(File path, String outputDirectory, Boolean isAnnotated, String calledBy) throws Exception {
        // Calling previous cascading model
        DictionaryBodySegmentationParser bodySegmentationParser = new DictionaryBodySegmentationParser();
        DictionaryDocument doc = bodySegmentationParser.processing(path);

        //Writing feature file
        String featuresFile="";
        if(calledBy.equals("lexical entry")){
            featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-LE.training.crossRef";
        }else if(calledBy.equals("sense")){
            featuresFile = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-sense.training.crossRef";
        }

        Writer featureWriter = new OutputStreamWriter(new FileOutputStream(new File(featuresFile), false), "UTF-8");

        //Create rng and css files for guiding the annotation
        File existingRngFile = new File("templates/crossRef.rng");
        File newRngFile = new File(outputDirectory + "/" + "crossRef.rng");
        copyFileUsingStream(existingRngFile, newRngFile);

        File existingCssFile = new File("templates/crossRef.css");
        File newCssFile = new File(outputDirectory + "/" + "crossRef.css");
//        Files.copy(Gui.getClass().getResourceAsStream("templates/lexicalEntry.css"), Paths.get("new_project","css","lexicalEntry.css"))
        copyFileUsingStream(existingCssFile, newCssFile);


        StringBuffer rawtxt = new StringBuffer();

        StringBuffer crossRefs = new StringBuffer();
        LexicalEntryParser lexicalEntryParser = new LexicalEntryParser();
        for (Pair<List<LayoutToken>, String> lexicalEntryLayoutTokens : doc.getBodyComponents().getLabels()) {

            if (lexicalEntryLayoutTokens.getRight().equals(DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL)) {
                LabeledLexicalInformation lexicalEntryComponents = lexicalEntryParser.process(lexicalEntryLayoutTokens.getLeft(), DICTIONARY_ENTRY_LABEL);

                for (Pair<List<LayoutToken>, String> lexicalEntryComponent : lexicalEntryComponents.getLabels()) {
                    if (lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_XR_LABEL) && calledBy.equals("lexical entry")) {
                        //Write raw text
                        for (LayoutToken txtline : lexicalEntryComponent.getLeft()) {
                            rawtxt.append(txtline.getText());
                        }
                        crossRefs.append("<xr>");
                        LayoutTokenization layoutTokenization = new LayoutTokenization(lexicalEntryComponent.getLeft());
                        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization()).toString();
                        featureWriter.write(featSeg + "\n");
                        if (isAnnotated) {

                            String labeledFeatures = null;
                            // if featSeg is null, it usually means that no body segment is found in the

                            if ((featSeg != null) && (featSeg.trim().length() > 0)) {


                                labeledFeatures = label(featSeg);
                                crossRefs.append(toTEICrossRef(labeledFeatures, layoutTokenization.getTokenization(), true));
                            }
                        } else {
                            crossRefs.append(DocumentUtils.replaceLinebreaksWithTags(DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(lexicalEntryComponent.getLeft()))));

                        }

                        crossRefs.append("</xr>");
                    }
                    if (lexicalEntryComponent.getRight().equals(LEXICAL_ENTRY_SENSE_LABEL) && calledBy.equals("sense")) {
                        SenseParser senseParser = new SenseParser();

                        LabeledLexicalInformation senseComponents = senseParser.process(lexicalEntryComponent.getLeft());
                        for (Pair<List<LayoutToken>, String> senseComponent : senseComponents.getLabels()) {
                            if(senseComponent.getRight().equals(SUBSENSE_SENSE_LABEL)){
                                SubSenseParser subSenseParser = new SubSenseParser();
                                LabeledLexicalInformation subSenseComponents = subSenseParser.process(senseComponent.getLeft());
                                for (Pair<List<LayoutToken>, String> subSenseComponent : subSenseComponents.getLabels()) {
                                    if (subSenseComponent.getRight().equals(SUB_SENSE_XR_LABEL)) {
                                        for (LayoutToken txtline : subSenseComponent.getLeft()) {
                                            rawtxt.append(txtline.getText());
                                        }
                                        crossRefs.append("<xr>");
                                        LayoutTokenization layoutTokenization = new LayoutTokenization(subSenseComponent.getLeft());
                                        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(layoutTokenization.getTokenization()).toString();
                                        featureWriter.write(featSeg + "\n");
                                        if (isAnnotated) {

                                            String labeledFeatures = null;
                                            // if featSeg is null, it usually means that no body segment is found in the

                                            if ((featSeg != null) && (featSeg.trim().length() > 0)) {


                                                labeledFeatures = label(featSeg);
                                                crossRefs.append(toTEICrossRef(labeledFeatures, layoutTokenization.getTokenization(), true));
                                            }
                                        } else {
                                            crossRefs.append(DocumentUtils.replaceLinebreaksWithTags(DocumentUtils.escapeHTMLCharac(LayoutTokensUtil.toText(subSenseComponent.getLeft()))));

                                        }

                                        crossRefs.append("</xr>");


                                    }

                                }

                            }


                        }

                    }
                }


            }


        }

        //Writing RAW file (only text)
        String outPathRawtext="";
        if(calledBy.equals("lexical entry")){
            outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-LE.training.crossRef.rawtxt";
        }else if(calledBy.equals("sense")){
            outPathRawtext = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-sense.training.crossRef.rawtxt";
        }
         FileUtils.writeStringToFile(new File(outPathRawtext), rawtxt.toString(), "UTF-8");


        // write the TEI file
        String outTei="";
        if(calledBy.equals("lexical entry")){
            outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-LE.training.crossRef.tei.xml";
        }else if(calledBy.equals("sense")){
            outTei = outputDirectory + "/" + path.getName().substring(0, path.getName().length() - 4) + "-sense.training.crossRef.tei.xml";
        }

        Writer teiWriter = new OutputStreamWriter(new FileOutputStream(new File(outTei), false), "UTF-8");
        teiWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + "<?xml-model href=\"crossRef.rng\" type=\"application/xml\" schematypens=\"http://relaxng.org/ns/structure/1.0\"\n" +
                "?>\n" + "<?xml-stylesheet type=\"text/css\" href=\"crossRef.css\"?>\n" +
                "<tei xml:space=\"preserve\">\n\t<teiHeader>\n\t\t<fileDesc xml:id=\"" +
                "\"/>\n\t</teiHeader>\n\t<text>");
        teiWriter.write("\n\t\t<body>");
        teiWriter.write(crossRefs.toString().replaceAll("&","&amp;"));
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