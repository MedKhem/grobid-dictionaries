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
    public static final TaggingLabel DICTIONARY_ENTRY = new TaggingLabelImpl(DictionaryModels.DICTIONARY_BODY_SEGMENTATION, DICTIONARY_ENTRY_LABEL);


    static {
        register(DICTIONARY_ENTRY);

    }
}
