package org.lolwat.tasks.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.alching.HighAlchemyTask;
import org.lolwat.tasks.fletching.FletchingTask;
import org.lolwat.tasks.fletching.StringingTask;

import java.util.*;

public class BankingTask implements WatTask {
    private HashMap<String, Integer> inventoryRequired; // Process inv second..
    private final HashMap<String, Integer> sellingItems; // Check this at the very top of banking operations
    private final int inventoriesWorth;
    private WatTask postTask;
    private HashMap<String, WatTask> gatheringTasks;

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

        if (post != null && !post.inventoryRequired().isEmpty()) {
            inventoryRequired = post.inventoryRequired();
        }

        if (gathering != null) {
            gatheringTasks = gathering;
        } else {
            gatheringTasks = new HashMap<>();
        }
    }

    @Override
    public void execute() {
        if (!Bank.isOpen()) {
            ItemUtils.bank(this);
            return;
        }

        if (ItemUtils.bankContains("Yew longbow", ConfigManager.getInstance().getConfigInt("min_bow_count")) && !(postTask instanceof HighAlchemyTask)) {
            Logger.log("Satisfied yew longbow count, alching");
            TaskManager.getInstance().setCurrentTask(new HighAlchemyTask());
            return;
        }

        if (ItemUtils.bankContains("Yew longbow (u)", ConfigManager.getInstance().getConfigInt("min_bow_count"))
                && !(postTask instanceof StringingTask)
                && !(postTask instanceof HighAlchemyTask)) {
            Logger.log("Satisfied yew longbow (u) count, stringing");
            TaskManager.getInstance().setCurrentTask(new StringingTask());
            return;
        }

        depositNonRequired();

        Logger.log("Sell Checker: starting");

        boolean allowedToSell = true;

        Logger.log("Trade unrestricted: " + allowedToSell + ", sell enabled for task: " +
                (postTask != null && postTask.data().containsKey("gp_to_generate")));

        Logger.log("Checking bank for items to sell based on thresholds");
        for (Item i : Bank.all()) {
            if (i == null) continue;
            if (postTask != null) {
                if (postTask.inventoryRequired().containsKey(i.getName())) {
                    continue;
                }

                if (postTask.clothesRequired().containsKey(i.getName())) {
                    continue;
                }
            }

            int threshold = ConfigManager.getInstance().getItemThreshold(i.getName());
            if (threshold != 0) {
                int toCheck = (threshold > 0 ? threshold + 1 : -threshold);
                if (Bank.count(i.getName()) >= toCheck) {
                    if (!sellingItems.containsKey(i.getName())) {
                        int toSell;
                        if (threshold > 0) {
                            toSell = Bank.count(i.getName()) - threshold;
                        } else {
                            toSell = Bank.count(i.getName());
                        }

                        sellingItems.put(i.getName(), toSell);
                    }
                }
            }
        }

        if (!sellingItems.isEmpty()) {
            boolean performSelling = false;
            if (Inventory.isFull()) {
                Bank.depositAllItems();
                Sleep.sleepUntil(Inventory::isEmpty, Calculations.random(5000, 10000));
            }

            for (Map.Entry<String, Integer> entry : sellingItems.entrySet()) {
                int triggerAmount = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                boolean triggered = Bank.contains(entry.getKey()) && Bank.count(entry.getKey()) >= triggerAmount;
                int toWithdraw = entry.getValue();

                if (!triggered && postTask != null && postTask.data().containsKey("gp_to_generate")) {
                    if (Integer.parseInt(postTask.data().get("gp_to_generate").toString()) > 0) {
                        int toGen = Integer.parseInt(postTask.data().get("gp_to_generate").toString());
                        int weHave = NumUtils.getItemPrice(entry.getKey()) * Bank.count(entry.getKey());
                        if (weHave >= toGen) {
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
                    if (!Inventory.isFull()) {
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

        Logger.log("Sell checker: finished");
        ItemUtils.setBankMode(BankMode.ITEM);

        HashMap<String, Integer> buyingRequired = new HashMap<>();

        Logger.log("Equipment: Beginning checks");
        if (postTask != null) {
            Logger.log("post task not null: " + postTask.getName());
            if (!postTask.clothesRequired().isEmpty()) {
                Logger.log("clothes required");
                for (Map.Entry<String, Integer> entry : postTask.clothesRequired().entrySet()) {
                    Logger.log(entry.getKey() + ": " + entry.getValue());
                    int amountRequired = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                    if (ItemUtils.equipmentContains(entry.getKey(), entry.getValue())) {
                        Logger.log("Equipment: Already equipped: " + entry.getKey());
                        Sleep.sleep(200, 400);
                        continue;
                    }

                    if (Inventory.count(x -> x != null && x.getName().contains(entry.getKey())) > amountRequired) {
                        Logger.log("Equipment: Depositing extras of: " + entry.getKey());
                        Bank.deposit(entry.getKey(), (Inventory.count(entry.getKey()) - amountRequired));
                        Sleep.sleepUntil(() -> Inventory.count(entry.getKey()) == amountRequired, 1500);
                    }

                    if (ItemUtils.inventoryContains(entry.getKey(), entry.getValue(), false)) {
                        Logger.log("Equipment: Already in inventory: " + entry.getKey());
                        Sleep.sleep(200, 400);
                        continue;
                    }

                    if (Bank.contains(x -> x != null && x.getName().contains(entry.getKey()) && x.getAmount() >= entry.getValue())) {
                        Logger.log("contains");
                        if (Inventory.isFull()) {
                            Bank.depositAllItems();
                            Sleep.sleepUntil(Inventory::isEmpty, Calculations.random(5000, 10000));
                        }

                        if (Bank.withdraw(x -> x != null && x.getName().contains(entry.getKey()), amountRequired)) {
                            Sleep.sleepUntil(() -> Inventory.contains(entry.getKey()), Calculations.random(5000, 10000));
                            if (Inventory.contains(entry.getKey())) {
                                Logger.log("Equipment: Successfully withdrew: " + entry.getKey());
                            }
                        }
                    } else {
                        buyingRequired.put(entry.getKey(), amountRequired);
                        Logger.log("Equipment: Need to buy " + (entry.getValue() > 0 ? amountRequired : -entry.getValue()) + " of: " + entry.getKey());
                    }
                }
            }

            Sleep.sleep(500, 1000);

            if (!postTask.clothesRequired().isEmpty()) {
                for (Map.Entry<String, Integer> entry : postTask.clothesRequired().entrySet()) {
                    if (!ItemUtils.equipmentContains(entry.getKey(), entry.getValue()) && ItemUtils.inventoryContains(entry.getKey(), entry.getValue(), false)) {
                        if (Bank.isOpen()) {
                            Bank.close();
                            Sleep.sleepUntil(() -> !Bank.isOpen(), Calculations.random(5000, 10000));
                        }

                        if (!GenericUtils.equipItem(entry.getKey(), null)) {
                            Logger.error("Equipment: Error equipping item: " + entry.getKey());
                        }
                    }
                }

                if (!Bank.isOpen()) {
                    Bank.open();
                    Sleep.sleepUntil(Bank::isOpen, Calculations.random(5000, 10000));
                }
            }
        }

        Logger.log("Equipment: Finished checking");

        Logger.log("Inventory: Beginning checks");
        if (!inventoryRequired.isEmpty()) {
            for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
                Logger.log("Inventory: Checking for: " + entry.getKey());
                ItemUtils.setBankMode(BankMode.ITEM);
                int amountRequired;
                if (ItemUtils.SINGULAR_ITEMS.contains(entry.getKey()))
                    amountRequired = 1;
                else {
                    amountRequired = entry.getValue() > 0 ? entry.getValue() : 1;
                }

                if (Inventory.contains(x -> x != null && x.getName().contains(entry.getKey()) && x.isNoted())) {
                    Logger.log("Inventory: Depositing noted: " + entry.getKey());
                    Bank.depositAll(entry.getKey());
                    Sleep.sleepUntil(() -> !Inventory.contains(entry.getKey()), 1500);
                }

                if (ItemUtils.inventoryContains(entry.getKey(), amountRequired, false)) {
                    if (entry.getValue() > 0 && ItemUtils.inventoryCount(entry.getKey(), false) > amountRequired) {
                        Logger.log("Inventory: Depositing extras of: " + entry.getKey());
                        Bank.deposit(entry.getKey(), (Inventory.count(entry.getKey()) - amountRequired));
                        Sleep.sleepUntil(() -> Inventory.count(entry.getKey()) == amountRequired, 1500);
                    }

                    Logger.log("Inventory: We have enough of " + entry.getKey() + " already");
                    continue;
                }

                if (Equipment.contains(entry.getKey())) {
                    Logger.log("Inventory item is actually equipped, skipping: " + entry.getKey());
                    Sleep.sleep(100, 200);
                    continue;
                }

                if (ItemUtils.bankContains(entry.getKey(), entry.getValue())) {
                    int toWithdraw = entry.getValue() > 0
                            ? entry.getValue()
                            : Bank.count(x -> x != null && x.getName().contains(entry.getKey()));

                    if (Inventory.isFull()) {
                        Logger.log("Inventory: Depositing all items, reason: Full, need space");
                        Bank.depositAllItems();
                        Sleep.sleep(100, 200);
                    }

                    int reduceBy = 0;
                    if (Inventory.contains(x -> x != null && x.getName().contains(entry.getKey()))) {
                        reduceBy = Inventory.count(x -> x != null && x.getName().contains(entry.getKey()));
                    }

                    toWithdraw -= reduceBy;

                    if (postTask != null && postTask instanceof HighAlchemyTask && !entry.getKey().contains("rune") && !entry.getKey().contains("coin")) {
                        ItemUtils.setBankMode(BankMode.NOTE);
                    }

                    for (String s : ItemUtils.SINGULAR_ITEMS) {
                        if (s.toLowerCase().contains(entry.getKey().toLowerCase())) {
                            toWithdraw = 1;
                            break;
                        }
                    }

                    if (buyingRequired.isEmpty()) {
                        if (!Bank.withdraw(x -> x != null && x.getName().contains(entry.getKey()), toWithdraw)) {
                            Logger.error("Inventory: Issue withdrawing " + entry.getKey());
                        } else {
                            Logger.log("Inventory: Withdrew " + toWithdraw + " of: " + entry.getKey());
                        }

                        Sleep.sleepUntil(() -> Inventory.contains(x -> x != null && x.getName().contains(entry.getKey())), Calculations.random(5000, 10000));
                    }
                } else {
                    if (Inventory.contains(entry.getKey()) && (Inventory.count(entry.getKey()) >= amountRequired || Inventory.get(entry.getKey()).getAmount() >= amountRequired)) {
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

                    if (entry.getValue().equals(1))
                        amountToBuy = 1;

                    buyingRequired.put(entry.getKey(), amountToBuy);
                    Logger.log("Inventory: We need to buy " + (entry.getValue() > 0 ? amountRequired : -entry.getValue()) + " of: " + entry.getKey());
                }
            }
        }
        Logger.log("Inventory: Finished checking");

        Logger.log("Exchanger: Beginning checks");
        if (!buyingRequired.isEmpty()) {
            if (gatheringTasks != null && !gatheringTasks.isEmpty()) {
                for (Map.Entry<String, WatTask> tasks : gatheringTasks.entrySet()) {
                    if (buyingRequired.containsKey(tasks.getKey())) {
                        Logger.log("We have an alternate pathway to get " + tasks.getKey() + ", setting task..");
                        TaskManager.getInstance().setCurrentTask(tasks.getValue());
                        return;
                    }
                }
            }

            ItemUtils.setBankMode(BankMode.ITEM);
            HashMap<String, Integer> m = new HashMap<>();

            if (Inventory.isFull()) {
                Logger.log("Exchanger: Depositing all items, reason: Full, need space");
                Bank.depositAllItems();
                Sleep.sleep(100, 400);
            }

            int finalPrice = 0;
            for (Map.Entry<String, Integer> entry : buyingRequired.entrySet()) {
                String itemFinal = entry.getKey();
                if (itemFinal.equals("Old school bond (untradeable)")) {
                    itemFinal = "Old school bond";
                }

                if (itemFinal.equals("Yew longbow") || itemFinal.equals("Yew longbow (u)")) {
                    Logger.log("We need to create more yew longbows");
                    TaskManager.getInstance().setCurrentTask(new FletchingTask(false));
                    return;
                }

                int itemMultiplier = entry.getValue() > 0 ? entry.getValue() : -entry.getValue();
                int initialPrice = NumUtils.getItemPrice(itemFinal);
                int multipliedPrice = initialPrice * itemMultiplier;
                finalPrice += multipliedPrice;
            }

            int toWithdraw = finalPrice;
            if (Inventory.contains("Coins")) {
                toWithdraw -= Inventory.count("Coins");
            }

            if ((Inventory.count("Coins") >= toWithdraw) || (Bank.contains("Coins") && Bank.count("Coins") >= toWithdraw)) {
                Bank.withdraw("Coins", Bank.count("Coins"));
                Sleep.sleep(100, 200);
                Bank.close();

                TaskManager.getInstance().setCurrentTask(new GrandExchangeTask("Buying required items", false, buyingRequired, this));
                return;
            } else {
                ItemUtils.setBankMode(BankMode.NOTE);
                Logger.log("Exchanger: Need to sell items");
                if (Inventory.isFull()) {
                    Bank.depositAllExcept("Coins");
                    Sleep.sleep(100, 200);
                }


                if (Bank.contains("Coins")) {
                    finalPrice -= Bank.count("Coins");
                }

                if (Inventory.contains("Coins")) {
                    finalPrice -= Inventory.count("Coins");
                }

                Logger.log("No items to sell, so reverse muling " + finalPrice + " gp");
                int totalPrice = finalPrice;
                TaskManager.getInstance().setCurrentTask(new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                    {
                        put("Coins", totalPrice);
                    }
                }, this));
                return;

            }
        }

        Logger.log("Exchanger: Finished checks");

        if (postTask != null && !(postTask instanceof MulingTask)) {
            depositNonRequired();
        }

        Logger.log("Banking: Complete");

        if (postTask != null) {
            Logger.log("sending to post task: " + postTask.getName());
            TaskManager.getInstance().setCurrentTask(postTask);
        }
    }

    private void handleEquipmentDeposit() {
        List<String> toUnequip = new ArrayList<>();
        if (!postTask.clothesRequired().isEmpty() && !Equipment.isEmpty()) {
            for (Item i : Equipment.all()) {
                if (i == null) continue;
                if (!postTask.inventoryTolerated().contains(i.getName())
                        && !postTask.clothesRequired().containsKey(i.getName())
                        && !postTask.inventoryRequired().containsKey(i.getName())) {

                    Logger.log("Task " + postTask.getName() + " does not require/tolerate: " + i.getName() + ", adding to list");
                    toUnequip.add(i.getName());
                }
            }

            if (toUnequip.isEmpty())
                return;

            if (toUnequip.size() == Equipment.all(Objects::nonNull).size()) {
                if (Equipment.size() > 0) {
                    Logger.log("Depositing all gear due to task: " + postTask.getName() + " not requiring/tolerating any of it");
                    Bank.depositAllEquipment();
                }
            } else {
                if (Inventory.isFull() && Bank.isOpen()) {
                    Bank.depositAllItems();
                    Sleep.sleepUntil(Inventory::isEmpty, Calculations.random(5000, 10000));
                }

                if (Bank.isOpen()) {
                    Bank.close();
                    Sleep.sleepUntil(() -> !Bank.isOpen(), Calculations.random(5000, 10000));
                }

                if (!Tabs.isOpen(Tab.EQUIPMENT)) {
                    Tabs.open(Tab.EQUIPMENT);
                    Sleep.sleepUntil(() -> Tabs.isOpen(Tab.EQUIPMENT), Calculations.random(5000, 10000));
                }

                for (Item i : Equipment.all()) {
                    if (i == null) continue;
                    if (Inventory.isFull()) break;

                    if (toUnequip.contains(i.getName())) {
                        if (!Equipment.unequip(x -> x != null && x.getName().contains(i.getName()))) {
                            Logger.error("Equipment: Error unequipping item");
                        } else {
                            Sleep.sleepUntil(() -> !Equipment.contains(x -> x != null && x.getName().contains(i.getName())), Calculations.random(5000, 10000));
                        }
                    }
                }

                if (!Tabs.isOpen(Tab.INVENTORY)) {
                    Tabs.open(Tab.INVENTORY);
                    Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), Calculations.random(5000, 10000));
                }

                if (!Bank.isOpen()) {
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
            //handleEquipmentDeposit();
        }

        boolean canDepositAll = true;
        for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
            if (Inventory.contains(entry.getKey())) {
                canDepositAll = false;
                break;
            }
        }

        for (String i : inventoryTolerated()) {
            if (Inventory.contains(i)) {
                canDepositAll = false;
                break;
            }
        }

        if (!canDepositAll) {
            if (!inventoryRequired.isEmpty()) {
                for (Map.Entry<String, Integer> entry : inventoryRequired.entrySet()) {
                    Logger.log("Inventory requires: " + entry.getKey());
                    if (Inventory.contains(entry.getKey()) && entry.getValue() > 0) {
                        if (Inventory.count(entry.getKey()) > entry.getValue()) {
                            if (Inventory.get(x -> x.getName().contains(entry.getKey())).isNoted()) {
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

                List<String> toKeep = new ArrayList<>();
                for (Item i : Inventory.all()) {
                    if (i == null)
                        continue;

                    for (String n : inventoryRequired().keySet()) {
                        if (i.getName().contains(n)) {
                            toKeep.add(i.getName());
                        }
                    }
                }

                Logger.log("Banking: Depositing all except: " + toKeep.toString());
                Bank.depositAllExcept(toKeep.toArray(new String[0]));
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
            if (!Inventory.all(Objects::nonNull).isEmpty()) {
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
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        if (postTask != null) {
            return postTask.clothesRequired();
        }

        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        if (postTask != null) {
            return postTask.inventoryRequired();
        }

        return new HashMap<>();
    }

    @Override
    public List<String> inventoryTolerated() {
        if (postTask != null) {
            return postTask.inventoryTolerated();
        }

        return new ArrayList<>();
    }
}