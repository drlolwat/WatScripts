package org.lolwat.misc.utils.smithing;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.misc.types.smithing.IngotType;
import org.lolwat.misc.types.smithing.SmithingType;
import org.lolwat.misc.utils.StringUtils;

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
            case ADAMANTITE: {
                ret.put("Adamantite ore", fullInventory ? 4 * inventoryLoads : 1);
                ret.put("Coal", fullInventory ? 24 * inventoryLoads : 6);
                break;
            }
            case RUNITE: {
                ret.put("Runite ore", fullInventory ? 3 * inventoryLoads : 1);
                ret.put("Coal", fullInventory ? 24 * inventoryLoads : 8);
                break;
            }
        }
        return ret;
    }

    public static HashMap<String, Integer> materialsForSmithing(SmithingType type, IngotType barType, boolean fullInventory, int inventoryLoads) {
        String barName = StringUtils.capitalize(barType.toString().toLowerCase()) + " bar";
        HashMap<String, Integer> ret = new HashMap<>();
        switch(type) {
            default:
            case AXE: {
                ret.put(barName, fullInventory ? 27 * inventoryLoads : 1);
                break;
            }
            case SCIMITAR: {
                ret.put(barName, fullInventory ? 26 * inventoryLoads : 2);
                break;
            }
            case PLATELEGS:
            case WARHAMMER: {
                ret.put(barName, fullInventory ? 27 * inventoryLoads : 3);
                break;
            }
            case PLATEBODY: {
                ret.put(barName, fullInventory ? 25 * inventoryLoads : 5);
                break;
            }
        }

        return ret;
    }

    public static SmithingType getBestSmithingChoice(IngotType type) {
        int smithingLevel = Skills.getRealLevel(Skill.SMITHING);

        if(type == IngotType.BRONZE) {
            if(smithingLevel >= 18) {
                return SmithingType.PLATEBODY;
            }
            else if(smithingLevel >= 9) {
                return SmithingType.WARHAMMER;
            }
            else if(smithingLevel >= 5) {
                return SmithingType.SCIMITAR;
            }
            else {
                return SmithingType.AXE;
            }
        }
        else if(type == IngotType.IRON) {
            return SmithingType.PLATEBODY;
        }
        else if(type == IngotType.STEEL) {
            return SmithingType.PLATEBODY;
        }
        else if(type == IngotType.MITHRIL) {
            return SmithingType.PLATEBODY;
        }
        else if(type == IngotType.ADAMANTITE) {
            return SmithingType.PLATEBODY;
        }
        else if(type == IngotType.RUNITE) {
            return SmithingType.PLATELEGS;
        }
        else {
            return SmithingType.AXE;
        }
    }

    public static int getIngotWidgetId(IngotType type) {
        switch(type) {
            default: return 14;
            case IRON: return 15;
            case SILVER: return 16;
            case STEEL: return 17;
            case GOLD: return 18;
            case MITHRIL: return 19;
            case ADAMANTITE: return 20;
            case RUNITE: return 21;
        }
    }
}
