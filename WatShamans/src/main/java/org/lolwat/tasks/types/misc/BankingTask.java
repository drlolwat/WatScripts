package org.lolwat.tasks.types.misc;

import org.dreambot.api.input.Mouse;
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
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.prayer.BuryBonesTask;

import java.time.Instant;
import java.util.*;

public class BankingTask implements WatTask {
    private final HashMap<String, Integer> inventoryRequired; // Process inv second..
    private final HashMap<String, Integer> sellingItems; // Check this at the very top of banking operations
    private final int inventoriesWorth;
    private WatTask postTask;
    private final List<String> restrictedItems = new ArrayList<String>() {
        {
            add("logs");
            add("trout");
            add("salmon");
            add("diamond necklace");
            add("gold bar");
        }
    };

    public BankingTask(HashMap<String, Integer> eqRequired, HashMap<String, Integer> invRequired, HashMap<String, Integer> sellList, Integer inventories, WatTask post) {
        if (invRequired == null || invRequired.isEmpty())
            inventoryRequired = new HashMap<>();
        else
            inventoryRequired = invRequired;

        if (sellList == null || sellList.isEmpty())
            sellingItems = new HashMap<>();
        else
            sellingItems = sellList;

        inventoriesWorth = inventories;
        postTask = post;
    }

    @Override
    public void execute(WatAIO instance) {
        if (NPCs.all("Banker").isEmpty()) {
            instance.currentTask = new TraversalTask(BankLocation.getNearest().getArea(3), this);
            return;
        }

        if (!Bank.isOpen()) {
            Bank.open();
            Sleep.sleepUntil(Bank::isOpen, 1500);
        }

        if (!Bank.isOpen())
            return;

        depositNonRequired();

        Logger.log("Sell Checker: starting");

        boolean allowedToSell = instance.TRADE_UNLOCKED;

        Logger.log("Trade unrestricted: " + allowedToSell);

        List<String> toRemove = new ArrayList<>();
        if(!allowedToSell) {
            for(String s : sellingItems.keySet()) {
                if(restrictedItems.contains(s.toLowerCase())) {
                    allowedToSell = true;
                } else {
                    toRemove.add(s);
                }
            }

            for(String s : toRemove) {
                sellingItems.remove(s);
            }

            toRemove.clear();
        }

        if(allowedToSell) {
            if (!sellingItems.isEmpty()) {
                boolean performSelling = false;
                if (Inventory.isFull()) {
                    Bank.depositAllItems();
                    Sleep.sleep(300, 600);
                }

                for (Map.Entry<String, Integer> entry : sellingItems.entrySet()) {
                    int triggerAmount = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                    if (Bank.contains(entry.getKey()) && Bank.get(entry.getKey()).getAmount() >= triggerAmount) {
                        checkAndSet(BankMode.NOTE);
                        Logger.log("Sell checker: found " + Bank.get(entry.getKey()).getName());
                        if (entry.getValue() > 0) {
                            int reduceBy = 0;
                            if (Inventory.contains(entry.getKey()))
                                reduceBy = Inventory.count(entry.getKey());

                            Logger.log("Taking " + (entry.getValue() - reduceBy) + " of " + entry.getKey());
                            Bank.withdraw(entry.getKey(), (entry.getValue() - reduceBy));
                        } else {
                            Logger.log("Taking " + entry.getKey());
                            Bank.withdrawAll(entry.getKey());
                        }
                        performSelling = true;
                    }
                }

                if (performSelling) {
                    instance.currentTask = new GrandExchangeTask("Selling at G.E", true, sellingItems, this);
                    return;
                }
            } else {
                Logger.log("Nothing to sell");
            }
        } else {
            Logger.log("Not allowed to sell (account restricted, or restricted items)");
        }

        Logger.log("SELLCHECKER: DONE");
        checkAndSet(BankMode.ITEM);

        HashMap<String, Integer> buyingRequired = new HashMap<>();

        // CHECKERS
        equipmentChecker(buyingRequired);
        inventoryChecker(instance, buyingRequired);
        buyChecker(instance, buyingRequired);

        if(!instance.MULE_DEAD && instance.TRADE_UNLOCKED) {
            int invMoney = 0;
            int bankMoney = 0;

            if(Inventory.contains("Coins")) {
                invMoney = Inventory.get("Coins").getAmount();
            }

            if(Bank.contains("Coins")) {
                bankMoney = Bank.get("Coins").getAmount();
            }

            if((invMoney + bankMoney) >= instance.MULE_TRIGGER) {
                int toWithdraw = (bankMoney - invMoney) - instance.MULE_SAFETY_NET;
                if(toWithdraw > 0) {
                    Logger.log("MULE TARGET MET, REDIRECTING TO MULE");
                    if(Inventory.isFull() || Inventory.emptySlotCount() < 2) {
                        Bank.depositAllExcept("Coins");
                    }
                    Sleep.sleep(100, 200);
                    Bank.withdraw("Coins", toWithdraw);
                    Sleep.sleep(200, 400);
                    postTask = new MulingTask("Muling Gold", Worlds.getCurrentWorld(), postTask);
                }
            }
        }

        Logger.log("FINALCHECKS: STARTING");

        if(postTask != null && !(postTask instanceof MulingTask)) {
            depositNonRequired();
        }

        // calculate net worth
        if(instance.NET_WORTH_GENERATED == 0) {
            instance.NET_WORTH = 0;
            for (Item i : Bank.all()) {
                if (i == null)
                    continue;

                if (i.getAmount() > 1) {
                    instance.NET_WORTH += LivePrices.get(i) * i.getAmount();
                } else {
                    instance.NET_WORTH += LivePrices.get(i);
                }
            }

            for (Item i : Inventory.all()) {
                if (i == null)
                    continue;

                if (i.getAmount() > 1) {
                    instance.NET_WORTH += LivePrices.get(i) * i.getAmount();
                } else {
                    instance.NET_WORTH += LivePrices.get(i);
                }
            }

            instance.NET_WORTH_GENERATED = Instant.now().getEpochSecond();
        } else {
            if((Instant.now().getEpochSecond() - instance.NET_WORTH_GENERATED) >= 3600) {
                instance.NET_WORTH_GENERATED = 0; // will generate net worth next time.
            }
        }

        Logger.log("FINALCHECKS: DONE");

        if (postTask != null) {
            if(postTask instanceof BuryBonesTask || postTask instanceof BreakingTask) {
                Bank.close();
            }

            instance.currentTask = postTask;
        }
    }

