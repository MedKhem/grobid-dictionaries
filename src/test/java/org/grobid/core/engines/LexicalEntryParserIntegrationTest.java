package org.grobid.core.engines;

import org.grobid.core.EngineMockTest;
import org.grobid.core.factory.AbstractDictionaryEngineFactory;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * Created by lfoppiano on 11/08/16.
 */
public class LexicalEntryParserIntegrationTest {

    LexicalEntryParser target;

    @BeforeClass
    public static void setInitialContext() throws Exception {

        AbstractDictionaryEngineFactory.init();
    }
    @Before
    public void setUp() throws Exception {
        target = new LexicalEntryParser();
    }

    
    @Test
    public void testProcess() throws Exception {
        File input = new File(this.getClass().getResource("BasicEnglish.pdf").toURI());
//        String output = target.process(input);
//        System.out.println(output);

//        assertThat(output, notNullValue());
    }








}
