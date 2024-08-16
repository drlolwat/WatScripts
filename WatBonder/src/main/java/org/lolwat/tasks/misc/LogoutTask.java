package org.lolwat.tasks.misc;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;

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
    public void execute() {
        if (!Client.isLoggedIn()) {
            return;
        }

        Area loc = BankLocation.getNearest().getArea(5);
        if (loc.contains(Players.getLocal())) {
            if (endingScript) {
                if (muleWealth) {
                    this.muleWealth = false;
                    TaskManager.getInstance().setCurrentTask(
                            new LiquidationTask(
                                    (!ConfigManager.getInstance().hasMuleConnectionFailed()
                                            && !ConfigManager.getInstance().getConfigBoolean("disable_mule")) ?
                                            new MulingTask("Muling wealth", Worlds.getCurrentWorld(), this) :
                                            new LogoutTask(true, false, this), 0
                            )
                    );
                    return;
                }

                if (Bank.isOpen()) {
                    Bank.close();
                    Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
                }
            }

            if (Client.isLoggedIn()) {
                if (!Tabs.open(Tab.LOGOUT)) {
                    Tabs.open(Tab.LOGOUT);
                    Sleep.sleep(100, 200);
                }

                Logger.log("WAIO: logging out for break");
                WatAIO.getInstance().disableLoginManager();
                Sleep.sleep(100, 200);
                Tabs.logout();
            }

            if (postScript != null) {
                if (!(postScript instanceof BreakingTask)) {
                    WatAIO.getInstance().enableLoginManager();
                } else {
                    WatAIO.getInstance().disableLoginManager();
                }

                Sleep.sleep(100, 200);
                TaskManager.getInstance().setCurrentTask(postScript, (postScript instanceof BreakingTask)
                        ? (int) postScript.data().get("seconds_to_run")
                        : 0);
            }

            if (endingScript) {
                Logger.log("WAIO: job done");
                ScriptManager.getScriptManager().stop();
            }

        } else {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(loc, this));
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
        return Skill.HITPOINTS;
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
}
