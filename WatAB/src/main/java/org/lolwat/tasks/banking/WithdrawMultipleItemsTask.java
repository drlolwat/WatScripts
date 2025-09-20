package org.lolwat.tasks.banking;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.exchange.BuyMultipleItemsTask;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WithdrawMultipleItemsTask implements WatTask {
    private final WatTask parent;
    private final HashMap<WatItem, Integer> toObtain;

    private final HashMap<WatItem, Integer> toWithdraw;
    private final HashMap<WatItem, Integer> toBuy;
    private int coinsAvailable;
    private int coinsSpending;
    private List<WatItem> hasObtained;

    public WithdrawMultipleItemsTask(HashMap<WatItem, Integer> toObtain, WatTask parent) {
        this.toObtain = toObtain;
        this.parent = parent;

        toWithdraw = new HashMap<>();
        toBuy = new HashMap<>();
        coinsAvailable = -1;
        coinsSpending = 0;
        hasObtained = new ArrayList<>();
    }

    @Override
    public void execute() {
        if (!Bank.isOpen()) {
            WatUtils.bank(this);
            return;
        }

        Bank.resetCache();

        if(coinsAvailable == -1) {
            coinsAvailable = Inventory.count("Coins") + Bank.count("Coins");
        }

        for(Map.Entry<WatItem, Integer> map : toObtain.entrySet()) {
            String itemName = map.getKey().getName();
            int amount = map.getValue();

            if (Inventory.count(x -> x != null && !x.isNoted() && x.getName().contains(itemName)) >= amount) {
                Logger.log("we have enough of item: " + itemName + " (" + amount + "), moving on");
                hasObtained.add(map.getKey());
                toBuy.remove(map.getKey());
                continue;
            }

            if (Inventory.count(x -> x != null && x.getName().contains(itemName) && x.isNoted()) > 0) {
                Logger.log("noted item found in inventory: " + itemName);
                if (!Bank.depositAll(itemName)) {
                    Logger.log("failed to deposit item: " + itemName);
                    return;
                }
            }

            if (Bank.count(x -> x != null && x.getName().contains(itemName)) >= amount) {
                Logger.log("adding" + amount + " of item to withdraw list: " + itemName);
                toWithdraw.put(map.getKey(), amount);
            } else {
                Logger.log("not enough of item: " + itemName + " in bank");
                int itemCost = (map.getKey().getPrice() * amount);
                if (coinsAvailable >= itemCost) {
                    if(!toBuy.containsKey(map.getKey())) {
                        Logger.log("adding " + amount + " of " + itemName + " to buy list");
                        toBuy.put(map.getKey(), amount);
                        coinsAvailable -= itemCost;
                        coinsSpending += itemCost;
                    } else {
                        Logger.log("item " + itemName + " was already in buy list");
                    }
                } else {
                    Logger.error("not enough coins to buy item: " + itemName + " x" + amount);
                    return;
                }
            }
        }

        if(!toBuy.isEmpty()) {
            Logger.log("we have to spend " + WatUtils.simplifyNumber(coinsSpending) + "gp on items");
            for(Map.Entry<WatItem, Integer> i : toBuy.entrySet()) {
                Logger.log(i.getKey().getName() + " x" + i.getValue());
            }
            Logger.log("handing off to G.E operations");
            TaskManager.getInstance().setCurrentTask(new BuyMultipleItemsTask(toBuy, this));
            return;
        }

        if(!toWithdraw.isEmpty()) {
            for(Map.Entry<WatItem, Integer> i : toWithdraw.entrySet()) {
                if(!Bank.withdraw(i.getKey().getName(), i.getValue())) {
                    Logger.error("WMITask: problem withdrawing " + i.getKey().getName() + " x" + i.getValue());
                    continue;
                }

                hasObtained.add(i.getKey());
            }
        }

        for(WatItem i : hasObtained) {
            toObtain.remove(i);
        }

        if(toObtain.isEmpty()) {
            Logger.log("obtained all gear required, sending to task: " + parent.getName());
            TaskManager.getInstance().setCurrentTask(parent);
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
