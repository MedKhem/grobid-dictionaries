package org.grobid.core.engines;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.SortedSet;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by lfoppiano on 11/08/16.
 */
public class LexicalEntriesParserTest {

    LexicalEntriesParser target;

    @Before
    public void setUp() throws Exception {
        target = new LexicalEntriesParser();
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
    public void testProcess() throws Exception {
        File input = new File(this.getClass().getResource("BasicEnglish.pdf").toURI());

        String output = target.process(input);

        assertThat(output, notNullValue());
    }

    @Test
    public void testGetLayoutTokenizations() throws Exception {
        File input = new File(this.getClass().getResource("BasicEnglish.page1.sample.pdf").toURI());

        GrobidAnalysisConfig config = GrobidAnalysisConfig.defaultInstance();
        DocumentSource documentSource = DocumentSource.fromPdf(input, config.getStartPage(), config.getEndPage(), config.getPdfAssetPath() != null);
        Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, config);
        SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);

        LayoutTokenization layoutTokenization = target.getLayoutTokenizations(doc, documentBodyParts);
        assertThat(layoutTokenization.getTokenization().isEmpty(), is(false));
        assertThat(layoutTokenization.getTokenization().size(), is(1696));
    }
}
