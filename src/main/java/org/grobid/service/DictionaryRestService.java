package org.grobid.service;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.resource.Singleton;
import org.grobid.core.main.LibraryLoader;
import org.grobid.core.mock.MockContext;
import org.grobid.core.utilities.GrobidProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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

    public DictionaryRestService() {
        LOGGER.info("Initiating Servlet DictionaryRestService");
        try {
            InitialContext intialContext = new javax.naming.InitialContext();
            String path2grobidHome = (String) intialContext.lookup("java:comp/env/org.grobid.home");
            String path2grobidProperty = (String) intialContext.lookup("java:comp/env/org.grobid.property");

            MockContext.setInitialContext(path2grobidHome, path2grobidProperty);

            LOGGER.debug(path2grobidHome);
            LOGGER.debug(path2grobidProperty);

            LibraryLoader.load();
            GrobidProperties.getInstance();

        } catch (final Exception exp) {
            LOGGER.error("GROBID Dictionaries initialisation failed: " + exp);
        }
    }


    @Path(PATH_DICTIONARY_SEGMENTATATION)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processDictionarySegmentation_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processDictionarySegmentation(inputStream);
    }

    @Path(PATH_DICTIONARY_BODY_SEGMENTATATION)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processDictionaryBodySegmentation_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processDictionaryBodySegmentation(inputStream,PATH_DICTIONARY_BODY_SEGMENTATATION);
    }

    @Path(PATH_LEXICAL_ENTRY)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processLexicalEntries_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processLexicalEntries(inputStream, PATH_LEXICAL_ENTRY);
    }

    @Path(PATH_FULL_DICTIONARY)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processFullDictionary_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
        return DictionaryProcessFile.processFullDictionary(inputStream, PATH_FULL_DICTIONARY);
    }

}
