package org.lolwat.Utils;

import org.dreambot.api.methods.skills.Skill;
import org.lolwat.Enums.OreType;

import java.util.Arrays;
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
}
