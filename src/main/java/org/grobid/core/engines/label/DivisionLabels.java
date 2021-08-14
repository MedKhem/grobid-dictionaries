package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;

public class DivisionLabels extends TaggingLabels{
    private DivisionLabels() {
        super();
    }

    public static final String DIV_GRAMGRP_LABEL = "<gramGrp>";
    public static final String DIV_SYNONYM_LABEL = "<synonym>";
    public static final String DIV_ANTONYM_LABEL = "<antonym>";
    public static final String DIV_XR_LABEL = "<xr>";
    public static final String DIV_PC_LABEL = "<pc>";
    public static final String DIV_DICTSCRAP_LABEL = "<dictScrap>";


    public static final TaggingLabel DIV_GRAMGRP = new TaggingLabelImpl(DictionaryModels.DIVISION, DIV_GRAMGRP_LABEL);
    public static final TaggingLabel DIV_SYNONYM = new TaggingLabelImpl(DictionaryModels.DIVISION, DIV_SYNONYM_LABEL);
    public static final TaggingLabel DIV_ANTONYM = new TaggingLabelImpl(DictionaryModels.DIVISION, DIV_ANTONYM_LABEL);
    public static final TaggingLabel DIV_XR = new TaggingLabelImpl(DictionaryModels.DIVISION, DIV_XR_LABEL);
    public static final TaggingLabel DIV_PC = new TaggingLabelImpl(DictionaryModels.DIVISION, DIV_PC_LABEL);
    public static final TaggingLabel DIV_DICTSCRAP = new TaggingLabelImpl(DictionaryModels.DIVISION, DIV_DICTSCRAP_LABEL);



    static {
        register(DIV_GRAMGRP);
        register(DIV_SYNONYM);
        register(DIV_ANTONYM);
        register(DIV_XR);
        register(DIV_PC);
        register(DIV_DICTSCRAP);

    }
}
