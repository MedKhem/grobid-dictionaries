package org.grobid.core.engines;

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

    public SimpleLabeled process(List<LayoutToken> senseEntry) {

        StringBuilder sb = new StringBuilder();
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

            FeatureVectorSense featureVectorForm = FeatureVectorSense.addFeaturesSense(token, "",
                    lineStatus, fontStatus);

            sb.append(featureVectorForm.printVector() + "\n");
        }

        String features = sb.toString();
        String output = label(features);


        SimpleLabeled labeledSense = transformResponse(output, senseEntry);

        return labeledSense;

    }

    public SimpleLabeled transformResponse(String modelOutput, List<LayoutToken> layoutTokens) {
        TaggingTokenClusteror clusteror = new TaggingTokenClusteror(DictionaryModels.SENSE,
                modelOutput, layoutTokens);

        List<TaggingTokenCluster> clusters = clusteror.cluster();
        SimpleLabeled labeledSense = new SimpleLabeled();

        for (TaggingTokenCluster cluster : clusters) {
            if (cluster == null) {
                continue;
            }
            TaggingLabel clusterLabel = cluster.getTaggingLabel();
            Engine.getCntManager().i((TaggingLabel) clusterLabel);

            List<LayoutToken> concatenatedTokens = cluster.concatTokens();
            String text = LayoutTokensUtil.toText(concatenatedTokens);
            String tagLabel = clusterLabel.getLabel();

            labeledSense.addLabel(new Pair(text, tagLabel));
        }

        return labeledSense;

    }
}