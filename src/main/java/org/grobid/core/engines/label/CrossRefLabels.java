package org.grobid.core.engines.label;

/**
 * Created by Med on 26.04.19.
 *
 *
 */
import org.grobid.core.engines.DictionaryModels;
public class CrossRefLabels extends TaggingLabels{


    private CrossRefLabels() {
        super();
    }
    public static final String CROSSREF_REF_LABEL = "<ref>";
    public static final String CROSSREF_LBL_LABEL = "<lbl>";
    public static final String CROSSREF_Relation_LABEL = "<relation>";
    public static final String CROSSREF_BIBL_LABEL = "<bibl>";
    public static final String CROSSREF_NOTE_LABEL = "<note>";
    public static final String CROSSREF_USG_LABEL = "<usg>";
    public static final String CROSSREF_DICTSCRAP_LABEL = "<dictScrap>";
    public static final String CROSSREF_PC_LABEL = "<pc>";

    public static final TaggingLabel CROSSREF_REF = new TaggingLabelImpl(DictionaryModels.CROSS_REF, CROSSREF_REF_LABEL);
    public static final TaggingLabel CROSSREF_LBL = new TaggingLabelImpl(DictionaryModels.CROSS_REF, CROSSREF_LBL_LABEL);
    public static final TaggingLabel CROSSREF_Relation = new TaggingLabelImpl(DictionaryModels.CROSS_REF, CROSSREF_Relation_LABEL);
    public static final TaggingLabel CROSSREF_BIBL = new TaggingLabelImpl(DictionaryModels.CROSS_REF, CROSSREF_BIBL_LABEL);
    public static final TaggingLabel CROSSREF_NOTE = new TaggingLabelImpl(DictionaryModels.CROSS_REF, CROSSREF_NOTE_LABEL);
    public static final TaggingLabel CROSSREF_USG = new TaggingLabelImpl(DictionaryModels.CROSS_REF, CROSSREF_USG_LABEL);
    public static final TaggingLabel CROSSREF_DICTSCRAP = new TaggingLabelImpl(DictionaryModels.CROSS_REF, CROSSREF_DICTSCRAP_LABEL);
    public static final TaggingLabel CROSSREF_PC = new TaggingLabelImpl(DictionaryModels.CROSS_REF, CROSSREF_PC_LABEL);

    static {
        register(CROSSREF_REF);
        register(CROSSREF_LBL);
        register(CROSSREF_Relation);
        register(CROSSREF_NOTE);
        register(CROSSREF_BIBL);
        register(CROSSREF_USG);
        register(CROSSREF_DICTSCRAP);
        register(CROSSREF_PC);

    }
}
