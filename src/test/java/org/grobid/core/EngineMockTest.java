package org.grobid.core;

import org.grobid.core.engines.Engine;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidDictionaryProperties;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EngineMockTest {
    private static Logger LOGGER = LoggerFactory.getLogger(EngineMockTest.class);
    protected static Engine engine;

    @BeforeClass
    public static void initInitialContext() throws Exception {
        LibraryLoader.load();
        GrobidDictionaryProperties.getInstance();
        engine = GrobidFactory.getInstance().createEngine();
    }
}