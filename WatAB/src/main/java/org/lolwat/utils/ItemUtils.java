package org.lolwat.utils;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.utils.types.CraftingType;
import org.lolwat.utils.types.IngotType;
import org.lolwat.utils.types.OreType;
import org.lolwat.utils.types.TreeType;

import java.util.HashMap;

public class ItemUtils {
    public static String getRockNameForType(OreType type) {
        switch(type) {
            default: return "";
            case COPPER: return "Copper rocks";
            case TIN: return "Tin rocks";
            case IRON: return "Iron rocks";
            case COAL: return "Coal rocks";
            case MITHRIL: return "Mithril rocks";
            case ADAMANTITE: return "Adamantite rocks";
            case RUNITE: return "Runite rocks";
        }
    }

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

    public static HashMap<Integer, String> pickaxeTypes = new HashMap<Integer, String>() {
        {
            put(41, "Rune pickaxe");
            put(31, "Adamant pickaxe");
            put(21, "Mithril pickaxe");
            put(11, "Steel pickaxe");
            put(1, "Bronze pickaxe");
        }
    };

    public static HashMap<Integer, String> hatchetTypes = new HashMap<Integer, String>() {
        {
            put(41, "Rune axe");
            put(31, "Adamant axe");
            put(21, "Mithril axe");
            put(11, "Steel axe");
            put(1, "Bronze axe");
        }
    };

    public static int getPickaxeLevel(String name) {
        name = name.toLowerCase();
        switch(name) {
            case "rune pickaxe": return 41;
            case "adamant pickaxe": return 31;
            case "mithril pickaxe": return 21;
            case "steel pickaxe": return 11;
            default: return 1;
        }
    }

    public static String getBestPickaxeForLevel() {
        int level = Skills.getRealLevel(Skill.MINING);

        if(level >= 41) {
            return pickaxeTypes.get(41);
        }
        else if(level >= 31) {
            return pickaxeTypes.get(31);
        }
        else if(level >= 21) {
            return pickaxeTypes.get(21);
        }
        else if(level >= 11) {
            return pickaxeTypes.get(11);
        }
        else {
            return pickaxeTypes.get(1);
        }
    }

    public static String getBestHatchetForLevel() {
        int level = Skills.getRealLevel(Skill.WOODCUTTING);

        if(level >= 41) {
            return hatchetTypes.get(41);
        }
        else if(level >= 31) {
            return hatchetTypes.get(31);
        }
        else if(level >= 21) {
            return hatchetTypes.get(21);
        }
        else if(level >= 11) {
            return hatchetTypes.get(11);
        }
        else {
            return hatchetTypes.get(1);
        }
    }

    public static String getLogName(TreeType type) {
        switch(type) {
            default: return "Logs";
            case OAK: return "Oak logs";
            case YEW: return "Yew logs";
            case MAPLE: return "Maple logs";
            case WILLOW: return "Willow logs";
        }
    }

    public static String getTreeName(TreeType type) {
        switch(type) {
            default: return "Tree";
            case OAK: return "Oak";
            case YEW: return "Yew";
            case MAPLE: return "Maple";
            case WILLOW: return "Willow";
        }
    }

    public static String getCraftingItemName(CraftingType type) {
        switch(type) {
            default: return "Wool";
            case GOLDWOOL: return "Golden fleece";
            case BOWSTRING: return "Flax";
            case ROPE: return "Hair";
        }
    }
}
