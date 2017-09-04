package org.grobid.trainer.sax;

import org.grobid.core.engines.label.LexicalEntryLabels;
import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Created by med on 15.11.16.
 */
public class TEIDictionarySegmentationSaxParser extends DefaultHandler {

    private StringBuffer accumulator = null; // current accumulated text

    private String output = null;
    private Stack<String> currentTags = null;
    private String currentTag = null;


    private List<String> labeled = null; // store line by line the labeled data

    public TEIDictionarySegmentationSaxParser() {
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
                // For text outside the three principle tags that will go to <dictScrap>
                currentTag = "<pc>";
                writeData(qName, false);

            }
            accumulator.setLength(0);

            currentTags.push("<"+qName+">");
            currentTag = "<"+qName+">";

        }

    }

    private void writeData(String qName, boolean pop) {
        if ( (qName.equals("body")||
                (qName.equals("headnote"))) ||
                (qName.equals("footnote"))||
        (qName.equals("dictScrap")) ||
                (qName.equals("text")) ) {
            if (currentTag == null) {
                return;
            }

            // For text outside the four principle tags that will go to <pc>
            if (pop) {
                if (!currentTags.empty()) {
                    currentTags.pop();
                }
            }

            if(qName.equals("text")){
                currentTag = "<pc>";
            }

            String text = getText();
            boolean begin = true;
            String[] tokens = text.split("\\+L\\+");

            boolean page = false;
            for(int p=0; p<tokens.length; p++) {
                String line = tokens[p].trim();
                if (line.equals("\n"))
                    continue;
                if (line.length() == 0)
                    continue;
                if (line.indexOf("+PAGE+") != -1) {
                    // page break should be a distinct feature
                    //labeled.add("@newpage\n");
                    line = line.replace("+PAGE+", "");
                    page = true;
                }

                StringTokenizer st = new StringTokenizer(line, " \t");
                if (!st.hasMoreTokens())
                    continue;
                String tok = st.nextToken();
                if (tok.length() == 0) continue;

                if (begin) {
                    labeled.add(tok + " I-" + currentTag + "\n");
                    begin = false;
                } else {
                    labeled.add(tok + " " + currentTag + "\n");
                }
                if (page) {
                    labeled.add("@newpage\n");
                    page = false;
                }

            }
            accumulator.setLength(0);
        }

    }

}
