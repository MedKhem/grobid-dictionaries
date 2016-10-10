package org.grobid.core.Document;

import org.grobid.core.GrobidModels;
import org.grobid.core.document.Document;
import org.grobid.core.engines.TaggingLabels;
import org.grobid.core.enums.PossibleTags;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Created by med on 05.10.16.
 */
public class TEIFormatterTest {

    TEIDictionaryFormatter target;
    Document doc;

    @BeforeClass
    public static void beforeClass() throws Exception {
        LibraryLoader.load();
        MockContext.setInitialContext();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        MockContext.destroyInitialContext();
    }

    @Before
    public void setUp() throws Exception {
        target = new TEIDictionaryFormatter(doc);
    }

    // Method to extend the labels in "org.grobid.core.engines.TaggingLabels" enum with list of possible tags specifc to grobid-dictionary project.
    // To be  run just once

    @Test
    public void toTEIFormat() throws Exception {
        for (PossibleTags tags : PossibleTags.values()) {
            TaggingLabels.labelFor(GrobidModels.DICTIONARIES_LEXICAL_ENTRIES, tags.toString());
        }

    }
}
