package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;

/**
 * Created by lfoppiano on 05/05/2017.
 */
public class FormLabels extends TaggingLabels {

    private FormLabels() {
        super();
    }

    public static final String LEMMA_FORM_LABEL = "<lemma>";
    public static final String PREFIX_FORM_LABEL = "<prefix>";
    public static final String SUFFIX_FORM_LABEL = "<suffix>";
    public static final String DEF_FORM_LABEL = "<def>";
    public static final String GLOSS_FORM_LABEL = "<gloss>";
    public static final String XR_FORM_LABEL = "<xr>";
    public static final String USG_FORM_LABEL = "<usg>";
    public static final String DICTSCRAP_FORM_LABEL = "<dictScrap>";
    public static final String PC_FORM_LABEL = "<pc>";
    public static final String GRAMMATICAL_GROUP_FORM_LABEL = "<gramGrp>";


    public static final TaggingLabel FORM_LEMMA = new TaggingLabelImpl(DictionaryModels.FORM, LEMMA_FORM_LABEL);
    public static final TaggingLabel FORM_PREFIX = new TaggingLabelImpl(DictionaryModels.FORM, PREFIX_FORM_LABEL);
    public static final TaggingLabel FORM_SUFFIX = new TaggingLabelImpl(DictionaryModels.FORM, SUFFIX_FORM_LABEL);
    public static final TaggingLabel FORM_DEF = new TaggingLabelImpl(DictionaryModels.FORM, DEF_FORM_LABEL);
    public static final TaggingLabel FORM_GLOSS = new TaggingLabelImpl(DictionaryModels.FORM, GLOSS_FORM_LABEL);
    public static final TaggingLabel FORM_XR = new TaggingLabelImpl(DictionaryModels.FORM, XR_FORM_LABEL);
    public static final TaggingLabel FORM_USG = new TaggingLabelImpl(DictionaryModels.FORM, USG_FORM_LABEL);
    public static final TaggingLabel FORM_DICTSCRAP = new TaggingLabelImpl(DictionaryModels.FORM, DICTSCRAP_FORM_LABEL);
    public static final TaggingLabel FORM_PUNCTUATION = new TaggingLabelImpl(DictionaryModels.FORM, PC_FORM_LABEL);
    public static final TaggingLabel FORM_GRAMMATICAL_GROUP = new TaggingLabelImpl(DictionaryModels.FORM, GRAMMATICAL_GROUP_FORM_LABEL);


    static {
        register(FORM_LEMMA);
        register(FORM_PREFIX);
        register(FORM_SUFFIX);
        register(FORM_DEF);
        register(FORM_GLOSS);
        register(FORM_XR);
        register(FORM_USG);
        register(FORM_PUNCTUATION);
        register(FORM_DICTSCRAP);
        register(FORM_GRAMMATICAL_GROUP);
    }
    
}
