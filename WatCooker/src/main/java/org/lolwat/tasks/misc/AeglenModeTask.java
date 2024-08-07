package org.lolwat.tasks.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;

public class AeglenModeTask implements WatTask {
    @Override
    public String getName() {
        return "Aeglen";
    }

    public AeglenModeTask() {
    }

    @Override
    public void execute() {
        int n = Calculations.random(5);
        if(n == 1) {
            for(Tab t : Tab.values()) {
                if(t.isOpen())
                    continue;

                if(!t.open()) {
                    Logger.log("Failed to open tab " + t);
                }
            }
        } else {
            TaskManager.getInstance().
                    setCurrentTask(new TraversalTask(Players.getLocal().getTile().getArea(10), this));
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 500;
    }

    @Override
    public boolean canPerformTask() {
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
}
