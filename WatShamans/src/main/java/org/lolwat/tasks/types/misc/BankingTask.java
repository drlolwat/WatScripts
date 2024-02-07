package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.combat.warriorguild.FightArmorSetTask;
import org.lolwat.tasks.types.combat.warriorguild.FightCyclopsTask;
import org.lolwat.tasks.types.magic.HighAlchemyTask;
import org.lolwat.tasks.types.prayer.BuryBonesTask;

import java.time.Instant;
import java.util.*;

public class BankingTask implements WatTask {
    private HashMap<String, Integer> inventoryRequired; // Process inv second..
    private HashMap<String, Integer> sellingItems; // Check this at the very top of banking operations
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

    public BankingTask(HashMap<String, Integer> invRequired, HashMap<String, Integer> sellList, Integer inventories, WatTask post) {
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

        if(post != null && !post.inventoryRequired().isEmpty()) {
            inventoryRequired = post.inventoryRequired();
        }
    }

    @Override
    public void execute(WatAIO instance) {
        //if(postTask != null) {
        //    Logger.error(postTask.getName());
        //}

        //TODO a list of chest names to use for ex. Duel arena/Castle wars chests
        if (NPCs.all("Banker").isEmpty() && GameObjects.all("Bank booth").isEmpty()) {
            GameObject chest = GameObjects.closest("Open chest");
            if(chest == null || !chest.hasAction("Bank")) {
                instance.currentTask = new TraversalTask(BankLocation.getNearest(Players.getLocal().getTile(), false).getArea(3), this);
                return;
            }
        }

        if (!Bank.isOpen()) {
            GameObject chest = GameObjects.closest("Open chest");
            if(chest == null || !chest.hasAction("Bank")) {
                Bank.open();
            } else {
                chest.interact();
            }

            Sleep.sleepUntil(Bank::isOpen, 1500);
        }

        if (!Bank.isOpen()) {
            return;
        }

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
                    Sleep.sleepUntil(Inventory::isEmpty, Calculations.random(5000, 10000));
                }

