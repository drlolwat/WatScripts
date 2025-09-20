package org.lolwat.misc.utils;

import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.managers.MobManager;
import org.lolwat.types.mobs.Mob;

import java.util.Map;

public class CombatUtils {
    private static boolean meetsRequirements(Mob mob, Skill skill) {
        if (mob.getLevelRequirements() != null) {
            for (Map.Entry<Skill, Integer> map : mob.getLevelRequirements().entrySet()) {
                Skill reqSkill = map.getKey();
                if (skill == Skill.MAGIC && (isMeleeSkill(reqSkill) || reqSkill == Skill.RANGED)) {
                    continue;
                }
                if (skill == Skill.RANGED && (isMeleeSkill(reqSkill) || reqSkill == Skill.MAGIC)) {
                    continue;
                }
                if (isMeleeSkill(skill) && (reqSkill == Skill.MAGIC || reqSkill == Skill.RANGED)) {
                    continue;
                }

                if (Skills.getRealLevel(reqSkill) < map.getValue()) {
                    return false;
                }
            }
        }

        if (mob.getQuestRequirements() != null) {
            for (Quest quest : mob.getQuestRequirements()) {
                if (!Quests.isFinished(quest)) {
                    return false;
                }
            }
        }

        return true;
    }
    public static Mob getBestMob(Skill skill) {
        Mob bestMob = null;
        int highestLevel = 0;

        for (Mob mob : MobManager.getInstance().getMobs()) {
            if (meetsRequirements(mob, skill)) {
                int mobLevel = mob.getLevelRequirements().getOrDefault(skill, 0);
                if (mobLevel > highestLevel) {
                    highestLevel = mobLevel;
                    bestMob = mob;
                } else if (mobLevel == highestLevel) {
                    bestMob = mob;
                }
            }
        }

        return bestMob;
    }

    private static boolean isMeleeSkill(Skill s) {
        return s == Skill.ATTACK || s == Skill.STRENGTH || s == Skill.DEFENCE;
    }
}
