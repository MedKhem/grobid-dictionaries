package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabelImpl;
import org.grobid.core.engines.label.TaggingLabels;

/**
 * Created by med on 08.11.16.
 */
public class DictionarySegmentationLabels extends TaggingLabels {

    private DictionarySegmentationLabels() {
        super();
    }

    public static final String DICTIONARY_HEADNOTE_LABEL = "<headnote>";
    public static final String DICTIONARY_BODY_LABEL = "<body>";
    public static final String DICTIONARY_FOOTNOTE_LABEL = "<footnote>";

    public static final TaggingLabel DICTIONARY_HEADNOTE = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_HEADNOTE_LABEL);
    public static final TaggingLabel DICTIONARY_BODY = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_BODY_LABEL);
    public static final TaggingLabel DICTIONARY_FOOTNOTE = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_FOOTNOTE_LABEL);

    static {
        register(DICTIONARY_HEADNOTE);
        register(DICTIONARY_BODY);
        register(DICTIONARY_FOOTNOTE);
    }

}
