package org.grobid.core.features;

import org.grobid.core.layout.LayoutToken;
import org.grobid.core.main.LibraryLoader;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by med on 16.08.16.
 */
public class FeatureVectorLexicalEntryTest {

    @Before
    public void setUp() throws Exception {
        LibraryLoader.load();
    }
    @Test
    public void testAddFeaturesLemma() throws Exception {
        FeatureVectorLexicalEntry output = FeatureVectorLexicalEntry.addFeaturesLemma(new LayoutToken("text"), "LE","LINESTART","NEWFONT");

        assertNotNull(output.string);
        assertNotNull(output.label);


        assertNotNull(output.fontSize);
        assertFalse(output.bold);
        assertFalse(output.italic);
        assertNotNull(output.capitalisation);
        assertNotNull(output.punctType);

        assertNotNull(output.lineStatus);
        assertNotNull(output.fontStatus);

    }

    @Test
    public void testPrintVector_sample1() throws Exception {
        FeatureVectorLexicalEntry output = FeatureVectorLexicalEntry.addFeaturesLemma(new LayoutToken("Text"), "LE","LINESTART","NEWFONT");
        String outputString = output.printVector();

        assertThat(outputString, is("Text text T Te Tex Text t xt ext Text 0.0 false false INITCAP NOPUNCT LINESTART NEWFONT LE"));
    }
}
