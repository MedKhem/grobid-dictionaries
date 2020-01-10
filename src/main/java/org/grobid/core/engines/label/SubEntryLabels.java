package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;

/**
 * Created by Med on 10.01.20.
 */
public class SubEntryLabels extends TaggingLabels {

    private SubEntryLabels() {
        super();
    }
    public static final String SUB_ENTRY_ENTRY_LABEL = "<subEntry>";
    public static final String SUB_ENTRY_XR_LABEL = "<xr>";
    public static final String SUB_ENTRY_NUM_LABEL = "<num>";
    public static final String SUB_ENTRY_NOTE_LABEL = "<note>";
    public static final String DICTSCRAP_SUB_ENTRY_LABEL = "<dictScrap>";
    public static final String PC_SUB_ENTRY_LABEL = "<pc>";

    public static final TaggingLabel SUB_ENTRY_ENTRY = new TaggingLabelImpl(DictionaryModels.SUB_ENTRY, SUB_ENTRY_ENTRY_LABEL);
    public static final TaggingLabel SUB_ENTRY_XR = new TaggingLabelImpl(DictionaryModels.SUB_ENTRY, SUB_ENTRY_XR_LABEL);
    public static final TaggingLabel SUB_ENTRY_NUM = new TaggingLabelImpl(DictionaryModels.SUB_ENTRY, SUB_ENTRY_NUM_LABEL);
    public static final TaggingLabel SUB_ENTRY_NOTE = new TaggingLabelImpl(DictionaryModels.SUB_ENTRY, SUB_ENTRY_NOTE_LABEL);
    public static final TaggingLabel SUB_ENTRY__DICTSCRAP = new TaggingLabelImpl(DictionaryModels.SUB_ENTRY, DICTSCRAP_SUB_ENTRY_LABEL);
    public static final TaggingLabel SUB_ENTRY__PC = new TaggingLabelImpl(DictionaryModels.SUB_ENTRY, PC_SUB_ENTRY_LABEL);



    static {
        register(SUB_ENTRY_ENTRY);
        register(SUB_ENTRY_XR);
        register(SUB_ENTRY_NUM);
        register(SUB_ENTRY_NOTE);
        register(SUB_ENTRY__DICTSCRAP);
        register(SUB_ENTRY__PC);
    }


}
