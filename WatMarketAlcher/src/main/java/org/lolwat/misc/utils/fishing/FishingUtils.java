package org.lolwat.misc.utils.fishing;

import org.lolwat.misc.types.mixed.FishType;

import java.util.HashMap;

public class FishingUtils {
    public static String getToolByFishType(FishType type) {
        switch(type) {
            default: return null; //return net later
            case SHRIMPS: return "Small fishing net";
            case HERRING:
            case PIKE: return "Fishing rod";
            case TUNA:
            case SWORDFISH: return "Harpoon";
            case SALMON: return "Fly fishing rod";
            case LOBSTER: return "Lobster pot";
        }
    }

    public static String getMenuItemByFishType(FishType type) {
        switch(type) {
            default: return null;
            case SHRIMPS: return "Net";
            case HERRING:
            case PIKE: return "Bait";
            case TUNA:
            case SWORDFISH: return "Harpoon";
            case SALMON: return "Lure";
            case LOBSTER: return "Cage";
        }
    }

    public static HashMap<String, Integer> getExtraFishingItems(FishType type) {
        HashMap<String, Integer> map = new HashMap<>();

        switch(type) {
            default: break;
            case LOBSTER: map.put("Coins", 25); break;
            case HERRING:
            case PIKE: map.put("Fishing bait", -1000); break;
            case SALMON: map.put("Feather", -1000); break;
        }

        return map;
    }
}
