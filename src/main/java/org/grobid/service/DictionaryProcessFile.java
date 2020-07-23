package org.grobid.service;

import org.grobid.core.engines.*;
import org.grobid.core.utilities.IOUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.*;
import java.util.NoSuchElementException;

/**
 * Created by med on 29.07.16.
 */
public class DictionaryProcessFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryProcessFile.class);
    public static Response processDictionarySegmentation(final InputStream inputStream) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        String retVal;
        // Does GrobidServiceProperties need to be imported or use properties as in DictionaryRestService class?
//        boolean isparallelExec = GrobidServiceProperties.isParallelExec();
        File originFile = null;
        Engine engine = null;

         /*
            PDF -> [pdf2xml] -> XML -> [GROBID Segmenter model] ->  Segmented document -> [DictionarySegmentationParser] -> List<LexicalEntries>
         */
        try {
            LOGGER.debug(">> set raw text for stateless quantity service'...");
            long start = System.currentTimeMillis();

            // Does GrobidRestUtils need to be imported ?
            originFile = IOUtilities.writeInputFile(inputStream);

            if (originFile == null) {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process - single thread! :)
                DictionarySegmentationParser dictionarySegmentationParser = new DictionarySegmentationParser();

                response = Response.ok(dictionarySegmentationParser.processToTEI(originFile)).build();
            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.", nseExp);
            response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
        } finally {
            IOUtilities.removeTempFile(originFile);
        }
        LOGGER.debug(methodLogOut());
        return response;
    }


    public static File writeInputAltoFile(InputStream inputStream) {
        LOGGER.debug(">> set origin document for stateless service'...");

        File originFile = null;
        OutputStream out = null;
        try {
            originFile = IOUtilities.newTempFile("origin", ".xml");

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
                        + originFile + "').", e);
                originFile = null;
            }
        }
        return originFile;
    }

    /**
     * Uploads the origin document which shall be extracted into TEI.
     *
     * @param inputStream the data of origin document
     * @return a response object mainly contain the TEI representation of the
     * full text
     */
    public static Response processInputFile(final InputStream inputStream, String modelToRun, String fileType) {
        LOGGER.debug(methodLogIn());
        Response response = null;
        File originFile = null;

        try {
            LOGGER.debug(">> set raw text for stateless quantity service'...");
            long start = System.currentTimeMillis();

            // The second file writing will close the inputstream. The closing in the first for Alto has been deactivated
            if (fileType.equals("PDF")){
                originFile = IOUtilities.writeInputFile(inputStream);
            }else if(fileType.equals("ALTO")){
                originFile = writeInputAltoFile(inputStream);
            }



            if (originFile == null ) {
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } else {
                // starts conversion process - single thread! :)

                DictionaryBodySegmentationParser dictionaryBodySegmentationParser = new DictionaryBodySegmentationParser();

                response = Response.ok(dictionaryBodySegmentationParser.processPDFToTEI(originFile, modelToRun)).build();



            }
        } catch (NoSuchElementException nseExp) {
            LOGGER.error("Could not get an engine from the pool within configured time. Sending service unavailable.", nseExp);
            response = Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            LOGGER.error("An unexpected exception occurs. ", e);
            String message = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(message).build();
        } finally {
            IOUtilities.removeTempFile(originFile);
//            IOUtilities.removeTempFile(originALTOFile);
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
