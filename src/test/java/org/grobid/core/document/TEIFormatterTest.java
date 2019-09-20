package org.grobid.core.document;

import org.grobid.core.EngineMockTest;
import org.grobid.core.factory.AbstractEngineFactory;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Created by med on 05.10.16.
 */
public class TEIFormatterTest  {

    TEIDictionaryFormatter target;
    DictionaryDocument doc;

    @BeforeClass
    public static void setInitialContext() throws Exception {

        AbstractEngineFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        target = new TEIDictionaryFormatter(doc);
    }


    // Method to extend the labels in "org.grobid.core.engines.label.TaggingLabels" enum with list of possible tags specifc to grobid-dictionary project.
    // To be  run just once

//    @Test
//    public void toTEIFormatLexicalEntry() throws Exception {
//        for (LexicalEntryLabels tags : LexicalEntryLabels.values()) {
//            TaggingLabel.labelFor(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, tags.toString());
//        }
//
//    }
}
