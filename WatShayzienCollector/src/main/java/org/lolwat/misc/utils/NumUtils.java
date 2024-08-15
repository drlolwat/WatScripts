package org.lolwat.misc.utils;

import org.dreambot.api.methods.grandexchange.LivePrices;

import java.util.HashMap;

public class NumUtils {
    private static HashMap<String, Integer> itemPrices;
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
        if(itemPrices == null) {
            itemPrices = new HashMap<>();
        }

        if(itemPrices.containsKey(item)) {
            return itemPrices.get(item);
        }

        itemPrices.put(item, (int) (LivePrices.getHigh(item) * 1.2));
        return itemPrices.get(item);
    }

    public static void raisePrice(String item) {
        int num;
        if(itemPrices.containsKey(item)) {
            num = itemPrices.get(item);
            itemPrices.remove(item);
        }
        else {
            num = LivePrices.get(item);
        }

        if(num < 10) {
            num = num * 2;
        }

        num = (int) (num * 1.2);
        itemPrices.put(item, num);
    }
}
