package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.tasks.WatTask;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.WatAIO;
import org.lolwat.tasks.types.combat.MeleeCombatTask;
import org.lolwat.tasks.types.crafting.JewelryTask;
import org.lolwat.tasks.types.smithing.SmithingItemTask;

import java.util.HashMap;
import java.util.Map;

public class BankingTask implements WatTask {
    private final String name;
    private final WatTask postTask;
    private final HashMap<String, Integer> requiredItems;
    private final boolean depositAllFirst;
    private final boolean buyMissingItems;
    private final HashMap<String, Integer> grandExchangeToBuy;
    private final HashMap<String, Integer> sellItemsToCheck;
    private final int buyMultiplier;

    public BankingTask(String taskName, HashMap<String, Integer> required, boolean depositAll, WatTask post, boolean buyMissing, HashMap<String, Integer> sellItems, int buyMulti) {
        name = taskName;
        requiredItems = required;
        depositAllFirst = depositAll;
        buyMissingItems = buyMissing;
        postTask = post;
        grandExchangeToBuy = new HashMap<>();
        sellItemsToCheck = sellItems;
        buyMultiplier = buyMulti;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        BankLocation loc = BankLocation.getNearest();
        // Are we near a banker?
        if(loc.getArea(10).contains(Players.getLocal())) {
            // Open the bank if we don't have it open
            if (!Bank.isOpen()) {
                Bank.open();
                return;
            }

            Sleep.sleepUntil(Bank::isOpen, 10000);

            if(Bank.contains("Coins")) {
                instance.netWorth = Bank.get("Coins").getAmount();
            }

            for(Item i : Bank.all()) {
                instance.netWorth += LivePrices.get(i);
            }

            // Does the bank request want us to deposit everything
            if (depositAllFirst && !Inventory.isEmpty()) {
                // Deposit everything except the items we require from the request
                for(Item it : Inventory.all()) {
                    if(it != null && requiredItems != null && !requiredItems.containsKey(it.getName())) {
                        if(postTask != null) {
                            // lets not get rid of hammers/moulds etc if the pre/post task needs em
                            if(postTask instanceof SmithingItemTask || postTask instanceof JewelryTask) {
                                if(it.getName().equalsIgnoreCase("hammer") || it.getName().contains("mould")) {
                                    continue;
                                }
                            }
                        }

                        Bank.depositAll(it);
                    }
                }

                if(!Equipment.isEmpty()) {
                    if(postTask != null && !(postTask instanceof MeleeCombatTask)) {
                        Bank.depositAllEquipment();
                    }
                }
            }

            if(Inventory.contains("Coins")) {
                Bank.deposit("Coins");
                Sleep.sleep(300, 600);
            }

            //TODO deposit items if we don't have enough space to meet the request

            // Let's check for the items and quantity that we want to trigger for selling, if provided
            if(sellItemsToCheck != null && sellItemsToCheck.size() > 0) {
                if (Skills.getTotalLevel() >= 150 && Quests.getQuestPoints() >= 10) {
                    Logger.log("==== Checking for sell-able items ====");
                    HashMap<String, Integer> items = new HashMap<>();
                    for (Map.Entry<String, Integer> item : sellItemsToCheck.entrySet()) {
                        if (Bank.contains(item.getKey())) {
                            Item it = Bank.get(item.getKey());
                            if (it != null && ((item.getValue() > 0 && it.getAmount() >= item.getValue()) || (item.getValue() < 0 && it.getAmount() >= -item.getValue()))) {
                                Logger.log("Sell target met for: " + item.getKey() + "(" + item.getValue() + ")");
                                items.put(it.getName(), item.getValue() < 0 ? it.getAmount() : item.getValue());
                            }
                        }
                    }

                    if (items.size() > 0) {
                        for (Map.Entry<String, Integer> it : items.entrySet()) {
                            if (!Inventory.isFull() && Bank.contains(it.getKey())) {
                                if (Bank.getWithdrawMode() != BankMode.NOTE) {
                                    Bank.setWithdrawMode(BankMode.NOTE);
                                }

                                Logger.log("Withdrawing " + it.getKey());
                                // we have to double check here lol just in case, for notes
                                Item bankItem = Bank.get(it.getKey());
                                if (bankItem != null) {
                                    if (bankItem.getAmount() == it.getValue()) {
                                        // If the item amount in the bank matches it.getValue(), withdraw all
                                        Bank.withdrawAll(it.getKey());
                                    } else {
                                        // Otherwise, withdraw the specific quantity
                                        Bank.withdraw(it.getKey(), it.getValue());
                                    }
                                    Sleep.sleep(100, 500);
                                }
                            }
                        }

                        Bank.close();
                        instance.sleep(1000, 3000);
                        instance.currentTask = new GrandExchangeTask("Selling items", true, items, this);
                        return;
                    }
                } else {
                    // likely not trade unlocked
                    Logger.log("i have items to sell, but i am not trade unlocked");
                }
            }

            if(Bank.getWithdrawMode() != BankMode.ITEM) {
                Bank.setWithdrawMode(BankMode.ITEM);
            }

            // Loop through our required items, backing it up first because i fucked the design up
            Logger.log("==== Making sure we have required items ====");
            for (Map.Entry<String, Integer> item : requiredItems.entrySet()) {
                if (grandExchangeToBuy.containsKey(item.getKey()))
                    continue;

                Logger.log("Checking Inventory for item: " + item.getKey());
                // Do we have the item in our inventory, and do we have enough of it?
                if (!Inventory.contains(item.getKey()) || (Inventory.contains(item.getKey()) && Inventory.get(item.getKey()).getAmount() < item.getValue())) {
                    Logger.log("We need to withdraw " + item.getKey());
                    // Nope, we don't have enough of it or none at all.
                    Item invItem = Inventory.get(item.getKey());
                    if (invItem != null) {
                        // We have some in our inventory already, so we actually don't need to withdraw the full amount
                        item.setValue(item.getValue() - invItem.getAmount());

                        // Maybe we no longer need to withdraw it, so skip this item
                        if (item.getValue() <= 0) {
                            Logger.log("We have enough " + item.getKey() + "(" + invItem.getAmount() + "), " + item.getValue() + " requested");
                            continue;
                        } else {
                            Logger.log("We only need to withdraw " + item.getValue() + " because we have some already");
                        }
                    }
                    // Let's check the bank now
                    if (Bank.contains(item.getKey())) {
                        // We own the item, great, do we own the right amount
                        Item bankItem = Bank.get(item.getKey());
                        if (bankItem.getAmount() >= item.getValue() || item.getValue() < 0) {
                            // We own the right amount already or just want to withdraw everything we have
                            if(item.getValue() > 0) {
                                Bank.withdraw(item.getKey(), item.getValue());
                            } else {
                                Bank.withdrawAll(item.getKey());
                            }
                        } else {
                            // We don't own the right amount of it
                            if (buyMissingItems) {
                                Logger.log("We need " + ((item.getValue() * buyMultiplier) - bankItem.getAmount()) + " more of " + item.getKey() + ", adding to G.E buy list");
                                grandExchangeToBuy.put(item.getKey(), (item.getValue() * buyMultiplier) - bankItem.getAmount());
                            } else {
                                Logger.error("We need " + item.getValue() + " of " + item.getKey() + " but we only have " + bankItem.getAmount());
                                instance.fatalError = true;
                                instance.removeTaskAndReset();
                                return;
                            }
                        }
                    } else {
                        // We don't own any of it
                        if (buyMissingItems) {
                            Logger.log("We need " + item.getValue() * buyMultiplier + " of " + item.getKey() + ", adding to G.E buy list");
                            grandExchangeToBuy.put(item.getKey(), (item.getValue() * buyMultiplier));
                        } else {
                            Logger.error("We need " + item.getValue() + " of " + item.getKey() + " but we own none");
                            instance.removeTaskAndReset();
                            return;
                        }
                    }
                }
            }

            //requiredItems.clear();

            Logger.log("==== Finished checking for required items ====");

            if (grandExchangeToBuy.size() > 0 && buyMissingItems) {
                Logger.log("We need to create a G.E task to buy " + grandExchangeToBuy.size() + " items: ");
                int totalValue = 0;
                for (Map.Entry<String, Integer> buyItem : grandExchangeToBuy.entrySet()) {
                    Logger.log(buyItem.getKey() + " Quantity of: " + buyItem.getValue());
                    // Calculate the amount of money we are going to need to buy everything at market price or so + 10%
                    if(LivePrices.get(buyItem.getKey()) >= 8) {
                        totalValue += (LivePrices.get(buyItem.getKey()) * (buyItem.getValue() > 1 ? buyItem.getValue() : -buyItem.getValue())) * 1.8;
                    } else {
                        totalValue += (10 * (buyItem.getValue() > 1 ? buyItem.getValue() : -buyItem.getValue())) * 1.8;
                    }
                }

                if(totalValue <= 0) {
                    totalValue = -totalValue * 3;
                }

                // Holy shit! Probably gonna be a lot.
                Logger.log("We are gonna need " + totalValue + " GP to buy everything we need.");
                if(Bank.contains("Coins")) {
                    Item bankCoins = Bank.get("Coins");
                    if(bankCoins != null && bankCoins.getAmount() >= totalValue) {
                        Bank.depositAllItems();
                        Bank.withdrawAll(bankCoins.getID()); //Bank.withdraw(bankCoins.getID(), totalValue);
                        Bank.close();
                        // Send them to the Grand Exchange and have them do another task after (null means it will evaluate when complete)
                        instance.currentTask = new GrandExchangeTask("Buying missing items", false, grandExchangeToBuy, this); // RETURNING THIS AS POST TASK INSTEAD OF NULL
                    }
                    else {
                        if(!instance.MULE_DEAD) {
                            Bank.depositAllItems();
                            Sleep.sleep(100, 200);
                            Logger.log("Setting up a reverse mule to get 100k gp");
                            instance.currentTask = new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() { { put("Coins", 100000); }}, this);
                        } else {
                            // TODO A FUNCTION
                            if(Inventory.size() > 0) {
                                Bank.depositAllItems();
                                Sleep.sleep(100, 200);
                            }

                            if(!Bank.getWithdrawMode().equals(BankMode.NOTE)) {
                                Bank.setWithdrawMode(BankMode.NOTE);
                                Sleep.sleep(100, 200);
                            }

                            HashMap<String, Integer> m = new HashMap<>();
                            for(String n : WatAIO.EMERG_SELL) {
                                if(!Inventory.contains(n) && Bank.contains(n)) {
                                    Bank.withdrawAll(n);
                                    Sleep.sleep(200, 400);
                                    m.put(n, -1);
                                }
                            }

                            if(!Bank.getWithdrawMode().equals(BankMode.ITEM)) {
                                Bank.setWithdrawMode(BankMode.ITEM);
                                Sleep.sleep(100, 200);
                            }

                            instance.currentTask = new GrandExchangeTask("Emergency sell", true, m, this);

                            //Logger.error("We don't have enough GP to fulfill the G.E orders. Need: " + totalValue + ", have: " + (bankCoins != null ? bankCoins.getAmount() : 0));
                            //instance.currentTask = null;
                        }
                    }
                }
                else {
                    if(!instance.MULE_DEAD) {
                        Bank.depositAllItems();
                        Sleep.sleep(100, 200);
                        Logger.log("Setting up a reverse mule to get 100k gp");
                        instance.currentTask = new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() { { put("Coins", 100000); }}, this);
                    } else {
                        if(Inventory.size() > 0) {
                            Bank.depositAllItems();
                            Sleep.sleep(100, 200);
                        }

                        if(!Bank.getWithdrawMode().equals(BankMode.NOTE)) {
                            Bank.setWithdrawMode(BankMode.NOTE);
                            Sleep.sleep(100, 200);
                        }

                        HashMap<String, Integer> m = new HashMap<>();
                        for(String n : WatAIO.EMERG_SELL) {
                            if(!Inventory.contains(n) && Bank.contains(n)) {
                                Bank.withdrawAll(n);
                                Sleep.sleep(200, 400);
                                m.put(n, -1);
                            }
                        }

                        if(!Bank.getWithdrawMode().equals(BankMode.ITEM)) {
                            Bank.setWithdrawMode(BankMode.ITEM);
                            Sleep.sleep(100, 200);
                        }

                        instance.currentTask = new GrandExchangeTask("Emergency sell", true, m, this);
                        /*
                        Logger.error("We don't have enough GP to fulfill the G.E orders.");
                        instance.currentTask = null;*/
                    }
                }
            } else {
                if(!instance.MULE_DEAD && (Quests.getQuestPoints() >= 10 && Skills.getTotalLevel() >= 150)) {
                    int invMoney = 0;
                    int bankMoney = 0;

                    if (Inventory.contains("Coins")) {
                        invMoney = Inventory.get("Coins").getAmount();
                    }

                    if (Bank.contains("Coins")) {
                        bankMoney = Bank.get("Coins").getAmount();
                    }

                    if ((invMoney + bankMoney) >= WatAIO.MULE_TRIGGER) {
                        int toWithdraw = (bankMoney - invMoney) - WatAIO.MULE_SAFETY_NET;
                        if (toWithdraw > 0) {
                            Bank.depositAllExcept("Coins");
                            Sleep.sleep(100, 500);
                            Bank.withdraw("Coins", toWithdraw);
                            Sleep.sleep(100, 500);
                            Bank.close();
                            instance.currentTask = new MulingTask("Muling Gold", Worlds.getCurrentWorld(), this);
                            return;
                        }
                    }
                }

                // We are done banking operations, and we are NOT setting up a Grand Exchange Task.
                requiredItems.clear();
                Bank.close();
                instance.currentTask = postTask;
            }
        }
        else {
            instance.currentTask = new TraversalTask(loc.getArea(10), this);
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 500;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {}

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
