package com.github.monzou.grinder.util;

import com.google.common.base.Strings;

/**
 * String utilities
 */
public final class Strings2 {

    public static String capitalize(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return "";
        }
        int length = value.length();
        return new StringBuilder(length).append(Character.toTitleCase(value.charAt(0))).append(value.substring(1)).toString();
    }

    public static String uncapitalize(String value) {
        if (Strings.isNullOrEmpty(value)) {
            return "";
        }
        int length = value.length();
        return new StringBuilder(length).append(Character.toLowerCase(value.charAt(0))).append(value.substring(1)).toString();
    }

    private Strings2() {
    }

}
