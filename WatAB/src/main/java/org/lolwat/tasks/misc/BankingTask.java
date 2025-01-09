package org.lolwat.tasks.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.types.gear.GearItem;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.misc.utils.OutfitUtils;
import org.lolwat.tasks.magic.HighAlchemyTask;
import org.lolwat.tasks.quests.wrapper.QuestWrapperTask;
import java.time.Instant;
import java.util.*;

public class BankingTask implements WatTask {
    private HashMap<String, Integer> inventoryRequired; // Process inv second..
    private final HashMap<String, Integer> sellingItems; // Check this at the very top of banking operations
    private final int inventoriesWorth;
    private WatTask postTask;
    private final HashMap<String, WatTask> gatheringTasks;

    private final List<String> restrictedItems = new ArrayList<String>() {
        {
            add("oak logs");
            add("willow logs");
            add("yew logs");
            add("raw shrimps");
            add("shrimps");
            add("raw anchovies");
            add("anchovies");
            add("raw lobster");
            add("lobster");
            add("clay");
            add("soft clay");
            add("copper ore");
            add("tin ore");
            add("iron ore");
            add("silver ore");
            add("gold ore");
            add("coal");
            add("mithril ore");
            add("adamantite ore");
            add("runite ore");
            add("cowhide");
            add("vial");
            add("vial of water");
            add("jug of water");
            add("fishing bait");
            add("feather");
            add("eye of newt");
            add("wine of zamorak");
            add("air rune");
            add("water rune");
            add("earth rune");
            add("fire rune");
            add("mind rune");
            add("chaos rune");
        }
    };

    public BankingTask(HashMap<String, Integer> invRequired, HashMap<String, Integer> sellList, Integer inventories, WatTask post) {
        this(invRequired, sellList, inventories, post, null);
    }

    public BankingTask(HashMap<String, Integer> invRequired, HashMap<String, Integer> sellList, Integer inventories, WatTask post, HashMap<String, WatTask> gathering) {
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

        if(post != null && !post.inventory().isEmpty()) {
            inventoryRequired = post.inventory();
        }

        if(gathering != null) {
            gatheringTasks = gathering;
        } else {
            gatheringTasks = new HashMap<>();
        }
    }

