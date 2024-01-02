package org.lolwat.misc.utils;

import org.dreambot.api.methods.grandexchange.LivePrices;

public class NumUtils {
    public static String simplifyNumber(double number) {
        if (number >= 1000000) {
            return String.format("%.2fM", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%.2fK", number / 1000);
        } else {
            return String.format("%.2f", number);
        }
    }

    public static int getItemPrice(String item) {
        return WikiPricing.getPrice(item);
    }
}
