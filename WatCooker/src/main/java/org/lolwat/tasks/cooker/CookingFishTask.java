package org.lolwat.tasks.cooker;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.HashMap;

public class CookingFishTask implements WatTask {
    Area usingArea = new Area();
    String fish;

    public CookingFishTask() {
        this.fish = "Raw shrimps";
    }

    @Override
    public String getName() {
        return "Cooking fish";
    }

    @Override
    public void execute() {
        for (java.util.Map.Entry<String, Integer> m : inventoryRequired().entrySet()) {
            if (Inventory.count(x -> x != null && !x.isNoted() && x.getName().equals(m.getKey())) < 1) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(null,
                        null, 28, this, null));
                return;
            }
        }

        if (!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
        }

        if (!usingArea.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(usingArea, this));
            return;
        }

        if (GameObjects.closest(x -> x != null && x.getName().toLowerCase().contains("range")).interact()) {
            Sleep.sleepUntil(() -> Widgets.getWidget(270) != null && Widgets.getWidget(270).isVisible(), 10000);
            if (Widgets.getWidget(270).getChild(14) != null && Widgets.getWidget(270).getChild(14).interact()) { //TODO null checks on the widgets
                Sleep.sleepUntil(() -> !Inventory.contains("Raw shrimps") || Dialogues.canContinue(), 60000);
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
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {
            {
                put(fish, 28);
            }
        };
    }
}
