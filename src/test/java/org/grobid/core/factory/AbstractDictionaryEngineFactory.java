package org.grobid.core.factory;

import org.grobid.core.engines.Engine;
import org.grobid.core.engines.ModelMap;
import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.grobid.core.lexicon.Lexicon;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidDictionaryProperties;
import org.grobid.core.utilities.GrobidDictionaryProperties;

/**
 * Created by Med on 14.12.19.
 */
public class AbstractDictionaryEngineFactory {


    /**
     * The engine.
     */
    private static Engine engine;

    /**
     * Return a new instance of engine if it doesn't exist, the existing
     * instance else.
     *
     * @return Engine
     */
    protected synchronized Engine getEngine() {
        return getEngine(false);
    }

    /**
     * Return a new instance of engine if it doesn't exist, the existing
     * instance else.
     *
     * @return Engine
     */
    protected synchronized Engine getEngine(boolean preload) {
        if (engine == null) {
            engine = createEngine(preload);
        }
        return engine;
    }

    /**
     * Return a new instance of engine.
     *
     * @return Engine
     */
    protected Engine createEngine() {
        return createEngine(false);
    }

    /**
     * Return a new instance of engine.
     *
     * @return Engine
     */
    protected Engine createEngine(boolean preload) {
        return new Engine(preload);
    }

    /**
     * Initializes all necessary things for starting grobid
     */
    public static void init() {
        GrobidDictionaryProperties.getInstance();
        LibraryLoader.load();
        Lexicon.getInstance();
    }

    /**
     * Initializes all the models 
     */
    @Deprecated
    public static void fullInit() {
        init();
        if (GrobidDictionaryProperties.getGrobidCRFEngine() == GrobidCRFEngine.CRFPP) {
            ModelMap.initModels();
        }
        //Lexicon.getInstance();
    }
}
