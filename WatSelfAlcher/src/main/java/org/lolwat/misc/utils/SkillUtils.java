package org.lolwat.misc.utils;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

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

    public static int getExperienceForLevel(int level) {
        int totalExp = 0;
        for (int i = 1; i < level; i++) {
            totalExp += (int) (Math.floor(i + 300 * Math.pow(2, i / 7.)) / 4);
        }

        return totalExp;
    }

    public static int getExperienceToNextLevel(Skill sk) {
        int expForCurrentLevel = Skills.getExperience(sk);
        int expForNextLevel = getExperienceForLevel(Skills.getRealLevel(sk) + 1);
        return expForNextLevel - expForCurrentLevel;
    }
}
