package org.grobid.trainer;

import org.grobid.core.engines.label.DictionarySegmentationLabels;
import org.grobid.core.factory.AbstractDictionaryEngineFactory;
import org.grobid.core.utilities.GrobidDictionaryProperties;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Created by Med on 05.12.19.
 */
public class DictionarySegmentationTrainerTest {


    DictionarySegmentationTrainer target;

    @BeforeClass
    public static void setInitialContext() throws Exception {

        AbstractDictionaryEngineFactory.init();
    }

    @Before
    public void setUp() throws Exception {
        target = new DictionarySegmentationTrainer();
        GrobidDictionaryProperties.getInstance();

    }

}
