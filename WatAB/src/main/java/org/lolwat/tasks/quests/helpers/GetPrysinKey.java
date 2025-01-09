package org.lolwat.tasks.quests.helpers;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.managers.TaskManager;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.HashMap;

public class GetPrysinKey implements WatTask {
    private final WatTask wrapper;

    public GetPrysinKey(WatTask wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public String getName() {
        return "Collecting Sir Prysin's key";
    }

    @Override
    public void execute() {
        if(ItemUtils.inventoryContains(2401, 1, false)) {
            Logger.log("Prysins key in inventory");
            TaskManager.getInstance().setCurrentTask(wrapper);
            return;
        }

        int drainStatus = PlayerSettings.getBitValue(2568); // 0=not touched, 1=key in sewer, 2=key taken
        if (drainStatus == 0) {
            if (!Inventory.contains("Bucket of water")) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                    {
                        put("Bucket of water", 1);
                    }
                }, null, 1, this));
                return;
            }

            Area drainArea = new Area(3225, 3497, 3227, 3491);
            if (!drainArea.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(drainArea, this));
                return;
            }

            GameObject object = GameObjects.closest(x -> x != null && x.exists()
                    && x.getName().equalsIgnoreCase("drain"));

            if (object != null) {
                if (!Inventory.use("Bucket of water") || !object.interact()) {
                    Logger.log("Failed to interact with drain");
                    return;
                }

                Sleep.sleepUntil(() -> PlayerSettings.getBitValue(2568) == 1, 5000);
            }
        } else {
            Area skeletons = new Area(
                    new Tile(3222, 9897, 0),
                    new Tile(3223, 9896, 0),
                    new Tile(3227, 9896, 0),
                    new Tile(3228, 9897, 0),
                    new Tile(3226, 9900, 0));

            if (!skeletons.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(skeletons, this));
                return;
            }

            GameObject key = GameObjects.closest(x -> x != null && x.exists() && x.getID() == 2401);
            if (key != null) {
                if (!key.interact("Take")) {
                    Logger.log("Failed to take key");
                    return;
                }

                Sleep.sleepUntil(() -> Inventory.contains(2401), 5000);
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return false;
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
