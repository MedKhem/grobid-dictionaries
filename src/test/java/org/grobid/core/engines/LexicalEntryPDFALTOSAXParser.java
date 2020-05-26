package org.grobid.core.engines;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.layout.GraphicObject;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.sax.PDFALTOSaxHandler;
import org.junit.Before;
import org.junit.Test;

import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL;
import static org.hamcrest.CoreMatchers.is;
import org.grobid.core.features.FeatureVectorLexicalEntry;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by Med on 12.05.20.
 */
public class LexicalEntryPDFALTOSAXParser {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    PDFALTOSaxHandler target;
    DocumentSource mockDocumentSource;
    Document document;
    private List<GraphicObject> images;

    @Before
    public void setUp() throws Exception {

        mockDocumentSource = createMock(DocumentSource.class);

        document = Document.createFromText("");
        images = new ArrayList<>();
        target = new PDFALTOSaxHandler(document, images);
    }

    @Test
    public void testParsing_pdf2XMLwithNoIMages_ShouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/pdfalto_noImages.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);

        List<LayoutToken> tokenList = target.getTokenization();
        System.out.print(tokenList.toString());
        assertTrue(tokenList.size() > 0);
        assertTrue(document.getImages().size() == 0);
        assertTrue(images.size() == 0);
        assertTrue(document.getPages().size() == 4);
        assertTrue(document.getBlocks().size() == 26);
    }

    @Test
    public void testParsing_pdfAltoCatalogs_ShouldWork() throws Exception {
        InputStream is = this.getClass().getResourceAsStream("/1871_08_RDA_N028-1.xml_final.new.xml");

        SAXParser p = spf.newSAXParser();
        p.parse(is, target);
        List<LayoutToken> tokenList = target.getTokenization();
        String featSeg = FeatureVectorLexicalEntry.createFeaturesFromLayoutTokens(tokenList, DICTIONARY_ENTRY_LABEL).toString();

//        System.out.print(featSeg);
        assertTrue(tokenList.size() > 0);
//        assertTrue(document.getImages().size() == 0);
//        assertTrue(images.size() == 0);
//        assertTrue(document.getPages().size() == 4);
//        assertTrue(document.getBlocks().size() == 26);
    }




}
