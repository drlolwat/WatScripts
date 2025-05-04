package org.lolwat.tasks.misc;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.types.WatTask;

public class EndScriptTask implements WatTask {

    public EndScriptTask() {
    }

    @Override
    public String getName() {
        return "Logging out";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute() {
        Logger.log("Liquidation complete");
        ScriptManager.getScriptManager().stop();
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public int loopTime() {
        return 0;
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
