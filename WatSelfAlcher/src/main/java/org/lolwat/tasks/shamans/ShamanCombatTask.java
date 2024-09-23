package org.lolwat.tasks.shamans;

import org.dreambot.api.methods.skills.Skill;
import org.lolwat.managers.types.WatTask;

import java.util.HashMap;

public class ShamanCombatTask implements WatTask {
    @Override
    public String getName() {
        return "Killing Shamans";
    }

    @Override
    public void execute() {
        //TODO combat task
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
        return Skill.RANGED;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }

    @Override
    public boolean requiresMembers() {
        return true;
    }
}
