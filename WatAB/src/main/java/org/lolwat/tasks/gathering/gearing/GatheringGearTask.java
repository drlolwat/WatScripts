package org.lolwat.tasks.gathering.gearing;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.exchange.BuySingleItemTask;
import org.lolwat.types.gear.WatTool;
import org.lolwat.types.interfaces.WatTask;

public class GatheringGearTask implements WatTask {
    private final Skill skill;
    private final WatTask parent;

    public GatheringGearTask(Skill sk, WatTask parent) {
        this.skill = sk;
        this.parent = parent;
    }

    @Override
    public void execute() {
        if(!Bank.isOpen()) {
            WatUtils.bank(this);
            return;
        }

        WatTool bestTool = ItemManager.getInstance().getBestTool(skill);
        if (Inventory.count(x -> x != null && x.getName().contains(bestTool.getName()) && x.isNoted()) > 0) {
            if (!Bank.depositAll(bestTool.getName())) {
                Logger.log("failed to deposit item: " + bestTool.getName());
                return;
            }

            Sleep.sleepUntil(() -> Inventory.count(x -> x != null && x.getName().contains(bestTool.getName()) && x.isNoted()) == 0, 5000);
        }

        if(Inventory.contains(x -> x != null && x.getName().contains(bestTool.getName()) && !x.isNoted())) {
            Logger.log("we have the tool we need: " + bestTool.getName());
            TaskManager.getInstance().setCurrentTask(parent);
            return;
        }

        if(Inventory.isFull()) {
            if(!Bank.depositAllItems()) {
                Logger.error("problem depositing all items when inv is full 1");
                return;
            }
        }

        if (Bank.contains(x -> x != null && x.getName().contains(bestTool.getName()))) {
            if(!Bank.withdraw(bestTool.getName(), 1)) {
                Logger.error("problem withdrawing tool: " + bestTool.getName());
            }

            Sleep.sleepUntil(() -> Inventory.contains(x -> x != null && x.getName().contains(bestTool.getName())), 5000);

            if(!Bank.depositAllExcept(bestTool.getName())) {
                Logger.error("problem depositing all items except needed tool");
            }

            TaskManager.getInstance().setCurrentTask(parent);
        } else {
            TaskManager.getInstance().setCurrentTask(new BuySingleItemTask(bestTool.getName(), 1, bestTool.getPrice(), this));
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return skill;
    }

    @Override
    public Integer avoidAfterLevel() {
        return ConfigManager.getInstance().getSkillTarget(skill);
    }
}
