package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;

/**
 * Created by lfoppiano on 05/05/2017.
 */
public class FormLabels extends TaggingLabels {

    private FormLabels() {
        super();
    }

    public static final String ORTHOGRAPHY_FORM_LABEL = "<orth>";
    public static final String PART_FORM_LABEL = "<part>";
    public static final String PRONUNCIATION_FORM_LABEL = "<pron>";
    public static final String ENDING_FORM_LABEL = "<ending>";
    public static final String GRAMMATICAL_GROUP_FORM_LABEL = "<gramGrp>";
    public static final String LANG_FORM_LABEL = "<lang>";
    public static final String NAME_FROM_LABEL = "<name>";
    public static final String  DESC_FROM_LABEL= "<desc>";
    public static final String  NOTE_FROM_LABEL= "<note>";
    public static final String  USG_FROM_LABEL= "<usg>";
    public static final String  LBL_FROM_LABEL= "<lbl>";
    public static final String DICTSCRAP_FORM_LABEL = "<dictScrap>";
    public static final String PC_FORM_LABEL = "<pc>";

    public static final TaggingLabel FORM_ORTHOGRAPHY = new TaggingLabelImpl(DictionaryModels.FORM, ORTHOGRAPHY_FORM_LABEL);
    public static final TaggingLabel FORM_PART = new TaggingLabelImpl(DictionaryModels.FORM, PART_FORM_LABEL);
    public static final TaggingLabel FORM_ENDING = new TaggingLabelImpl(DictionaryModels.FORM, ENDING_FORM_LABEL);
    public static final TaggingLabel FORM_PRONUNCIATION = new TaggingLabelImpl(DictionaryModels.FORM, PRONUNCIATION_FORM_LABEL);
    public static final TaggingLabel FORM_GRAMMATICAL_GROUP = new TaggingLabelImpl(DictionaryModels.FORM, GRAMMATICAL_GROUP_FORM_LABEL);
    public static final TaggingLabel FORM_LANG = new TaggingLabelImpl(DictionaryModels.FORM, LANG_FORM_LABEL);
    public static final TaggingLabel FORM_NAME = new TaggingLabelImpl(DictionaryModels.FORM, NAME_FROM_LABEL);
    public static final TaggingLabel FORM_NOTE = new TaggingLabelImpl(DictionaryModels.FORM,  NOTE_FROM_LABEL );
    public static final TaggingLabel FORM_USG = new TaggingLabelImpl(DictionaryModels.FORM, USG_FROM_LABEL);
    public static final TaggingLabel FORM_DESC = new TaggingLabelImpl(DictionaryModels.FORM, DESC_FROM_LABEL );
    public static final TaggingLabel FORM_LBL = new TaggingLabelImpl(DictionaryModels.FORM, LBL_FROM_LABEL);
    public static final TaggingLabel FORM_DICTSCRAP = new TaggingLabelImpl(DictionaryModels.FORM, DICTSCRAP_FORM_LABEL);
    public static final TaggingLabel FORM_PUNCTUATION = new TaggingLabelImpl(DictionaryModels.FORM, PC_FORM_LABEL);

    static {
        register(FORM_ORTHOGRAPHY);
        register(FORM_PART);
        register(FORM_PRONUNCIATION);
        register(FORM_GRAMMATICAL_GROUP);
        register(FORM_LANG);
        register(FORM_NAME);
        register(FORM_DESC);
        register(FORM_NOTE);
        register(FORM_USG);
        register(FORM_LBL);
        register(FORM_PUNCTUATION);
        register(FORM_DICTSCRAP);
        register(FORM_ENDING);
    }
    
}
