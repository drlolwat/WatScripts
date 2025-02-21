package org.lolwat.misc.utils.fishing;

import org.lolwat.managers.ItemManager;
import org.lolwat.misc.types.mixed.FishType;
import org.lolwat.types.gear.WatItem;

import java.util.HashMap;

public class FishingUtils {
    public static String getToolByFishType(FishType type) {
        switch(type) {
            default: return null; //return net later
            case SHRIMPS2:
            case SHRIMPS: return "Small fishing net";
            case HERRING:
            case SARDINE:
            case PIKE: return "Fishing rod";
            case TUNA:
            case SWORDFISH: return "Harpoon";
            case TROUT:
            case SALMON: return "Fly fishing rod";
            case LOBSTER: return "Lobster pot";
        }
    }

    public static String getMenuItemByFishType(FishType type) {
        switch(type) {
            default: return null;
            case SHRIMPS2: return "Small Net";
            case SHRIMPS: return "Net";
            case HERRING:
            case SARDINE:
            case PIKE: return "Bait";
            case TUNA:
            case SWORDFISH: return "Harpoon";
            case TROUT:
            case SALMON: return "Lure";
            case LOBSTER: return "Cage";
        }
    }

    public static HashMap<WatItem, Integer> getExtraFishingItems(FishType type) {
        HashMap<WatItem, Integer> map = new HashMap<>();

        switch(type) {
            default: break;
            case LOBSTER: map.put(ItemManager.getInstance().getItem("Coins"), 25); break;
            case HERRING:
            case SARDINE:
            case PIKE: map.put(ItemManager.getInstance().getItem("Fishing bait"), -1000); break;
            case TROUT:
            case SALMON: map.put(ItemManager.getInstance().getItem("Feather"), -1000); break;
        }

        return map;
    }
}
