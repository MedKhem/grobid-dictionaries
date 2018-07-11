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
    public static final String SENSE_SENSE_LABEL = "<sense>";
    public static final String DEF_SENSE_LABEL = "<def>";
    public static final String NOTE_SENSE_LABEL = "<note>";
    public static final String CIT_SENSE_LABEL = "<cit>";
    public static final String GRAMMATICAL_GROUP_SENSE_LABEL = "<gramGrp>";
    public static final String DICTSCRAP_SENSE_LABEL = "<dictScrap>";
    public static final String PC_SENSE_LABEL = "<pc>";

    public static final TaggingLabel SENSE_SENSE = new TaggingLabelImpl(DictionaryModels.SENSE, SENSE_SENSE_LABEL);
    public static final TaggingLabel SENSE_DEF = new TaggingLabelImpl(DictionaryModels.SENSE, DEF_SENSE_LABEL);
    public static final TaggingLabel SENSE_NOTE = new TaggingLabelImpl(DictionaryModels.SENSE, NOTE_SENSE_LABEL);
    public static final TaggingLabel SENSE_CIT = new TaggingLabelImpl(DictionaryModels.SENSE, CIT_SENSE_LABEL);
    public static final TaggingLabel SENSE_GRAMMATICAL_GROUP = new TaggingLabelImpl(DictionaryModels.SENSE, GRAMMATICAL_GROUP_SENSE_LABEL);
    public static final TaggingLabel SENSE_DICTSCRAP = new TaggingLabelImpl(DictionaryModels.SENSE, DICTSCRAP_SENSE_LABEL);
    public static final TaggingLabel SENSE_PC = new TaggingLabelImpl(DictionaryModels.SENSE, PC_SENSE_LABEL);



    static {
        register(SENSE_SENSE);
        register(SENSE_DEF);
        register(SENSE_NOTE);
        register(SENSE_CIT);
        register(SENSE_GRAMMATICAL_GROUP);
        register(SENSE_DICTSCRAP);
        register(SENSE_PC);
    }
    
}
