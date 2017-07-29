package org.grobid.core.engines;

import org.grobid.core.data.LabeledLexicalInformation;
import org.grobid.core.data.SimpleLabeled;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.features.FeatureVectorForm;
import org.grobid.core.features.FeaturesUtils;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.grobid.core.document.TEIDictionaryFormatter.createMyXMLString;

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

    public StringBuilder processToTEI(List<LayoutToken> formEntry) {
        //This method is used by the parent parser to get the TEI to include the general TEI output
        StringBuilder featureMatrix = new StringBuilder();
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
                    lineStatus, fontStatus);

            featureMatrix.append(featureVectorForm.printVector() + "\n");
        }

        String features = featureMatrix.toString();
        String output = label(features);


        LabeledLexicalInformation labeledForm = process(output, formEntry);

        StringBuilder sb = new StringBuilder();

        sb.append("<form>").append("\n");
        StringBuilder gramGrp = new StringBuilder();
        for (Pair<List<LayoutToken>, String> entryForm : labeledForm.getLabels()) {
            String tokenForm = LayoutTokensUtil.normalizeText(entryForm.getA());
            String labelForm = entryForm.getB();

            String content = TextUtilities.HTMLEncode(tokenForm);
            content = content.replace("&lt;lb/&gt;", "<lb/>");
            if (!labelForm.equals("<gramGrp>")) {
                sb.append(createMyXMLString(labelForm.replaceAll("[<>]", ""), content));
            } else if (labelForm.equals("<gramGrp>")) {
                gramGrp.append(createMyXMLString(labelForm.replaceAll("[<>]", ""), content));
            }
        }
        sb.append("</form>").append("\n");
        sb.append(gramGrp.toString()).append("\n");


        return sb;

    }

    public LabeledLexicalInformation process(String modelOutput, List<LayoutToken> layoutTokens) {
        //This method is used by the parent parser to feed a following parser with a cluster of layout tokens
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.FORM,
                modelOutput, layoutTokens);

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

            labelledLayoutTokens.addLabel(new Pair(concatenatedTokens,tagLabel));

        }

        return labelledLayoutTokens;

    }
}