package org.lolwat.utils;

import org.dreambot.api.methods.skills.Skill;
import org.lolwat.utils.types.FishType;
import org.lolwat.utils.types.OreType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SkillUtils {
    public static boolean isFreeToPlay(Skill skill) {
        List<Skill> f2pSkills = Arrays.asList(
                Skill.ATTACK,
                Skill.STRENGTH,
                Skill.DEFENCE,
                Skill.RANGED,
                Skill.PRAYER,
                Skill.MAGIC,
                Skill.RUNECRAFTING,
                Skill.HITPOINTS,
                Skill.COOKING,
                Skill.WOODCUTTING,
                Skill.FISHING,
                Skill.FIREMAKING,
                Skill.CRAFTING,
                Skill.SMITHING,
                Skill.MINING
        );

        return f2pSkills.contains(skill);
    }

    public static Integer getMinimumLevelForRock(OreType type) {
        switch(type) {
            default: return 1;
            case IRON: return 15;
            case COAL: return 30;
            case MITHRIL: return 55;
            case ADAMANTITE: return 70;
            case RUNITE: return 85;
        }
    }

    public static String getToolByFishType(FishType type) {
        switch(type) {
            default: return null; //return net later
            case SHRIMP: return "Small fishing net";
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
            case SHRIMP: return "Net";
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