    private void buyChecker(WatAIO instance, HashMap<String, Integer> buyingRequired) {
        Logger.log("BUYCHECKER: STARTING");
        if (!buyingRequired.isEmpty()) {
            checkAndSet(BankMode.ITEM);
            HashMap<String, Integer> m = new HashMap<>();

            if(Inventory.fullSlotCount() >= 26) {
                Bank.depositAllItems();
                Sleep.sleep(100, 400);
            }

            int finalPrice = 0;
            for (Map.Entry<String, Integer> entry : buyingRequired.entrySet()) {
                int itemMultiplier = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                int initialPrice = NumUtils.getItemPrice(entry.getKey());
                int multipliedPrice = initialPrice * itemMultiplier;
                finalPrice += multipliedPrice;
            }

            if ((Bank.contains("Coins") && Bank.count("Coins") >= finalPrice)) {
                Bank.withdraw("Coins", finalPrice);
                Sleep.sleep(100, 200);
                Bank.close();

                instance.currentTask = new GrandExchangeTask("Buying required items", false, buyingRequired, this);
                return;
            } else {
                boolean canSell = false;

                for (String s : instance.EMERGENCY_SELL) {
                    if (Bank.contains(s)) {
                        canSell = true;
                    }
                }

                if (canSell && instance.TRADE_UNLOCKED) {
                    checkAndSet(BankMode.NOTE);
                    Logger.log("BUYCHECKER: NEED TO SELL");
                    if (Inventory.isFull()) {
                        Bank.depositAllExcept("Coins");
                        Sleep.sleep(100, 200);
                    }

                    boolean needsMule = true;
                    Collections.shuffle(instance.EMERGENCY_SELL);
                    for (String n : instance.EMERGENCY_SELL) {
                        if (!Inventory.contains(n) && Bank.contains(n)) {
                            Bank.withdrawAll(n);
                            Sleep.sleep(200, 400);
                            m.put(n, -1);
                            needsMule = false;
                        }
                    }

                    if (needsMule) {
                        if(!instance.MULE_DEAD) {
                            if (Bank.contains("Coins")) {
                                finalPrice -= Bank.count("Coins");
                            }

                            if (Inventory.contains("Coins")) {
                                finalPrice -= Inventory.count("Coins");
                            }

                            Logger.log("No items to sell, so reverse muling " + finalPrice + " gp");
                            int totalPrice = finalPrice;
                            instance.currentTask = new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                                {
                                    put("Coins", totalPrice);
                                }
                            }, this);
                            return;
                        } else {
                            Logger.log("We are out of GP, time to go and make some.");
                            WatAIO.NEED_MM = true;
                            instance.currentTask = null;
                            return;
                        }
                    }
                } else {
                    if (!instance.MULE_DEAD) {
                        if(Bank.contains("Coins")) {
                            finalPrice -= Bank.count("Coins");
                        }

                        if(Inventory.contains("Coins")) {
                            finalPrice -= Inventory.count("Coins");
                        }

                        Bank.depositAllItems();
                        Sleep.sleep(100, 200);
                        Logger.log("Trade locked, so reverse muling " + finalPrice + " gp");
                        int totalPrice = finalPrice;
                        instance.currentTask = new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                            {
                                put("Coins", totalPrice);
                            }
                        }, this);
                        return;
                    } else {
                        Logger.log("We are out of GP, time to go and make some.");
                        WatAIO.NEED_MM = true;
                        instance.currentTask = null;
                        return;
                    }
                }
            }

            if (!m.isEmpty()) {
                Logger.log("BUYCHECKER: HANDING OFF TO GRAND EXCHANGE");
                instance.currentTask = new GrandExchangeTask("Selling at G.E", true, m, this);
                return;
            }
        }

        Logger.log("BUYCHECKER: DONE");
    }

    private void inventoryChecker(WatAIO instance, HashMap<String, Integer> buyingRequired) {
        Logger.log("INVENTORYCHECKER: STARTING");
        if (!inventoryRequired.isEmpty()) {
            checkAndSet(BankMode.ITEM);
            for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
                int amountRequired = entry.getValue() > 0 ? entry.getValue() : 1;
                if (instance.SINGULAR_ITEMS.contains(entry.getKey()))
                    amountRequired = 1;

                if (Inventory.contains(entry.getKey()) && Inventory.count(entry.getKey()) >= amountRequired) {
                    if(Inventory.get(entry.getKey()).isNoted()) {
                        Bank.depositAll(entry.getKey());
                        Sleep.sleep(100, 300);
                    } else {
                        Logger.log("INVENTORYCHECKER: ITEM ALREADY IN INVENTORY, MINIMUM QUANTITY MET");
                        continue;
                    }
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

                    if (buyingRequired.isEmpty() && Bank.withdraw(entry.getKey(), toWithdraw)) {
                        Logger.log("INVENTORYCHECKER: WITHDREW " + toWithdraw + " OF " + entry.getKey());
                    }
                } else {
                    // needing to buy it.
                    int amountToBuy = (entry.getValue() > 0 ? entry.getValue() : -entry.getValue()) * inventoriesWorth;
                    for (String s : instance.SINGULAR_ITEMS) {
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
    }

    private void equipmentChecker(HashMap<String, Integer> buyingRequired) {
        Logger.log("EQUIPMENTCHECKER: STARTING");
        if(postTask != null) {
            if (!postTask.clothesRequired().isEmpty()) {
                for (Map.Entry<String, Integer> entry : postTask.clothesRequired().entrySet()) {
                    int amountRequired = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                    if (Equipment.contains(entry.getKey()) && Equipment.count(entry.getKey()) >= amountRequired) {
                        Logger.log("EQUIPMENTCHECKER: ITEM ALREADY EQUIPPED");
                        continue;
                    }

                    if (Inventory.contains(entry.getKey()) && Inventory.count(entry.getKey()) >= amountRequired) {
                        Logger.log("EQUIPMENTCHECKER: ITEM IN INVENTORY ALREADY");
                        continue;
                    }

                    if (Bank.contains(entry.getKey()) && Bank.count(entry.getKey()) >= amountRequired) {
                        if (Inventory.fullSlotCount() >= 26) { // deposit only if we need the space
                            depositNonRequired();
                            Sleep.sleep(100, 200);
                        }

                        if (buyingRequired.isEmpty() && Bank.withdraw(entry.getKey(), amountRequired)) {
                            Sleep.sleep(200, 400);
                            if (Inventory.contains(entry.getKey())) {
                                Logger.log("EQUIPMENTCHECKER: WITHDREW ITEM");
                            }
                        }
                    } else {
                        // need to buy.
                        buyingRequired.put(entry.getKey(), entry.getValue());
                        Logger.log("EQUIPMENTCHECKER: NEED TO BUY " + (entry.getValue() > 0 ? amountRequired : -entry.getValue()) + " OF " + entry.getKey());
                    }
                }
            }

            Sleep.sleep(500, 1000);
            for (String s : postTask.clothesRequired().keySet()) {
                for (Item i : Equipment.all()) {
                    if ((i != null && postTask != null) && !postTask.clothesRequired().containsKey(i.getName())) {
                        Bank.depositAllEquipment();
                        Sleep.sleep(50, 120);
                        equipmentChecker(buyingRequired);
                        break;
                    }
                }

                if (!Equipment.contains(s)) {
                    if (Inventory.contains(s) && !GenericUtils.equipItem(s, null)) {
                        Logger.error("Error equipping item in BankingTask");
                    }
                }
            }

            if (postTask.clothesRequired().isEmpty() && !Equipment.isEmpty()) {
                Bank.depositAllEquipment();
            }
        }

        Logger.log("EQUIPMENTCHECKER: DONE");
    }

    private void depositNonRequired() {
        if(Inventory.isEmpty()) {
            return;
        }

        if(postTask != null) {
            boolean hasRequired = false;
            if (inventoryRequired != null && !inventoryRequired.isEmpty()) {
                for (String i : inventoryRequired.keySet()) {
                    if (Inventory.contains(i)) {
                        hasRequired = true;
                    }
                }
            }

            if (postTask.clothesRequired() != null && !postTask.clothesRequired().isEmpty()) {
                for (String i : postTask.clothesRequired().keySet()) {
                    if (Inventory.contains(i)) {
                        hasRequired = true;
                    }
                }
            }

            if (!hasRequired) {
                Bank.depositAllItems();
                Sleep.sleep(200, 600);
                return;
            }

            //List<Item> toDeposit = new ArrayList<>();
            HashMap<Item, Integer> toDeposit = new HashMap<>();
            for (Item i : Inventory.all()) {
                if (i == null)
                    continue;

                if (inventoryRequired != null && !inventoryRequired.containsKey(i.getName()) && postTask.clothesRequired() != null && !postTask.clothesRequired().containsKey(i.getName())) {
                    int am = i.getAmount();
                    if(toDeposit.containsKey(i)) {
                        am = am + toDeposit.get(i);
                    }

                    toDeposit.put(i, am);
                }
            }

            if (!toDeposit.isEmpty()) {
                for (Map.Entry<Item, Integer> i : toDeposit.entrySet()) {
                    if(i.getValue() == 1) {
                        if(!Inventory.get(i.getKey().getName()).interact()) {
                            Logger.log("issue interacting with item in inventory");
                        }
                    } else {
                        Bank.depositAll(i.getKey());
                    }

                    Sleep.sleep(10, 30);
                }
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
        return "Banking";
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

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }
}
