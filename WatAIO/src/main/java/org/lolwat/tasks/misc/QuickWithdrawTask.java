package org.lolwat.tasks.misc;

import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;

public class QuickWithdrawTask implements WatTask {
    private final int itemId;
    private final int itemQty;
    private final WatTask alternative;
    private final WatTask postTask;

    public QuickWithdrawTask(int itemId, int itemQty, WatTask alternative, WatTask postTask) {
        this.itemId = itemId;
        this.itemQty = itemQty;
        this.alternative = alternative;
        this.postTask = postTask;
    }

    @Override
    public String getName() {
        return "Banking";
    }

    @Override
    public void execute() {
        if(!Bank.isOpen()) {
            ItemUtils.bank(this);
            return;
        }

        if(ItemUtils.inventoryContains(itemId, itemQty, false)) {
            TaskManager.getInstance().setCurrentTask(postTask);
        } else {
            if(ItemUtils.bankContains(itemId, itemQty, false)) {
                if(!Bank.withdraw(itemId, itemQty)) {
                    return;
                }

                if(ItemUtils.inventoryContains(itemId, itemQty, false)) {
                    TaskManager.getInstance().setCurrentTask(postTask);
                }
            } else {
                TaskManager.getInstance().setCurrentTask(alternative);
            }
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

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }
}
