package org.lolwat.misc.utils;

import org.dreambot.api.methods.skills.Skill;
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
}
