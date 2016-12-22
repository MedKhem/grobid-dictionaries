package org.grobid.trainer.sax;

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
 * Created by med on 06.12.16.
 */
public class TEIDictionarySegmentationSaxParserTest {
    TEIDictionarySegmentationSaxParser target;
    SAXParserFactory spf;

    @Before
    public void setUp() throws Exception {
        target = new TEIDictionarySegmentationSaxParser();
        spf = SAXParserFactory.newInstance();
    }


    @Test
    public void testCrapAtZero() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("dictionarySegmentationTestFile.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();
        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(5));
        assertThat(labeled.get(0), is("crap I-<pc>\n"));
    }

    @Test
    public void testCrapAfterFirstTag() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("dictionarySegmentationTestFile1.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(4));

        assertThat(labeled.get(1), is("crap I-<pc>\n"));
    }

    @Test
    public void testCrapAfterSecondTag() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("dictionarySegmentationTestFile2.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(4));

        assertThat(labeled.get(2), is("crap I-<pc>\n"));
    }

    @Test
    public void testCrapAtTheEnd() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("dictionarySegmentationTestFile3.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(4));

        assertThat(labeled.get(3), is("crap I-<pc>\n"));
    }

}
