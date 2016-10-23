package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by med on 18.10.16.
 */
public class LexicalEntryParser extends AbstractParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LexicalEntryParser.class);
    private static volatile DictionaryParser instance;

    public LexicalEntryParser() {
        super(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES);
    }

    public static DictionaryParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }
    /**
     * Create a new instance.
     */
    private static synchronized void getNewInstance() {
        instance = new DictionaryParser();
    }

}
