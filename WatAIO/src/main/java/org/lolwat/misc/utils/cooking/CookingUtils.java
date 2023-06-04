package org.lolwat.misc.utils.cooking;

import org.lolwat.misc.types.mixed.FishType;

import java.util.HashMap;

public class CookingUtils {
    public static HashMap<String, Integer> getRequiredItems(FishType type, boolean fullInventory, int inventoryCount) {
        HashMap<String, Integer> ret = new HashMap<>();

        switch(type) {
            default: case SHRIMPS: ret.put("Raw shrimps", fullInventory ? 28 * inventoryCount : 1); break;
            case HERRING: ret.put("Raw herring", fullInventory ? 28 * inventoryCount : 1); break;
            case PIKE: ret.put("Raw pike", fullInventory ? 28 * inventoryCount : 1); break;
            case TUNA: ret.put("Raw tuna", fullInventory ? 28 * inventoryCount : 1); break;
            case SALMON: ret.put("Raw salmon", fullInventory ? 28 * inventoryCount : 1); break;
            case LOBSTER: ret.put("Raw lobster", fullInventory ? 28 * inventoryCount : 1); break;
            case SWORDFISH: ret.put("Raw swordfish", fullInventory ? 28 * inventoryCount : 1); break;
            case TROUT: ret.put("Raw trout", fullInventory ? 28 * inventoryCount : 1); break;
        }

        return ret;
    }
}
