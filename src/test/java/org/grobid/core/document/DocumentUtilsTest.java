package org.grobid.core.document;

import org.grobid.core.engines.DictionarySegmentationParser;
import org.grobid.core.engines.EngineParsers;
import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.enums.DictionarySegmentationLabel;
import org.grobid.core.features.FeaturesUtils;
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
 * Created by med on 20.10.16.
 */
public class DocumentUtilsTest {

    DocumentUtils target;

    @Before
    public void setUp() throws Exception {
        target = new DocumentUtils();
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
    public void testGetLayoutTokenizationsFromDocAndDocPart1() throws Exception {
        Pair<DictionaryDocument, SortedSet<DocumentPiece>> output = prepare("BasicEnglish.pdf");
        assertThat(output, notNullValue());

        LayoutTokenization layoutTokenization = target.getLayoutTokenizations(output.a, output.b);
        assertThat(layoutTokenization.getTokenization().isEmpty(), is(false));
        assertThat(layoutTokenization.getTokenization().size(), is(23474));

    }

    // No body is found in the following files since no similar document has been used as training data
//    @Test
//    public void testGetLayoutTokenizationsFromDocAndDocPart2() throws Exception {
//        Pair<DictionaryDocument, SortedSet<DocumentPiece>> output = prepare("LettreP-117082016.pdf");
//        assertThat(output, notNullValue());
//
//        LayoutTokenization layoutTokenization = target.getLayoutTokenizations(output.a, output.b);
//        assertThat(layoutTokenization.getTokenization().isEmpty(), is(false));
//        assertThat(layoutTokenization.getTokenization().size(), is(1550));
//    }
//
//    @Test
//    public void testGetLayoutTokenizationsFromDocAndDocPart3() throws Exception {
//        Pair<DictionaryDocument, SortedSet<DocumentPiece>> output = prepare("Larrousse-205-208.pdf");
//        assertThat(output, notNullValue());
//
//        LayoutTokenization layoutTokenization = target.getLayoutTokenizations(output.a, output.b);
//        assertThat(layoutTokenization.getTokenization().isEmpty(), is(false));
//        assertThat(layoutTokenization.getTokenization().size(), is(4238));
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
        DictionaryDocument doc =  parser.initiateProcessing(input, config);
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabel.BODY);

        return new Pair<>(doc, documentBodyParts);

    }
}
