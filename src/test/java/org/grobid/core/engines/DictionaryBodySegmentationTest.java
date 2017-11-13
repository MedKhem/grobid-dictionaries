package org.grobid.core.engines;

import org.grobid.core.EngineMockTest;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * Created by med on 07.12.16.
 */
public class DictionaryBodySegmentationTest extends EngineMockTest{
    DictionaryBodySegmentationParser target;

    @Before
    public void setUp() throws Exception {
        target = new DictionaryBodySegmentationParser();
    }

//    @Test
//    public void testProcess() throws Exception {
//        File input = new File(this.getClass().getResource("BFirstTwo.pdf").toURI());
//        String body = target.process(input);
//        System.out.print(body);
//        assertThat(body, notNullValue());
//
//    }

}