                for (Map.Entry<String, Integer> entry : sellingItems.entrySet()) {
                    int triggerAmount = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                    if (Bank.contains(entry.getKey()) && Bank.get(entry.getKey()).getAmount() >= triggerAmount) {
                        checkAndSet(BankMode.NOTE);
                        Logger.log("Sell checker: found " + Bank.get(entry.getKey()).getName());
                        if(!Inventory.isFull()) {
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
                        } else {
                            Logger.log("Sell checker: found " + Bank.get(entry.getKey()).getName() + ", but inventory is full");
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

        Logger.log("Sell checker: finished");
        checkAndSet(BankMode.ITEM);

        HashMap<String, Integer> buyingRequired = new HashMap<>();

        Logger.log("Equipment: Beginning checks");
        if(postTask != null) {
            if (!postTask.clothesRequired().isEmpty()) {
                for (Map.Entry<String, Integer> entry : postTask.clothesRequired().entrySet()) {
                    int amountRequired = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                    if (Equipment.contains(entry.getKey()) && Equipment.count(entry.getKey()) >= amountRequired) {
                        Logger.log("Equipment: Already equipped: " + entry.getKey());
                        Sleep.sleep(200, 400);
                        continue;
                    }

                    if(Inventory.count(entry.getKey()) > amountRequired) {
                        Logger.log("Equipment: Depositing extras of: " + entry.getKey());
                        Bank.deposit(entry.getKey(), (Inventory.count(entry.getKey()) - amountRequired));
                        Sleep.sleepUntil(() -> Inventory.count(entry.getKey()) == amountRequired, 1500);
                    }

                    if (Inventory.contains(entry.getKey()) && Inventory.count(entry.getKey()) >= amountRequired) {
                        Logger.log("Equipment: Already in inventory: " + entry.getKey());
                        Sleep.sleep(200, 400);
                        continue;
                    }

                    if (Bank.contains(entry.getKey()) && Bank.count(entry.getKey()) >= amountRequired) {
                        if (Inventory.isFull()) {
                            Bank.depositAllItems();
                            Sleep.sleepUntil(Inventory::isEmpty, Calculations.random(5000, 10000));
                        }

                        if (Bank.withdraw(entry.getKey(), amountRequired)) {
                            Sleep.sleepUntil(() -> Inventory.contains(entry.getKey()), Calculations.random(5000, 10000));
                            if (Inventory.contains(entry.getKey())) {
                                Logger.log("Equipment: Successfully withdrew: " + entry.getKey());
                            }
                        }

                    } else {
                        //TODO a path to obtaining untradeables that isnt hardcoded per shit
                        if(entry.getKey().contains("defender")) {
                            Logger.log("We are going to go and obtain a " + entry.getKey() + " first. Restarting loop.");

                            String latestObtained = "";

                            if(!Equipment.contains(x -> x.getName().contains("defender"))) {
                                if (Bank.contains("Rune defender"))
                                    latestObtained = "Rune defender";
                                else if (Bank.contains("Adamant defender"))
                                    latestObtained = "Adamant defender";
                                else if (Bank.contains("Mithril defender"))
                                    latestObtained = "Mithril defender";
                                else if (Bank.contains("Black defender"))
                                    latestObtained = "Black defender";
                                else if (Bank.contains("Steel defender"))
                                    latestObtained = "Steel defender";
                                else if (Bank.contains("Iron defender"))
                                    latestObtained = "Iron defender";
                                else if (Bank.contains("Bronze defender"))
                                    latestObtained = "Bronze defender";
                            } else {
                                latestObtained = Equipment.get(x -> x.getName().contains("defender")).getName();
                            }

                            int tokens = Bank.count("Warrior guild token") + Inventory.count("Warrior guild token");
                            if(tokens >= 200) {
                                instance.currentTask = new FightCyclopsTask(postTask.trainsSkill(), new HashMap<String, Integer>() {
                                    {
                                        put("Lobster", 20);
                                    }
                                }, latestObtained);
                                return;
                            }
                            else {
                                instance.currentTask = new FightArmorSetTask(postTask.trainsSkill(), new HashMap<String, Integer>() {
                                    {
                                        put("Lobster", 20);
                                    }
                                }, latestObtained);
                            }
                            return;
                        }

                        // need to buy.
                        buyingRequired.put(entry.getKey(), amountRequired);
                        Logger.log("Equipment: Need to buy " + (entry.getValue() > 0 ? amountRequired : -entry.getValue()) + " of: " + entry.getKey());
                    }
                }
            }

            Sleep.sleep(500, 1000);

            if(!postTask.clothesRequired().isEmpty()) {
                for (Map.Entry<String, Integer> entry : postTask.clothesRequired().entrySet()) {
                    if (!Equipment.contains(entry.getKey()) && Inventory.contains(entry.getKey())) {
                        if(Bank.isOpen()) {
                            Bank.close();
                            Sleep.sleepUntil(() -> !Bank.isOpen(), Calculations.random(5000, 10000));
                        }

                        if (!GenericUtils.equipItem(entry.getKey(), null)) {
                            Logger.error("Equipment: Error equipping item");
                        }
                    }
                }

                if(!Bank.isOpen()) {
                    Bank.open();
                    Sleep.sleepUntil(Bank::isOpen, Calculations.random(5000, 10000));
                }
            }
            /*
            for (String s : postTask.clothesRequired().keySet()) {
                if(!Equipment.contains(s)) {
                    if (Inventory.contains(s) && GenericUtils.canEquipTool(s) && !GenericUtils.equipItem(s, null)) {
                        Logger.error("Equipment: Error equipping item");
                    }
                }
            }*/

            if (postTask.clothesRequired().isEmpty() && !Equipment.isEmpty()) {
                Bank.depositAllEquipment();
            }
        }

        Logger.log("Equipment: Finished checking");

        Logger.log("Inventory: Beginning checks");
        if (!inventoryRequired.isEmpty()) {
            for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
                checkAndSet(BankMode.ITEM);
                int amountRequired;
                if (instance.SINGULAR_ITEMS.contains(entry.getKey()))
                    amountRequired = 1;
                else {
                    amountRequired = entry.getValue() > 0 ? entry.getValue() : 1;
                }

                if (Inventory.contains(entry.getKey()) && Inventory.count(entry.getKey()) >= amountRequired) {
                    if(Inventory.get(entry.getKey()).isNoted() && (postTask != null && !(postTask instanceof HighAlchemyTask))) {
                        Bank.depositAll(entry.getKey());
                        Sleep.sleep(100, 300);
                    } else {
                        if(entry.getValue() > 0 && Inventory.count(entry.getKey()) > amountRequired) {
                            Logger.log("Inventory: Depositing extras of: " + entry.getKey());
                            Bank.deposit(entry.getKey(), (Inventory.count(entry.getKey()) - amountRequired));
                            Sleep.sleepUntil(() -> Inventory.count(entry.getKey()) == amountRequired, 1500);
                        }

                        Logger.log("Inventory: We have enough of " + entry.getKey() + " already");
                        continue;
                    }
                }

                if (Bank.contains(entry.getKey()) && Bank.count(entry.getKey()) >= amountRequired) {
                    int toWithdraw = entry.getValue() > 0 ? entry.getValue() : Bank.count(entry.getKey());
                    if (Inventory.isFull()) {
                        Logger.log("Inventory: Depositing all items, reason: Full, need space");
                        Bank.depositAllItems();
                        Sleep.sleep(100, 200);
                    }

                    int reduceBy = 0;
                    if (Inventory.contains(entry.getKey())) {
                        reduceBy = Inventory.count(entry.getKey());
                    }

                    toWithdraw -= reduceBy;

                    if(postTask != null && postTask instanceof HighAlchemyTask && !entry.getKey().contains("rune") && !entry.getKey().contains("coin")) {
                        checkAndSet(BankMode.NOTE);
                    }

                    Item i = Bank.get(entry.getKey());
                    if(i != null) {
                        if(Bank.needToScroll(i)) {
                            Bank.scroll(entry.getKey());
                            Sleep.sleepUntil(() -> !Bank.needToScroll(i), 5000);
                        }
                    }

                    if (buyingRequired.isEmpty()) {
                        if(!Bank.withdraw(entry.getKey(), toWithdraw)) {
                            Logger.error("Inventory: Issue withdrawing " + entry.getKey());
                        } else {
                            Logger.log("Inventory: Withdrew " + toWithdraw + " of: " + entry.getKey());
                            Sleep.sleepUntil(() -> Inventory.contains(entry.getKey()), Calculations.random(5000, 10000));
                        }
                    }
                } else {
                    if(Inventory.contains(entry.getKey()) && (Inventory.count(entry.getKey()) >= amountRequired || Inventory.get(entry.getKey()).getAmount() >= amountRequired)) {
                        Logger.log("Inventory: We have enough of " + entry.getKey() + " already");
                        continue;
                    }

                    int amountToBuy = (entry.getValue() > 0 ? entry.getValue() : -entry.getValue()) * inventoriesWorth;
                    for (String s : instance.SINGULAR_ITEMS) {
                        if (s.toLowerCase().contains(entry.getKey().toLowerCase())) {
                            amountToBuy = 1;
                            break;
                        }
                    }

                    buyingRequired.put(entry.getKey(), amountToBuy);
                    Logger.log("Inventory: We need to buy " + (entry.getValue() > 0 ? amountRequired : -entry.getValue()) + " of: " + entry.getKey());
                }
            }
        }
        Logger.log("Inventory: Finished checking");

        Logger.log("Exchanger: Beginning checks");
        if (!buyingRequired.isEmpty()) {
            checkAndSet(BankMode.ITEM);
            HashMap<String, Integer> m = new HashMap<>();

            if(Inventory.isFull()) {
                Logger.log("Exchanger: Depositing all items, reason: Full, need space");
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

            int toWithdraw = finalPrice;
            if(Inventory.contains("Coins")) {
                toWithdraw -= Inventory.count("Coins");
            }

            if ((Bank.contains("Coins") && Bank.count("Coins") >= toWithdraw)) {
                Item i = Bank.get("Coins");
                if(i != null) {
                    if(Bank.needToScroll(i)) {
                        Bank.scroll("Coins");
                        Sleep.sleepUntil(() -> !Bank.needToScroll(i), 5000);
                    }
                }

                Bank.withdraw("Coins", Bank.count("Coins"));
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
                    Logger.log("Exchanger: Need to sell items");
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
                        // Restricted moneymaker time...
                        Logger.log("We are out of GP, time to go and make some.");
                        WatAIO.NEED_MM = true;
                        instance.currentTask = null;
                        return;
                    }
                }
            }

            if (!m.isEmpty()) {
                Logger.log("Exchanger: Handing off to G.E task");
                instance.currentTask = new GrandExchangeTask("Selling at G.E", true, m, this);
                return;
            }
        }

        Logger.log("Exchanger: Finished checks");

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

        Logger.log("Final checks: Net worth, etc.");

        if(postTask != null && !(postTask instanceof MulingTask)) {
            depositNonRequired();
        }

        // did we really need to double check here?
        /*if(postTask != null) {
            for (String s : postTask.clothesRequired().keySet()) {
                if(!Equipment.contains(s)) {
                    if (Bank.contains(s)) {
                        Bank.withdraw(s, 1);
                        Sleep.sleepUntil(() -> Inventory.contains(s), 1500);
                    }

                    if (Inventory.contains(s)) {
                        Inventory.get(s).interact("Wear");
                        Sleep.sleepUntil(() -> Equipment.contains(s), 1500);
                    }
                }
            }
        }

        for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
            String itemName = entry.getKey();
            int requiredQuantity = entry.getValue();
            if (!Inventory.contains(itemName) || Inventory.count(itemName) < requiredQuantity) {
                Bank.withdraw(itemName, requiredQuantity);
                Sleep.sleepUntil(() -> Inventory.contains(itemName) && Inventory.count(itemName) == requiredQuantity, 1500);
            }
        }*/

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

        Logger.log("Banking: Complete");

        if (postTask != null) {
            if(postTask instanceof BuryBonesTask || postTask instanceof BreakingTask) {
                Bank.close();
            }

            instance.currentTask = postTask;
        }
    }

