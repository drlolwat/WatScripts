package org.lolwat.tasks.cooker;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.misc.BankingTask;

import java.util.HashMap;
import java.util.Objects;

public class MakingWineTask implements WatTask {
    int winesRequiredToLevel = 0;
    public MakingWineTask() {
        // calculate how many wines needed to get from current level to 74
    }

    @Override
    public String getName() {
        return "Making wine";
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

        if(!Inventory.use("Grapes")) {
            Logger.error("Could not use grapes");
            return;
        }

        if(!Inventory.interact(x -> x != null && x.getName().equals("Jug of water"))) {
            Logger.error("Could not use jug of water");
            return;
        }

        Sleep.sleepUntil(() -> Widgets.getWidget(270) != null && Objects.requireNonNull(Widgets.getWidget(270)).isVisible(), 5000);

        Widget w = Widgets.getWidget(270);
        if(w != null) {
            WidgetChild c = w.getChild(14); // only ever 1
            if(c != null) {
                if(c.interact()) {
                    Sleep.sleepUntil(() -> !Inventory.contains("Jug of water") || Dialogues.canContinue(), 60000);
                }
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
                put("Jug of wine", 14);
                put("Grapes", 14);
            }
        };
    }
}
