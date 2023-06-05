package org.lolwat.misc.utils;

public class StringUtils {
    public static String capitalize(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
