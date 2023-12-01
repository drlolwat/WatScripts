package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.GrandExchangeItem;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Entity;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.util.HashMap;

public class GrandExchangeTask implements WatTask {
    private final String name;
    private final HashMap<String, Integer> itemList;
    private final WatTask postTask;
    private final boolean isSelling;
    private final int retries = 0;

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
                    Logger.log("Cancelling/collecting offers to free G.E spots");
                    for(GrandExchangeItem i : GrandExchange.getItems()) {
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
                if(GrandExchange.isReadyToCollect()) {
                    GrandExchange.collect();
                }

                if(GrandExchange.getItem(item.getKey()) != null) {
                    Logger.log("Tell lolwat");
                }

                if (isSelling) {
                    Logger.log("Selling: " + item.getKey());
                    if(Inventory.contains(item.getKey())) {
                        Inventory.get(item.getKey()).interact();
                        Sleep.sleep(100, 600);

                        if(GrandExchange.setPrice((int) (LivePrices.get(item.getKey()) / 1.2))) {
                            Sleep.sleep(100, 200);
                            GrandExchange.confirm();
                            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 1000);
                            GrandExchange.collect();
                        }
                        else {
                            return;
                        }
                    }
                    else {
                        Logger.log("Item was not in inventory: " + item.getKey());
                    }
                } else {
                    Logger.log("Buying: " + item.getKey());
                    GrandExchange.openBuyScreen(slot);

                    Sleep.sleep(400, 800);

                    while(item.getValue() != 0) {
                        if(!GrandExchange.isBuyOpen()) {
                            break;
                        }

                        // Add the item.
                        if (GrandExchange.addBuyItem(item.getKey())) {
                            Sleep.sleep(100, 220);
                            int itemCost = NumUtils.getItemPrice(item.getKey());

                            if(GrandExchange.setPrice(itemCost)) {
                                Sleep.sleep(100, 200);
                            }

                            Sleep.sleep(100, 300);

                            if (item.getValue() != 1 && !instance.SINGULAR_ITEMS.contains(item.getKey())) {
                                GrandExchange.setQuantity(item.getValue() >= 1 ? item.getValue() : -item.getValue());
                                Sleep.sleep(100, 300);
                            }

                            GrandExchange.confirm();
                            Sleep.sleep(100, 300);

                            Widget w = Widgets.getWidget(289);
                            if(w != null) {
                                WidgetChild c = w.getChild(8); // yes button
                                if(c.interact()) {
                                    Sleep.sleep(100, 200);
                                } else {
                                    Logger.log("unable to interact with G.E warning");
                                }
                            }

                            item.setValue(0);

                            Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(slot), 20000);

                            if (GrandExchange.isReadyToCollect(slot)) {
                                GrandExchange.collect();
                            }

                            Sleep.sleep(50, 125);
                        }
                    }
                }
            }

            //itemList.clear();

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

            /*
            if(postTask != null && !(postTask instanceof BankingTask)) {
                Bank.depositAllItems();
            }
            else {
                Bank.depositAllItems();
            }*/

            Sleep.sleep(300, 800);
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

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }
}
