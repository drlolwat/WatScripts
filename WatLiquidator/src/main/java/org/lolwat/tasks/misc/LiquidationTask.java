package org.lolwat.tasks.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;

import java.util.HashMap;
import java.util.Map;

public class LiquidationTask implements WatTask {
    WatTask postScript;
    int toLiquidate;

    public LiquidationTask(WatTask post, int amount) {
        postScript = post;
        toLiquidate = amount;
    }

    @Override
    public String getName() {
        return "Liquidating";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute() {
        Area loc = BankLocation.GRAND_EXCHANGE.getArea(5);
        if(loc.contains(Players.getLocal())) {
            if(!Bank.isOpen()) {
                Bank.open();
                Sleep.sleepUntil(Bank::isOpen, Calculations.random(3000, 6000));
                return;
            }

            if(!Equipment.isEmpty()) {
                Bank.depositAllEquipment();
                Sleep.sleepUntil(Equipment::isEmpty, Calculations.random(3000, 6000));
            }

            int currentValue = 0;
            HashMap<String, Integer> toWithdraw = new HashMap<>();
            for(Item i : Bank.all()) {
                if(i == null || !i.isTradable() || i.getName().equals("Coins"))
                    continue;

                if(postScript != null) {
                    if(postScript.clothesRequired().containsKey(i.getName()) || postScript.inventoryRequired().containsKey(i.getName())) {
                        continue;
                    }
                }

                if(toLiquidate > 0 && currentValue >= toLiquidate) {
                    break;
                }

                int q = i.getAmount();
                int a = LivePrices.get(i.getName()) * q;

                if(a >= 5000) {
                    toWithdraw.put(i.getName(), q);
                    currentValue += a;
                }
            }

            if(!toWithdraw.isEmpty()) {
                HashMap<String, Integer> currentlySelling = new HashMap<>();
                for (Map.Entry<String, Integer> m : toWithdraw.entrySet()) {
                    if(postScript != null) {
                        if(postScript.clothesRequired().containsKey(m.getKey()) || postScript.inventoryRequired().containsKey(m.getKey())) {
                            continue;
                        }
                    }

                    ItemUtils.setBankMode(BankMode.NOTE);
                    if (!Bank.withdraw(m.getKey(), m.getValue())) {
                        Logger.error("Failed to withdraw " + m.getKey() + " x" + m.getValue());
                        continue;
                    }

                    currentlySelling.put(m.getKey(), m.getValue());

                    if (Inventory.isFull() || toWithdraw.equals(currentlySelling)) {
                        TaskManager.getInstance().setCurrentTask(new GrandExchangeTask("Liquidating", true, currentlySelling, this));
                        return;
                    }
                }
            } else {
                if(!Bank.isOpen()) {
                    if(!Bank.open()) {
                        Logger.error("Failed to open bank");
                        return;
                    }

                    Sleep.sleepUntil(Bank::isOpen, Calculations.random(3000, 6000));
                }

                if(Bank.isOpen()) {
                    Bank.close();
                    Sleep.sleepUntil(() -> !Bank.isOpen(), Calculations.random(3000, 6000));
                }

                if(GrandExchange.isOpen()) {
                    GrandExchange.close();
                    Sleep.sleepUntil(() -> !GrandExchange.isOpen(), Calculations.random(3000, 6000));
                }

                TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, null, null));
            }
        } else {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(loc, this));
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }
}
