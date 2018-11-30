package org.grobid.core.engines;

import org.grobid.core.EngineMockTest;
import org.grobid.core.document.DictionaryDocument;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.engines.label.DictionaryBodySegmentationLabels;
import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by med on 16.11.16.
 */
public class DictionarySegmentationTest extends EngineMockTest {
    DictionarySegmentationParser target;

    @Before
    public void setUp() throws Exception {
        target = new DictionarySegmentationParser();
    }

//    @Test
//    public void testProcess() throws Exception {
//        String inputDirectory = "/home/med/git/grobid/grobid-dictionaries/resources/dataset/dictionary-segmentation/corpus/pdf/";
//        String outputDirectory = "/home/med/git/grobid/grobid-dictionaries/tmp";
//
//       int n = target.createTrainingBatch(inputDirectory,outputDirectory);
//
//        assertFalse(n == 0);
//    }

        @Test
    public void testProcess() throws Exception {
            File input = new File(this.getClass().getResource("BFirstTwo.pdf").toURI());
            String body = target.processToTEI(input);
//            System.out.print(body);
            assertThat(body, notNullValue());

    }

//    @Test
//    public void testOptimise() throws Exception {
//        File input = new File(this.getClass().getResource("Annuaire1198-1207-Page01-03.pdf").toURI());
//        GrobidAnalysisConfig config = GrobidAnalysisConfig.builder().generateTeiIds(true).build();
//        DictionaryDocument doc = target.initiateProcessing( input,  config);
//        target.optimise(doc, DictionarySegmentationLabels.DICTIONARY_DICTSCRAP_LABEL);
//        //Optimise Headnotes
//
//        System.out.println(doc.getFootnotesOptimised().getLabels().toString());
//
//    }

}