    private void depositNonRequired() {
        if(Inventory.isEmpty()) {
            return;
        }

        if(postTask != null) {
            if(!postTask.clothesRequired().isEmpty() && !Equipment.isEmpty()) {
                for(Item i : Equipment.all()) {
                    if(i == null) continue;
                    if(!postTask.clothesRequired().containsKey(i.getName())) {
                        Logger.log("We have the wrong equipment on, depositing all.");
                        Bank.depositAllEquipment();
                        Sleep.sleepUntil(Equipment::isEmpty, Calculations.random(5000, 10000));
                        break;
                    }
                }
            }
        }

        if(!inventoryRequired.isEmpty()) {
            for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
                Logger.log("Inventory requires: " + entry.getKey());
                if (Inventory.contains(entry.getKey()) && entry.getValue() > 0) {
                    if(Inventory.count(entry.getKey()) > entry.getValue()) {
                        Logger.log("Final: Depositing extras of: " + entry.getKey() + ", have: " + Inventory.count(entry.getKey()) + ", need: " + entry.getValue());
                        Bank.deposit(entry.getKey(), (Inventory.count(entry.getKey()) - entry.getValue()));
                        Sleep.sleepUntil(() -> Inventory.count(entry.getKey()) == entry.getValue(), 1500);
                    }
                }
            }

            for(Item i : Inventory.all()) {
                if(i == null)
                    continue;

                if(postTask != null && postTask.inventoryTolerated().contains(i.getName())) {
                    Logger.log("Banking: Inventory tolerates item: " + i.getName());
                    continue;
                }

                if(inventoryRequired != null && !inventoryRequired.containsKey(i.getName())) {
                    Logger.log("Banking: Depositing " + i.getName() + ", not required.");
                    Bank.depositAll(i.getName());
                    Sleep.sleepUntil(() -> !Inventory.contains(i.getName()), 1500);
                    continue;
                }

                if(i.isNoted()) {
                    Logger.log("Banking: Depositing " + i.getName() + ", noted.");
                    Bank.depositAll(i.getName());
                    Sleep.sleepUntil(() -> !Inventory.contains(i.getName()), 1500);
                }
            }
        } else {
            for (Item i : Inventory.all()) {
                if(i == null) continue;

                if (postTask != null && postTask.inventoryTolerated().contains(i.getName())) {
                    Logger.log("Banking: Inventory tolerates item: " + i.getName());
                    continue;
                }

                Logger.log("Banking: Depositing " + i.getName() + ", not required.");
                Bank.depositAll(i.getName());
                Sleep.sleepUntil(() -> !Inventory.contains(i.getName()), 5000);
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

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
