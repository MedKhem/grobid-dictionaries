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
@Path(DictionaryPathes.PATH_DICTIONARY)
public class DictionaryRestService implements DictionaryPathes {
    private static final Logger LOGGER = LoggerFactory.getLogger(DictionaryRestService.class);
    private static final String INPUT = "input";

    public DictionaryRestService() {
        LOGGER.info("Initiating Servlet DictionaryRestService");
        try {
            InitialContext intialContext = new javax.naming.InitialContext();
            String path2grobidHome = (String) intialContext.lookup("java:comp/env/org.grobid.home");
            String path2grobidProperty = (String) intialContext.lookup("java:comp/env/org.grobid.property");

            MockContext.setInitialContext(path2grobidHome, path2grobidProperty);

            System.out.println(path2grobidHome);
            System.out.println(path2grobidProperty);

            LibraryLoader.load();
            GrobidProperties.getInstance();

        } catch (final Exception exp) {
            System.err.println("GROBID Dictionaries initialisation failed: " + exp);
            exp.printStackTrace();
        }
    }


    @Path(PATH_HEADER)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processHeaderDocument_post(@FormDataParam(INPUT) InputStream inputStream,
                                               @FormDataParam("consolidate") String consolidate
    ) throws Exception {
        boolean consol = false;
        if ((consolidate != null) && (consolidate.equals("1"))) {
            consol = true;
        }
        Response ob = null;
        return ob;
        // return DictionaryProcessFile.processStatelessHeaderDocument(inputStream, consol, false);
    }

    @Path(PATH_ENTRIES)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    @POST
    public Response processLexicalEntries_post(@FormDataParam(INPUT) InputStream inputStream) throws Exception {
//        return DictionaryProcessFile.processLexicalEntries(inputStream);
        return null;
    }
}
