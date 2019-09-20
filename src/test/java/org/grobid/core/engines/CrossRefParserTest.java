package org.grobid.core.engines;

import org.grobid.core.EngineMockTest;
import org.grobid.core.factory.AbstractEngineFactory;
import org.grobid.core.layout.LayoutToken;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by Med on 30.04.19.
 */
public class CrossRefParserTest  {
    CrossRefParser target;

    @BeforeClass
    public static void setInitialContext() throws Exception {

        AbstractEngineFactory.init();
    }
    @Before
    public void setUp() throws Exception {
        target = new CrossRefParser();
    }

//    @Test
//    public void testProcessToTEI() throws Exception {
////        File input = new File(this.getClass().getResource("close.pdf").toURI());
//        List<LayoutToken> formEntry = new ArrayList<LayoutToken>();
//        String token1 = "hi";
//        String token2 = "SYN";
//        formEntry.add(new LayoutToken(token1));
//        formEntry.add(new LayoutToken(token2));

//        System.out.println("nextToand " + body.charAt(5824));
//        assertThat(formEntry, notNullValue());

//    }


}
