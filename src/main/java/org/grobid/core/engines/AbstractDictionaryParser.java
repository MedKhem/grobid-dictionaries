package org.grobid.core.engines;

import org.grobid.core.GrobidModel;
import org.grobid.core.analyzers.GrobidAnalyzer;
import org.grobid.core.engines.tagging.GenericTagger;
import org.grobid.core.engines.tagging.GrobidCRFEngine;
import org.grobid.core.engines.tagging.TaggerFactory;
import org.grobid.core.utilities.counters.CntManager;
import org.grobid.core.utilities.counters.impl.CntManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Med on 05.12.19.
 */
public class AbstractDictionaryParser implements GenericTagger, Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDictionaryParser.class);
    private GenericTagger genericTagger;
    protected GrobidAnalyzer analyzer = GrobidAnalyzer.getInstance();

    protected CntManager cntManager = CntManagerFactory.getNoOpCntManager();

    protected AbstractDictionaryParser(GrobidModel model, String modelCategory) {
        this(model, CntManagerFactory.getNoOpCntManager(), modelCategory);
    }

    protected AbstractDictionaryParser(GrobidModel model, CntManager cntManager, String modelCategory) {
        this.cntManager = cntManager;
        genericTagger = DictionaryTaggerFactory.getTagger(model);
    }

    protected AbstractDictionaryParser(GrobidModel model, CntManager cntManager, GrobidCRFEngine engine) {
        this.cntManager = cntManager;
        genericTagger = DictionaryTaggerFactory.getTagger(model, engine);
    }

    @Override
    public String label(Iterable<String> data) {
        return genericTagger.label(data);
    }

    @Override
    public String label(String data) {
        return genericTagger.label(data);
    }

    @Override
    public void close() throws IOException {
        try {
            genericTagger.close();
        } catch (Exception e) {
            LOGGER.warn("Cannot close the parser: " + e.getMessage());
            //no op
        }
    }
}
