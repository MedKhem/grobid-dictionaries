package org.grobid.core.features;

import org.grobid.core.features.enums.PonctuationType;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 05/05/2017.
 */
public class FeaturesUtilsTest {

    @Test
    public void testCheckPonctuationType() throws Exception {

        String output = FeaturesUtils.checkPunctuationType("ab");
        assertThat(output, is(PonctuationType.NOPUNCT.toString()));

        String output2 = FeaturesUtils.checkPunctuationType("(");
        assertThat(output2, is(PonctuationType.OPENBRACKET.toString()));

        String output3 = FeaturesUtils.checkPunctuationType("%");
        assertThat(output3, is(PonctuationType.PUNCT.toString()));

        String output4 = FeaturesUtils.checkPunctuationType("}");
        assertThat(output4, is(PonctuationType.ENDBRACKET.toString()));

    }

}