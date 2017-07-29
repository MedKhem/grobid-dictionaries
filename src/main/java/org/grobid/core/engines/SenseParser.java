package org.grobid.core.engines;

import org.grobid.core.data.LabeledLexicalInformation;
import org.grobid.core.data.SimpleLabeled;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.features.FeatureVectorSense;
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
import java.io.IOException;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.grobid.core.document.TEIDictionaryFormatter.createMyXMLString;

/**
 * Created by lfoppiano on 05/05/2017.
 */
public class SenseParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SenseParser.class);
    private static volatile SenseParser instance;

    public SenseParser() {
        super(DictionaryModels.SENSE);
    }


    public static SenseParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    private static synchronized void getNewInstance() {
        instance = new SenseParser();
    }

    public StringBuilder processToTEI(List<LayoutToken> senseEntry) {
        //This method is used by the parent parser to get the TEI to include the general TEI output
        StringBuilder featureMatrix = new StringBuilder();
        String previousFont = null;
        String fontStatus = null;
        String lineStatus = null;

        int counter = 0;
        int nbToken = senseEntry.size();
        for (LayoutToken token : senseEntry) {
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
                previousTokenText = senseEntry.get(counter - 1).getText();
                previousTokenIsNewLineAfter = senseEntry.get(counter - 1).isNewLineAfter();
                nextTokenText = senseEntry.get(counter + 1).getText();
                nextTokenIsNewLineAfter = senseEntry.get(counter + 1).isNewLineAfter();

                // Check the existence of the afterNextToken
                if ((nbToken > counter + 2) && (senseEntry.get(counter + 2) != null)) {
                    afterNextTokenIsNewLineAfter = senseEntry.get(counter + 2).isNewLineAfter();
                }

                lineStatus = FeaturesUtils.checkLineStatus(text, previousTokenIsNewLineAfter, previousTokenText, nextTokenIsNewLineAfter, nextTokenText, afterNextTokenIsNewLineAfter);

            }
            counter++;

            String[] returnedFont = FeaturesUtils.checkFontStatus(token.getFont(), previousFont);
            previousFont = returnedFont[0];
            fontStatus = returnedFont[1];

            FeatureVectorSense featureVectorSense = FeatureVectorSense.addFeaturesSense(token, "",
                    lineStatus, fontStatus);

            featureMatrix.append(featureVectorSense.printVector() + "\n");
        }

        String features = featureMatrix.toString();
        String output = label(features);


        LabeledLexicalInformation labeledSense = process(output, senseEntry);
        StringBuilder sb = new StringBuilder();

        sb.append("<sense>").append("\n");
        //I apply the form also to the sense to recognise the grammatical group, if any!

        for (Pair<List<LayoutToken>, String> entrySense : labeledSense.getLabels()) {
            String tokenSense = LayoutTokensUtil.normalizeText(entrySense.getA());
            String labelSense = entrySense.getB();

            String content = TextUtilities.HTMLEncode(tokenSense);
            content = content.replace("&lt;lb/&gt;", "<lb/>");

            sb.append(createMyXMLString(labelSense.replaceAll("[<>]", ""), content));

        }
        sb.append("</sense>").append("\n");
        return sb;

    }

    public LabeledLexicalInformation process(String modelOutput, List<LayoutToken> layoutTokens) {
        //This method is used by the parent parser to feed a following parser with a cluster of layout tokens
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.SENSE,
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