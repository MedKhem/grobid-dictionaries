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
 * Created by med on 08.12.16.
 */
public class TEIDictionaryBodySegmentationTest {

    TEIDictionaryBodySegmentationSaxParser target;
    SAXParserFactory spf;

    @Before
    public void setUp() throws Exception {
        target = new TEIDictionaryBodySegmentationSaxParser();
        spf = SAXParserFactory.newInstance();
    }


    @Test
    public void testSmallParsing_shouldWork() throws Exception {
        InputStream input = this.getClass().getResourceAsStream("BasicEnglish1.training.dictionaryBodySegmentation.tei.xml");
        SAXParser p = spf.newSAXParser();
        p.parse(input, target);

        List<String> labeled = target.getLabeledResult();

        assertThat(labeled.size(), greaterThan(0));
        assertThat(labeled.size(), is(127));

        assertThat(labeled.get(0), is("absolutely I-<entry>\n"));
    }
}
