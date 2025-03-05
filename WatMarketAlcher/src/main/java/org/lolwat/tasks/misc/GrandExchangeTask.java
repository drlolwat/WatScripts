package org.lolwat.tasks.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatScript;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.TeleportManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.NumUtils;

import java.util.HashMap;

public class GrandExchangeTask implements WatTask {
    private final String name;
    private final HashMap<String, Integer> itemList;
    private final WatTask postTask;
    private final boolean isSelling;
    private int retries = 0;

    public GrandExchangeTask(String taskName, boolean selling, HashMap<String, Integer> items, WatTask post) {
        name = taskName;
        itemList = items;
        postTask = post;
        isSelling = selling;
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
    public void execute() {
        if (Inventory.isFull()) {
            Logger.log("Inventory is full, going to bank to deposit items.");
            if (!Bank.open(BankLocation.GRAND_EXCHANGE)) {
                Logger.log("Unable to open bank at Grand Exchange.");
                return;
            }

            Sleep.sleepUntil(Bank::isOpen, 5000);

            if (Bank.isOpen()) {
                // Deposit all except items in itemList and coins
                Bank.depositAllExcept(item -> itemList.containsKey(item.getName()) || item.getName().equalsIgnoreCase("Coins"));
                Sleep.sleepUntil(() -> !Inventory.isFull(), 3000);
            } else {
                Logger.log("Failed to open bank.");
                return;
            }

            Logger.log("Items deposited, returning to Grand Exchange.");
            if (!GrandExchange.open()) {
                Logger.log("Unable to open Grand Exchange.");
                return;
            }
        }


        if (NPCs.closest("Grand Exchange Clerk") != null) {
            Logger.log("Opening Grand Exchange");

            if (!GrandExchange.isOpen()) {
                GrandExchange.open();
                Sleep.sleepUntil(GrandExchange::isOpen, Calculations.random(5000, 10000));
                return;
            }

            Sleep.sleep(1000, 3000);

            Logger.log("==== Beginning G.E Main Operations ====");
            Logger.log("We have " + itemList.size() + " items to deal with");
            for (java.util.Map.Entry<String, Integer> item : itemList.entrySet()) {
                // Get a slot. If unavailable, will cancel the other offers we have going
                if (GrandExchange.getFirstOpenSlot() == -1) {
                    Sleep.sleep(500, 900);

                    if (GrandExchange.isReadyToCollect()) {
                        GrandExchange.collect();
                    }

                    Sleep.sleep(500, 900);

                    if (GrandExchange.getFirstOpenSlot() == -1) {
                        for (GrandExchangeItem gxIt : GrandExchange.getItems()) {

                            if (gxIt.isReadyToCollect()) {
                                GrandExchange.collect();
                            } else {
                                GrandExchange.cancelOffer(gxIt.getSlot());
                            }

                            Sleep.sleep(100, 400);
                        }

                        Sleep.sleep(100, 300);
                        GrandExchange.collect();
                        Sleep.sleep(500, 800);
                    }
                }

                int slot = GrandExchange.getFirstOpenSlot();
                if (slot == -1) {
                    Logger.log("Cancelling/collecting offers to free G.E spots");
                    for (GrandExchangeItem i : GrandExchange.getItems()) {
                        if (i.isReadyToCollect()) {
                            GrandExchange.collect();
                            Sleep.sleep(100, 300);
                        } else {
                            GrandExchange.cancelOffer(i.getSlot());
                            Sleep.sleepUntil(i::isReadyToCollect, 1000);
                            if (i.isReadyToCollect()) {
                                GrandExchange.collect();
                                Sleep.sleep(100, 300);
                            }
                        }
                    }
                }

                if (item.getValue() == 0) {
                    Logger.error("Trying to sell 0 of " + item.getKey());
                    continue;
                }

                // pre collect
                if (GrandExchange.isReadyToCollect()) {
                    GrandExchange.collect();
                }

                if (GrandExchange.getItem(item.getKey()) != null) {
                    Logger.log("Tell lolwat");
                }

                if (isSelling) {
                    Logger.log("Selling: " + item.getKey());
                    if (Inventory.contains(item.getKey())) {
                        Inventory.get(item.getKey()).interact();
                        Sleep.sleep(100, 600);

                        for (int i = 0; i <= 3; i++) {
                            if (!GrandExchange.getDecreasePriceFivePercentButton().interact()) {
                                Logger.log("error lowering g.e price");
                            }

                            Sleep.sleep(100, 300);
                        }

                        if (!GrandExchange.confirm()) {
                            Logger.log("error confirming g.e offer");
                            continue;
                        }

                        Sleep.sleepUntil(GrandExchange::isReadyToCollect, Calculations.random(800, 1800));

                        if (!GrandExchange.isReadyToCollect()) {
                            Logger.log("item did not sell instantly");
                            continue;
                        }

                        if (!GrandExchange.collect()) {
                            Logger.log("error collecting items from g.e");
                            continue;
                        }

                        Sleep.sleep(100, 500);
                    } else {
                        Logger.log("Item was not in inventory: " + item.getKey());
                    }
                } else {
                    String itemFinal = item.getKey();
                    if (itemFinal.equals("Old school bond (untradeable)")) {
                        itemFinal = "Old school bond";
                    } else if (TeleportManager.getInstance().isTeleportItem(itemFinal)) {
                        itemFinal = TeleportManager.getInstance().getChargedItemName(itemFinal);
                    }

                    Logger.log("Buying: " + itemFinal);

                    if (!GrandExchange.isBuyOpen() && !GrandExchange.openBuyScreen(slot)) {
                        Logger.error("Error opening buy screen in G.E, returning to postTask");
                        TaskManager.getInstance().setCurrentTask(postTask);
                        return;
                    }

                    Sleep.sleepUntil(GrandExchange::isBuyOpen, 10000);

                    while (item.getValue() != 0) {
                        if (retries >= 5) {
                            retries = 0;
                            Logger.log("Something went wrong with the G.E loop, breaking it.");
                            break;
                        }
                        else if(retries >= 1) {
                            if(itemFinal.equals(ConfigManager.getInstance().getCurrentTarget())) {
                                retries = 0;
                                break;
                            }
                        }

                        if (!GrandExchange.isBuyOpen()) {
                            Sleep.sleep(1000, 2000);

                            if (!GrandExchange.openBuyScreen(slot)) {
                                Logger.log("Error ensuring buy screen is open in G.E");
                            }

                            Sleep.sleepUntil(GrandExchange::isBuyOpen, 10000);

                            if (!GrandExchange.isBuyOpen()) {
                                Logger.log("Buy screen was not open when it was meant to be.");
                                retries++;
                                continue;
                            }
                        }

                        // Add the item.
                        if (GrandExchange.addBuyItem(itemFinal)) {
                            Sleep.sleepUntil(GrandExchange::isBuyOpen, 5000);

                            int itemCost = NumUtils.getItemPrice(itemFinal);
                            if (itemFinal.equals(ConfigManager.getInstance().getCurrentTarget())) {
                                int livePrice = ConfigManager.getInstance().itemCost(itemFinal);

                                itemCost = livePrice
                                        + ConfigManager.getInstance().getConfigInt("price_modifier");

                                Logger.log("price of alchable is " + livePrice + ", safely ignore the above log");
                                Logger.log("modified price from " + livePrice + " + to " + itemCost);
                            }

                            int coins = Inventory.count("Coins") + (Bank.isCached() ? Bank.count("Coins") : 0);
                            if (itemCost > coins) {
                                Logger.error("Didn't have enough coins to fulfill offer, breaking loop.");
                                TaskManager.getInstance().setCurrentTask(postTask);
                                return;
                            }

                            if (GrandExchange.setPrice(itemCost)) {
                                Sleep.sleep(100, 200);
                            }

                            Sleep.sleep(100, 300);

                            if (item.getValue() != 1) {
                                int q = item.getValue() >= 1 ? item.getValue() : -item.getValue();

                                if (q > ConfigManager.getInstance().getCurrentTargetAmount()) {
                                    q = ConfigManager.getInstance().getCurrentTargetAmount();
                                }

                                GrandExchange.setQuantity(q);
                                Sleep.sleep(100, 300);
                            }

                            GrandExchange.confirm();
                            Sleep.sleep(100, 300);

                            Widget w = Widgets.getWidget(289);
                            if (w != null) {
                                WidgetChild c = w.getChild(8); // yes button
                                if (c.interact()) {
                                    Sleep.sleep(100, 200);
                                } else {
                                    Logger.log("unable to interact with G.E warning");
                                }
                            }

                            Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(slot), 15000);

                            if (GrandExchange.isReadyToCollect(slot) || GrandExchange.isReadyToCollect()) {
                                if (!GrandExchange.collect()) {
                                    Logger.log("Error collecting items");
                                }

                                String trueFinal = itemFinal;
                                Sleep.sleepUntil(() -> Inventory.contains(x -> x != null && x.getName().equals(trueFinal)), 6000);

                                if (Inventory.contains(x -> x != null && x.getName().equals(trueFinal))) {
                                    item.setValue(0);
                                }

                                if (trueFinal.equals(ConfigManager.getInstance().getCurrentTarget())) {
                                    int amountHave = Inventory.count(x -> x != null
                                            && x.getName().equals(ConfigManager.getInstance().getCurrentTarget()))
                                            + (Bank.isCached()
                                            ? Bank.count(x -> x != null
                                            && x.getName().equals(ConfigManager.getInstance().getCurrentTarget()))
                                            : 0);

                                    if (amountHave > 0) {
                                        ConfigManager.getInstance().setCurrentTargetAmount(amountHave);
                                        ConfigManager.getInstance().addItemExpiry(ConfigManager.getInstance().getCurrentTarget());
                                    }
                                }
                            } else {
                                if (GrandExchange.cancelOffer(slot)) {
                                    Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(slot), 5000);
                                    if (!GrandExchange.collect()) {
                                        retries++;
                                        item.setValue(0);
                                        Logger.error("Error collecting cancelled item from G.E");
                                    } else {
                                        if (!item.getKey().equals(ConfigManager.getInstance().getCurrentTarget())) {
                                            Logger.log("cancelling offer");
                                            Logger.log("Cancelled and raised purchase price of " + item.getKey() + "...");
                                            NumUtils.raisePrice(item.getKey());

                                            if (NumUtils.getItemPrice(item.getKey()) > Inventory.count("Coins")) {
                                                Logger.log("Item price is higher than coin stack, breaking");
                                                break;
                                            }
                                        } else {
                                            retries++;
                                        }
                                    }
                                }
                            }

                            Sleep.sleep(50, 125);
                        }
                    }
                }
            }

            Sleep.sleep(3000);
            if (GrandExchange.isReadyToCollect()) {
                GrandExchange.collect();
            }

            int amountHave = Inventory.count(x -> x != null
                    && x.getName().equals(ConfigManager.getInstance().getCurrentTarget()))
                    + (Bank.isCached()
                    ? Bank.count(x -> x != null
                    && x.getName().equals(ConfigManager.getInstance().getCurrentTarget()))
                    : 0);

            if (amountHave > 0) {
                Logger.log("we fulfilled some of our alch buy, using those");
                if (!GrandExchange.close()) {
                    Logger.error("problem closing GE1");
                }
                ConfigManager.getInstance().setCurrentTargetAmount(amountHave);
                ConfigManager.getInstance().addItemExpiry(ConfigManager.getInstance().getCurrentTarget());
            } else {
                ConfigManager.getInstance().addItemExpiry(ConfigManager.getInstance().getCurrentTarget());
                ConfigManager.getInstance().getNewAlchTarget();
            }

            Logger.log("==== Grand Exchange Operations: Complete ====");
            GrandExchange.close();

            if (!Bank.open()) {
                Bank.open();
            }

            Sleep.sleepUntil(Bank::isOpen, 7500);

            Sleep.sleep(300, 800);
            TaskManager.getInstance().setCurrentTask(postTask);
        } else {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(BankLocation.GRAND_EXCHANGE.getArea(3), this));
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 5;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatScript instance) {

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
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
