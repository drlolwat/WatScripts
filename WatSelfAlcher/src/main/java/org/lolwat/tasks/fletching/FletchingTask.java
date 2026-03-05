package org.lolwat.tasks.fletching;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.LiquidationTask;

import java.util.HashMap;
import java.util.Map;

public class FletchingTask implements WatTask {
    private final boolean stopAtGoal;

    public FletchingTask(boolean stopAtGoal) {
        this.stopAtGoal = stopAtGoal;
    }

    @Override
    public String getName() {
        return "Fletching (" + (stopAtGoal ? "Training" : "Making unstrung yews") + ")";
    }

    @Override
    public void execute() {
        if(Skills.getRealLevel(Skill.FLETCHING) >= 70 && stopAtGoal) {
            Logger.log("Fletching is at 70, handing off to next task");
            TaskManager.getInstance().setCurrentTask(new LiquidationTask(new FletchingTask(false), 0));
            return;
        }

        for (Map.Entry<String, Integer> entry : inventoryRequired().entrySet()) {
            if (!ItemUtils.inventoryContains(entry.getKey(), 1, false)) {
                Logger.log("Missing " + entry.getValue() + " " + entry.getKey());
                TaskManager.getInstance().setCurrentTask(new BankingTask(null, null,
                        stopAtGoal ? Calculations.random(10, 20) : Calculations.random(145, 155), this, null));
                return;
            }
        }

        if (Bank.isOpen()) {
            Bank.close();
            return;
        }

        if(!Tabs.isOpen(Tab.INVENTORY)) {
            if(!Tabs.open(Tab.INVENTORY)) {
                Logger.log("Failed to open inventory tab");
                return;
            }

            Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), 5000);
        }

        if(Widgets.isVisible(270)) {
            WatUtils.handleWidgetFletching();
            return;
        }

        if (!Inventory.use("Knife")) {
            Logger.log("Failed to use knife");
            return;
        }

        if (!Inventory.use(WatUtils.getBestLogType())) {
            Logger.log("Failed to use " + WatUtils.getBestLogType());
            return;
        }

        if(!Players.getLocal().isAnimating()) {
            Sleep.sleepUntil(() -> Widgets.isVisible(270), 5000);
            WatUtils.handleWidgetFletching();
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
        return Skill.FLETCHING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 99;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> inventory = new HashMap<>();
        inventory.put(WatUtils.getBestLogType(), 27);
        inventory.put("Knife", 1);

        return inventory;
    }
}