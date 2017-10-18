package org.grobid.core.document;

import org.grobid.core.engines.DictionarySegmentationParser;
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
    }


//    @Test
//    public void testGetLayoutTokenizationsFromDocAndDocPart1() throws Exception {
//        Pair<DictionaryDocument, SortedSet<DocumentPiece>> output = prepare("BasicEnglish.pdf");
//        assertThat(output, notNullValue());
//
//        LayoutTokenization layoutTokenization = target.getLayoutTokenizations(output.a, output.b);
//        assertThat(layoutTokenization.getTokenization().isEmpty(), is(false));
//        assertThat(layoutTokenization.getTokenization().size(), is(23695));
//
//    }
//    @Test
//    public void testGetHeadnote() throws Exception {
//        DictionaryDocument doc = prepareDictionaryDocument("BasicEnglish.pdf");
//        SortedSet<DocumentPiece> headNotesOfAllPages = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_HEADNOTE_LABEL);
//
//
//        assertThat(headNotesOfAllPages.first(), notNullValue());
//        System.out.println(doc.getDocumentPieceText(Iterables.get(headNotesOfAllPages,13)));
//
//
//    }

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
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

        return new Pair<>(doc, documentBodyParts);

    }

    DictionaryDocument prepareDictionaryDocument(String file) {
        // Get all partsand not just the body
        File input = null;
        try {
            input = new File(this.getClass().getResource(file).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DictionarySegmentationParser parser = new DictionarySegmentationParser();
        DictionaryDocument doc =  parser.initiateProcessing(input, config);
       // SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentDictionaryPart(DictionarySegmentationLabels.DICTIONARY_BODY_LABEL);

        return doc;

    }
}
