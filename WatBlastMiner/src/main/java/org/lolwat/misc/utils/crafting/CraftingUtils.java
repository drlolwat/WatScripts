package org.lolwat.misc.utils.crafting;

import org.lolwat.misc.types.crafting.CraftingType;

import java.util.HashMap;

public class CraftingUtils {
    public static String getCraftingItemName(CraftingType type) {
        switch(type) {
            default: return "Wool";
            case GOLDWOOL: return "Golden fleece";
            case BOWSTRING: return "Flax";
            case ROPE: return "Hair";
        }
    }

    public static HashMap<String, Integer> getMaterialsForJewelry(CraftingType type, boolean fullInventory, int inventoryLoads) {
        HashMap<String, Integer> ret = new HashMap<>();

        switch(type) {
            case GOLDRING: {
                ret.put("Ring mould", 1);
                ret.put("Gold bar", fullInventory ? 27 * inventoryLoads : 1);
                break;
            }

            case GOLDAMULET: {
                ret.put("Amulet mould", 1);
                ret.put("Gold bar", fullInventory ? 27 * inventoryLoads : 1);
            }
        }

        return ret;
    }

    public static int getSpinnerWidgetId(CraftingType type) {
        switch(type) {
            default: return 0;
            case WOOL: return 14;
            case GOLDWOOL: return 15;
            case BOWSTRING: return 16;
            case ROPE: return 17;
            case CROSSBOWSTRING_S: return 18;
            case CROSSBOWSTRING_TR: return 19;
            case MAGICSTRING: return 20;
        }
    }

    public static int getJewelryParentId(CraftingType type) {
        switch(type) {
            default:
            case GOLDAMULET:
            case GOLDRING: return 446;
        }
    }

    public static int getJewelryChildId(CraftingType type) {
        switch(type) {
            default:
            case GOLDRING: return 8;
            case GOLDAMULET: return 37;
        }
    }
}
