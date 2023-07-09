package org.lolwat.tasks.types.misc;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.tasks.WatTask;

public class LogoutTask implements WatTask {
    boolean endingScript;
    WatTask postScript;

    public LogoutTask(boolean endScript, WatTask post) {
        endingScript = endScript;
        postScript = post;
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
    public void execute(WatAIO instance) {
        if(!Client.isLoggedIn()) {
            return;
        }

        Area loc = BankLocation.getNearest().getArea(5);
        if(loc.contains(Players.getLocal())) {
            if(Client.isLoggedIn()) {
                if(!Tabs.open(Tab.LOGOUT)) {
                    Tabs.open(Tab.LOGOUT);
                    Sleep.sleep(100, 200);
                }

                instance.disableLoginManager();
                Sleep.sleep(100, 200);
                Tabs.logout();
            }

            if(postScript != null) {

                if(!(postScript instanceof BreakingTask)) {
                    instance.enableLoginManager();
                }

                Sleep.sleep(100, 200);
                instance.currentTask = postScript;
                return;
            }

            if(endingScript) {
                ScriptManager.getScriptManager().stop();
            }
        } else {
            instance.currentTask = new TraversalTask(loc, this);
        }
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
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public Skill trainsSkill() {
        return null;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }
}
