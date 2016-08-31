package org.grobid.trainer;

import org.grobid.core.EngineMockTest;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 31/08/16.
 */
public class LexicalEntryTrainerTest extends EngineMockTest {

    LexicalEntryTrainer target;

    @Before
    public void setUp() throws Exception {
        target = new LexicalEntryTrainer();
    }

    @Test
    public void testGLabelByToken_tokenFirstPosition_shouldWork() throws Exception {
        String token = "p";

        List<String> labelledList = Arrays.asList(new String[]{
                "p I-<label1>",
                "bao <label1>",
                "miao <label1>",
                "ciao I<label2>"
        });


        String label = target.getLabelByToken(token, 0, labelledList);
        assertThat(label, is("I-<label1>"));
    }

    @Test
    public void testGetLabelByToken_tokenPresentInThirdPosition_sholdReturnNull() throws Exception {
        String token = "miao";

        List<String> labelledList = Arrays.asList(new String[]{
                "p I-<label1>",
                "bao <label1>",
                "miao <label1>",
                "ciao I<label2>"
        });


        String label = target.getLabelByToken(token, 0, labelledList);
        assertThat(label, is("<label1>"));
    }

    @Test
    public void testGetLabelByToken_tokenNotPrenset_sholdReturnNull() throws Exception {
        String token = "sbao";

        List<String> labelledList = Arrays.asList(new String[]{
                "p I-<label1>",
                "bao <label1>",
                "miao <label1>",
                "ciao I<label2>"
        });


        String label = target.getLabelByToken(token, 0, labelledList);
        assertNull(label);
    }


    @Test
    public void testGetLabelByToken_tokenNotPrensetWithinTheBoundaries_sholdReturnNull() throws Exception {
        String token = "bao";

        List<String> labelledList = Arrays.asList(new String[]{
                "p I-<label1>",
                "bao <label1>",
                "miao <label1>",
                "ciao <label2>",
                "my <label2>",
                "name <label2>",
                "is <label2>",
                "miao <label2>",
                "ciao <label2>",
                "bao <label2>",
        });


        String label = target.getLabelByToken(token, 2, labelledList);
        assertNull(label);
    }

}