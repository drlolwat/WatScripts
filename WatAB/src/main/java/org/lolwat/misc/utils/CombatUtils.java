package org.lolwat.misc.utils;

import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.managers.MobManager;
import org.lolwat.types.mobs.Mob;

import java.util.Map;

public class CombatUtils {
    private static boolean meetsRequirements(Mob mob) {
        if(mob.getQuestRequirements() != null) {
            for (Map.Entry<Skill, Integer> entry : mob.getLevelRequirements().entrySet()) {
                if (Skills.getRealLevel(entry.getKey()) < entry.getValue()) {
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
            if (meetsRequirements(mob)) {
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
