package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.tasks.WatTask;

import java.util.*;

public class BankingTask implements WatTask {
    private HashMap<String, Integer> equipmentRequired; // Process equipment first
    private HashMap<String, Integer> inventoryRequired; // Process inv second..
    private HashMap<String, Integer> sellingItems; // Check this at the very top of banking operations
    private int inventoriesWorth;
    private WatTask postTask;

    public BankingTask(HashMap<String, Integer> eqRequired, HashMap<String, Integer> invRequired, HashMap<String, Integer> sellList, Integer inventories, WatTask post) {
        if (eqRequired == null || eqRequired.size() == 0)
            equipmentRequired = new HashMap<>();
        else
            equipmentRequired = eqRequired;

        if (invRequired == null || invRequired.size() == 0)
            inventoryRequired = new HashMap<>();
        else
            inventoryRequired = invRequired;

        if (sellList == null || sellList.size() == 0)
            sellingItems = new HashMap<>();
        else
            sellingItems = sellList;

        inventoriesWorth = inventories;
        postTask = post;
    }

    @Override
    public void execute(WatAIO instance) {
        if (NPCs.all("Banker").size() == 0) {
            instance.currentTask = new TraversalTask(BankLocation.getNearest().getArea(5), this);
            return;
        }

        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 1500);
        }

        if (!Bank.isOpen())
            return;

        depositNonRequired();

        Logger.log("SELLCHECKER: STARTING");
        if (sellingItems.size() > 0) {
            boolean performSelling = false;
            if (Inventory.isFull()) {
                Bank.depositAllItems();
                Sleep.sleep(100, 200);
            }

            for (Map.Entry<String, Integer> entry : sellingItems.entrySet()) {
                int triggerAmount = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                if (Bank.contains(entry.getKey()) && Bank.get(entry.getKey()).getAmount() >= triggerAmount) {
                    checkAndSet(BankMode.NOTE);
                    Logger.log("SELLCHECKER: FOUND " + Bank.get(entry.getKey()).getName());
                    if (entry.getValue() > 0) {
                        int reduceBy = 0;
                        if (Inventory.contains(entry.getKey()))
                            reduceBy = Inventory.count(entry.getKey());

                        Logger.log("WITHDRAWING " + (entry.getValue() - reduceBy) + " OF " + entry.getKey());
                        Bank.withdraw(entry.getKey(), (entry.getValue() - reduceBy));
                    } else {
                        Logger.log("WITHDRAWING ALL OF " + entry.getKey());
                        Bank.withdrawAll(entry.getKey());
                    }
                    performSelling = true;
                }
            }

            if (performSelling) {
                instance.currentTask = new GrandExchangeTask("Selling proceeds", true, sellingItems, this);
                return;
            }
        }

        Logger.log("SELLCHECKER: DONE");
        checkAndSet(BankMode.ITEM);

        HashMap<String, Integer> buyingRequired = new HashMap<>();

        Logger.log("EQUIPMENTCHECKER: STARTING");
        if (equipmentRequired.size() > 0) {
            for (Map.Entry<String, Integer> entry : equipmentRequired.entrySet()) {
                int amountRequired = entry.getValue() > 0 ? entry.getValue() : 1;
                if (Equipment.contains(entry.getKey()) && Equipment.count(entry.getKey()) >= amountRequired) {
                    Logger.log("EQUIPMENTCHECKER: ITEM ALREADY EQUIPPED");
                    continue;
                }

                if (Inventory.contains(entry.getKey()) && Inventory.count(entry.getKey()) >= amountRequired) {
                    if (Inventory.interact(entry.getKey(), "Wear") || Inventory.interact(entry.getKey(), "Wield")) {
                        Logger.log("EQUIPMENTCHECKER: ITEM IN INVENTORY, EQUIPPING");
                        continue;
                    }
                }

                if (Bank.contains(entry.getKey()) && Bank.get(entry.getKey()).getAmount() >= amountRequired) {
                    if (Inventory.isFull()) { // deposit only if we need the space
                        depositNonRequired();
                        Sleep.sleep(100, 200);
                    }

                    if (Bank.withdraw(entry.getKey())) {
                        if (Inventory.contains(entry.getKey()) && Inventory.interact(entry.getKey(), "Equip")) {
                            Logger.log("EQUIPMENTCHECKER: WITHDREW ITEM, EQUIPPING");
                        }
                    }
                } else {
                    // need to buy.
                    buyingRequired.put(entry.getKey(), entry.getValue());
                    Logger.log("EQUIPMENTCHECKER: NEED TO BUY " + (entry.getValue() > 0 ? amountRequired : -entry.getValue()) + " OF " + entry.getKey());
                }
            }
        }
        Logger.log("EQUIPMENTCHECKER: DONE");

        Logger.log("INVENTORYCHECKER: STARTING");
        if (inventoryRequired.size() > 0) {
            for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
                int amountRequired = entry.getValue() > 0 ? entry.getValue() : 1; //TODO make it so arrows etc can be more than 1

                if (Inventory.contains(entry.getKey()) && Inventory.count(entry.getKey()) >= amountRequired) {
                    Logger.log("INVENTORYCHECKER: ITEM ALREADY IN INVENTORY, MINIMUM QUANTITY MET");
                    continue;
                }

                if (Bank.contains(entry.getKey()) && Bank.count(entry.getKey()) >= amountRequired) {
                    int toWithdraw = entry.getValue() > 0 ? entry.getValue() : Bank.count(entry.getKey());
                    if (Inventory.isFull()) {
                        Bank.depositAllItems();
                        Sleep.sleep(100, 200);
                    }

                    int reduceBy = 0;
                    if (Inventory.contains(entry.getKey())) {
                        reduceBy = Inventory.count(entry.getKey());
                    }

                    toWithdraw -= reduceBy;

                    if (Bank.withdraw(entry.getKey(), toWithdraw)) {
                        Logger.log("INVENTORYCHECKER: WITHDREW " + toWithdraw + " OF " + entry.getKey());
                    }
                } else {
                    // needing to buy it.
                    int amountToBuy = (entry.getValue() > 0 ? entry.getValue() : -entry.getValue()) * inventoriesWorth;
                    for (String s : WatAIO.SINGULAR_ITEMS) {
                        if (s.toLowerCase().contains(entry.getKey().toLowerCase())) {
                            amountToBuy = 1;
                            break;
                        }
                    }

                    buyingRequired.put(entry.getKey(), amountToBuy);
                    Logger.log("INVENTORYCHECKER: NEED TO BUY " + (entry.getValue() > 0 ? amountRequired : -entry.getValue()) + " OF " + entry.getKey());
                }
            }
        }

        Logger.log("INVENTORYCHECKER: DONE");

        Logger.log("BUYCHECKER: STARTING");
        if (buyingRequired.size() > 0) {
            checkAndSet(BankMode.ITEM);
            HashMap<String, Integer> m = new HashMap<>();

            int finalPrice = 0;
            for (Map.Entry<String, Integer> entry : buyingRequired.entrySet()) {
                int itemMultiplier = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                int initialPrice = LivePrices.get(entry.getKey()) * itemMultiplier; // price*itemCount
                finalPrice += (int) (initialPrice * 1.8); // safety stack?
            }

            if(Inventory.contains("Coins")) {
                Bank.deposit("Coins");
                Sleep.sleep(200, 400);
            }

            if ((Bank.contains("Coins") && Bank.count("Coins") >= finalPrice)) {
                Bank.withdraw("Coins", finalPrice);
                Sleep.sleep(100, 200);
                Bank.close();

                instance.currentTask = new GrandExchangeTask("Buying required items", false, buyingRequired, this);
                return;
            } else {
                boolean canSell = false;

                for (String s : WatAIO.EMERG_SELL) {
                    if (Bank.contains(s)) {
                        canSell = true;
                    }
                }

                if (canSell) {
                    checkAndSet(BankMode.NOTE);
                    Logger.log("BUYCHECKER: NEED TO SELL");
                    if (Inventory.isFull()) {
                        Bank.depositAllExcept("Coins");
                        Sleep.sleep(100, 200);
                    }

                    Collections.shuffle(WatAIO.EMERG_SELL);
                    for (String n : WatAIO.EMERG_SELL) {
                        if (!Inventory.contains(n) && Bank.contains(n)) {
                            Bank.withdrawAll(n);
                            Sleep.sleep(200, 400);
                            m.put(n, -1);
                        }
                    }
                } else {
                    if (!instance.MULE_DEAD) {
                        Bank.depositAllItems();
                        Sleep.sleep(100, 200);
                        Logger.log("Setting up a reverse mule to get " + (finalPrice * 2) + " gp");
                        int totalPrice = finalPrice;
                        instance.currentTask = new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                            {
                                put("Coins", (totalPrice * 2));
                            }
                        }, this);
                        return;
                    } else {
                        // logic to do a money gathering task.
                    }
                }
            }

            if (m.size() > 0) {
                Logger.log("BUYCHECKER: HANDING OFF TO GRAND EXCHANGE");
                instance.currentTask = new GrandExchangeTask("Emergency sell", true, m, this);
                return;
            }
        }

        Logger.log("BUYCHECKER: DONE");

        Logger.log("FINALCHECKS: STARTING");
        if (Inventory.contains("Coins")) {
            Bank.depositAll("Coins");
            Sleep.sleep(100, 200);
        }

        depositNonRequired();

        Logger.log("FINALCHECKS: DONE");

        Bank.close();

        if (postTask != null)
            instance.currentTask = postTask;
    }

    private void depositNonRequired() {
        if(Inventory.isEmpty()) {
            return;
        }

        for(Item i : Inventory.all()) {
            if(i == null)
                continue;

            boolean depositAll = true;
            if(inventoryRequired != null) {
                for (String s : inventoryRequired.keySet()) {
                    if (Inventory.contains(s)) {
                        depositAll = false;
                    }
                }
            }

            if(equipmentRequired != null) {
                for (String s : equipmentRequired.keySet()) {
                    if (Inventory.contains(s)) {
                        depositAll = false;
                    }
                }
            }

            if(!depositAll) {
                if ((inventoryRequired != null && !inventoryRequired.containsKey(i.getName())) && (equipmentRequired != null && !equipmentRequired.containsKey(i.getName()))) {
                    Bank.depositAll(i);
                    Sleep.sleep(100, 200);
                }
            }
            else {
                Bank.depositAllItems();
            }
        }
    }

    public void checkAndSet(BankMode mode) {
        if(Bank.getWithdrawMode().equals(mode)) {
            return;
        }

        Bank.setWithdrawMode(mode);
        Sleep.sleep(100, 200);
    }

    @Override
    public String getName() {
        return "Banking v2";
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
    public int loopTime() {
        return 650;
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
