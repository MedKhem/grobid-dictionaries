package org.grobid.service;

/**
 * This interface contains the path extensions for accessing the dictionary service.
 * <p>
 * Created by med on 29.07.16.
 */
public interface DictionaryPaths {

    /**
     * path extension for Dictionary service.
     */
    public static final String PATH_DICTIONARY = "/";

    /**
     * path extension for initiateProcessing dictionary entries.
     */
    public static final String PROCESS_DICTIONARY_SEGMENTATION = "processDictionarySegmentation";
    public static final String PROCESS_DICTIONARY_BODY_SEGMENTATION = "processDictionaryBodySegmentation";
//    public static final String PROCESS_DICTIONARY_BODY_SEGMENTATION_OPTIMISED = "processDictionaryBodySegmentationOptimised";
    public static final String PATH_LEXICAL_ENTRY = "processLexicalEntry";
//    public static final String PATH_LEXICAL_ENTRY_OPTIMISED = "processLexicalEntryOptimised";
    public static final String PATH_FULL_DICTIONARY = "processFullDictionary";
//    public static final String PATH_FULL_DICTIONARY_OPTIMISED = "processFullDictionaryOptimised";
}
