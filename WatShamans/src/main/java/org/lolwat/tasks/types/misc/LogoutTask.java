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
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.tasks.WatTask;

import java.util.HashMap;

public class LogoutTask implements WatTask {
    boolean endingScript;
    boolean muleWealth;
    WatTask postScript;

    public LogoutTask(boolean endScript, boolean muleOff, WatTask post) {
        endingScript = endScript;
        postScript = post;
        muleWealth = muleOff;
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
                if(muleWealth) {
                    this.muleWealth = false;
                    instance.MULE_SAFETY_NET = 0;
                    instance.MULE_TRIGGER = 1;
                    HashMap<String, Integer> li = new HashMap<>();
                    for(String s : instance.EMERGENCY_SELL) {
                        li.put(s, -1);
                    }

                    instance.currentTask = new BankingTask(null, null, li, 1, this);
                    return;
                }

                ScriptManager.getScriptManager().stop();
                Logger.log("WAIO: Trade unrestricted, stopping");
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

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }
}
