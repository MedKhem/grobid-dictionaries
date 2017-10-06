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

    public static final String MENTIONED_ETYM_LABEL = "<mentioned>";
    public static final String LANG_ETYM_LABEL = "<lang>";
    public static final String SEG_ETYM_LABEL = "<seg>";
    public static final String DEF_ETYM_LABEL = "<def>";
    //public static final String QUOTE_ETYM_LABEL = "<quote>";
    public static final String BIBL_ETYM_LABEL = "<bibl>";

    public static final TaggingLabel ETYM_MENTIONED = new TaggingLabelImpl(DictionaryModels.ETYM, MENTIONED_ETYM_LABEL);
    public static final TaggingLabel ETYM_LANG = new TaggingLabelImpl(DictionaryModels.ETYM, LANG_ETYM_LABEL);
    public static final TaggingLabel ETYM_SEG = new TaggingLabelImpl(DictionaryModels.ETYM, SEG_ETYM_LABEL);
    public static final TaggingLabel ETYM_DEF = new TaggingLabelImpl(DictionaryModels.ETYM, DEF_ETYM_LABEL);
   // public static final TaggingLabel ETYM_CIT = new TaggingLabelImpl(DictionaryModels.ETYM, CIT_ETYM_LABEL);
    //public static final TaggingLabel ETYM_QUOTE = new TaggingLabelImpl(DictionaryModels.ETYM, QUOTE_ETYM_LABEL);
    public static final TaggingLabel ETYM_BIBL = new TaggingLabelImpl(DictionaryModels.ETYM, BIBL_ETYM_LABEL);
    public static final TaggingLabel FORM_OTHER = new TaggingLabelImpl(DictionaryModels.ETYM, OTHER_LABEL);
    public static final TaggingLabel FORM_PUNCTUATION = new TaggingLabelImpl(DictionaryModels.ETYM, PUNCTUATION_LABEL);

    static {
        register(ETYM_MENTIONED);
        register(ETYM_LANG);
        register(ETYM_SEG);
        register(ETYM_DEF);
       // register(ETYM_CIT);
        //register(ETYM_QUOTE);
        register(ETYM_BIBL);
        register(FORM_PUNCTUATION);
        register(FORM_OTHER);
    }
}
