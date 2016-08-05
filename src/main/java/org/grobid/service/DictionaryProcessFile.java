package org.grobid.service;

import org.grobid.core.data.Figure;
import org.grobid.core.data.Table;
import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.*;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.layout.LayoutTokenization;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.grobid.core.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.function.Consumer;

/**
 * Created by med on 29.07.16.
 */
public class DictionaryProcessFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryProcessFile.class);

    /**
     * Uploads the origin document which shall be extracted into TEI.
     *
     * @param inputStream the data of origin document
     * @return a response object mainly contain the TEI representation of the
     * full text
     */

    public static Response processLexicalEntries(final InputStream inputStream) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal;
        // Does GrobidServiceProperties need to be imported or use properties as in DictionaryRestService class?
//        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;


         /*
            PDF -> [pdf2xml] -> XML -> [GROBID Segmenter model] ->  Segmented document -> [LexicalEntriesParser] -> List<LexicalEntries>
         */

        try {
            LOGGER.debug(">> set raw text for stateless quantity service'...");
            long start = System.currentTimeMillis();

            // Does GrobidRestUtils need to be imported ?
            originFile = writeInputFile(inputStream);

            if (originFile == null) {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process - single thread! :)

                GrobidAnalysisConfig config =
                        GrobidAnalysisConfig.builder()
//                                .consolidateHeader(false)
//                                .consolidateCitations(false)
//                                .startPage(-1)
//                                .endPage(-1)
                                .generateTeiIds(true)
//                                .pdfAssetPath(null)
                                .build();
                LexicalEntriesParser lexEntriesParser = new LexicalEntriesParser();
                System.out.println(lexEntriesParser.processing(originFile,config));
                //List<LexicalEnties> entries = lexicalEntriesParser.extractLexicalEntries(documentBodyParts, config);


            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.", nseExp);
            response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getCause().getMessage()).build();
        } finally {
            removeTempFile(originFile);
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


    //To be moved somewhere generic where can be used.
    @Deprecated
    public static File writeInputFile(InputStream inputStream) {
        LOGGER.debug(">> set origin document for stateless service'...");

        File originFile = null;
        OutputStream out = null;
        try {
            originFile = newTempFile("origin", ".pdf");

            out = new FileOutputStream(originFile);

            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            LOGGER.error(
                    "An internal error occurs, while writing to disk (file to write '"
                            + originFile + "').", e);
            originFile = null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                inputStream.close();
            } catch (IOException e) {
                LOGGER.error("An internal error occurs, while writing to disk (file to write '"
                        + originFile + "').");
                originFile = null;
            }
        }
        return originFile;
    }

    @Deprecated
    public static File newTempFile(String fileName, String extension) {
        try {
            return File.createTempFile(fileName, extension, GrobidProperties.getTempPath());
        } catch (IOException e) {
            throw new GrobidException(
                    "Could not create temprorary file, '" + fileName + "." +
                            extension + "' under path '" + GrobidProperties.getTempPath() + "'.");
        }
    }

    @Deprecated
    public static void removeTempFile(final File file) {
        try {
            // sanity cleaning
            Utilities.deleteOldies(GrobidProperties.getTempPath(), 300);
            LOGGER.debug("Removing " + file.getAbsolutePath());
            file.delete();
        } catch (Exception exp) {
            LOGGER.error("Error while deleting the temporary file: " + exp);
        }
    }
}
