package org.grobid.core.engines;
import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidModels;
/**
 * Created by med on 30.11.16.
 */
public class DictionaryModels {


    public static final GrobidModel DICTIONARY_SEGMENTATION = GrobidModels.modelFor("dictionary-segmentation");
    public static final GrobidModel DICTIONARY_BODY_SEGMENTATION = GrobidModels.modelFor("dictionary-body-segmentation");
    public static final GrobidModel LEXICAL_ENTRY = GrobidModels.modelFor("lexical-entry");

    public static final GrobidModel FORM = GrobidModels.modelFor("form");
    public static final GrobidModel SENSE = GrobidModels.modelFor("sense");
    public static final GrobidModel SUB_SENSE = GrobidModels.modelFor("sub-sense");
    public static final GrobidModel GRAMMATICAL_GROUP = GrobidModels.modelFor("gramGrp");
    public static final GrobidModel ETYM_QUOTE = GrobidModels.modelFor("etymQuote");
    public static final GrobidModel ETYM = GrobidModels.modelFor("etym");
    public static final GrobidModel CROSS_REF = GrobidModels.modelFor("crossRef");


}
