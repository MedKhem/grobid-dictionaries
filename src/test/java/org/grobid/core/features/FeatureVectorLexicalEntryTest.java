package org.grobid.core.features;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.document.DocumentUtils;
import org.grobid.core.engines.DictionaryParser;
import org.grobid.core.engines.EngineParsers;
import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.layout.LayoutToken;
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

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

/**
 * Created by med on 16.08.16.
 */
public class FeatureVectorLexicalEntryTest {

    FeatureVectorLexicalEntry target;
    DocumentUtils target2;

    @Before
    public void setUp() throws Exception {
        target = new FeatureVectorLexicalEntry();
        target2 = new DocumentUtils();
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
    public void testAddFeaturesLemma() throws Exception {
        FeatureVectorLexicalEntry output = FeatureVectorLexicalEntry.addFeaturesLexicalEntries(new LayoutToken("text"), "LE","LINESTART","NEWFONT");

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
        FeatureVectorLexicalEntry output = FeatureVectorLexicalEntry.addFeaturesLexicalEntries(new LayoutToken("Text"), "LE","LINESTART","NEWFONT");
        String outputString = output.printVector();

        assertThat(outputString, is("Text text T Te Tex Text t xt ext Text 0.0 false false INITCAP NOPUNCT LINESTART NEWFONT LE"));
    }


    @Test
    public void testCreateFeaturesFromLayoutTokens()throws Exception {
        Pair<Document, SortedSet<DocumentPiece>> input = prepare("BasicEnglish.pdf");
        LayoutTokenization layoutTokenization = target2.getLayoutTokenizations(input.a, input.b);
        StringBuilder output = target.createFeaturesFromLayoutTokens(layoutTokenization);
        assertThat(output, notNullValue());
        System.out.println("hi"+output);

    }

    @Test
    public void testCreateFeaturesFromPDF() throws Exception {
        File input = new File(this.getClass().getResource("BasicEnglish.pdf").toURI());
        String output = target.createFeaturesFromPDF(input).toString();
//        System.out.println("hi"+output);

        assertThat(output, notNullValue());
    }


    Pair<Document, SortedSet<DocumentPiece>> prepare(String file) {

        File input = null;
        try {
            input = new File(this.getClass().getResource(file).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DocumentSource documentSource = DocumentSource.fromPdf(input, config.getStartPage(), config.getEndPage(), config.getPdfAssetPath() != null);
        Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, config);
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);

        return new Pair<>(doc, documentBodyParts);

    }


}