    @Override
    public void execute() {
        if(!Bank.isOpen()) {
            ItemUtils.bank(this);
            return;
        }

        depositNonRequired();

        Logger.log("Sell Checker: starting");

        boolean allowedToSell = ConfigManager.getInstance().isTradeUnlocked()
                || (postTask != null && postTask.data().containsKey("gp_to_generate"))
                || GenericUtils.isMember();

        Logger.log("Trade unrestricted: " + allowedToSell + ", sell enabled for task: " +
                (postTask != null && postTask.data().containsKey("gp_to_generate")));

        // new threshold stuff
        Logger.log("Checking bank for items to sell based on thresholds");
        for(Item i : Bank.all()) {
            if(i == null) continue;
            if(postTask != null) { // we dont want to sell items that are required for the next task
                if(postTask.inventory().containsKey(i.getName())) {
                    continue;
                }

                if(postTask.loadout().containsKey(i.getName())) {
                    continue;
                }
            }

            int threshold = ConfigManager.getInstance().getItemThreshold(i.getName());
            if(threshold != 0) {
                int toCheck = (threshold > 0 ? threshold+1 : -threshold);
                if(Bank.count(i.getName()) >= toCheck) {
                    if(!sellingItems.containsKey(i.getName())) {
                        int toSell;
                        if(threshold > 0) {
                            toSell = Bank.count(i.getName()) - threshold;
                        } else {
                            toSell = Bank.count(i.getName());
                        }

                        sellingItems.put(i.getName(), toSell);
                    }
                }
            }
        }

        List<String> toRemove = new ArrayList<>();
        if(!allowedToSell) {
            for(String s : sellingItems.keySet()) {
                if(restrictedItems.contains(s.toLowerCase())) {
                    toRemove.add(s);
                } else {
                    allowedToSell = true;
                }
            }

            for(String s : toRemove) {
                sellingItems.remove(s);
            }

            toRemove.clear();
        }

        if(allowedToSell) {
            if (sellingItems.size() > 0) {
                boolean performSelling = false;
                if (Inventory.isFull()) {
                    Bank.depositAllItems();
                    Sleep.sleepUntil(Inventory::isEmpty, Calculations.random(5000, 10000));
                }

                for (Map.Entry<String, Integer> entry : sellingItems.entrySet()) {
                    int triggerAmount = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                    boolean triggered = Bank.contains(entry.getKey()) && Bank.count(entry.getKey()) >= triggerAmount;
                    int toWithdraw = entry.getValue();

                    if(!triggered && postTask != null && postTask.data().containsKey("gp_to_generate")) {
                        if(Integer.parseInt(postTask.data().get("gp_to_generate").toString()) > 0) {
                            int toGen = Integer.parseInt(postTask.data().get("gp_to_generate").toString());
                            int weHave = NumUtils.getItemPrice(entry.getKey()) * Bank.count(entry.getKey());
                            if(weHave >= toGen) {
                                postTask.data().remove("gp_to_generate");
                                Logger.log("BankingTask: Wealth generation goals met");
                                triggered = true;
                                toWithdraw = toGen / NumUtils.getItemPrice(entry.getKey());
                            } else {
                                Logger.log("BankingTask: Wealth generation goals not met");
                            }
                        }
                    }

                    if (triggered) {
                        ItemUtils.setBankMode(BankMode.NOTE);
                        Logger.log("Sell checker: found " + Bank.get(entry.getKey()).getName());
                        if(!Inventory.isFull()) {
                            if (entry.getValue() > 0) {
                                int reduceBy = 0;
                                if (Inventory.contains(entry.getKey()))
                                    reduceBy = Inventory.count(entry.getKey());

                                Logger.log("Taking " + (toWithdraw - reduceBy) + " of " + entry.getKey());
                                Bank.withdraw(entry.getKey(), (toWithdraw - reduceBy));
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
                    TaskManager.getInstance().setCurrentTask(new GrandExchangeTask("Selling at G.E", true, sellingItems, this));
                    return;
                }
            } else {
                Logger.log("Nothing to sell");
            }
        } else {
            Logger.log("Not allowed to sell (account restricted, or restricted items only)");
        }

        Logger.log("Sell checker: finished");
        ItemUtils.setBankMode(BankMode.ITEM);

        HashMap<String, Integer> buyingRequired = new HashMap<>();

        Logger.log("Equipment: Beginning checks");
        if(postTask != null) {
            if (!postTask.loadout().isEmpty()) {
                for (Map.Entry<String, Integer> entry : postTask.loadout().entrySet()) {
                    int amountRequired = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                    if (Equipment.contains(entry.getKey()) && Equipment.count(entry.getKey()) >= amountRequired) {
                        Logger.log("Equipment: Already equipped: " + entry.getKey());
                        Sleep.sleep(200, 400);
                        continue;
                    }

                    if (Inventory.count(entry.getKey()) > amountRequired) {
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
                        if (entry.getKey().contains("defender")) {
                            Logger.log("We are going to go and obtain a " + entry.getKey() + " first. Restarting loop.");

                            String latestObtained = "";

                            if (!Equipment.contains(x -> x.getName().contains("defender"))) {
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
                            if (tokens >= 200) {
                                TaskManager.getInstance().setCurrentTask(new FightCyclopsTask(postTask.trainsSkill(), new HashMap<String, Integer>() {
                                    {
                                        put("Lobster", 20);
                                    }
                                }, latestObtained));
                                return;
                            } else {
                                TaskManager.getInstance().setCurrentTask(new FightArmorSetTask(postTask.trainsSkill(), new HashMap<String, Integer>() {
                                    {
                                        put("Lobster", 20);
                                    }
                                }, latestObtained));
                            }
                            return;
                        } else {
                            if (OutfitUtils.isOutfitItem(entry.getKey())) {
                                if (OutfitUtils.isGracefulItem(entry.getKey())) {
                                    if (Bank.contains("Mark of grace")) {
                                        // calculate total cost of missing pieces, and then see which ones we can get
                                        // using the marks that we have.
                                        int totalCost = 0;
                                        int totalMarks = Bank.count("Mark of grace") + Inventory.count("Mark of grace");
                                        List<String> gracefulItems = new ArrayList<>();
                                        for (Map.Entry<String, Integer> e : OutfitUtils.gracefulItems.entrySet()) {
                                            if (!Bank.contains(e.getKey()) && !Inventory.contains(e.getKey())
                                                    && !Equipment.contains(e.getKey())) {

                                                if ((totalCost + e.getValue()) > totalMarks)
                                                    break;

                                                Logger.log("We need to get: " + e.getKey() + " for " + e.getValue() + " marks");
                                                gracefulItems.add(e.getKey());
                                                totalCost += e.getValue();
                                            }
                                        }

                                        if(!gracefulItems.isEmpty()) {
                                            if(!Bank.withdraw("Mark of grace", totalCost)) {
                                                Logger.error("Failed to withdraw marks of grace");
                                                return;
                                            }

                                            TaskManager.getInstance().setCurrentTask(new BuyGracefulOutfitTask(this, gracefulItems));
                                            return;
                                        }
                                    } else {
                                        Logger.log("No marks of grace, can't be buying the outfit..");
                                    }
                                }
                            } else { // probably a else if untradeable, obtain path here. similar to defenders but less hardcoded lol
                                // need to buy.
                                buyingRequired.put(entry.getKey(), amountRequired);
                                Logger.log("Equipment: Need to buy " + (entry.getValue() > 0 ? amountRequired : -entry.getValue()) + " of: " + entry.getKey());
                            }
                        }
                    }
                }
            }

            Sleep.sleep(500, 1000);

            if(!postTask.loadout().isEmpty()) {
                for (Map.Entry<String, Integer> entry : postTask.loadout().entrySet()) {
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

            handleEquipmentDeposit();
        }

        Logger.log("Equipment: Finished checking");

        Logger.log("Inventory: Beginning checks");
        if (!inventoryRequired.isEmpty()) {
            for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
                ItemUtils.setBankMode(BankMode.ITEM);
                int amountRequired;
                if (ItemUtils.SINGULAR_ITEMS.contains(entry.getKey()))
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

                if(Equipment.contains(entry.getKey())) {
                    Logger.log("Inventory item is actually equipped, skipping: " + entry.getKey());
                    Sleep.sleep(100, 200);
                    continue;
                }

                if (Bank.contains(x -> x != null && x.getName().equals(entry.getKey()))
                        && Bank.count(x -> x != null && x.getName().equals(entry.getKey())) >= amountRequired) {

                    int toWithdraw = entry.getValue() > 0
                            ? entry.getValue()
                            : Bank.count(x -> x != null && x.getName().equals(entry.getKey()));

                    if (Inventory.isFull()) {
                        Logger.log("Inventory: Depositing all items, reason: Full, need space");
                        Bank.depositAllItems();
                        Sleep.sleep(100, 200);
                    }

                    int reduceBy = 0;
                    if (Inventory.contains(x -> x != null && x.getName().equals(entry.getKey()))) {
                        reduceBy = Inventory.count(x -> x != null && x.getName().equals(entry.getKey()));
                    }

                    toWithdraw -= reduceBy;

                    if(postTask != null && postTask instanceof HighAlchemyTask && !entry.getKey().contains("rune") && !entry.getKey().contains("coin")) {
                        ItemUtils.setBankMode(BankMode.NOTE);
                    }

                    Item i = Bank.get(x -> x != null && x.getName().equals(entry.getKey()));
                    if(i != null) {
                        while(Bank.needToScroll(i)) {
                            Bank.scroll(entry.getKey());
                        }
                    }

                    for(String s : ItemUtils.SINGULAR_ITEMS) {
                        if(s.toLowerCase().contains(entry.getKey().toLowerCase())) {
                            toWithdraw = 1;
                            break;
                        }
                    }

                    if (buyingRequired.isEmpty()) {
                        if(!Bank.withdraw(x -> x != null && x.getName().equals(entry.getKey()), toWithdraw)) {
                            Logger.error("Inventory: Issue withdrawing " + entry.getKey());
                        } else {
                            Logger.log("Inventory: Withdrew " + toWithdraw + " of: " + entry.getKey());
                        }

                        Sleep.sleepUntil(() -> Inventory.contains(x -> x != null && x.getName().equals(entry.getKey())), Calculations.random(5000, 10000));
                    }
                } else {
                    if(Inventory.contains(entry.getKey()) && (Inventory.count(entry.getKey()) >= amountRequired || Inventory.get(entry.getKey()).getAmount() >= amountRequired)) {
                        Logger.log("Inventory: We have enough of " + entry.getKey() + " already");
                        continue;
                    }

                    int amountToBuy = (entry.getValue() > 0 ? entry.getValue() : -entry.getValue()) * inventoriesWorth;
                    for (String s : ItemUtils.SINGULAR_ITEMS) {
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
            if(gatheringTasks != null && !gatheringTasks.isEmpty()) {
                for(Map.Entry<String, WatTask> tasks : gatheringTasks.entrySet()) {
                    if(buyingRequired.containsKey(tasks.getKey())) {
                        Logger.log("We have an alternate pathway to get " + tasks.getKey() + ", setting task..");
                        TaskManager.getInstance().setCurrentTask(tasks.getValue());
                        return;
                    }
                }
            }

            ItemUtils.setBankMode(BankMode.ITEM);
            HashMap<String, Integer> m = new HashMap<>();

            if(Inventory.isFull()) {
                Logger.log("Exchanger: Depositing all items, reason: Full, need space");
                Bank.depositAllItems();
                Sleep.sleep(100, 400);
            }

            int finalPrice = 0;
            for (Map.Entry<String, Integer> entry : buyingRequired.entrySet()) {
                String itemFinal = entry.getKey();
                if(itemFinal.equals("Old school bond (untradeable)")) {
                    itemFinal = "Old school bond";
                }

                int itemMultiplier = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                int initialPrice = NumUtils.getItemPrice(itemFinal);
                int multipliedPrice = initialPrice * itemMultiplier;
                finalPrice += multipliedPrice;
            }

            int toWithdraw = finalPrice;
            if(Inventory.contains("Coins")) {
                toWithdraw -= Inventory.count("Coins");
            }

            if (Bank.count("Coins") >= toWithdraw) {
                Bank.withdraw("Coins", Bank.count("Coins"));
                Sleep.sleep(100, 200);
                Bank.close();

                TaskManager.getInstance().setCurrentTask(new GrandExchangeTask("Buying required items", false, buyingRequired, this));
                return;
            } else {
                if (ConfigManager.getInstance().isTradeUnlocked()) {
                    ItemUtils.setBankMode(BankMode.NOTE);
                    Logger.log("Exchanger: Need to sell items");
                    if (Inventory.isFull()) {
                        Bank.depositAllExcept("Coins");
                        Sleep.sleep(100, 200);
                    }

                    if (!ConfigManager.getInstance().getConfigBoolean("disable_mule") && !ConfigManager.getInstance().hasMuleConnectionFailed()) {
                        if (Bank.contains("Coins")) {
                            finalPrice -= Bank.count("Coins");
                        }

                        if (Inventory.contains("Coins")) {
                            finalPrice -= Inventory.count("Coins");
                        }

                        int totalCoins = Inventory.count("Coins") + Bank.count("Coins");
                        if(totalCoins < ConfigManager.getInstance().getConfigInt("keep_min_gold")) {
                            finalPrice += ConfigManager.getInstance().getConfigInt("keep_min_gold");
                            Logger.log("Triggered minimum gold threshold, adding " + ConfigManager.getInstance().getConfigInt("keep_min_gold") + " gp");
                        }

                        Logger.log("No items to sell, so reverse muling " + finalPrice + " gp");

                        int finalTotalPrice = finalPrice;
                        TaskManager.getInstance().setCurrentTask(new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                            {
                                put("Coins", finalTotalPrice);
                            }
                        }, this));
                        return;
                    } else {
                        Logger.log("We are out of GP, time to go and make some.");
                        TaskManager.getInstance().getSpecificSkillTask(Skill.HITPOINTS, toWithdraw);
                        return;
                    }
                } else {
                    if (!ConfigManager.getInstance().getConfigBoolean("disable_mule") && !ConfigManager.getInstance().hasMuleConnectionFailed()) {
                        if(Bank.contains("Coins")) {
                            finalPrice -= Bank.count("Coins");
                        }

                        if(Inventory.contains("Coins")) {
                            finalPrice -= Inventory.count("Coins");
                        }

                        Bank.depositAllItems();
                        Sleep.sleep(100, 200);

                        int totalCoins = Inventory.count("Coins") + Bank.count("Coins");
                        if(totalCoins < ConfigManager.getInstance().getConfigInt("keep_min_gold")) {
                            finalPrice += ConfigManager.getInstance().getConfigInt("keep_min_gold");
                            Logger.log("Triggered minimum gold threshold, adding " + ConfigManager.getInstance().getConfigInt("keep_min_gold") + " gp");
                        }

                        int totalPrice = finalPrice;
                        Logger.log("Trade locked, so reverse muling " + finalPrice + " gp");
                        TaskManager.getInstance().setCurrentTask(new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                            {
                                put("Coins", totalPrice);
                            }
                        }, this));
                        return;
                    } else {
                        // Restricted moneymaker time...
                        Logger.log("We are out of GP, time to go and make some. (Restricted)");
                        TaskManager.getInstance().getSpecificSkillTask(Skill.HITPOINTS, toWithdraw);
                        return;
                    }
                }
            }
        }

        Logger.log("Exchanger: Finished checks");

        if(!ConfigManager.getInstance().hasMuleConnectionFailed() && ConfigManager.getInstance().isTradeUnlocked()
                && !ConfigManager.getInstance().getConfigBoolean("disable_mule")) {
            int invMoney = 0;
            int bankMoney = 0;

            if (Inventory.contains("Coins")) {
                invMoney = Inventory.get("Coins").getAmount();
            }

            if (Bank.contains("Coins")) {
                bankMoney = Bank.get("Coins").getAmount();
            }

            if ((invMoney + bankMoney) >= ConfigManager.getInstance().getConfigInt("mule_trigger")) {
                int toWithdraw = (bankMoney - invMoney) - ConfigManager.getInstance().getConfigInt("mule_safety_net");
                if (toWithdraw > 0) {
                    Logger.log("MULE TARGET MET, REDIRECTING TO MULE");
                    if (Inventory.isFull() || Inventory.emptySlotCount() < 2) {
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

        // calculate net worth
        if(ConfigManager.getInstance().getNetWorthGeneratedAt() == 0) {
            int total = 0;
            for (Item i : Bank.all()) {
                if (i == null)
                    continue;

                if (i.getAmount() > 1) {
                    total += LivePrices.get(i) * i.getAmount();
                } else {
                    total += LivePrices.get(i);
                }
            }

            for (Item i : Inventory.all()) {
                if (i == null)
                    continue;

                if (i.getAmount() > 1) {
                    total += LivePrices.get(i) * i.getAmount();
                } else {
                    total += LivePrices.get(i);
                }
            }

            ConfigManager.getInstance().setNetWorth(total);
            ConfigManager.getInstance().setNetWorthGeneratedAt(Instant.now().getEpochSecond());
        } else {
            if((Instant.now().getEpochSecond() - ConfigManager.getInstance().getNetWorthGeneratedAt()) >= 3600) {
                ConfigManager.getInstance().setNetWorthGeneratedAt(0); // will generate net worth next time.
            }
        }

        Logger.log("Banking: Complete");

        if (postTask != null) {
            if(postTask instanceof BuryBonesTask || postTask instanceof BreakingTask || postTask instanceof QuestWrapperTask) {
                Bank.close();
            }

            TaskManager.getInstance().setCurrentTask(postTask);
        }
    }

    private void handleEquipmentDeposit() {
        List<String> toUnequip = new ArrayList<>();
        if (!postTask.loadout().isEmpty() && !Equipment.isEmpty()) {
            for(Item i : Equipment.all()) {
                if(i == null) continue;
                if(!postTask.inventoryTolerated().contains(i.getName())
                        && !postTask.loadout().containsKey(i.getName())
                        && !postTask.inventory().containsKey(i.getName())) {

                    Logger.log("Task " + postTask.getName() + " does not require/tolerate: " + i.getName() + ", adding to list");
                    toUnequip.add(i.getName());
                }
            }

            if(toUnequip.isEmpty())
                return;

            if(toUnequip.size() == Equipment.all(Objects::nonNull).size()) {
                if(Equipment.size() > 0) {
                    Logger.log("Depositing all gear due to task: " + postTask.getName() + " not requiring/tolerating any of it");
                    Bank.depositAllEquipment();
                }
            }
            else {
                if(Inventory.isFull() && Bank.isOpen()) {
                    Bank.depositAllItems();
                    Sleep.sleepUntil(Inventory::isEmpty, Calculations.random(5000, 10000));
                }

                if(Bank.isOpen()) {
                    Bank.close();
                    Sleep.sleepUntil(() -> !Bank.isOpen(), Calculations.random(5000, 10000));
                }

                if(!Tabs.isOpen(Tab.EQUIPMENT)) {
                    Tabs.open(Tab.EQUIPMENT);
                    Sleep.sleepUntil(() -> Tabs.isOpen(Tab.EQUIPMENT), Calculations.random(5000, 10000));
                }

                for(Item i : Equipment.all()) {
                    if(i == null) continue;
                    if(Inventory.isFull()) break;

                    if(toUnequip.contains(i.getName())) {
                        if(!Equipment.unequip(x -> x != null && x.getName().equals(i.getName()))) {
                            Logger.error("Equipment: Error unequipping item");
                        } else {
                            Sleep.sleepUntil(() -> !Equipment.contains(x -> x != null && x.getName().equals(i.getName())), Calculations.random(5000, 10000));
                        }
                    }
                }

                if(!Tabs.isOpen(Tab.INVENTORY)) {
                    Tabs.open(Tab.INVENTORY);
                    Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), Calculations.random(5000, 10000));
                }

                if(!Bank.isOpen()) {
                    Bank.open();
                    Sleep.sleepUntil(Bank::isOpen, Calculations.random(5000, 10000));
                }
            }
        }
    }

    private void depositNonRequired() {
        if (Inventory.isEmpty()) {
            return;
        }

        if (postTask != null) {
            handleEquipmentDeposit();
        }

        boolean canDepositAll = true;
        for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
            if(Inventory.contains(entry.getKey())) {
                canDepositAll = false;
                break;
            }
        }

        for (String i : inventoryTolerated()) {
            if(Inventory.contains(i)) {
                canDepositAll = false;
                break;
            }
        }

        if(!canDepositAll) {
            if (!inventoryRequired.isEmpty()) {
                for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
                    Logger.log("Inventory requires: " + entry.getKey());
                    if (Inventory.contains(entry.getKey()) && entry.getValue() > 0) {
                        if (Inventory.count(entry.getKey()) > entry.getValue()) {
                            if(Inventory.get(x -> x.getName().equals(entry.getKey())).isNoted()) {
                                Logger.log("Final: depositing noted: " + entry.getKey());
                                Bank.depositAll(entry.getKey());
                                Sleep.sleepUntil(() -> !Inventory.contains(entry.getKey()), 1500);
                            } else {
                                Logger.log("Final: Depositing extras of: " + entry.getKey() + ", have: " + Inventory.count(entry.getKey()) + ", need: " + entry.getValue());
                                Bank.deposit(entry.getKey(), (Inventory.count(entry.getKey()) - entry.getValue()));
                                Sleep.sleepUntil(() -> Inventory.count(entry.getKey()) == entry.getValue(), 1500);
                            }
                        }
                    }
                }

                for (Item i : Inventory.all()) {
                    if (i == null)
                        continue;

                    if (postTask != null && postTask.inventoryTolerated().contains(i.getName())) {
                        Logger.log("Banking: Inventory tolerates item: " + i.getName());
                        continue;
                    }

                    if (inventoryRequired != null && !inventoryRequired.containsKey(i.getName())) {
                        Logger.log("Banking: Depositing " + i.getName() + ", not required.");
                        Bank.depositAll(i.getName());
                        Sleep.sleepUntil(() -> !Inventory.contains(i.getName()), 1500);
                        continue;
                    }

                    if (i.isNoted() && (postTask != null && !(postTask instanceof HighAlchemyTask))) {
                        Logger.log("Banking: Depositing " + i.getName() + ", noted.");
                        Bank.depositAll(i.getName());
                        Sleep.sleepUntil(() -> !Inventory.contains(i.getName()), 1500);
                    }
                }
            } else {
                for (Item i : Inventory.all()) {
                    if (i == null) continue;

                    if (postTask != null && postTask.inventoryTolerated().contains(i.getName())) {
                        Logger.log("Banking: Inventory tolerates item: " + i.getName());
                        continue;
                    }

                    Logger.log("Banking: Depositing " + i.getName() + ", not required.");
                    Bank.depositAll(i.getName());
                    Sleep.sleepUntil(() -> !Inventory.contains(i.getName()), 5000);
                }
            }
        } else {
            if(!Inventory.all(Objects::nonNull).isEmpty()) {
                Logger.log("Banking: required/tolerated no items in inventory, depositing all.");
                Bank.depositAllItems();
                Sleep.sleepUntil(Inventory::isEmpty, Calculations.random(5000, 10000));
            }
        }
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
    public Skill trainsSkill() {
        return postTask != null ? postTask.trainsSkill() : Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public HashMap<EquipmentSlot, GearItem> loadout() {
        if(postTask != null) {
            return postTask.loadout();
        }

        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventory() {
        if(postTask != null) {
            return postTask.inventory();
        }

        return new HashMap<>();
    }

    @Override
    public List<String> inventoryTolerated() {
        if(postTask != null) {
            return postTask.inventoryTolerated();
        }

        return new ArrayList<>();
    }
}