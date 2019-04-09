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
 * Created by med on 12.12.16.
 */
public class TEIDictionaryBodySegmentationSaxParserTest {


    TEIDictionaryBodySegmentationSaxParser target;
    SAXParserFactory spf;

    @Before
    public void setUp() throws Exception {
        target = new TEIDictionaryBodySegmentationSaxParser();
        spf = SAXParserFactory.newInstance();
    }


    @Test
    public void testFeatureLevel() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("bodyFile.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        // Test if the second token of the same line is annotated (if this fails, means that the feature generation is
        // working on the line Level and not on the token level)
        assertThat(labeled.get(1), is("the <entry>\n"));

    }
    @Test
    public void testTags() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("BasicEnglish1.training.dictionaryBodySegmentation.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(827));

        assertThat(labeled.get(0), is("absolutely I-<entry>\n"));
        assertThat(labeled.get(101), is(". I-<pc>\n"));
    }

    @Test
    public void testCrapAtZero() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("BodySegmentationTestFile.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();
        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(2));
        assertThat(labeled.get(0), is("crap I-<pc>\n"));
    }

    @Test
    public void testCrapBetweenTwoSameTags() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("BodySegmentationTestFile1.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(3));

        assertThat(labeled.get(1), is("crap I-<pc>\n"));
    }

    @Test
    public void testCrapAtTheEnd() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("BodySegmentationTestFile2.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(3));

        assertThat(labeled.get(2), is("crap I-<pc>\n"));
    }



}
