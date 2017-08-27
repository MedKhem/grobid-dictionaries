package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;

import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.PUNCTUATION_LABEL;

/**
 * Created by Med on 26.08.17.
 */
public class EtymLabels extends TaggingLabels {

    private EtymLabels() {
        super();
    }

    public static final String ATTESTED_FORM_ETYM_LABEL = "<attForm>";
    public static final String ETYM_RELATION_ETYM_LABEL = "<etymRel>";
    public static final String SEG_ETYM_LABEL = "<seg>";
    public static final String LITERARY_CITATION_ETYM_LABEL = "<litCitation>";

    public static final TaggingLabel ETYM_ATTESTED_FORM = new TaggingLabelImpl(DictionaryModels.ETYM, ATTESTED_FORM_ETYM_LABEL);
    public static final TaggingLabel ETYM_ETYM_RELATION = new TaggingLabelImpl(DictionaryModels.ETYM, ETYM_RELATION_ETYM_LABEL);
    public static final TaggingLabel ETYM_SEG = new TaggingLabelImpl(DictionaryModels.ETYM, SEG_ETYM_LABEL);
    public static final TaggingLabel ETYM_LITERARY_CITATION = new TaggingLabelImpl(DictionaryModels.ETYM, LITERARY_CITATION_ETYM_LABEL);
    public static final TaggingLabel FORM_OTHER = new TaggingLabelImpl(DictionaryModels.ETYM, OTHER_LABEL);
    public static final TaggingLabel FORM_PUNCTUATION = new TaggingLabelImpl(DictionaryModels.ETYM, PUNCTUATION_LABEL);

    static {
        register(ETYM_ATTESTED_FORM);
        register(ETYM_ETYM_RELATION);
        register(ETYM_SEG);
        register(ETYM_LITERARY_CITATION);
        register(FORM_PUNCTUATION);
        register(FORM_OTHER);
    }
}
