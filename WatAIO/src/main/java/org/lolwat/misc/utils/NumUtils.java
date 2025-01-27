package org.lolwat.misc.utils;

import org.dreambot.api.methods.grandexchange.LivePrices;

import java.util.HashMap;

public class NumUtils {
    private static HashMap<String, Integer> itemPrices;
    private static double multiplier = 1.2;
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

        int num = (int) (LivePrices.getHigh(item) * multiplier);

        if(num < 100) {
            num = num * 7;
        }
        else if(num < 500) {
            num = num * 2;
        }
        else if(num < 1000) {
            num = (int)Math.ceil(num * 1.5);
        } else {
            num = (int) (num * multiplier);
        }

        itemPrices.put(item, num);
        return itemPrices.get(item);
    }

    public static void raisePrice(String item) {
        int num;
        if(itemPrices.containsKey(item)) {
            num = itemPrices.get(item);
            itemPrices.remove(item);
        }
        else {
            num = (int) (LivePrices.getHigh(item) * multiplier);
        }

        int currentHigh = LivePrices.getHigh(item);
        if(num > (currentHigh * 20)) {
            num = currentHigh;
        }

        if(num < 100) {
            num = num * 3;
        }
        else if(num < 500) {
            num = num * 3;
        }
        else if(num < 1000) {
            num = num * 2;
        }
        else {
            num = (int) (num * multiplier);
        }

        itemPrices.put(item, num);
    }
}
