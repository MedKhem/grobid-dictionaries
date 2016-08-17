package org.grobid.core.features.enums;

/**
 * Created by med on 17.08.16.
 */
public enum FontStatus {

    SAMEFONT("SAMEFONT"),
    NEWFONT("NEWFONT");


    private final String name;

    private FontStatus(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
