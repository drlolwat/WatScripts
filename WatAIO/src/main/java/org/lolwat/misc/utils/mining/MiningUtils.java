package org.lolwat.misc.utils.mining;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.managers.types.WatConfig;
import org.lolwat.misc.types.mixed.OreType;

import java.util.HashMap;

public class MiningUtils {
    public static HashMap<Integer, String> pickaxeTypes = new HashMap<Integer, String>() {
        {
            put(41, "Rune pickaxe");
            put(31, "Adamant pickaxe");
            put(21, "Mithril pickaxe");
            put(11, "Steel pickaxe");
            put(1, "Bronze pickaxe");
        }
    };

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

        if(WatConfig.getToolFailures() >= 3) {
            return pickaxeTypes.get(1);
        }

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
}
