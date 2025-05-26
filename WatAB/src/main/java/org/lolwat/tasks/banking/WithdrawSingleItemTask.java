package org.lolwat.tasks.banking;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.exchange.BuySingleItemTask;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;

public class WithdrawSingleItemTask implements WatTask {
    private final WatTask parent;
    private final String itemName;
    private final int amount;

    public WithdrawSingleItemTask(String itemName, int amount, WatTask parent) {
        this.itemName = itemName;
        this.amount = amount;
        this.parent = parent;
    }

    @Override
    public void execute() {
        if(Inventory.count(x -> x != null && !x.isNoted() && x.getName().contains(itemName)) >= amount) {
            Logger.log("we have enough of item: " + itemName + " (" + amount + "), moving on");
            TaskManager.getInstance().setCurrentTask(parent);
            return;
        }

        if(!Bank.isOpen()) {
            WatUtils.bank(this);
            return;
        }

        Bank.resetCache();

        int coins = Inventory.count("Coins") + Bank.count("Coins");
        WatItem i = ItemManager.getInstance().getItem(itemName);
        if(i == null) {
            Logger.log("item not found: " + itemName);
            return;
        }

        if(Inventory.count(x -> x != null && x.getName().contains(itemName) && x.isNoted()) > 0) {
            Logger.log("noted item found in inventory: " + itemName);
            if(!Bank.depositAll(itemName)) {
                Logger.log("failed to deposit item: " + itemName);
                return;
            }
        }

        if(Bank.count(x -> x != null && x.getName().contains(itemName)) >= amount) {
            Logger.log("withdrawing " + amount + " of item: " + itemName);
            if(!Bank.withdraw(itemName, amount)) {
                Logger.log("failed to withdraw item: " + itemName);
                return;
            }

            if(!Inventory.contains(itemName)) {
                Logger.log("failed to withdraw item: " + itemName);
                return;
            }
        } else {
            Logger.log("not enough of item: " + itemName + " in bank");
            if(coins >= (i.getPrice() * amount)) {
                TaskManager.getInstance().setCurrentTask(new BuySingleItemTask(itemName, amount, i.getPrice(), this));
            }
            else {
                Logger.log("not enough coins to buy item: " + itemName);
                return;
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return parent.trainsSkill();
    }

    @Override
    public Integer avoidAfterLevel() {
        return parent.avoidAfterLevel();
    }
}
