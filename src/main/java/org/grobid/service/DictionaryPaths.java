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
     * path extension for processing dictionary entries.
     */
    public static final String PATH_DICTIONARY_SEGMENTATATION = "processDictionary";
    public static final String PATH_DICTIONARY_BODY_SEGMENTATATION = "processDictionaryBody";
    public static final String PATH_LEXICAL_ENTRY = "processLexicalEntry";
}
