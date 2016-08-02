package org.grobid.core.engines;

import org.grobid.core.GrobidModels;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutToken;
import org.grobid.core.utilities.OffsetPosition;
import org.grobid.data.LexicalEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by med on 02.08.16.
 */
public class LexicalEntriesParser extends AbstractParser {
    private static final Logger logger = LoggerFactory.getLogger(LexicalEntriesParser.class);

    private static volatile LexicalEntriesParser instance;


    public static LexicalEntriesParser getInstance() {
        if (instance == null) {
            getNewInstance();
        }
        return instance;
    }

    /**
     * Create a new instance.
     */
    private static synchronized void getNewInstance() {

        instance = new LexicalEntriesParser();
    }

    private LexicalEntry lexicalEntryLexicon = null;

    private LexicalEntriesParser() {
        super(GrobidModels.QUANTITIES);
      //  lexicalEntryLexicon = LexicalEntry.getInstance();

    }

    /**
     * Extract all occurrences of measurement/quantities from a simple piece of text.
     */
    public Response extractLexicalEntries(File inputFile, GrobidAnalysisConfig config) throws Exception {

        Response ob = null;
        return ob;

    }

}
