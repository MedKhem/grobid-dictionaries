package org.grobid.trainer.sax;
import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.grobid.core.engines.label.LexicalEntryLabels.LEXICAL_ENTRY_ETYM_LABEL;

/**
 * Created by Med on 04.09.17.
 */
public class TEIEtymSaxParser extends DefaultHandler {

    private StringBuffer accumulator = null; // current accumulated text

    private Stack<String> currentTags = null;
    private String currentTag = null;
    private List<String> labeled = null; // store line by line the labeled data
    private String parentTag = LEXICAL_ENTRY_ETYM_LABEL;

    public TEIEtymSaxParser() {
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
            if (qName.equals("etym")) {
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
        if ((qName.equals("mentioned")) || (qName.equals("lang")) ||
                (qName.equals("seg")) || (qName.equals("bibl")) || (qName.equals("def")) || (qName.equals("dictScrap"))) {
            if (currentTag == null) {
                return;
            }

            if (pop) {
                if (!currentTags.empty()) {
                    currentTags.pop();
                }
            }
            if (qName.equals("etym")) {
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
                            labeled.add(content + " " + parentTag + " I-" + currentTag + "\n");
                            begin = false;
                        } else {
                            labeled.add(content + " " + parentTag + " " + currentTag + "\n");
                        }
                    }
                }
                begin = false;
            }
            accumulator.setLength(0);
        }
    }
}

