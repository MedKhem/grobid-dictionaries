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
    public static final String GRAMMATICAL_GROUP_FORM_LABEL = "<gramGrp>";
    public static final String LEXICAL_ENTRY_INFLECTED_LABEL = "<inflected>";
    public static final String LEXICAL_ENTRY_ENDING_LABEL = "<ending>";
    public static final String LEXICAL_ENTRY_VARIANT_LABEL = "<variant>";
    public static final String LEXICAL_ENTRY_PRON_LABEL = "<pron>";
    public static final String LANG_FORM_LABEL = "<lang>";
    public static final String NAME_FROM_LABEL = "<name>";
    public static final String DESC_FROM_LABEL= "<desc>";
    public static final String NOTE_FROM_LABEL= "<note>";
    public static final String USG_FROM_LABEL= "<usg>";
    public static final String LBL_FROM_LABEL= "<lbl>";
    public static final String MENTIONED_FROM_LABEL= "<mentioned>";
    public static final String XR_FROM_LABEL= "<xr>";
    public static final String DICTSCRAP_FORM_LABEL = "<dictScrap>";
    public static final String PC_FORM_LABEL = "<pc>";

    public static final TaggingLabel FORM_ORTHOGRAPHY = new TaggingLabelImpl(DictionaryModels.FORM, ORTHOGRAPHY_FORM_LABEL);
    public static final TaggingLabel FORM_PART = new TaggingLabelImpl(DictionaryModels.FORM, PART_FORM_LABEL);
    public static final TaggingLabel FORM_PRONUNCIATION = new TaggingLabelImpl(DictionaryModels.FORM, PRONUNCIATION_FORM_LABEL);
    public static final TaggingLabel FORM_GRAMMATICAL_GROUP = new TaggingLabelImpl(DictionaryModels.FORM, GRAMMATICAL_GROUP_FORM_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_INFLECTED = new TaggingLabelImpl(DictionaryModels.FORM, LEXICAL_ENTRY_INFLECTED_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_ENDING = new TaggingLabelImpl(DictionaryModels.FORM, LEXICAL_ENTRY_ENDING_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_VARIANT = new TaggingLabelImpl(DictionaryModels.FORM, LEXICAL_ENTRY_VARIANT_LABEL);
    public static final TaggingLabel FORM_LANG = new TaggingLabelImpl(DictionaryModels.FORM, LANG_FORM_LABEL);
    public static final TaggingLabel FORM_NAME = new TaggingLabelImpl(DictionaryModels.FORM, NAME_FROM_LABEL);
    public static final TaggingLabel FORM_NOTE = new TaggingLabelImpl(DictionaryModels.FORM,  NOTE_FROM_LABEL );
    public static final TaggingLabel FORM_USG = new TaggingLabelImpl(DictionaryModels.FORM, USG_FROM_LABEL);
    public static final TaggingLabel FORM_DESC = new TaggingLabelImpl(DictionaryModels.FORM, DESC_FROM_LABEL );
    public static final TaggingLabel FORM_LBL = new TaggingLabelImpl(DictionaryModels.FORM, LBL_FROM_LABEL);
    public static final TaggingLabel FORM_DICTSCRAP = new TaggingLabelImpl(DictionaryModels.FORM, DICTSCRAP_FORM_LABEL);
    public static final TaggingLabel FORM_PUNCTUATION = new TaggingLabelImpl(DictionaryModels.FORM, PC_FORM_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_PRON = new TaggingLabelImpl(DictionaryModels.FORM, LEXICAL_ENTRY_PRON_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_MENTIONED = new TaggingLabelImpl(DictionaryModels.FORM, MENTIONED_FROM_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_XR = new TaggingLabelImpl(DictionaryModels.FORM, XR_FROM_LABEL);

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
        register(LEXICAL_ENTRY_INFLECTED);
        register(LEXICAL_ENTRY_ENDING);
        register(LEXICAL_ENTRY_VARIANT);
        register(FORM_PUNCTUATION);
        register(FORM_DICTSCRAP);
        register(LEXICAL_ENTRY_PRON);
        register(LEXICAL_ENTRY_MENTIONED);
        register(LEXICAL_ENTRY_XR);
    }
    
}
