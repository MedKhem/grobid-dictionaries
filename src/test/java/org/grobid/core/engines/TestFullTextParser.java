package org.grobid.core.engines;

import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.enums.DictionarySegmentationLabel;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.Pair;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.SortedSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by med on 21.10.16.
 *
 * These tests are to check if the methods for getting LayoutToknization and Text from pdf document are
 * the same in grobid and grobid -dictionaries
 *
 * The test are the same as in DocumentUtilsTest
 */
public class TestFullTextParser {

    FullTextParser target;

    @Before
    public void setUp() throws Exception {
        target = new Engine().getParsers().getFullTextParser();
    }
    @BeforeClass
    public static void beforeClass() throws Exception {
        LibraryLoader.load();
        MockContext.setInitialContext();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        MockContext.destroyInitialContext();
    }



    @Test
    public void testGetBodyFeaturedWithItsLayoutTokenization() throws Exception {

        Pair<DictionaryDocument, SortedSet<DocumentPiece>> output = prepare("BasicEnglish.pdf");
        assertThat(output, notNullValue());
        Pair<String, LayoutTokenization> featuresAndLayoutTokens = target.getBodyTextFeatured(output.a, output.b);
        assertThat(featuresAndLayoutTokens.b.getTokenization().size(), is(23472));
//        System.out.println("hi"+featuresAndLayoutTokens.a);


    }

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
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabel.BODY);

        return new Pair<>(doc, documentBodyParts);
    }



    }
