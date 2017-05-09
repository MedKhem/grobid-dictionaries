package org.grobid.trainer.sax;

import org.apache.commons.lang3.StringUtils;
import org.grobid.core.data.LabeledForm;
import org.grobid.core.utilities.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;


public class TEIFormSaxParser extends DefaultHandler {

    private StringBuffer accumulator = null;
    private Stack<String> currentTags = null;
    private String currentTag = null;

    private LabeledForm currentForm = null;
    private List<LabeledForm> labeled = null;


    public TEIFormSaxParser() {
        labeled = new ArrayList<>();
        currentTags = new Stack<>();
        accumulator = new StringBuffer();
    }

    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
            throws SAXException {

        if (qName.equals("lb")) {
            accumulator.append(" +L+ ");
        } else if (qName.equals("pb")) {
            accumulator.append(" +PAGE+ ");
        } else if (qName.equals("space")) {
            accumulator.append(" ");
        } else {
            if (isFormTag(qName)) {
                currentForm = new LabeledForm();
                for (int i = 0; i < atts.getLength(); i++) {
                    if (StringUtils.equals(atts.getLocalName(i), "parent")) {
                        currentForm.setParentTag(atts.getValue(i));
                        break;
                    }
                }
            }
            accumulator.setLength(0);

            currentTags.push("<" + qName + ">");
            currentTag = "<" + qName + ">";
        }

    }

    @Override
    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {
        if (isRelevantTag(qName)) {
            writeData();
            if (!currentTags.isEmpty()) {
                currentTag = currentTags.peek();
            }
        } else if ("form".equals(qName)) {
            labeled.add(currentForm);
        }
    }

    @Override
    public void characters(char[] buffer, int start, int length) {
        accumulator.append(buffer, start, length);
    }

    public String getText() {
        if (accumulator != null) {
            return accumulator.toString().trim();
        } else {
            return null;
        }
    }

    public List<LabeledForm> getLabeledResult() {
        return labeled;
    }

    private void writeData() {
        if (currentTag == null) {
            return;
        }

        String text = getText();
        // we segment the text
        StringTokenizer st = new StringTokenizer(text, " \n\t" + TextUtilities.fullPunctuations, true);
        boolean begin = true;
        while (st.hasMoreTokens()) {
            String tok = st.nextToken().trim();
            if (tok.length() == 0)
                continue;

            String content = tok;
            if (content.length() > 0) {
                if (begin) {
                    currentForm.addLabel(new Pair(content, "I-" + currentTag));
                    begin = false;
                } else {
                    currentForm.addLabel(new Pair(content, currentTag));
                }
            }
            begin = false;
        }
        accumulator.setLength(0);
    }

    private boolean isRelevantTag(String qName) {
        if ("orth".equals(qName) || "pron".equals(qName)
                || "gramGrp".equals(qName) || "other".equals(qName)) {
            return true;
        }
        return false;
    }

    private boolean isFormTag(String qName) {
        if ("form".equals(qName)) {
            return true;
        }
        return false;
    }

}
