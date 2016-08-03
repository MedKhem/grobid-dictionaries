package org.grobid.service;

import org.grobid.core.document.Document;
import org.grobid.core.document.DocumentPiece;
import org.grobid.core.document.DocumentSource;
import org.grobid.core.engines.Engine;
import org.grobid.core.engines.EngineParsers;
import org.grobid.core.engines.LexicalEntriesParser;
import org.grobid.core.engines.SegmentationLabel;
import org.grobid.core.engines.config.GrobidAnalysisConfig;
import org.grobid.core.exceptions.GrobidException;
import org.grobid.core.factory.GrobidFactory;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.core.utilities.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.*;
import java.util.NoSuchElementException;
import java.util.SortedSet;

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
                                                 final boolean consolidate) {
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
                engine = GrobidFactory.getInstance().getEngine();
                LexicalEntriesParser lexicalEntriesParser = LexicalEntriesParser.getInstance();

                // Do we need to create GrobidAnalysisConfig as in Grobid? if yes, if not how to adapt the method of GQ with inputStream?
                GrobidAnalysisConfig config =
                        GrobidAnalysisConfig.builder()
                                .consolidateHeader(consolidate)
                                .consolidateCitations(false)
                                .startPage(-1)
                                .endPage(-1)
                                .generateTeiIds(true)
                                .pdfAssetPath(null)
                                .build();

                // Segmenter to identify the document's block
                DocumentSource documentSource = DocumentSource.fromPdf(originFile, config.getStartPage(), config.getEndPage(), config.getPdfAssetPath() != null);
                Document doc = new EngineParsers().getSegmentationParser().processing(documentSource, config);

                SortedSet<DocumentPiece> documentBodyParts = doc.getDocumentPart(SegmentationLabel.BODY);

                //List<LexicalEnties> entries = lexicalEntriesParser.extractLexicalEntries(documentBodyParts, config);


                removeTempFile(originFile);


            }
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
