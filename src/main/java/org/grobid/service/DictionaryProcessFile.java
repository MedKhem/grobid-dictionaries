package org.grobid.service;

import org.grobid.core.engines.Engine;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.factory.GrobidPoolingFactory;
import org.grobid.data.LexicalEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.grobid.core.engines.LexicalEntriesParser;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by med on 29.07.16.
 */
public class DictionaryProcessFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryProcessFile.class);

    /**
     * Uploads the origin document which shall be extracted into TEI.
     *
     * @param inputStream the data of origin document
     * @param consolidate the consolidation option allows GROBID to exploit Crossref
     *                    web services for improving header information

     * @return a response object mainly contain the TEI representation of the
     * full text
     */

    public static Response processLexicalEntries(final InputStream inputStream,
                                                 final boolean consolidate)  {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal;
        // Does GrobidServiceProperties need to be imported or use properties as in DictionaryRestService class?
        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;

        try {
            LOGGER.debug(">> set raw text for stateless quantity service'...");
            long start = System.currentTimeMillis();

            // Does GrobidRestUtils need to be imported ?
            originFile = GrobidRestUtils.writeInputFile(inputStream);

           // Do we need to create GrobidAnalysisConfig as in Grobid? if yes, if not how to adapt the method of GQ with inputStream?
            GrobidAnalysisConfig config = null;


            LexicalEntriesParser lexicalEntriesParser = LexicalEntriesParser.getInstance();
            Response response1 = lexicalEntriesParser.extractLexicalEntries(originFile, config);
            long end = System.currentTimeMillis();

  /*          StringBuilder jsonBuilder = null;
            if (measurements != null) {
                jsonBuilder = new StringBuilder();
                jsonBuilder.append("{ ");
                jsonBuilder.append("\"runtime\" : " + (end - start));
                jsonBuilder.append(", \"measurements\" : [ ");
                boolean first = true;
                for (Measurement measurement : measurements) {
                    if (first)
                        first = false;
                    else
                        jsonBuilder.append(", ");
                    jsonBuilder.append(measurement.toJson());
                }
                jsonBuilder.append("] }");
            } else
                response = Response.status(Response.Status.NO_CONTENT).build();

            System.out.println(jsonBuilder.toString());

            if (jsonBuilder != null) {
                response = Response.status(Response.Status.OK).entity(jsonBuilder.toString())
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON + "; charset=UTF-8")
                        .build();
            } */
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.", nseExp);
            response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getCause().getMessage()).build();
        }
        LOGGER.debug(methodLogOut());
        return response;
    }

    private static String methodLogIn() {
        return ">> " + DictionaryProcessFile.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }

    private static String methodLogOut() {
        return "<< " + DictionaryProcessFile.class.getName() + "." + Thread.currentThread().getStackTrace()[1].getMethodName();
    }
}
