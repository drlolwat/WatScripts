package org.lolwat.tasks.checks;

import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.misc.BondingTask;

public class MemberCheckTask implements WatTask {
    @Override
    public String getName() {
        return "Checking wealth";
    }

    @Override
    public void execute() {
        if(ConfigManager.getInstance().getConfigBoolean("bond_account")) {
            if(!GenericUtils.isMember()) {
                Logger.log("P2P check failed, bonding account");
                TaskManager.getInstance().setFutureTask(new BondingTask(this));
                return;
            }
        }

        Logger.log("P2P checker passed, or skipped due to config");
        TaskManager.getInstance().setFutureTask(new WealthCheckTask());
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
