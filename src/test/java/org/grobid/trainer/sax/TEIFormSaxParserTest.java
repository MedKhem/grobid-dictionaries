package org.grobid.trainer.sax;

import org.grobid.core.data.SimpleLabeled;
import org.junit.Before;
import org.junit.Test;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 19/08/16.
 */
public class TEIFormSaxParserTest {
    TEIFormSaxParser target;
    SAXParserFactory spf;

    @Before
    public void setUp() throws Exception {
        target = new TEIFormSaxParser();
        spf = SAXParserFactory.newInstance();
    }

//
//    @Test
//    public void testSmallParsing_shouldWork() throws Exception {
//        InputStream input = this.getClass().getResourceAsStream("/form.model.sample.training.data.xml");
//        SAXParser p = spf.newSAXParser();
//        p.parse(input, target);
//
//        List<SimpleLabeled> labeled = target.getLabeledResult();
//
//        assertThat(labeled.size(), greaterThan(0));
//        assertThat(labeled.size(), is(2));
//
//        assertThat(labeled.get(0).getLabels().get(0).getLeft(), is("ally"));
//        assertThat(labeled.get(0).getLabels().get(0).getRight(), is("I-<orth>"));
//    }


}