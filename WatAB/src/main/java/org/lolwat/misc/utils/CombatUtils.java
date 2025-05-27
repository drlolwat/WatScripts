package org.lolwat.misc.utils;

import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.managers.MobManager;
import org.lolwat.types.mobs.Mob;

public class CombatUtils {
    private static boolean meetsRequirements(Mob mob, Skill skill) {
        if (mob.getLevelRequirements() != null) {
            Integer req = mob.getLevelRequirements().get(skill);
            if (req != null && Skills.getRealLevel(skill) < req) {
                return false;
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
}
