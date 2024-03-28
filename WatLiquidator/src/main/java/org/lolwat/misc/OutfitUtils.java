package org.lolwat.misc;

import java.util.HashMap;

public class OutfitUtils {
    public static HashMap<String, Integer> gracefulItems = new HashMap<String, Integer>() {
        {
            put("Graceful hood", 35);
            put("Graceful cape", 40);
            put("Graceful top", 55);
            put("Graceful legs", 60);
            put("Graceful gloves", 30);
            put("Graceful boots", 40);
        }
    };

    public static boolean isOutfitItem(String item) {
        return item.toLowerCase().contains("graceful");
    }

    public static boolean isGracefulItem(String item) {
        return gracefulItems.containsKey(item);
    }
}
