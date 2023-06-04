package org.lolwat.misc.utils.smithing;

import org.lolwat.misc.types.smithing.IngotType;

import java.util.HashMap;

public class SmithingUtils {
    public static HashMap<String, Integer> getMaterialsForBar(IngotType type, boolean fullInventory, int inventoryLoads) {
        HashMap<String, Integer> ret = new HashMap<>();
        switch(type) {
            default: return ret;
            case BRONZE: {
                ret.put("Copper ore", fullInventory ? 14 * inventoryLoads : 1);
                ret.put("Tin ore", fullInventory ? 14 * inventoryLoads : 1);
                break;
            }
            case IRON: {
                ret.put("Iron ore", fullInventory? 28 * inventoryLoads : 1);
                break;
            }
            case STEEL: {
                ret.put("Iron ore", fullInventory ? 9 * inventoryLoads : 1);
                ret.put("Coal", fullInventory ? 18 * inventoryLoads : 2);
                break;
            }
            case GOLD: {
                ret.put("Gold ore", fullInventory ? 28 * inventoryLoads : 1);
                break;
            }
            case SILVER: {
                ret.put("Silver ore", fullInventory ? 28 * inventoryLoads : 1);
                break;
            }
            case MITHRIL: {
                ret.put("Mithril ore", fullInventory ? 5 * inventoryLoads : 1);
                ret.put("Coal", fullInventory ? 20 * inventoryLoads : 4);
                break;
            }
            case ADAMANT: {
                ret.put("Adamantite ore", fullInventory ? 4 * inventoryLoads : 1);
                ret.put("Coal", fullInventory ? 24 * inventoryLoads : 6);
                break;
            }
            case RUNE: {
                ret.put("Runite ore", fullInventory ? 3 * inventoryLoads : 1);
                ret.put("Coal", fullInventory ? 24 * inventoryLoads : 8);
                break;
            }
        }
        return ret;
    }

    public static int getIngotWidgetId(IngotType type) {
        switch(type) {
            default: return 14;
            case IRON: return 15;
            case SILVER: return 16;
            case STEEL: return 17;
            case GOLD: return 18;
            case MITHRIL: return 19;
            case ADAMANT: return 20;
            case RUNE: return 21;
        }
    }
}
