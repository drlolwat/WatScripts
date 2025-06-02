package org.lolwat.tasks.alching;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;


public class BuyAlchItemTask implements WatTask {
    private final WatTask post;

    public BuyAlchItemTask(WatTask post) {
        this.post = post;
    }

    @Override
    public String getName() {
        return "Exchanging (Alching)";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute() {
        String target = ConfigManager.getInstance().getCurrentTarget();
        int targetQty = ConfigManager.getInstance().getCurrentTargetAmount();
        int targetCost = ConfigManager.getInstance().itemCost(target) + ConfigManager.getInstance().getConfigInt("price_modifier");
        int targetId = ConfigManager.getInstance().getItemIds().get(target);

        int myCoins = Inventory.count("Coins");// + Bank.count("Coins");
        if((double) (targetCost * targetQty) > myCoins) {
            targetQty = (int) Math.floor((double) myCoins / targetCost) - 1;
            ConfigManager.getInstance().setCurrentTargetAmount(targetQty);
            if(targetQty > 0) {
                Logger.log("onTask: changing targetQty to " + targetQty + " for affordability");
            } else {
                Logger.log("onTask: qty was subzero, getting new target");
                ConfigManager.getInstance().getNewAlchTarget();
            }
            return;
        }

        if (targetQty <= 0) {
            ConfigManager.getInstance().getNewAlchTarget();
            Logger.error("had a qty of 0 for some reason");
            return;
        }

        if (Bank.isOpen() && !Bank.close()) {
            Logger.error("error closing bank");
            return;
        }

        Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);

        if (!GrandExchange.isOpen()) {
            if (!GrandExchange.open()) {
                Logger.error("error opening G.E [baiTask]");
                TaskManager.getInstance().setCurrentTask(post);
                return;
            }

            Sleep.sleepUntil(GrandExchange::isOpen, 5000);
        }

        if (GrandExchange.isReadyToCollect()) {
            while (GrandExchange.isReadyToCollect()) {
                if (!GrandExchange.collect()) {
                    Logger.error("prelim: failed to collect at G.E");
                }

                Sleep.sleepUntil(() -> !GrandExchange.isReadyToCollect(), 500);
            }

            Logger.warn("failsafe: getting new target, there were items in the G.E");
            ConfigManager.getInstance().getNewAlchTarget();
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

            if (!GrandExchange.buyItem(targetId, targetQty, targetCost)) {
                ConfigManager.getInstance().removeCooldown(target);
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

        // send to wait task
        TaskManager.getInstance().setCurrentTask(new WaitForOfferTask(slot, target, post));
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }
}
