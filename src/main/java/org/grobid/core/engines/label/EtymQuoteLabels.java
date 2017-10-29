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
    public static final String SEG_ETYMQUOTE_LABEL = "<seg>";
    public static final String QUOTE__ETYMQUOTE_LABEL = "<quote>";
    public static final String DICTSCRAP_ETYMQUOTE_LABEL = "<dictScrap>";

    public static final TaggingLabel ETYM_QUOTE_SEG = new TaggingLabelImpl(DictionaryModels.ETYM_QUOTE, SEG_ETYMQUOTE_LABEL);
    public static final TaggingLabel ETYM_QUOTE_QUOTE = new TaggingLabelImpl(DictionaryModels.ETYM_QUOTE, QUOTE__ETYMQUOTE_LABEL);
    public static final TaggingLabel ETYM_QUOTEM_OTHER = new TaggingLabelImpl(DictionaryModels.ETYM_QUOTE, DICTSCRAP_ETYMQUOTE_LABEL);
    public static final TaggingLabel ETYM_QUOTE_PUNCTUATION = new TaggingLabelImpl(DictionaryModels.ETYM_QUOTE, PUNCTUATION_LABEL);

    static {

        register(ETYM_QUOTE_SEG);
        register(ETYM_QUOTE_QUOTE);
        register(ETYM_QUOTEM_OTHER);
        register(ETYM_QUOTE_PUNCTUATION);
    }
}
