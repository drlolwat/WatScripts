package org.lolwat.tasks.misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.GroundItem;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;

public class GatheringTask implements WatTask {
    private final String objectName;
    private final String inventoryName;
    private final int amount;
    private final Area location;
    private final boolean ground;
    private final WatTask postTask;

    public GatheringTask(String gameObject, String inventoryItem, Integer amountRequired, Area loc, boolean groundItem, WatTask post) {
        objectName = gameObject;
        inventoryName = inventoryItem;
        amount = amountRequired;
        location = loc;
        postTask = post;
        ground = groundItem;
    }

    @Override
    public String getName() {
        return "Gathering";
    }

    @Override
    public void execute() {
        if(Inventory.count(inventoryName) >= amount) {
            Logger.log("Have enough of item: " + inventoryName + " (" + amount + "), moving on");
            TaskManager.getInstance().setCurrentTask(postTask);
            return;
        }

        if(!location.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(location, this));
            return;
        }

        if(!ground) {
            GameObject i = GameObjects.closest(x -> x != null && x.exists() && x.getName().equals(objectName));
            if (i != null) {
                if (!i.interact()) {
                    Logger.log("Failed to interact with item: " + i.getName() + " (" + objectName + "?)");
                }

                Sleep.sleepUntil(() -> !i.exists() && Players.getLocal().isStandingStill(), 5000);
            }
        } else {
            GroundItem i = GroundItems.closest(x -> x != null && x.exists() && x.getName().equals(objectName));
            if (i != null) {
                if (!i.interact("Take")) {
                    Logger.log("Failed to interact with item: " + i.getName() + " (" + objectName + "?)");
                }

                Sleep.sleepUntil(() -> !i.exists() && Players.getLocal().isStandingStill(), 5000);
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
