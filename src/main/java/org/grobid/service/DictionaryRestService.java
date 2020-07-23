package org.grobid.service;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.utilities.GrobidDictionaryProperties;
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
            GrobidDictionaryProperties.getInstance();
        } catch (final Exception exp) {
            LOGGER.error("GROBID Dictionaries initialisation failed: " + exp);
        }
    }


    @Path("{fileType}.processDictionarySegmentation")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processDictionarySegmentation_post(@FormDataParam(INPUT) InputStream inputStream,
                                                       @PathParam("fileType") String fileType) throws Exception {
        return DictionaryProcessFile.processDictionarySegmentation(inputStream);
    }
    @Path("{fileType}.processBibliographySegmentation")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processBibliographySegmentation_post(@FormDataParam(INPUT_BIB) InputStream inputStream,
                                                         @PathParam("fileType") String fileType) throws Exception {
        return DictionaryProcessFile.processDictionarySegmentation(inputStream);
    }

    @Path("{fileType}.processDictionaryBodySegmentation")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processDictionaryBodySegmentation_post(@FormDataParam(INPUT) InputStream inputStream,
                                                           @PathParam("fileType") String fileType) throws Exception {
        return DictionaryProcessFile.processInputFile(inputStream, PROCESS_DICTIONARY_BODY_SEGMENTATION,fileType);
    }

    @Path("{fileType}.processBibliographyBodySegmentation")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processBibliographyBodySegmentation_post(@FormDataParam(INPUT_BIB) InputStream inputStream,
                                                             @PathParam("fileType") String fileType) throws Exception {
        return DictionaryProcessFile.processInputFile(inputStream, PROCESS_BIBLIOGRAPHY_BODY_SEGMENTATION,fileType);
    }
//    @Path(PROCESS_DICTIONARY_BODY_SEGMENTATION_OPTIMISED)
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.APPLICATION_XML)
//    @POST
//    public Response processDictionaryBodySegmentationOptimised_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
//        return DictionaryProcessFile.processDictionaryBodySegmentation(inputStream, PROCESS_DICTIONARY_BODY_SEGMENTATION_OPTIMISED);
//    }


    @Path("{fileType}/{constant}.processLexicalEntry")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processLexicalEntries_post(@FormDataParam(INPUT) InputStream inputStream,
                                               @PathParam("fileType") String fileType,
                                               @PathParam("constant") String constant) throws Exception {
        return DictionaryProcessFile.processInputFile(inputStream, constant,fileType);
    }

    @Path("{fileType}.processBibliographyEntry")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processBibliographyEntries_post(@FormDataParam(INPUT_BIB) InputStream inputStream,
                                                    @PathParam("fileType") String fileType) throws Exception {
        return DictionaryProcessFile.processInputFile(inputStream, PATH_BIBLIOGRAPHY_ENTRY,fileType);
    }
//    @Path(PATH_LEXICAL_ENTRY_OPTIMISED)
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.APPLICATION_XML)
//    @POST
//    public Response processLexicalEntriesOptimised_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
//        return DictionaryProcessFile.processInputFile(inputStream, PATH_LEXICAL_ENTRY_OPTIMISED);
//    }

    @Path("{fileType}/{form}/{sense}/{etym}/{re}/{xr}/{subEntry}/{note}.processFullDictionary")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processFullDictionary_post(@FormDataParam(INPUT) InputStream inputStream,
                                               @PathParam("fileType") String fileType,
                                               @PathParam("form") String form,
                                               @PathParam("sense") String sense,
                                               @PathParam("etym") String etym,
                                               @PathParam("re") String re,
                                               @PathParam("xr") String xr,
                                               @PathParam("subEntry") String subEntry,
                                               @PathParam("note") String note) throws Exception {

        return DictionaryProcessFile.processInputFile(inputStream, form+"-"+sense+"-"+etym+"-"+re+"-"+xr+"-"+subEntry+"-"+note,fileType);
    }

//    @Path(PATH_FULL_DICTIONARY_OPTIMISED)
//    @Consumes(MediaType.MULTIPART_FORM_DATA)
//    @Produces(MediaType.APPLICATION_XML)
//    @POST
//    public Response processFullDictionaryOptimised_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
//        return DictionaryProcessFile.processFullDictionary(inputStream, PATH_FULL_DICTIONARY_OPTIMISED);
//    }

}
