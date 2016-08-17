package org.grobid.core.features.enums;

/**
 * Created by med on 17.08.16.
 */
public enum PonctuationType {

    OPENBRACKET("OPENBRACKET"),
    ENDBRACKET("ENDBRACKET"),
    DOT("DOT"),
    COMMA("COMMA"),
    HYPHEN("HYPHEN"),
    QUOTE("QUOTE"),
    SLASH("SLASH"),
    EXPONENT("EXPONENT"),
    NOPUNCT("NOPUNCT");

    private final String name;

    private PonctuationType(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
