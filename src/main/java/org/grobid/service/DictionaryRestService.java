package org.grobid.service;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

/**
 * RESTful service for GROBID dictionary extension.
 * <p>
 * Created by med on 29.07.16.
 */
@Singleton
@Path(DictionaryPaths.PATH_DICTIONARY)
public class DictionaryRestService implements DictionaryPaths {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryRestService.class);
    private static final String INPUT = "input";
    private static final String INPUT_BIB = "inputBib";

    public DictionaryRestService() {
        LOGGER.info("Initiating Servlet DictionaryRestService");
        try {
            LibraryLoader.load();
            GrobidProperties.getInstance();
        } catch (final Exception exp) {
            LOGGER.error("GROBID Dictionaries initialisation failed: " + exp);
        }
    }


    @Path(PROCESS_DICTIONARY_SEGMENTATION)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processDictionarySegmentation_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processDictionarySegmentation(inputStream);
    }
    @Path(PROCESS_BIBLIOGRAPHY_SEGMENTATION)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processBibliographySegmentation_post(@FormDataParam(INPUT_BIB) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processDictionarySegmentation(inputStream);
    }

    @Path(PROCESS_DICTIONARY_BODY_SEGMENTATION)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processDictionaryBodySegmentation_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processDictionaryBodySegmentation(inputStream, PROCESS_DICTIONARY_BODY_SEGMENTATION);
    }

    @Path(PROCESS_BIBLIOGRAPHY_BODY_SEGMENTATION)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processBibliographyBodySegmentation_post(@FormDataParam(INPUT_BIB) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processDictionaryBodySegmentation(inputStream, PROCESS_BIBLIOGRAPHY_BODY_SEGMENTATION);
    }
//    @Path(PROCESS_DICTIONARY_BODY_SEGMENTATION_OPTIMISED)
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.APPLICATION_XML)
//    @POST
//    public Response processDictionaryBodySegmentationOptimised_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
//        return DictionaryProcessFile.processDictionaryBodySegmentation(inputStream, PROCESS_DICTIONARY_BODY_SEGMENTATION_OPTIMISED);
//    }


    @Path(PATH_LEXICAL_ENTRY)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processLexicalEntries_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processLexicalEntries(inputStream, PATH_LEXICAL_ENTRY);
    }

    @Path(PATH_BIBLIOGRAPHY_ENTRY)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processBibliographyEntries_post(@FormDataParam(INPUT_BIB) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processLexicalEntries(inputStream, PATH_BIBLIOGRAPHY_ENTRY);
    }
//    @Path(PATH_LEXICAL_ENTRY_OPTIMISED)
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.APPLICATION_XML)
//    @POST
//    public Response processLexicalEntriesOptimised_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
//        return DictionaryProcessFile.processLexicalEntries(inputStream, PATH_LEXICAL_ENTRY_OPTIMISED);
//    }

    @Path("{form}/{sense}/{etym}/{re}/{xr}/{subEntry}/{note}.processFullDictionary")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processFullDictionary_post(@FormDataParam(INPUT) InputStream inputStream,
                                               @PathParam("form") String form,
                                               @PathParam("sense") String sense,
                                               @PathParam("etym") String etym,
                                               @PathParam("re") String re,
                                               @PathParam("xr") String xr,
                                               @PathParam("subEntry") String subEntry,
                                               @PathParam("note") String note) throws Exception {

        return DictionaryProcessFile.processFullDictionary(inputStream, form+"-"+sense+"-"+etym+"-"+re+"-"+xr+"-"+subEntry+"-"+note);
    }

//    @Path(PATH_FULL_DICTIONARY_OPTIMISED)
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.APPLICATION_XML)
//    @POST
//    public Response processFullDictionaryOptimised_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
//        return DictionaryProcessFile.processFullDictionary(inputStream, PATH_FULL_DICTIONARY_OPTIMISED);
//    }

}
