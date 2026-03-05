package org.lolwat.tasks.exchange;

import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.misc.TraversalTask;

public class BuySingleItemTask implements WatTask {
    private final String item;
    private final int quantity;
    private final int pricePer;
    private final WatTask parent;

    public BuySingleItemTask(String item, int quantity, int pricePer, WatTask parent) {
        this.item = item;
        this.quantity = quantity;
        this.pricePer = pricePer;
        this.parent = parent;
    }

    @Override
    public String getName() {
        return "Exchange (New) - Buy " + item + " x" + quantity + " @ " + pricePer + " each";
    }

    @Override
    public void execute() {
        if(!BankLocation.GRAND_EXCHANGE.getArea(15).contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(BankLocation.GRAND_EXCHANGE.getArea(15), this));
            return;
        }

        if(Bank.isOpen()) {
            if(!Bank.close()) {
                Logger.error("problem closing bank during BuySingleItemTask");
                return;
            }

            Sleep.sleepUntil(() -> !Bank.isOpen(), 10000);
            return;
        }

        if(!GrandExchange.isOpen()) {
            if(!GrandExchange.open()) {
                Logger.error("problem opening grand exchange during BuySingleItemTask");
                return;
            }

            Sleep.sleepUntil(GrandExchange::isOpen, 10000);
            return;
        }

        if (GrandExchange.isReadyToCollect()) {
            while (GrandExchange.isReadyToCollect()) {
                if (!GrandExchange.collect()) {
                    Logger.error("prelim: failed to collect at G.E");
                }

                Sleep.sleepUntil(() -> !GrandExchange.isReadyToCollect(), 500);
            }
            return;
        }

        int slot = GrandExchange.getFirstOpenSlot();
        if (slot > -1) {
            if (!GrandExchange.isBuyOpen()) {
                if (!GrandExchange.openBuyScreen(slot)) {
                    Logger.error("failed to open slot " + slot);
                    return;
                }

                Sleep.sleepUntil(() -> GrandExchange.isBuyOpen() && GrandExchange.isSearchOpen(), 5000);
            }

            if (!GrandExchange.buyItem(item, quantity, pricePer)) {
                while(GrandExchange.isOpen()) {
                    if(!GrandExchange.close()) {
                        Logger.error("problem closing exchange during problem buying item");
                    }
                }
                return;
            }

            Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(slot), 3000);
        } else {
            Logger.warn("cancelling all offers as we have no slots");
        }

        TaskManager.getInstance().setCurrentTask(new WaitForOfferTask(slot, item, parent));
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public Skill trainsSkill() {
        return parent.trainsSkill();
    }
}
