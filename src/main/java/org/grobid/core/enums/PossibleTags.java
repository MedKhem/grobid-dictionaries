package org.grobid.core.enums;

/**
 * Created by med on 26.08.16.
 */
public enum PossibleTags {



    ENTRY("entry"),
    FORM("form"),
    ETYM("etym"),
    SENSE("sense"),
    METAMARK("metamark"),
    RE("re"),
    FRONT("front"),
    NOTE("note");


    private final String tag;

    private PossibleTags(String s) {
        tag = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : tag.equals(otherName);
    }

    public String toString() {
        return this.tag;
    }

}
