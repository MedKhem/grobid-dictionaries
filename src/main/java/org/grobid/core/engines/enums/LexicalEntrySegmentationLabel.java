package org.grobid.core.engines.enums;

/**
 * Created by med on 08.11.16.
 */
public enum LexicalEntrySegmentationLabel {


    FORM("<form>"),
    ETYM("<etym>"),
    SENSE("<sense>"),
    RE("<re>"),
    NOTE("<note>");


    private String tag;

    LexicalEntrySegmentationLabel(String s) {
        tag = s;
    }

    public boolean equalsName(String otherName) {
        return otherName != null && tag.equals(otherName);
    }


    public String getLabel() {
        return this.tag;
    }
}
