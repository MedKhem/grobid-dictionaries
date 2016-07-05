package org.grobid.core.trainer.sax;


import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX handler for TEI-style annotations. should work for patent PDM and our usual scientific paper encoding.
 * Measures are inline quantities annotations.
 * The training data for the CRF models are generated during the XML parsing.
 *
 * @author Patrice Lopez
 */
public class DictionaryAnnotationSaxHandler extends DefaultHandler {

}
