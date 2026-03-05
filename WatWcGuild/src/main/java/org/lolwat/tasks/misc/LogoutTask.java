package org.lolwat.tasks.misc;

import org.dreambot.api.Client;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;

public class LogoutTask implements WatTask {
    private final WatTask post;

    public LogoutTask(WatTask post) {
        this.post = post;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void execute() {
        if (Client.isLoggedIn()) {
            Logger.log("Logging out, disabling login manager");
            WatScript.getInstance().disableLoginManager();
            if (!Tabs.open(Tab.LOGOUT)) {
                Tabs.open(Tab.LOGOUT);
                Sleep.sleep(100, 200);
            }

            Sleep.sleep(100, 200);
            Tabs.logout();
            Sleep.sleepUntil(() -> !Client.isLoggedIn(), 10000);
            Logger.log("LogoutTask: sending to post task");
            TaskManager.getInstance().setCurrentTask(post);
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

}
