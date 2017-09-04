package org.grobid.core.engines;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.LabeledLexicalInformation;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.features.FeatureVectorForm;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import org.grobid.core.features.FeaturesUtils;
import org.grobid.core.features.enums.LineStatus;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.tokenization.TaggingTokenCluster;
import org.grobid.core.tokenization.TaggingTokenClusteror;
import org.grobid.core.utilities.LayoutTokensUtil;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.grobid.core.document.TEIDictionaryFormatter.createMyXMLString;
import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL;
import static org.grobid.service.DictionaryPaths.PATH_FULL_DICTIONARY;
import static org.grobid.service.DictionaryPaths.PATH_LEXICAL_ENTRY;

/**
 * Created by Med on 26.08.17.
 */
public class EtymParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FormParser.class);
    private static volatile FormParser instance;

    public EtymParser() {
        super(DictionaryModels.ETYM);
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

    public StringBuilder processToTei(List<LayoutToken> etymEntry) {
        LabeledLexicalInformation labeledSense = process(etymEntry, PATH_FULL_DICTIONARY);
        StringBuilder sb = new StringBuilder();

        sb.append("<etym>").append("\n");
        //I apply the form also to the sense to recognise the grammatical group, if any!

        for (Pair<List<LayoutToken>, String> entrySense : labeledSense.getLabels()) {
            String tokenSense = LayoutTokensUtil.normalizeText(entrySense.getA());
            String labelSense = entrySense.getB();

            String content = TextUtilities.HTMLEncode(tokenSense);
            content = content.replace("&lt;lb/&gt;", "<lb/>");

            sb.append(createMyXMLString(labelSense.replaceAll("[<>]", ""), content));

        }
        sb.append("</etym>").append("\n");
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

                labeledLexicalEntry.addLabel(new Pair(concatenatedTokens, tagLabel));
            }
        }


        return labeledLexicalEntry;
    }
}