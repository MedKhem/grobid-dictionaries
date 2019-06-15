package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;

/**
 * Created by lfoppiano on 08/05/2017.
 */
public class GrammaticalGroupLabels extends TaggingLabels {

    private GrammaticalGroupLabels() {
        super();
    }

    public static final String POS_GRMGROUP_LABEL = "<pos>";
    public static final String GRAM_GRMGROUP_LABEL = "<gram>";
    public static final String TNS_GRMGROUP_LABEL = "<tns>";
    public static final String GEN_GRMGROUP_LABEL = "<gen>";
    public static final String NUMBER_GRMGROUP_LABEL = "<number>";
    public static final String SUBC_GRMGROUP_LABEL = "<subc>";
    public static final String LBL_GRMGROUP_LABEL = "<lbl>";
    public static final String NOTE_GRMGROUP_LABEL = "<note>";
    public static final String OREF_GRMGROUP_LABEL = "<oRef>";
    public static final String PC_GRMGROUP_LABEL = "<pc>";
    public static final String DICTIONARY_DICTSCRAP_LABEL = "<dictScrap>";
    public static final TaggingLabel GRAMMATICAL_GROUP_POS= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, POS_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_GRAM= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, GRAM_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_TNS= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, TNS_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_GEN = new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, GEN_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_OTHER = new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, DICTIONARY_DICTSCRAP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_NUMBER= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, NUMBER_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_SUBC= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, SUBC_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_LBL= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, LBL_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_NOTE= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, NOTE_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_OREF= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, OREF_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_PC= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, PC_GRMGROUP_LABEL);

    static {
        register(GRAMMATICAL_GROUP_POS);
        register(GRAMMATICAL_GROUP_GRAM);
        register(GRAMMATICAL_GROUP_TNS);
        register(GRAMMATICAL_GROUP_GEN);
        register(GRAMMATICAL_GROUP_OTHER);
        register(GRAMMATICAL_GROUP_NUMBER);
        register(GRAMMATICAL_GROUP_SUBC);
        register(GRAMMATICAL_GROUP_LBL);
        register(GRAMMATICAL_GROUP_NOTE);
        register(GRAMMATICAL_GROUP_OREF);
        register(GRAMMATICAL_GROUP_PC);
    }
}