package org.lolwat.Tasks;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatMiner;

import java.util.HashMap;
import java.util.Map;

public class DynamicBankingTask implements WatTask {
    private final String name;
    private final WatTask postTask;
    private final HashMap<String, Integer> requiredItems;
    private final boolean depositAllFirst;
    private final boolean buyMissingItems;
    private final HashMap<String, Integer> grandExchangeToBuy;

    public DynamicBankingTask(String taskName, HashMap<String, Integer> required, boolean depositAll, WatTask post, boolean buyMissing) {
        name = taskName;
        requiredItems = required;
        depositAllFirst = depositAll;
        buyMissingItems = buyMissing;
        postTask = post;
        grandExchangeToBuy = new HashMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void execute(WatMiner instance) {
        // Are we near a banker?
        if(NPCs.closest("Banker") != null) {
            // Open the bank if we don't have it open
            if (!Bank.isOpen()) {
                Bank.open();
            }

            // Sleep until the bank is open, or 7.5 seconds, whichever comes first
            Sleep.sleepUntil(Bank::isOpen, 7500);

            // If it's still not open, we'll restart the loop which will redo the above.
            if (!Bank.isOpen()) {
                return;
            }

            // Does the bank request want us to deposit everything
            if (depositAllFirst) {
                // Deposit everything except the items we require from the request
                //Bank.depositAllExcept(requiredItems.keySet().toArray(new String[0]));
                Bank.depositAllEquipment();
            }

            // Loop through our required items
            Logger.log("==== Looping through required items ====");
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
                        if (bankItem.getAmount() >= item.getValue()) {
                            // We own the right amount already
                            Bank.withdraw(item.getKey(), item.getValue());
                        } else {
                            // We don't own the right amount of it
                            if (buyMissingItems) {
                                Logger.log("We need " + item.getValue() + " more of " + item.getKey() + ", adding to G.E buy list");
                                grandExchangeToBuy.put(item.getKey(), item.getValue());
                            } else {
                                Logger.error("We need " + item.getValue() + " of " + item.getKey() + " but we only have " + bankItem.getAmount());
                                instance.fatalError = true;
                            }
                            return;
                        }
                    } else {
                        // We don't own any of it
                        if (buyMissingItems) {
                            Logger.log("We need " + item.getValue() + " more of " + item.getKey() + ", adding to G.E buy list");
                            grandExchangeToBuy.put(item.getKey(), item.getValue());
                        } else {
                            Logger.error("We need " + item.getValue() + " of " + item.getKey() + " but we own none");
                            instance.fatalError = true;
                        }
                        return;
                    }
                }
            }

            requiredItems.clear();

            Logger.log("We are finished with checking our required items.");

            if (grandExchangeToBuy.size() > 0 && buyMissingItems) {
                Logger.log("We need to create a DynamicGrandExchangeTask to buy " + grandExchangeToBuy.size() + " items: ");
                int totalValue = 0;
                for (Map.Entry<String, Integer> buyItem : grandExchangeToBuy.entrySet()) {
                    Logger.log(buyItem.getKey() + " Quantity of: " + buyItem.getValue());
                    // Calculate the amount of money we are going to need to buy everything at market price or so + 10%
                    totalValue += (LivePrices.get(buyItem.getKey()) * buyItem.getValue()) * 2;
                }

                // Holy shit! Probably gonna be a lot.
                Logger.log("We are gonna need " + totalValue + " GP to buy everything we need.");
                if(Bank.contains("Coins")) {
                    Item bankCoins = Bank.get("Coins");
                    if(bankCoins != null && bankCoins.getAmount() >= totalValue) {
                        Bank.depositAllItems();
                        Bank.withdraw(bankCoins.getID(), totalValue);
                        Bank.close();
                        // Send them to the Grand Exchange and have them do another task after (null means it will evaluate when complete)
                        instance.currentTask = new DynamicGrandExchangeTask("Buying missing items", false, grandExchangeToBuy, postTask);
                    }
                    else {
                        Logger.error("We don't have enough GP to fulfill the G.E orders. Need: " + totalValue + ", have: " + (bankCoins != null ? bankCoins.getAmount() : 0));
                        instance.fatalError = true;
                    }
                }
            } else {
                // We are done banking operations, and we are NOT setting up a Grand Exchange Task.
                Bank.close();
                if(postTask != null) {
                    instance.currentTask = postTask;
                }
            }
        }
        else {
            BankLocation loc = BankLocation.getNearest();
            if(loc != null && Walking.shouldWalk(7)) {
                Walking.walk(loc);
            }
        }
    }

    @Override
    public int loopTime() {
        return 50;
    }
}
