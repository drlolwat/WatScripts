package org.lolwat.misc.utils;

import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.utilities.Logger;

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
        if (itemPrices == null) {
            itemPrices = new HashMap<>();
        }

        if (itemPrices.containsKey(item)) {
            return itemPrices.get(item);
        }

        double livePrice = LivePrices.get(item);
        int adjustedPrice = (int) Math.ceil(livePrice * 1.5);
        itemPrices.put(item, adjustedPrice);
        //Logger.log("Price of " + item + " is " + adjustedPrice);
        return adjustedPrice;
    }

    public static void raisePrice(String item) {
        double num;
        if (itemPrices.containsKey(item)) {
            num = itemPrices.get(item);
            itemPrices.remove(item);
        } else {
            num = LivePrices.get(item);
        }

        if (num < 10) {
            num = num * 2;
        }

        num = Math.ceil(num * 1.5);

        double currentHigh = LivePrices.get(item);
        if (num > (currentHigh * 3)) {
            num = currentHigh;
        }

        int finalPrice = (int) num;
        Logger.log("Setting price of " + item + " to " + finalPrice);
        itemPrices.put(item, finalPrice);
    }
}