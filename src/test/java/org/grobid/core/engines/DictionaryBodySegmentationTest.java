package org.grobid.core.engines;

import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by med on 07.12.16.
 */
public class DictionaryBodySegmentationTest {
    DictionaryBodySegmentationParser target;

    @Before
    public void setUp() throws Exception {
        target = new DictionaryBodySegmentationParser();
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
//        File input = new File(this.getClass().getResource("BFirstTwo.pdf").toURI());
//        String body = target.process(input);
//        System.out.print(body);
//        assertThat(body, notNullValue());
//
//    }

    @Test
    public void testTrainingDataGeneration() throws Exception {
        File input = new File(this.getClass().getResource("BasicEnglish.page2.sample.pdf").toURI());
        target.createTrainingDictionaryBody(input, "/tmp");


    }
}
