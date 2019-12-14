package org.grobid.core.engines;
import org.grobid.core.GrobidModel;
import org.grobid.core.GrobidDictionaryModels;
/**
 * Created by med on 30.11.16.
 */
public class DictionaryModels {


    public static final GrobidModel DICTIONARY_SEGMENTATION = GrobidDictionaryModels.modelFor("dictionary-segmentation");
    public static final GrobidModel DICTIONARY_BODY_SEGMENTATION = GrobidDictionaryModels.modelFor("dictionary-body-segmentation");
    public static final GrobidModel LEXICAL_ENTRY = GrobidDictionaryModels.modelFor("lexical-entry");

    public static final GrobidModel FORM = GrobidDictionaryModels.modelFor("form");
    public static final GrobidModel SENSE = GrobidDictionaryModels.modelFor("sense");
//    public static final GrobidModel MORPHO_GRAMMATICAL = GrobidDictionaryModels.modelFor("morph-gram");
    public static final GrobidModel SUB_SENSE = GrobidDictionaryModels.modelFor("sub-sense");
    public static final GrobidModel GRAMMATICAL_GROUP = GrobidDictionaryModels.modelFor("gramGrp");
    public static final GrobidModel ETYM_QUOTE = GrobidDictionaryModels.modelFor("etymQuote");
    public static final GrobidModel ETYM = GrobidDictionaryModels.modelFor("etym");
    public static final GrobidModel CROSS_REF = GrobidDictionaryModels.modelFor("crossRef");



}
