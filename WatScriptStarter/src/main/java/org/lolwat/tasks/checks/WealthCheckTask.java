package org.lolwat.tasks.checks;

import org.dreambot.api.methods.skills.Skill;
import org.lolwat.managers.types.WatTask;

public class WealthCheckTask implements WatTask {
    @Override
    public String getName() {
        return "Checking wealth";
    }

    @Override
    public void execute() {

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
        return null;
    }
}
