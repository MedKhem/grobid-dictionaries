package org.grobid.core.features.enums;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Created by lfoppiano on 19/08/16.
 */
public class LineStatusTest {
    @Test
    public void testFromNameTest_unknownString_shouldReturnNull() throws Exception {
        assertNull(LineStatus.fromName("ALLLINE"));
    }

    @Test
    public void testFromNameTest_nullString_shouldReturnNull() throws Exception {
        assertNull(LineStatus.fromName(null));
    }

    @Test
    public void testFromNameTest_knownString_shouldReturnValue() throws Exception {
        assertNotNull(LineStatus.fromName("LINEEND"));
        assertThat(LineStatus.fromName("LINEEND").toString(), is("LINEEND"));
    }

}