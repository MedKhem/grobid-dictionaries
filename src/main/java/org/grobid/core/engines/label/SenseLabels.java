package org.grobid.core.engines.label;

import org.grobid.core.engines.DictionaryModels;

import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_SENSE_LABEL;

/**
 * Created by lfoppiano on 05/05/2017.
 */
public class SenseLabels extends TaggingLabels {

    private SenseLabels() {
        super();
    }

    public static final TaggingLabel SENSE_SENSE = new TaggingLabelImpl(DictionaryModels.SENSE, LEXICAL_ENTRY_SENSE_LABEL);
    public static final TaggingLabel SENSE_GRAMMATICAL_GROUP = new TaggingLabelImpl(DictionaryModels.SENSE, LEXICAL_ENTRY_SENSE_LABEL);


    static {
        register(SENSE_SENSE);
        register(SENSE_GRAMMATICAL_GROUP);
    }
    
}
