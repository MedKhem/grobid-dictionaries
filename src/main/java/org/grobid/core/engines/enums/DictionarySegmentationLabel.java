package org.grobid.core.engines.enums;

/**
 * Created by med on 08.11.16.
 */
public enum DictionarySegmentationLabel {


    HEADNOTE("<headnote>"),
    BODY("<body>"),
    FOOTNOTE("<footnote>");


    private String tag;

    DictionarySegmentationLabel(String s) {
        tag = s;
    }

    public boolean equalsName(String otherName) {
        return otherName != null && tag.equals(otherName);
    }


    public String getLabel() {
        return this.tag;
    }
}
