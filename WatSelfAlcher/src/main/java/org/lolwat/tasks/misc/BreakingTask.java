package org.lolwat.tasks.misc;

import org.dreambot.api.Client;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;

import java.time.Instant;
import java.util.HashMap;

public class BreakingTask implements WatTask {
    private final double endsAt;
    private final HashMap<String, Object> data;

    public BreakingTask(double expireAt) {
        endsAt = expireAt;
        data = new HashMap<String, Object>() { { put("seconds_to_run", endsAt-Instant.now().getEpochSecond()); } };
    }

    @Override
    public String getName() {
        return "Breaking";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute() {
        if(Client.isLoggedIn()) {
            TaskManager.getInstance().setCurrentTask(new LogoutTask(false, false,this));
            return;
        }

        if(Instant.now().getEpochSecond() >= endsAt) {
            if(!Client.isLoggedIn()) {
                WatAIO.getInstance().enableLoginManager();
                TaskManager.getInstance().getNewTask();
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public int loopTime() {
        return 600;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

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
    public HashMap<String, Object> data() {
        return data;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
