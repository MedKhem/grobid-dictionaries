package org.grobid.core.features;

import org.grobid.core.engines.DictionaryParser;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by med on 20.10.16.
 */
public class FeatureUtilsTest {

    FeaturesUtils target;

    @Before
    public void setUp() throws Exception {
        target = new FeaturesUtils();
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


    @Test
    public void testCheckLineStatus() throws Exception {
        LayoutToken l = new LayoutToken("a");

        l.setNewLineAfter(true);
        String actualStatus1 = target.checkLineStatus(l,true);
        assertThat(actualStatus1,  is("LINESTART"));


        l.setNewLineAfter(true);
        String actualStatus5 = target.checkLineStatus(l,false);
        assertThat(actualStatus5,  is("LINESTART"));

        l.setNewLineAfter(false);
        String actualStatus6 = target.checkLineStatus(l,true);
        assertThat(actualStatus6,  is("LINEEND"));

        l.setNewLineAfter(false);
        String actualStatus7 = target.checkLineStatus(l,false);
        assertThat(actualStatus7,  is("LINEIN"));



    }

}
