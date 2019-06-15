package org.grobid.trainer.sax;

import org.grobid.core.data.SimpleLabeled;
//import org.grobid.core.utilities.Pair;
import org.apache.commons.lang3.tuple.Pair;
import org.grobid.core.utilities.TextUtilities;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import static org.apache.commons.lang3.StringUtils.isNotBlank;


public class TEISenseSaxParser extends DefaultHandler {

    private StringBuffer accumulator = null;
    private Stack<String> currentTags = null;
    private String currentTag = null;


    private SimpleLabeled currentSense = null;
    private List<SimpleLabeled> labeled = null;



    public TEISenseSaxParser() {
        labeled = new ArrayList<>();
        currentTags = new Stack<>();
        accumulator = new StringBuffer();
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
    public List<SimpleLabeled> getLabeledResult() {
        return labeled;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isRelevantTag(qName)) {
            writeData();
            if (!currentTags.isEmpty()) {
                currentTag = currentTags.pop();
            }
        }else if ("sense".equals(qName)) {
            labeled.add(currentSense);
        }

    }
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
            throws SAXException {


        if (qName.equals("lb")) {
//            accumulator.append(" +L+ ");
        } else if (qName.equals("pb")) {
//            accumulator.append(" +PAGE+ ");
        } else if (qName.equals("space")) {
//            accumulator.append(" ");
        } else {
            if (isSenseTag(qName) ) {

                    currentSense = new SimpleLabeled();

            }

            // we have to write first what has been accumulated yet with the upper-level tag
            String text = getText();
            if (isNotBlank(text)) {
                currentTag = "<pc>";
                writeData();

            }
            accumulator.setLength(0);

            currentTags.push("<" + qName + ">");
            currentTag = "<" + qName + ">";

        }
    }







    private void writeData() {
        if (currentTag == null) {
            return;
        }
//
//        if (pop) {
//            if (!currentTags.empty()) {
//                currentTags.pop();
//            }
//        }
//        if (qName.equals("sense")) {
//            currentTag = "<pc>";
//        }

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
                        currentSense.addLabel(Pair.of(content, "I-" + currentTag));

                        begin = false;
                    } else {
                        currentSense.addLabel(Pair.of(content,   currentTag));
                    }
                }

            begin = false;
        }
        accumulator.setLength(0);
    }

    private boolean isRelevantTag(String qName) {
        if (("subSense".equals(qName)) || "gramGrp".equals(qName) || "num".equals(qName)
                || "note".equals(qName) || "dictScrap".equals(qName) || "pc".equals(qName) ) {
            return true;
        }
        return false;
    }



    private boolean isSenseTag(String qName) {
        if ("sense".equals(qName)) {
            return true;
        }
        return false;
    }

}
