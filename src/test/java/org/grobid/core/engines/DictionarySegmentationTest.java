package org.grobid.core.engines;

import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

/**
 * Created by med on 16.11.16.
 */
public class DictionarySegmentationTest {
    DictionarySegmentationParser target;

    @Before
    public void setUp() throws Exception {
        target = new DictionarySegmentationParser();
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


//    @Test
//    public void testProcess() throws Exception {
//        String inputDirectory = "/home/med/git/grobid/grobid-dictionaries/resources/dataset/dictionary-segmentation/corpus/pdf/";
//        String outputDirectory = "/home/med/git/grobid/grobid-dictionaries/tmp";
//
//       int n = target.createTrainingBatch(inputDirectory,outputDirectory);
//
//        assertFalse(n == 0);
//    }

        @Test
    public void testProcess() throws Exception {
            File input = new File(this.getClass().getResource("BFirstTwo.pdf").toURI());
            String body = target.processToTEI(input);
            System.out.print(body);
            assertThat(body, notNullValue());

    }


}
