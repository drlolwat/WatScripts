package org.lolwat.misc.utils.woodcutting;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.managers.types.WatConfig;
import org.lolwat.misc.types.mixed.TreeType;

import java.util.HashMap;

public class WoodcuttingUtils {
    public static HashMap<Integer, String> hatchetTypes = new HashMap<Integer, String>() {
        {
            put(41, "Rune axe");
            put(31, "Adamant axe");
            put(21, "Mithril axe");
            put(11, "Steel axe");
            put(1, "Bronze axe");
        }
    };

    public static String getBestHatchetForLevel() {
        int level = Skills.getRealLevel(Skill.WOODCUTTING);

        if(WatConfig.getToolFailures() >= 3) {
            return hatchetTypes.get(1);
        }

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
            case OAK: return "Oak tree";
            case YEW: return "Yew tree";
            case MAPLE: return "Maple tree";
            case WILLOW: return "Willow tree";
        }
    }
}
