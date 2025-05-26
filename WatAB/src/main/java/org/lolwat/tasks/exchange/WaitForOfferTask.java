package org.lolwat.tasks.exchange;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;

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
                Logger.error("we had a weird issue");
                TaskManager.getInstance().setCurrentTask(post);
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

            Sleep.sleepUntil(GrandExchange::isReadyToCollect, 5000);

            if(GrandExchange.isReadyToCollect() && !GrandExchange.collect()) {
                Logger.error("problem collecting");
                return;
            }
        }

        int amountHave = Inventory.count(x -> x != null && x.getName().equals(target));
        if (amountHave > 0) {
            if (!GrandExchange.close()) {
                Logger.error("problem closing GE1");
            }

            Logger.log("purchased " + amountHave + " of " + target);
        } else {
            Logger.log("we didnt buy shiiiit");
            WatItem item = ItemManager.getInstance().getItem(target);
            if (item != null) {
                Logger.log("raisin price");
                item.raisePrice();
            }
        }

        TaskManager.getInstance().setCurrentTask(post);
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return post.trainsSkill();
    }

    @Override
    public Integer avoidAfterLevel() {
        return post.avoidAfterLevel();
    }
}
