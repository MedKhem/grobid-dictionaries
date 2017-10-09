package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;

import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.PUNCTUATION_LABEL;

/**
 * Created by Med on 06.10.17.
 */
public class EtymQuoteLabels  extends TaggingLabels {

    private EtymQuoteLabels() {
        super();
    }
    public static final String SEG_ETYM_LABEL = "<seg>";
    public static final String QUOTE_ETYM_LABEL = "<quote>";
    public static final String DICTIONARY_DICTSCRAP_LABEL = "<dictScrap>";

    public static final TaggingLabel ETYM_SEG = new TaggingLabelImpl(DictionaryModels.ETYM, SEG_ETYM_LABEL);
    public static final TaggingLabel ETYM_QUOTE = new TaggingLabelImpl(DictionaryModels.ETYM, QUOTE_ETYM_LABEL);
    public static final TaggingLabel FORM_OTHER = new TaggingLabelImpl(DictionaryModels.ETYM, DICTIONARY_DICTSCRAP_LABEL);
    public static final TaggingLabel FORM_PUNCTUATION = new TaggingLabelImpl(DictionaryModels.ETYM, PUNCTUATION_LABEL);

    static {

        register(ETYM_SEG);
        register(ETYM_QUOTE);
        register(FORM_PUNCTUATION);
        register(FORM_OTHER);
    }
}
