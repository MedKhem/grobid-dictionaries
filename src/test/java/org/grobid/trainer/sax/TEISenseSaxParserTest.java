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
public class TEISenseSaxParserTest {
    TEISenseSaxParser target;
    SAXParserFactory spf;

    @Before
    public void setUp() throws Exception {
        target = new TEISenseSaxParser();
        spf = SAXParserFactory.newInstance();
    }


    @Test
    public void testSmallParsing_shouldWork() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("/sense.model.sample.training.data.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<SimpleLabeled> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(2));

        assertThat(labeled.get(0).getLabels().get(0).getLeft(), is("verb"));
        assertThat(labeled.get(0).getLabels().get(0).getRight(), is("I-<gramGrp>"));
    }

    @Test
    public void testNoteLabel() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("/train_set_2.training.sense.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<SimpleLabeled> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(46));

        assertThat(labeled.get(1).getLabels().get(1).getLeft(), is("L"));
        assertThat(labeled.get(1).getLabels().get(1).getRight(), is("I-<def>"));
    }


}