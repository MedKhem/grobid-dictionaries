package org.grobid.core.features.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by med on 17.08.16.
 */
public enum LineStatus {
    LINE_START("LINESTART"),
    LINE_IN("LINEIN"),
    LINE_END("LINEEND");

    private final String name;

    LineStatus(String s) {
        name = s;
    }

    public static LineStatus fromName(String name) {
        final List<LineStatus> first = Arrays.stream(LineStatus.values()).filter(lineStatus -> {
            return StringUtils.equals(lineStatus.toString(), name);
        }).collect(toList());

        return first.size() > 0 ? first.get(0) : null;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
