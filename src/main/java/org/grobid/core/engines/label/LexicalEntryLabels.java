package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;
import org.grobid.core.engines.label.TaggingLabel;
import org.grobid.core.engines.label.TaggingLabelImpl;
import org.grobid.core.engines.label.TaggingLabels;

/**
 * Created by med on 26.08.16.
 */
public class LexicalEntryLabels extends TaggingLabels {

    private LexicalEntryLabels() {
        super();
    }

    private static final String DICTIONARY_ENTRY_LABEL = "<entry>";
    private static final String DICTIONARY_FORM_LABEL = "<form>";
    private static final String DICTIONARY_ETYM_LABEL = "<etym>";
    private static final String DICTIONARY_SENSE_LABEL = "<sense>";
    private static final String DICTIONARY_METAMARK_LABEL = "<metamark>";
    private static final String DICTIONARY_RE_LABEL = "<re>";
    private static final String DICTIONARY_NOTE_LABEL = "<note>";

    public static final TaggingLabel DICTIONARY_ENTRY = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_ENTRY_LABEL);
    public static final TaggingLabel DICTIONARY_FORM = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_FORM_LABEL);
    public static final TaggingLabel DICTIONARY_ETYM = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_ETYM_LABEL);
    public static final TaggingLabel DICTIONARY_SENSE = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_SENSE_LABEL);
    public static final TaggingLabel DICTIONARY_METAMARK = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_METAMARK_LABEL);
    public static final TaggingLabel DICTIONARY_RE = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_RE_LABEL);
    public static final TaggingLabel DICTIONARY_NOTE = new TaggingLabelImpl(DictionaryModels.DICTIONARY_SEGMENTATION, DICTIONARY_NOTE_LABEL);


    static {
        register(DICTIONARY_ENTRY);
        register(DICTIONARY_FORM);
        register(DICTIONARY_ETYM);
        register(DICTIONARY_SENSE);
        register(DICTIONARY_METAMARK);
        register(DICTIONARY_RE);
        register(DICTIONARY_NOTE);

    }

}
