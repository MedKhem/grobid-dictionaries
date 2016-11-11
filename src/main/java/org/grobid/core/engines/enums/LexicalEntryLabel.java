package org.grobid.core.engines.enums;

/**
 * Created by med on 26.08.16.
 */
public enum LexicalEntryLabel {



    ENTRY("<entry>"),
    FORM("<form>"),
    ETYM("<etym>"),
    SENSE("<sense>"),
    METAMARK("<metamark>"),
    RE("<re>"),
    NOTE("<note>");


    private String tag;

    LexicalEntryLabel(String s) {
        tag = s;
    }

    public boolean equalsName(String otherName) {
        return otherName != null && tag.equals(otherName);
    }


    public String getLabel() {
        return this.tag;
    }

}
