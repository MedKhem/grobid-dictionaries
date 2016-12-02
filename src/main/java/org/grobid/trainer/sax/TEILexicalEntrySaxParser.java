package org.grobid.trainer.sax;

/**
 *
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
import org.grobid.core.engines.label.LexicalEntryLabels;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import static org.apache.commons.lang3.StringUtils.isBlank;


public class TEILexicalEntrySaxParser extends DefaultHandler {

    private StringBuffer accumulator = null; // current accumulated text

    private String output = null;
    private Stack<String> currentTags = null;
    private String currentTag = null;
    private LexicalEntryLabels possibleTag;

    private boolean figureBlock = false;
    private boolean tableBlock = false;

    private List<String> labeled = null; // store line by line the labeled data

    public TEILexicalEntrySaxParser() {
        labeled = new ArrayList<String>();
        currentTags = new Stack<String>();
        accumulator = new StringBuffer();
    }

    //Store the text of an element
    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    //Get the text of the document
    public String getText() {
        if (accumulator != null) {
            //System.out.println(accumulator.toString().trim());
            return accumulator.toString().trim();
        } else {
            return null;
        }
    }

    public List<String> getLabeledResult() {
        return labeled;
    }

    public void endElement(java.lang.String uri,
                           java.lang.String localName,
                           java.lang.String qName) throws SAXException {
        if ( (!qName.equals("lb")) && (!qName.equals("pb")) ) {
            writeData(qName, true);
            if (!currentTags.empty()) {
                currentTag = currentTags.peek();
            }
        }

    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {
        if (qName.equals("lb")) {
            accumulator.append(" +L+ ");
        }
        else if (qName.equals("pb")) {
            accumulator.append(" +PAGE+ ");
        }
        else if (qName.equals("space")) {
            accumulator.append(" ");
        }
        else {
            // we have to write first what has been accumulated yet with the upper-level tag
            String text = getText();
            if (!isBlank(text)) {
                    writeData(qName, false);

            }
            accumulator.setLength(0);

            currentTags.push("<"+qName+">");
            currentTag = "<"+qName+">";

        }

    }

    private void writeData(String qName, boolean pop) {
        if ( (qName.equals("entry")) ||
                (qName.equals("form")) ||
                (qName.equals("etym")) ||
                (qName.equals("sense")) ||
                (qName.equals("metamark")) ||
                (qName.equals("re")) ||
                (qName.equals("front")) ||
                (qName.equals("note"))
                ) {
            if (currentTag == null) {
                return;
            }

            if (pop) {
                if (!currentTags.empty()) {
                    currentTags.pop();
                }
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
                }
                else if (tok.equals("+PAGE+")) {
                    // page break should be a distinct feature
                    //labeled.add("@newpage\n");
                }
                else {
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
