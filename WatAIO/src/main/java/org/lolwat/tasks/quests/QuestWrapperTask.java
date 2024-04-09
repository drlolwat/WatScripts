package org.lolwat.tasks.quests;

import com.google.common.collect.Lists;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.managers.types.WatQuest;
import org.lolwat.managers.types.WatTask;

import java.util.HashMap;
import java.util.List;

public class QuestWrapperTask implements WatTask {
    private WatQuest questTask = null;
    @Override
    public String getName() {
        return "Quest Selection";
    }

    @Override
    public void execute() {
        if(questTask == null) {
            // TODO: QuestManager, evaluate through available quests and select one
        } else {
            questTask.execute();
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public WatQuest questTask() {
        return questTask;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        if(questTask() != null)
            return questTask().clothesRequired();

        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        if(questTask() != null)
            return questTask().inventoryRequired();

        return new HashMap<>();
    }

    @Override
    public List<String> inventoryTolerated() {
        if(questTask() != null)
            return questTask().inventoryTolerated();

        return Lists.newArrayList();
    }

    @Override
    public boolean requiresMembers() {
        if(questTask() != null)
            return questTask().requiresMembers();

        return false;
    }
}
