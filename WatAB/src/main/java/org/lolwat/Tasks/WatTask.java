package org.lolwat.Tasks;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import java.util.*;

public interface WatTask {
    String name = "";
    HashMap<Skill, Integer> levelRequirements = new HashMap<>();
    List<Quest> questRequirements = new ArrayList<>();

    default String getName() {
        return name;
    }

    default boolean hasLevelRequirements() {
        for(Map.Entry<Skill, Integer> map : levelRequirements.entrySet()) {
            if(map.getValue() > Skills.getRealLevel(map.getKey())) {
                return false;
            }
        }

        return true;
    }

    default boolean hasQuestRequirements() {
        for(Quest q : questRequirements) {
            if(!Quests.isFinished(q)) {
                return false;
            }
        }

        return true;
    }
}
