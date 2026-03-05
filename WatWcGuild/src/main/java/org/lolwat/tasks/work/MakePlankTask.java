package org.lolwat.tasks.work;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MakePlankTask implements WatTask {
    Area sawmill = new Area(1624, 3501, 1626, 3498);
    @Override
    public String getName() {
        return "Creating planks";
    }

    @Override
    public void execute() {
        for(Map.Entry<String, Integer> item : inventoryRequired().entrySet()) {
            if(Inventory.count(x -> x != null && !x.isNoted() && x.getName().equals(item.getKey())) < item.getValue()) {
                Logger.log("Missing: " + item.getKey());
                TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 400, this, null));
                return;
            }
        }

        if(!sawmill.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(sawmill, this));
            return;
        }

        NPC operator = NPCs.closest("Sawmill operator");
        if(operator != null) {
            if(!operator.interact("Buy-plank")) {
                Logger.error("failed to buy plank");
                return;
            }

            Sleep.sleepUntil(() -> Widgets.getWidget(270) != null && Objects.requireNonNull(Widgets.getWidget(270)).isVisible(), 5000);

            Widget w = Widgets.getWidget(270);
            if(w != null) {
                WidgetChild child = w.getChild(17);
                if(child != null && child.isVisible()) {
                    if(!child.interact("Make")) {
                        Logger.error("problem making");
                        return;
                    }

                    Sleep.sleepUntil(() -> !Inventory.contains(x -> x.getName().contains("logs")), 5000);
                    WatScript.getInstance().setPlanksCreated(WatScript.getInstance().getPlanksCreated() + 27);
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
    public Skill trainsSkill() {
        return Skill.WOODCUTTING;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Mahogany logs", 27);
        ret.put("Coins", 27 * 1500);
        return ret;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Skills necklace(", 1);
        ret.put("Ring of wealth (", 1);
        return ret;
    }
}
