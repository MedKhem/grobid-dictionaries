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
    public static final String TNS_GRMGROUP_LABEL = "<tns>";
    public static final String GEN_GRMGROUP_LABEL = "<gen>";
    public static final String NUMBER_GRMGROUP_LABEL = "<number>";

    public static final TaggingLabel GRAMMATICAL_GROUP_POS= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, POS_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_TNS= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, TNS_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_GEN = new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, GEN_GRMGROUP_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_OTHER = new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, OTHER_LABEL);
    public static final TaggingLabel GRAMMATICAL_GROUP_NUMBER= new TaggingLabelImpl(DictionaryModels.GRAMMATICAL_GROUP, NUMBER_GRMGROUP_LABEL);

    static {
        register(GRAMMATICAL_GROUP_POS);
        register(GRAMMATICAL_GROUP_TNS);
        register(GRAMMATICAL_GROUP_GEN);
        register(GRAMMATICAL_GROUP_OTHER);
        register(GRAMMATICAL_GROUP_NUMBER);
    }
}