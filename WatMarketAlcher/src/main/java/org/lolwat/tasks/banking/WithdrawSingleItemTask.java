package org.lolwat.tasks.banking;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.alching.HighAlchemyTask;
import org.lolwat.tasks.exchange.BuySingleItemTask;
import org.lolwat.tasks.misc.MulingTask;

import java.util.HashMap;

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
    public String getName() {
        return "Withdrawing " + itemName + " x" + amount;
    }

    @Override
    public void execute() {
        int targetAmount = amount;
        if (amount < 0) targetAmount = Math.abs(amount);

        if (Inventory.count(x -> x != null && !x.isNoted() && x.getName().contains(itemName)) >= targetAmount) {
            Logger.log("we have enough of item: " + itemName + " (" + targetAmount + "), moving on");
            TaskManager.getInstance().setCurrentTask(parent);
            return;
        }

        if (!Bank.isOpen()) {
            ItemUtils.bank(this);
            return;
        }

        Bank.resetCache();

        int price = (int) (LivePrices.get(itemName) * 1.3); // 30% markup for buying
        int totalAmount = Bank.count(x -> x != null && x.getName().contains(itemName)) +
                Inventory.count(x -> x != null && x.getName().contains(itemName));
        int coins = Inventory.count("Coins") + Bank.count("Coins");

        if (Inventory.count(x -> x != null && x.getName().contains(itemName) && x.isNoted()) > 0) {
            Logger.log("noted item found in inventory: " + itemName);
            if (!Bank.depositAll(itemName)) {
                Logger.log("failed to deposit item: " + itemName);
                return;
            }
        }

        int inventoryAmount = Inventory.count(x -> x != null && x.getName().contains(itemName));
        int bankAmount = Bank.count(x -> x != null && x.getName().contains(itemName));

        if (amount > 0) {
            if (bankAmount >= (amount - inventoryAmount)) {
                Logger.log("withdrawing " + (amount - inventoryAmount) + " of item: " + itemName);
                if (!Bank.withdraw(itemName, (amount - inventoryAmount))) {
                    Logger.log("failed to withdraw item: " + itemName);
                    return;
                }
                if (!Inventory.contains(itemName)) {
                    Logger.log("failed to withdraw item: " + itemName);
                    return;
                }
            } else {
                Logger.log("not enough of item: " + itemName + " in bank");
                int amountToBuy = amount - totalAmount;

                if(itemName.equalsIgnoreCase("nature rune")) {
                    amountToBuy = ConfigManager.getInstance().getConfigInt("buy_nature_qty") - totalAmount;
                }

                if (coins >= (price * amountToBuy)) {
                    TaskManager.getInstance().setCurrentTask(new BuySingleItemTask(itemName, amountToBuy, price, this));
                } else {
                    Logger.log("reverse muling - no money available apparently");
                    TaskManager.getInstance().setFutureTask(new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                        {
                            put("Coins", ConfigManager.getInstance().getConfigInt("keep_gp"));
                        }
                    }, new HighAlchemyTask()));
                    return;
                }
            }
        } else {
            if (bankAmount > 0) {
                Logger.log("withdrawing all of item: " + itemName);
                if (!Bank.withdrawAll(itemName)) {
                    Logger.log("failed to withdraw all of item: " + itemName);
                    return;
                }
                if (!Inventory.contains(itemName)) {
                    Logger.log("failed to withdraw item: " + itemName);
                    return;
                }
            } else {
                int amountToBuy = Math.abs(amount);

                if(itemName.equalsIgnoreCase("nature rune")) {
                    amountToBuy = ConfigManager.getInstance().getConfigInt("buy_nature_qty") - totalAmount;
                }

                Logger.log("not enough of item: " + itemName + " in bank, buying " + amountToBuy);
                if (coins >= (price * amountToBuy)) {
                    TaskManager.getInstance().setCurrentTask(new BuySingleItemTask(itemName, amountToBuy, price, this));
                } else {
                    Logger.log("reverse muling - no money available apparently");
                    TaskManager.getInstance().setFutureTask(new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                        {
                            put("Coins", ConfigManager.getInstance().getConfigInt("keep_gp"));
                        }
                    }, new HighAlchemyTask()));
                    return;
                }
            }
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
