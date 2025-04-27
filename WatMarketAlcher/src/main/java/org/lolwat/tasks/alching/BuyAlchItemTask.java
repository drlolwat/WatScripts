package org.lolwat.tasks.alching;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;

import java.time.Instant;


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

        if(targetQty == 0) {
            ConfigManager.getInstance().getNewAlchTarget();
            Logger.error("had a qty of 0 for some reason");
            return;
        }

        if(Bank.isOpen() && !Bank.close()) {
            Logger.error("error closing bank");
            return;
        }

        Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);

        if(!GrandExchange.isOpen()) {
            if(!GrandExchange.open()) {
                Logger.error("error opening G.E [baiTask]");
                return;
            }

            Sleep.sleepUntil(GrandExchange::isOpen, 5000);
        }

        if(GrandExchange.isReadyToCollect()) {
            while(GrandExchange.isReadyToCollect()) {
                if(!GrandExchange.collect()) {
                    Logger.error("prelim: failed to collect at G.E");
                }

                Sleep.sleepUntil(() -> !GrandExchange.isReadyToCollect(), 500);
            }

            Logger.warn("failsafe: getting new target, there were items in the G.E");
            ConfigManager.getInstance().getNewAlchTarget();
            return;
        }

        int slot = GrandExchange.getFirstOpenSlot();
        if(slot > -1) {
            if(!GrandExchange.isBuyOpen()) {
                if(!GrandExchange.openBuyScreen(slot)) {
                    Logger.error("failed to open slot " + slot);
                    return;
                }

                Sleep.sleepUntil(() -> GrandExchange.isBuyOpen() && GrandExchange.isSearchOpen(), 5000);
            }

            if(!GrandExchange.addBuyItem(target)) {
                if(GrandExchange.isOpen() && !GrandExchange.close()) {
                    Logger.error("error adding buy item: " + target);
                }

                return;
            }

            Sleep.sleepUntil(() -> GrandExchange.getCurrentChosenItemID() == targetId, 5000);

            if(GrandExchange.getCurrentChosenItemID() != targetId) {
                Logger.error("error with item id, needed " + targetId + " but got " + GrandExchange.getCurrentChosenItemID());
                while(GrandExchange.isOpen()) {
                    if (!GrandExchange.close()) {
                        Logger.error("problem closing the G.E");
                    }
                }

                ConfigManager.getInstance().removeCooldown(target);
                return;
            }

            if(GrandExchange.getCurrentPrice() >= targetCost) {
                if(!GrandExchange.setPrice(targetCost)) {
                    Logger.error("error setting price per item");
                    return;
                }

                Sleep.sleepUntil(() -> GrandExchange.getCurrentPrice() == targetCost, 5000);
            }

            if(GrandExchange.getCurrentAmount() != targetQty) {
                if(!GrandExchange.setQuantity(targetQty)) {
                    Logger.error("error setting item qty");
                    return;
                }

                Sleep.sleepUntil(() -> GrandExchange.getCurrentAmount() == targetQty, 5000);
            }

            if(!GrandExchange.confirm()) {
                Logger.error("problem confirming offer..");
            }

            Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(slot), 3000);

            long started = Instant.now().getEpochSecond();
            while(!GrandExchange.isReadyToCollect(slot)) {
                if(!GrandExchange.isOpen() || !GrandExchange.slotContainsItem(slot) || !Client.isLoggedIn()) {
                    break;
                }

                long now = Instant.now().getEpochSecond();
                if((now - started) > 45) {
                    Logger.warn("got stuck in loop during purchase, breaking");
                    break;
                }
                else if((now - started) > 15) {
                    Logger.log("waited 15s for offer to fulfill, cancelling");

                    if(!GrandExchange.cancelOffer(slot)) {
                        Logger.error("error cancelling offer");
                    }

                    Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(slot), 5000);
                }
            }

            if(GrandExchange.isReadyToCollect() && !GrandExchange.collect()) {
                Logger.error("error collecting from exchange");
                return;
            }

            started = Instant.now().getEpochSecond();
            while(GrandExchange.slotContainsItem(slot)) {
                long now = Instant.now().getEpochSecond();
                if(GrandExchange.isBuyOpen() || !GrandExchange.isOpen() || (now - started) > 15 || !Client.isLoggedIn()) {
                    break;
                }

                Sleep.sleepUntil(() -> !GrandExchange.slotContainsItem(slot), 100);
            }

            int amountHave = Inventory.count(x -> x != null && x.getName().equals(target));
            if (amountHave > 0) {
                Logger.log("we fulfilled some of our alch buy, using those");
                if (!GrandExchange.close()) {
                    Logger.error("problem closing GE1");
                }

                ConfigManager.getInstance().setCurrentTargetAmount(amountHave);
                ConfigManager.getInstance().addItemExpiry(target);

                if(ConfigManager.getInstance().getPurchasedAmount().get(target) != null) {
                    int toPut = amountHave + ConfigManager.getInstance().getPurchasedAmount().get(target);
                    ConfigManager.getInstance().getPurchasedAmount().put(target, toPut);
                } else {
                    ConfigManager.getInstance().getPurchasedAmount().put(target, amountHave);
                }

                if(ConfigManager.getInstance().getPurchasedAmount().get(target) >= ConfigManager.getInstance().getBuyLimits().get(target)) {
                    Logger.warn("bought >= 4 hour limit for: " + target);
                    ConfigManager.getInstance().addItemExpiry(target);
                }

                /*if((ConfigManager.getInstance().getBuyLimits().get(target) / 4) > amountHave) {
                    Logger.log("but got less than a q of the buy limit, so wont get again");
                    ConfigManager.getInstance().getNoBuy().add(target);
                }*/

                TaskManager.getInstance().setCurrentTask(post);
            } else {
                Logger.log("didnt get any items at all, finding new target");
                ConfigManager.getInstance().getNoBuy().add(target);
                ConfigManager.getInstance().addItemExpiry(target);
                ConfigManager.getInstance().getNewAlchTarget();
            }
        }
        else {
            Logger.warn("cancelling all offers as we have no slots");
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

}
