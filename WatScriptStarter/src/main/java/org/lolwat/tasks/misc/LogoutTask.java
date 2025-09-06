package org.lolwat.tasks.misc;

import org.dreambot.api.Client;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.checks.WealthCheckTask;

public class LogoutTask implements WatTask {
    @Override
    public String getName() {
        return "Logging out";
    }

    @Override
    public void execute() {
        if(!Client.isLoggedIn()) {
            WatScript.getInstance().enableLoginManager();
            TaskManager.getInstance().setFutureTask(new WealthCheckTask());
        } else {
            if (!Tabs.isOpen(Tab.LOGOUT)) {
                Tabs.open(Tab.LOGOUT);
                Sleep.sleepUntil(() -> Tabs.isOpen(Tab.LOGOUT), 5000);
            }

            Tabs.logout();
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }
}
