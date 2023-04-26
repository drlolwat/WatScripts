package org.lolwat.Tasks.Dynamic;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Entity;
import org.lolwat.Tasks.WatTask;
import org.lolwat.WatMiner;

import java.util.HashMap;

public class DynamicGrandExchangeTask implements WatTask {
    private final String name;
    private final HashMap<String, Integer> itemList;
    private final WatTask postTask;
    private final boolean isSelling;

    public DynamicGrandExchangeTask(String taskName, boolean selling, HashMap<String, Integer> items, WatTask post) {
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
    public void execute(WatMiner instance) {
        if(NPCs.closest("Grand Exchange Clerk") != null) {
            Logger.log("Grand Exchange Clerk: within sight");
            Entity clerk = NPCs.closest("Grand Exchange Clerk");
            if (clerk != null && !Map.isTileOnScreen(clerk.getTile())) {
                Camera.rotateToEntity(clerk);
            }

            // Check if we are selling here, if so, make sure we already have everything we need.
            // if not, quickly create a new task to grab the shit we need from the bank.
            if(isSelling) { // 2 loops of the inventory shouldn't really harm performance, it's just bad practice
                HashMap<String, Integer> req = new HashMap<>();
                for (java.util.Map.Entry<String, Integer> item : itemList.entrySet()) {
                    if(!Inventory.contains(item.getKey())) {
                        req.put(item.getKey(), item.getValue());
                    }
                }

                if(req.size() > 0) {
                    instance.currentTask = new DynamicBankingTask("Grabbing required items", req, true, this, false);
                    return;
                }
            }

            if (!GrandExchange.isOpen()) {
                GrandExchange.open();
            }

            Logger.log("Opening Grand Exchange");
            Sleep.sleepUntil(GrandExchange::isOpen, 7500);

            if (!GrandExchange.isOpen()) {
                return;
            }

            Logger.log("==== Beginning G.E Main Operations ====");
            for (java.util.Map.Entry<String, Integer> item : itemList.entrySet()) {
                // Get a slot. If unavailable, will cancel the other offers we have going
                int slot = GrandExchange.getFirstOpenSlot();
                if (slot == -1) {
                    GrandExchange.cancelAll();
                    return;
                }

                if (item.getValue() == 0)
                    continue;

                if (GrandExchange.contains(item.getKey())) {
                    Logger.error("Stuck buying/selling item: " + item.getKey() + ", maybe try buying/selling it manually?");
                    instance.fatalError = true;
                    return;
                }

                if (isSelling) {
                    if(Inventory.contains(item.getKey())) {
                        Logger.log("Selling: " + item.getKey());
                        Inventory.get(item.getKey()).interact();
                        Sleep.sleep(100, 600);
                        GrandExchange.setPrice((int) (LivePrices.get(item.getKey()) / 0.7));
                        Sleep.sleep(100, 200);
                        GrandExchange.confirm();
                        Sleep.sleepUntil(GrandExchange::isReadyToCollect, 1000);
                        GrandExchange.collect();
                    }
                } else {
                    Logger.log("Buying: " + item.getKey());
                    GrandExchange.openBuyScreen(slot);

                    // Sleep until the buy screen is open, or 5 seconds, whichever is faster
                    Sleep.sleep(400, 800);

                    // Add the item.
                    GrandExchange.addBuyItem(item.getKey());
                    Sleep.sleep(100, 300);
                    GrandExchange.setPrice((int) (LivePrices.get(item.getKey()) * 1.8));
                    Sleep.sleep(100, 300);
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

            itemList.clear();

            Logger.log("==== Grand Exchange Operations: Complete ====");
            GrandExchange.close();

            if (!Bank.open()) {
                Bank.open();
            }

            Sleep.sleepUntil(Bank::isOpen, 7500);
            Bank.depositAllItems();
            Bank.close();

            instance.currentTask = null;

        }
        else {
            if(Walking.shouldWalk(7)) {
                Logger.log("Walking to Grand Exchange");
                Walking.walk(BankLocation.GRAND_EXCHANGE);
            }
        }
    }

    @Override
    public int loopTime() {
        return 5;
    }
}
