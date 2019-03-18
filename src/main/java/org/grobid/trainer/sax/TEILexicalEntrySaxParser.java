package org.grobid.trainer.sax;

/**
 * SAX parser for the TEI format for fulltext data encoded for training. Normally all training data should
 * be in this unique format for the fulltext model.
 * The segmentation of tokens must be identical as the one from pdf2xml files so that
 * training and online input tokens are aligned.
 *
 * @author Patrice Lopez, Mohamed Khemakhem
 */

import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.grobid.core.engines.label.DictionaryBodySegmentationLabels.DICTIONARY_ENTRY_LABEL;


public class TEILexicalEntrySaxParser extends DefaultHandler {

    private StringBuffer accumulator = null; // current accumulated text

    private Stack<String> currentTags = null;
    private String currentTag = null;
    private List<String> labeled = null; // store line by line the labeled data
    private String parentTag = DICTIONARY_ENTRY_LABEL;

    public TEILexicalEntrySaxParser() {
        labeled = new ArrayList<>();
        currentTags = new Stack<>();
        accumulator = new StringBuffer();
    }

    //Store the text of an element
    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    //Get the text of the document
    public String getText() {
        if (accumulator != null) {
            return accumulator.toString().trim();
        } else {
            return null;
        }
    }

    public List<String> getLabeledResult() {
        return labeled;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ((!qName.equals("lb")) && (!qName.equals("pb"))) {
            writeData(qName, true);
            if (!currentTags.empty()) {
                currentTag = currentTags.peek();
            }
        }

    }

    @Override
    public void startElement(String namespaceURI, String localName,
                             String qName, Attributes atts)
            throws SAXException {

        if (qName.equals("lb")) {
            accumulator.append(" +L+ ");
        } else if (qName.equals("pb")) {
            accumulator.append(" +PAGE+ ");
        } else if (qName.equals("space")) {
            accumulator.append(" ");
        } else {
            if (qName.equals("entry") || qName.equals("re")) {
                parentTag = "<" + qName + ">";
            }

            // we have to write first what has been accumulated yet with the upper-level tag
            String text = getText();
            if (isNotBlank(text)) {
                currentTag = "<pc>";
                writeData(qName, false);

            }
            accumulator.setLength(0);

            currentTags.push("<" + qName + ">");
            currentTag = "<" + qName + ">";

        }

    }

    private void writeData(String qName, boolean pop) {
        if ((qName.equals("form")) || (qName.equals("etym")) || (qName.equals("inflected")) ||  (qName.equals("ending")) || (qName.equals("note")) ||
                (qName.equals("sense")) || (qName.equals("re")) || (qName.equals("num")) || (qName.equals("dictScrap"))|| (qName.equals("xr"))) {
            if (currentTag == null) {
                return;
            }

            if (pop) {
                if (!currentTags.empty()) {
                    currentTags.pop();
                }
            }
            if (qName.equals("entry")) {
                currentTag = "<pc>";
            }

            String text = getText();
            // we segment the text
            StringTokenizer st = new StringTokenizer(text, " \n\t" + TextUtilities.fullPunctuations, true);
            boolean begin = true;
            while (st.hasMoreTokens()) {
                String tok = st.nextToken().trim();
                if (tok.length() == 0)
                    continue;

                if (tok.equals("+L+")) {
                    //labeled.add("@newline\n");
                } else if (tok.equals("+PAGE+")) {
                    // page break should be a distinct feature
                    //labeled.add("@newpage\n");
                } else {
                    String content = tok;
                    int i = 0;
                    if (content.length() > 0) {
                        if (begin) {
                            labeled.add(content + " I-" + currentTag + "\n");
                            begin = false;
                        } else {
                            labeled.add(content + " " + currentTag + "\n");
                        }
                    }
                }
                begin = false;
            }
            accumulator.setLength(0);
        }
    }

}
