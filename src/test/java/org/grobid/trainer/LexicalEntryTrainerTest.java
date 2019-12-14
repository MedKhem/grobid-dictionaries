package org.grobid.trainer;

import org.grobid.core.EngineMockTest;
import org.grobid.core.factory.AbstractDictionaryEngineFactory;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 31/08/16.
 */
public class LexicalEntryTrainerTest  {

    LexicalEntryTrainer target;

    @BeforeClass
    public static void setInitialContext() throws Exception {

        AbstractDictionaryEngineFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        target = new LexicalEntryTrainer();
    }






//    @Test
//    public void testGetLabelByToken_tokenNotPrensetWithinTheBoundaries_sholdReturnNull() throws Exception {
//        String token = "bao";
//
//        List<String> labelledList = Arrays.asList(new String[]{
//                "p I-<label1>",
//                "bao <label1>",
//                "miao <label1>",
//                "ciao <label2>",
//                "my <label2>",
//                "name <label2>",
//                "is <label2>",
//                "miao <label2>",
//                "ciao <label2>",
//                "bao <label2>",
//        });
//
//
//        String label = target.getLabelByToken(token, 2, labelledList);
//        assertNull(label);
//    }

}