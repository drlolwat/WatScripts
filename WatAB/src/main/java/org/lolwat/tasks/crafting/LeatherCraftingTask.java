package org.lolwat.tasks.crafting;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;

import java.util.HashMap;
import java.util.Objects;

public class LeatherCraftingTask implements WatTask {
    int avoidAfterLevel = 0;
    int totalInventories;

    public LeatherCraftingTask(int avoidAfterLevel) {
        this.avoidAfterLevel = avoidAfterLevel;
        this.totalInventories = Calculations.random(10, 30);
    }

    @Override
    public void execute() {
        if(!ItemUtils.hasInventory()) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, totalInventories, this, null));
            return;
        }

        if(Bank.isOpen()) {
            Bank.close();
            return;
        }

        if(!Inventory.use("Needle")) {
            Logger.error("Could not use needle");
            return;
        }

        if(!Inventory.interact("Leather")) {
            Logger.error("Could not interact with leather");
            return;
        }

        Sleep.sleepUntil(() -> Widgets.getWidget(270) != null && Objects.requireNonNull(Widgets.getWidget(270)).isVisible(), 5000);

        Widget w = Widgets.getWidget(270);
        if(w != null) {
            WidgetChild c = w.getChild(14); // gloves, maybe do more later
            if(c != null) {
                if(c.interact()) {
                    Sleep.sleepUntil(() -> !Inventory.contains("Leather") || !Inventory.contains("Thread") || Dialogues.canContinue(), 60000);
                }
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.CRAFTING) <= avoidAfterLevel;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.CRAFTING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAfterLevel;
    }

    @Override
    public HashMap<WatItem, Integer> inventory() {
        HashMap<WatItem, Integer> reqs = new HashMap<>();
        reqs.put(ItemManager.getInstance().getItem("Leather"), 25);
        reqs.put(ItemManager.getInstance().getItem("Needle"), 1);
        reqs.put(ItemManager.getInstance().getItem("Thread"), 6);
        return reqs;
    }
}
