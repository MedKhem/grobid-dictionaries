package org.grobid.core.enums;

/**
 * Created by med on 26.08.16.
 */
public enum PossibleTags {



    ENTRY("<entry>"),
    FORM("<form>"),
    ETYM("<etym>"),
    SENSE("<sense>"),
    RE("<re>"),
    METAMARK("<metamark>"),
    NOTE("<note>");


    private final String tag;

    PossibleTags(String s) {
        tag = s;
    }

    public boolean equalsName(String otherName) {
        return otherName != null && tag.equals(otherName);
    }

    public String toString() {
        return this.tag;
    }

}
