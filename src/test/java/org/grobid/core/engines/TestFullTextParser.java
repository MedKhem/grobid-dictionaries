package org.grobid.core.engines;

import org.grobid.core.EngineMockTest;
import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.Pair;
import org.junit.Before;
import org.junit.BeforeClass;

import java.io.File;
import java.net.URISyntaxException;
import java.util.SortedSet;

/**
 * Created by med on 21.10.16.
 *
 * These tests are to check if the methods for getting LayoutToknization and Text from pdf document are
 * the same in grobid and grobid -dictionaries
 *
 * The test are the same as in DocumentUtilsTest
 */
public class TestFullTextParser extends EngineMockTest{

    FullTextParser target;

    @Before
    public void setUp() throws Exception {
        target = new Engine().getParsers().getFullTextParser();
    }

//
//    @Test
//    public void testGetBodyFeaturedWithItsLayoutTokenization() throws Exception {
//
//        Pair<DictionaryDocument, SortedSet<DocumentPiece>> output = prepare("BasicEnglish.pdf");
//        assertThat(output, notNullValue());
//        Pair<String, LayoutTokenization> featuresAndLayoutTokens = target.getBodyTextFeatured(output.a, output.b);
//        assertThat(featuresAndLayoutTokens.b.getTokenization().size(), is(23681));
////        System.out.println("hi"+featuresAndLayoutTokens.a);
//
//
//    }

    // No body is found in the following files since no similar document has been used as training data

//    @Test
//    public void testGetBodyFeaturedWithItsLayoutTokenization1() throws Exception {
//
//
//        Pair<DictionaryDocument, SortedSet<DocumentPiece>> output = prepare("LettreP-117082016.pdf");
//        assertThat(output, notNullValue());
//        Pair<String, LayoutTokenization> featuresAndLayoutTokens = target.getBodyTextFeatured(output.a, output.b);
//        assertThat(featuresAndLayoutTokens.b.getTokenization().size(), is(1549));
//
//
//    }
//
//    @Test
//    public void testGetBodyFeaturedWithItsLayoutTokenization2() throws Exception {
//
//
//        Pair<DictionaryDocument, SortedSet<DocumentPiece>> output = prepare("Larrousse-205-208.pdf");
//        assertThat(output, notNullValue());
//        Pair<String, LayoutTokenization> featuresAndLayoutTokens = target.getBodyTextFeatured(output.a, output.b);
//        assertThat(featuresAndLayoutTokens.b.getTokenization().size(), is(4237));
//
//
//    }
//
//    @Test
//    public void testGetBodyFeaturedWithItsLayoutTokenization3() throws Exception {
//
//        Pair<DictionaryDocument, SortedSet<DocumentPiece>> output = prepare("LettreP-117082016.pdf");
//        assertThat(output, notNullValue());
//        Pair<String, LayoutTokenization> featuresAndLayoutTokens = target.getBodyTextFeatured(output.a, output.b);
////        assertThat(featuresAndLayoutTokens.b.getTokenization().size(), is(23825));
//        System.out.println("hi"+featuresAndLayoutTokens.a);
//
//
//    }

    Pair<DictionaryDocument, SortedSet<DocumentPiece>> prepare(String file) {

        File input = null;
        try {
            input = new File(this.getClass().getResource(file).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionarySegmentationParser parser = new DictionarySegmentationParser();
        DictionaryDocument doc = parser.initiateProcessing(input, config);
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

        return new Pair<>(doc, documentBodyParts);
    }



    }
