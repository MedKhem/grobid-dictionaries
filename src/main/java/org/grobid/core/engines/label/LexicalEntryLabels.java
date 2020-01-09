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

    public static final String LEXICAL_ENTRY_LEMMA_LABEL = "<lemmaGrp>";
    public static final String LEXICAL_ENTRY_ETYM_LABEL = "<etym>";
    public static final String LEXICAL_ENTRY_SENSE_LABEL = "<sense>";
    public static final String LEXICAL_ENTRY_RE_LABEL = "<re>";
    public static final String LEXICAL_ENTRY_XR_LABEL = "<xr>";
    public static final String LEXICAL_ENTRY_SUB_ENTRY_LABEL = "<subEntry>";
    public static final String LEXICAL_ENTRY_NOTE_LABEL = "<note>";
    public static final String LEXICAL_ENTRY_FROM_GRAMGRP_LABEL = "<formGramGrp>";
    public static final String LEXICAL_ENTRY_SENSE_GRAMGRP_LABEL = "<senseGramGrp>";
    public static final String LEXICAL_ENTRY_PC_LABEL = "<pc>";
    public static final String LEXICAL_ENTRY_OTHER_LABEL = "<dictScrap>";


    public static final TaggingLabel LEXICAL_ENTRY_LEMMA = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_LEMMA_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_ETYM = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_ETYM_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_SENSE = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_SENSE_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_RE = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_RE_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_XR = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_XR_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_SUB_ENTRY = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_SUB_ENTRY_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_NOTE = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_NOTE_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_FROM_GRAMGRP = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_FROM_GRAMGRP_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_SENSE_GRAMGRP = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_SENSE_GRAMGRP_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_PC = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_PC_LABEL);
    public static final TaggingLabel LEXICAL_ENTRY_OTHER = new TaggingLabelImpl(DictionaryModels.LEXICAL_ENTRY, LEXICAL_ENTRY_OTHER_LABEL);


    static {
        register(LEXICAL_ENTRY_LEMMA);
        register(LEXICAL_ENTRY_ETYM);
        register(LEXICAL_ENTRY_SENSE);
        register(LEXICAL_ENTRY_RE);
        register(LEXICAL_ENTRY_XR);
        register(LEXICAL_ENTRY_SUB_ENTRY);
        register(LEXICAL_ENTRY_NOTE);
        register(LEXICAL_ENTRY_FROM_GRAMGRP);
        register(LEXICAL_ENTRY_SENSE_GRAMGRP);
        register(LEXICAL_ENTRY_PC);
        register(LEXICAL_ENTRY_OTHER);

    }
}
