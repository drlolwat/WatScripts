package org.lolwat.misc.utils.crafting;

import org.lolwat.misc.types.crafting.CraftingType;
import org.lolwat.misc.utils.StringUtils;

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

        String mouldType;

        if(type.toString().toLowerCase().contains("ring")) {
            mouldType = "Ring";
        } else {
            mouldType = "Amulet";
        }

        switch(type) {
            case AMULET:
            case RING: {
                ret.put("Gold bar", fullInventory ? 27 * inventoryLoads : 1);
                break;
            }

            case SAPPHIRERING: {
                ret.put("Gold bar", fullInventory ? 13 * inventoryLoads : 1);
                ret.put("Sapphire", fullInventory ? 13 * inventoryLoads : 1);
                break;
            }

            case DIAMONDAMULET:
            case EMERALDAMULET:
            case EMERALDRING: {
                ret.put("Gold bar", fullInventory ? 13 * inventoryLoads : 1);
                ret.put("Emerald", fullInventory ? 13 * inventoryLoads : 1);
                break;
            }

            case RUBYAMULET: {
                ret.put("Gold bar", fullInventory ? 13 * inventoryLoads : 1);
                ret.put("Ruby", fullInventory ? 13 * inventoryLoads : 1);
                break;
            }

        }

        String extra = mouldType + " mould";
        ret.put(extra, 1);

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
            case SAPPHIRERING:
            case EMERALDRING:
            case EMERALDAMULET:
            case RUBYAMULET:
            case DIAMONDAMULET:
            case AMULET:
            case RING: return 446;
        }
    }

    public static int getJewelryChildId(CraftingType type) {
        switch(type) {
            default:
            case RING: return 8;
            case SAPPHIRERING: return 9;
            case EMERALDRING: return 10;
            case AMULET: return 37;
            case EMERALDAMULET: return 39;
            case RUBYAMULET: return 40;
            case DIAMONDAMULET: return 41;
        }
    }
}
