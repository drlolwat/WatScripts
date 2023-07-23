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
        int livePrice = LivePrices.get(item);
        int ret = 1;

        if(livePrice > 0) {
            if(livePrice >= 10000) {
                ret = (int) (livePrice * 1.3);
            }
            else if(livePrice >= 5000) {
                ret = (int) (livePrice * 1.8);
            }
            else {
                if (livePrice <= 1000) {
                    ret = livePrice * 2;
                } else {
                    ret = (int) (livePrice * 1.8);
                }
            }
        }

        return (int) (ret * 1.2);
    }
}
