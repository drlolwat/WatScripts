package org.lolwat.tasks.types.misc;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.WatTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public void execute(WatAIO instance) {
        Area loc = BankLocation.GRAND_EXCHANGE.getArea(5);
        if(loc.contains(Players.getLocal())) {
            if(!Bank.isOpen()) {
                Bank.open();
                Sleep.sleepUntil(Bank::isOpen, Calculations.random(3000, 6000));
                return;
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
                int a = NumUtils.getItemPrice(i.getName()) * q;

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

                    checkAndSet(BankMode.NOTE);
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
                TaskManager.getInstance().setCurrentTask(postScript);
            }
        } else {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(loc, this));
        }
    }

    public void checkAndSet(BankMode mode) {
        if(Bank.getWithdrawMode().equals(mode)) {
            return;
        }

        Bank.setWithdrawMode(mode);
        Sleep.sleep(100, 200);
    }//TODO move to utils

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
    public Quest completesQuest() {
        return null;
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
