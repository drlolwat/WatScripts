package org.lolwat.Tasks.Types.Misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.Tasks.WatTask;
import org.lolwat.WatAIO;

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
    public void execute(WatAIO instance) {
        if(NPCs.closest("Grand Exchange Clerk") != null) {
            Logger.log("Grand Exchange Clerk: within sight");
            Entity clerk = NPCs.closest("Grand Exchange Clerk");
            if (clerk != null && !Map.isTileOnScreen(clerk.getTile())) {
                Camera.rotateToEntity(clerk);
            }

            Logger.log("Opening Grand Exchange");

            if (!GrandExchange.isOpen()) {
                GrandExchange.open();
                return;
            }

            Sleep.sleep(1000, 3000);

            Logger.log("==== Beginning G.E Main Operations ====");
            Logger.log("We have " + itemList.size() + " items to deal with");
            for (java.util.Map.Entry<String, Integer> item : itemList.entrySet()) {
                // Get a slot. If unavailable, will cancel the other offers we have going
                if (GrandExchange.getFirstOpenSlot() == -1) {
                    Sleep.sleep(500, 900);
                    if(GrandExchange.isReadyToCollect()) {
                        GrandExchange.collect();
                    }
                    Sleep.sleep(500, 900);

                    if (GrandExchange.getFirstOpenSlot() == -1) {
                        for(GrandExchangeItem gxIt : GrandExchange.getItems()) {

                            if(gxIt.isReadyToCollect()) {
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
                if(slot == -1) {
                    Logger.log("Crazy slot issue again with the G.E");
                    instance.currentTask = null;
                    instance.fatalError = true;
                    return;
                }

                if (item.getValue() == 0) {
                    Logger.error("Trying to sell 0 of " + item.getKey());
                    continue;
                }

                // pre collect
                if(GrandExchange.isReadyToCollect()) {
                    GrandExchange.collect();
                }

                /*
                if (GrandExchange.contains(item.getKey())) {
                    if(retries >= 3) {
                        Logger.error("Stuck buying/selling item: " + item.getKey() + ", maybe try buying/selling it manually?");
                        instance.fatalError = true;
                    }
                    retries++;
                    return;
                }*/

                if (isSelling) {
                    Logger.log("Selling: " + item.getKey());
                    if(Inventory.contains(item.getKey())) {
                        Inventory.get(item.getKey()).interact();
                        Sleep.sleep(100, 600);
                        WidgetChild fivePercent = GrandExchange.getDecreasePriceFivePercentButton();

                        if(fivePercent != null) {
                            fivePercent.interact();
                            fivePercent.interact();
                        }

                        Sleep.sleep(100, 200);
                        GrandExchange.confirm();
                        Sleep.sleepUntil(GrandExchange::isReadyToCollect, 1000);
                        GrandExchange.collect();
                    }
                    else {
                        Logger.log("Item was not in inventory: " + item.getKey());
                    }
                } else {
                    Logger.log("Buying: " + item.getKey());
                    GrandExchange.openBuyScreen(slot);

                    Sleep.sleep(400, 800);

                    while(item.getValue() > 0) {
                        // Add the item.
                        if (GrandExchange.addBuyItem(item.getKey())) {
                            int itemCost = (int) (LivePrices.get(item.getKey()) * 1.3);

                            if(itemCost <= 7) {
                                itemCost = 10;
                            }

                            if(Inventory.contains("Coins") && itemCost > Inventory.get("Coins").getAmount()) {
                                itemCost = Inventory.get("Coins").getAmount();
                            }

                            Sleep.sleep(100, 300);
                            GrandExchange.setPrice(itemCost);
                            Sleep.sleep(100, 300);

                            if(item.getValue() > 1) {
                                GrandExchange.setQuantity(item.getValue());
                                Sleep.sleep(100, 300);
                            }

                            GrandExchange.confirm();

                            // Sleep until the item is available..
                            Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(slot), 10000);

                            if (GrandExchange.isReadyToCollect(slot)) {
                                GrandExchange.collect();
                                item.setValue(0);
                            }

                            Sleep.sleep(50, 125);
                        }
                    }
                }
            }

            itemList.clear();

            Sleep.sleep(3000);
            if(GrandExchange.isReadyToCollect()) {
                GrandExchange.collect();
            }

            Logger.log("==== Grand Exchange Operations: Complete ====");
            GrandExchange.close();

            if (!Bank.open()) {
                Bank.open();
            }

            Sleep.sleepUntil(Bank::isOpen, 7500);
            Bank.depositAllItems();
            Bank.close();

            instance.currentTask = postTask;
        }
        else {
            instance.currentTask = new TraversalTask(BankLocation.GRAND_EXCHANGE.getTile(), false, this);
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
}
