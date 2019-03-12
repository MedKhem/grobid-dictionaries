package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;

/**
 * Created by Med on 11.03.19.
 */
public class SubSenseLabels extends TaggingLabels {


    private SubSenseLabels() {
        super();
    }
    public static final String SUB_SENSE_DEF_LABEL = "<def>";
    public static final String SUB_SENSE_EXAMPLE_LABEL = "<example>";
    public static final String SUB_SENSE_TRANSLATION_LABEL = "<translation>";
    public static final String SUB_SENSE_USAGE_LABEL = "<usg>";
    public static final String SUB_SENSE_RE_LABEL = "<re>";
    public static final String SUB_SENSE_ETYM_LABEL = "<etym>";
    public static final String SUB_SENSE_XR_LABEL = "<xr>";
    public static final String SUB_SENSE_LBL_LABEL = "<lbl>";
    public static final String SUB_SENSE_DICTSCRAP_LABEL = "<dictScrap>";
    public static final String SUB_SENSE_PC_LABEL = "<pc>";

    public static final TaggingLabel SUB_SENSE_DEF = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_DEF_LABEL);
    public static final TaggingLabel SUB_SENSE_EXAMPLE = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_EXAMPLE_LABEL);
    public static final TaggingLabel SUB_SENSE_TRANSLATION = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_TRANSLATION_LABEL);
    public static final TaggingLabel SUB_SENSE_USAGE = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_USAGE_LABEL);
    public static final TaggingLabel SUB_SENSE_RE = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_RE_LABEL);
    public static final TaggingLabel SUB_SENSE_ETYM = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_ETYM_LABEL);
    public static final TaggingLabel SUB_SENSE_XR = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_XR_LABEL);
    public static final TaggingLabel SUB_SENSE_LBL = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_LBL_LABEL);
    public static final TaggingLabel SUB_SENSE_DICTSCRAP = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_DICTSCRAP_LABEL);
    public static final TaggingLabel SUB_SENSE_PC = new TaggingLabelImpl(DictionaryModels.SUB_SENSE, SUB_SENSE_PC_LABEL);


    static {
        register(SUB_SENSE_DEF);
        register(SUB_SENSE_EXAMPLE);
        register(SUB_SENSE_TRANSLATION);
        register(SUB_SENSE_USAGE);
        register(SUB_SENSE_RE);
        register(SUB_SENSE_ETYM);
        register(SUB_SENSE_XR);
        register(SUB_SENSE_LBL);
        register(SUB_SENSE_DICTSCRAP);
        register(SUB_SENSE_PC);

    }
}
