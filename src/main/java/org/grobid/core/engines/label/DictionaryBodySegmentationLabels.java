package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabelImpl;
import org.grobid.core.engines.label.TaggingLabels;

/**
 * Created by med on 08.11.16.
 */
public class DictionaryBodySegmentationLabels extends TaggingLabels {

    private DictionaryBodySegmentationLabels() {
        super();
    }

    public static final String DICTIONARY_ENTRY_LABEL = "<entry>";
    public static final String DICTIONARY_BODY_PC_LABEL = "<pc>";
    public static final String DICTIONARY_BODY_OTHER_LABEL = "<other>";

    public static final TaggingLabel DICTIONARY_ENTRY = new TaggingLabelImpl(DictionaryModels.DICTIONARY_BODY_SEGMENTATION, DICTIONARY_ENTRY_LABEL);
    public static final TaggingLabel DICTIONARY_BODY_PC = new TaggingLabelImpl(DictionaryModels.DICTIONARY_BODY_SEGMENTATION, DICTIONARY_BODY_PC_LABEL);
    public static final TaggingLabel DICTIONARY_BODY_OTHER = new TaggingLabelImpl(DictionaryModels.DICTIONARY_BODY_SEGMENTATION, DICTIONARY_BODY_OTHER_LABEL);


    static {
        register(DICTIONARY_ENTRY);
        register(DICTIONARY_BODY_PC);
        register(DICTIONARY_BODY_OTHER);

    }
}
