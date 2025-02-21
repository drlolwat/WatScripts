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
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.misc.utils.crafting.CraftingUtils;
import org.lolwat.tasks.misc.BankingTask;

import java.util.*;

public class GemCuttingTask implements WatTask {
    private int avoidAfterLevel = 0;
    private int totalInventories = 1;

    public GemCuttingTask(int avoidAfterLevel) {
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

        String cuttingGem = CraftingUtils.getBestGemToCut();

        if(!Inventory.use("Chisel")) {
            Logger.error("Could not use chisel");
            return;
        }

        if(!Inventory.interact(x -> x != null && x.getName().equals(cuttingGem))) {
            Logger.error("Could not cut gem");
            return;
        }

        Sleep.sleepUntil(() -> Widgets.getWidget(270) != null && Objects.requireNonNull(Widgets.getWidget(270)).isVisible(), 5000);

        Widget w = Widgets.getWidget(270);
        if(w != null) {
            WidgetChild c = w.getChild(14); // only ever 1
            if(c != null) {
                if(c.interact()) {
                    Sleep.sleepUntil(() -> !Inventory.contains(cuttingGem) || Dialogues.canContinue(), 60000);
                }
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.CRAFTING) < avoidAfterLevel && Skills.getRealLevel(Skill.CRAFTING) >= 20;
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
        reqs.put(ItemManager.getInstance().getItem(CraftingUtils.getBestGemToCut()), 27);
        reqs.put(ItemManager.getInstance().getItem("Chisel"), 1);
        return reqs;
    }
}
