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
    public static final String FIRST_LEMMA_FORM_LABEL = "<firstLemma>";
    public static final String EXAMPLE_FORM_LABEL = "<example>";
    public static final String DEF_FORM_LABEL = "<def>";
    public static final String GLOSS_FORM_LABEL = "<variant>";
    public static final String SYN_XR_FORM_LABEL = "<synRef>";
    public static final String ANT_XR_FORM_LABEL = "<antRef>";
    public static final String USG_FORM_LABEL = "<usg>";
    public static final String DICTSCRAP_FORM_LABEL = "<dictScrap>";
    public static final String PC_FORM_LABEL = "<pc>";
    public static final String GRAMMATICAL_GROUP_FORM_LABEL = "<gramGrp>";
    public static final String SOURCE_FORM_LABEL = "<source>";


    public static final TaggingLabel FORM_LEMMA = new TaggingLabelImpl(DictionaryModels.FORM, LEMMA_FORM_LABEL);
    public static final TaggingLabel FORM_FIRST_LEMMA = new TaggingLabelImpl(DictionaryModels.FORM, FIRST_LEMMA_FORM_LABEL);
    public static final TaggingLabel FORM_SOURCE = new TaggingLabelImpl(DictionaryModels.FORM, SOURCE_FORM_LABEL);
    public static final TaggingLabel FORM_DEF = new TaggingLabelImpl(DictionaryModels.FORM, DEF_FORM_LABEL);
    public static final TaggingLabel FORM_EXAMPLE = new TaggingLabelImpl(DictionaryModels.FORM, EXAMPLE_FORM_LABEL);

    public static final TaggingLabel FORM_GLOSS = new TaggingLabelImpl(DictionaryModels.FORM, GLOSS_FORM_LABEL);
    public static final TaggingLabel FORM_XR_SYN = new TaggingLabelImpl(DictionaryModels.FORM, SYN_XR_FORM_LABEL);
    public static final TaggingLabel FORM_XR_ANT = new TaggingLabelImpl(DictionaryModels.FORM, ANT_XR_FORM_LABEL);
    public static final TaggingLabel FORM_USG = new TaggingLabelImpl(DictionaryModels.FORM, USG_FORM_LABEL);
    public static final TaggingLabel FORM_DICTSCRAP = new TaggingLabelImpl(DictionaryModels.FORM, DICTSCRAP_FORM_LABEL);
    public static final TaggingLabel FORM_PUNCTUATION = new TaggingLabelImpl(DictionaryModels.FORM, PC_FORM_LABEL);
    public static final TaggingLabel FORM_GRAMMATICAL_GROUP = new TaggingLabelImpl(DictionaryModels.FORM, GRAMMATICAL_GROUP_FORM_LABEL);


    static {
        register(FORM_LEMMA);
        register(FORM_FIRST_LEMMA);
        register(FORM_SOURCE);
        register(FORM_EXAMPLE);
//        register(FORM_PREFIX);
//        register(FORM_SUFFIX);
        register(FORM_DEF);
        register(FORM_GLOSS);
        register(FORM_XR_SYN);
        register(FORM_XR_ANT);
        register(FORM_USG);
        register(FORM_PUNCTUATION);
        register(FORM_DICTSCRAP);
        register(FORM_GRAMMATICAL_GROUP);
    }
    
}
