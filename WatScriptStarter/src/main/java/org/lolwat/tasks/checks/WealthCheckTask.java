package org.lolwat.tasks.checks;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.misc.MulingTask;
import org.lolwat.tasks.misc.StartNextScriptTask;

import java.util.HashMap;

public class WealthCheckTask implements WatTask {
    @Override
    public String getName() {
        return "Checking wealth";
    }

    @Override
    public void execute() {
        int minWealthNeeded = ConfigManager.getInstance().getConfigInt("need_gp");
        boolean muleOffExtra = ConfigManager.getInstance().getConfigBoolean("mule_extra_gp");

        int inventoryCoins = Inventory.count("Coins");
        Logger.log("WealthCheck: " + NumUtils.simplifyNumber(inventoryCoins) + " (" + inventoryCoins + ") coins in the inventory");
        if(!muleOffExtra) {
            if (inventoryCoins >= minWealthNeeded) {
                Logger.log("Wealth check was passed based on inventory.");
                Logger.log("We aren't muling extra, so no need to check the bank.");
                TaskManager.getInstance().setFutureTask(new StartNextScriptTask());
                return;
            }
        }

        if(!Bank.isOpen()) {
            ItemUtils.bank(this);
            return;
        }

        if(Inventory.isFull()) {
            if(!Bank.depositAllItems()) {
                Logger.error("Error depositing all items.");
                return;
            }
        }

        int bankCoins = Bank.count("Coins");
        int totalCoins = inventoryCoins + bankCoins;

        if(totalCoins >= minWealthNeeded) {
            Logger.log("Wealth check was passed based on bank and inventory.");
            if(muleOffExtra) {
                Logger.log("Now we will mule off the extra coins.");
                if(!Bank.depositAll("Coins")) {
                    Logger.error("Error depositing inventory coins.");
                    return;
                }

                int toMule = totalCoins - minWealthNeeded;
                Logger.log("We are muling " + NumUtils.simplifyNumber(toMule) + " (" + toMule + ") coins");

                if(!Bank.withdraw("Coins", toMule)) {
                    Logger.error("Problem withdrawing the coins we need to mule.");
                    return;
                }

                Logger.log("Passing off to mule task, then starting the next script.");
                TaskManager.getInstance().setFutureTask(new MulingTask("Muling", Worlds.getCurrentWorld(), new StartNextScriptTask()));
            } else {
                Logger.log("We are not muling the extra gp, so we can start the next script now.");
                TaskManager.getInstance().setFutureTask(new StartNextScriptTask());
            }
        } else {
            Logger.log("We need to reverse mule to meet our target gp.");
            TaskManager.getInstance().setFutureTask(new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>(){
                {
                    put("Coins", minWealthNeeded - totalCoins);
                }
            }, this));
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
        return null;
    }
}
