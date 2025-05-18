package org.lolwat.tasks.alching;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;

import java.time.Instant;

public class WaitForOfferTask implements WatTask {
    private final long started;
    private final int slot;
    private final String target;
    private final WatTask post;

    public WaitForOfferTask(int slot, String target, WatTask post) {
        this.started = Instant.now().getEpochSecond();
        this.slot = slot;
        this.target = target;
        this.post = post;
    }

    @Override
    public String getName() {
        return "Waiting for offer";
    }

    @Override
    public void execute() {
        if(!GrandExchange.isOpen()) {
            if(!GrandExchange.open()) {
                Logger.error("onWait: problem opening grand exchange");
                return;
            }

            Sleep.sleepUntil(GrandExchange::isOpen, 5000);
            return;
        }

        if (!GrandExchange.isReadyToCollect(slot)) {
            if (!GrandExchange.slotContainsItem(slot)) {
                Logger.log("slot did not contain item");
                TaskManager.getInstance().setCurrentTask(post);
                return;
            }

            long now = Instant.now().getEpochSecond();
            long timeBeen = (now - started);

            if(timeBeen < 15) {
                return;
            } else {
                if (timeBeen > 15) { // open ended for further timeouts i guess
                    Logger.log("waited 15s for offer to fulfill, cancelling");
                    if (!GrandExchange.cancelOffer(slot)) {
                        Logger.error("error cancelling offer");
                        return;
                    }

                    Sleep.sleepUntil(() -> GrandExchange.isReadyToCollect(slot), 1000);

                    if(!GrandExchange.isReadyToCollect(slot)) {
                        return;
                    }
                }
            }
        }

        if (GrandExchange.isReadyToCollect() && !GrandExchange.collect()) {
            Logger.error("error collecting from exchange");
            return;
        }

        if(GrandExchange.slotContainsItem(slot)) {
            long now = Instant.now().getEpochSecond();
            if (GrandExchange.isBuyOpen() || !GrandExchange.isOpen() || (now - started) > 30 || !Client.isLoggedIn()) {
                Logger.error("we had a weird issue, sending to alchemy..");
                TaskManager.getInstance().setCurrentTask(new HighAlchemyTask());
                return;
            }

            Sleep.sleepUntil(() -> !GrandExchange.slotContainsItem(slot), 5000);

            if(GrandExchange.slotContainsItem(slot)) {
                Logger.log("slot still contained the item we need, going back");
                return;
            }
        }

        if(GrandExchange.getUsedSlots() > 0) {
            Logger.log("we have offers in the window, ridding them");
            if(!GrandExchange.cancelAll()) {
                Logger.error("problem cancelling all offers..");
                return;
            }

            if(GrandExchange.getUsedSlots() > 0) {
                Logger.log("still got items, returning");
                return;
            }

            if(GrandExchange.isReadyToCollect() && !GrandExchange.collect()) {
                Logger.error("problem collecting");
                return;
            }
        }

        int amountHave = Inventory.count(x -> x != null && x.getName().equals(target));
        if (amountHave > 0) {
            Logger.log("we fulfilled some of our alch buy, using those");
            if (!GrandExchange.close()) {
                Logger.error("problem closing GE1");
            }

            ConfigManager.getInstance().setCurrentTargetAmount(amountHave);
            ConfigManager.getInstance().addItemExpiry(target);

            if (ConfigManager.getInstance().getPurchasedAmount().get(target) != null) {
                int toPut = amountHave + ConfigManager.getInstance().getPurchasedAmount().get(target);
                ConfigManager.getInstance().getPurchasedAmount().put(target, toPut);
            } else {
                ConfigManager.getInstance().getPurchasedAmount().put(target, amountHave);
            }

            if (ConfigManager.getInstance().getPurchasedAmount().get(target) >= ConfigManager.getInstance().getBuyLimits().get(target)) {
                Logger.warn("bought >= 4 hour limit for: " + target);
                ConfigManager.getInstance().addItemExpiry(target);
            }

            TaskManager.getInstance().setFutureTask(post);
        } else {
            Logger.log("didnt get any items at all, finding new target");
            ConfigManager.getInstance().getNoBuy().add(target);
            ConfigManager.getInstance().addItemExpiry(target);
            ConfigManager.getInstance().getNewAlchTarget();
            TaskManager.getInstance().setFutureTask(new HighAlchemyTask());
        }
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
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }
}
