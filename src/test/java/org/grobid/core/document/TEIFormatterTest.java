package org.grobid.core.document;

import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Created by med on 05.10.16.
 */
public class TEIFormatterTest {

    TEIDictionaryFormatter target;
    Document doc;

    @Before
    public void setUp() throws Exception {
        target = new TEIDictionaryFormatter(doc);
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        LibraryLoader.load();
        MockContext.setInitialContext();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        MockContext.destroyInitialContext();
    }


    // Method to extend the labels in "org.grobid.core.engines.TaggingLabels" enum with list of possible tags specifc to grobid-dictionary project.
    // To be  run just once

//    @Test
//    public void toTEIFormatLexicalEntry() throws Exception {
//        for (LexicalEntryLabel tags : LexicalEntryLabel.values()) {
//            TaggingLabel.labelFor(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, tags.toString());
//        }
//
//    }
}
