package org.lolwat.tasks.alching;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;

public class AlchingBankingTask implements WatTask {
    private final WatTask post;

    public AlchingBankingTask(WatTask post) {
        this.post = post;
    }

    @Override
    public String getName() {
        return "Banking (Alching)";
    }

    @Override
    public void execute() {
        if (post == null) {
            Logger.error("HAB: post task was null for some reason, going to HA");
            TaskManager.getInstance().setCurrentTask(new HighAlchemyTask());
            return;
        }

        if(GrandExchange.isOpen()) {
            if(!GrandExchange.close()) {
                Logger.error("HAB: problem closing GE");
                return;
            }

            Sleep.sleepUntil(() -> !GrandExchange.isOpen(), 5000);
        }

        if (!Bank.isOpen()) {
            ItemUtils.bank(this);
            return;
        }

        if(Bank.contains("Nature rune")) {
            if(!Bank.withdrawAll("Nature rune")) {
                Logger.error("error withdrawing excess runes");
                return;
            }
        }

        if(Bank.contains("Coins")) {
            if(!Bank.withdrawAll("Coins")) {
                Logger.error("error withdrawing excess coins");
                return;
            }
        }

        Bank.resetCache();

        String target = ConfigManager.getInstance().getCurrentTarget();
        int targetsOnHand = Bank.count(x -> x != null && x.getName().equals(target))
                + Inventory.count(x -> x != null && x.getName().equals(target));

        if (targetsOnHand > 0) {
            if (target != null) {
                if (Bank.contains(target)) {
                    ItemUtils.setBankMode(BankMode.NOTE);
                    if (!Bank.withdrawAll(target)) {
                        Logger.error("problem withdrawing HA target");
                        return;
                    }
                    ItemUtils.setBankMode(BankMode.ITEM);
                }

                Sleep.sleepUntil(() -> !Bank.contains(target) && Inventory.contains(target), 5000);

                if (!Inventory.contains(target)) {
                    Logger.error("somehow didnt have the item");
                    return;
                }

                TaskManager.getInstance().setCurrentTask(post);
            } else {
                Logger.error("target was null, getting a new one");
                ConfigManager.getInstance().getNewAlchTarget();
            }
        } else {
            Logger.log("getting new target during banking");
            ConfigManager.getInstance().getNewAlchTarget();
            TaskManager.getInstance().setCurrentTask(new BuyAlchItemTask(post));
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
