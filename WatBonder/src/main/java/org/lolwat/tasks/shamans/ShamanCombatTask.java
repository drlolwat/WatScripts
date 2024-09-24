package org.lolwat.tasks.shamans;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.HashMap;
import java.util.Map;

public class ShamanCombatTask implements WatTask {
    @Override
    public String getName() {
        return "Killing Shamans";
    }

    @Override
    public void execute() {
        for(Map.Entry<String, Integer> entry : inventoryRequired().entrySet()) {
            if(!ItemUtils.inventoryContains(entry.getKey(), entry.getValue(), false)) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryRequired(), null, 1, this, null));
                return;
            }
        }

        for(Map.Entry<String, Integer> entry : clothesRequired().entrySet()) {
            if(!ItemUtils.equipmentContains(entry.getKey(), entry.getValue())) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryRequired(), null, 1, this, null));
                return;
            }
        }

        Area monsterArea = new Area(1289, 10100, 1296, 10093);
        if(!monsterArea.contains(Players.getLocal())) {
            Area lowerLanding = new Tile(1312, 10086).getArea(3);
            if (!lowerLanding.contains(Players.getLocal())) {
                Area topEntrance = new Tile(1313, 3683, 0).getArea(2);
                if (!topEntrance.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(topEntrance, this));
                    return;
                }

                GameObject entrance = GameObjects.closest(34405);
                if(entrance != null) {
                    if(!entrance.interact()) {
                        Logger.log("failed to interact w entrance");
                        return;
                    }
                }

                Sleep.sleepUntil(() -> lowerLanding.contains(Players.getLocal()), 5000);
            }

            GameObject gate = GameObjects.closest(34642);
            if(gate != null) {
                if(!gate.interact()) {
                    Logger.log("failed to interact w gate");
                    return;
                }

                Sleep.sleepUntil(() -> monsterArea.contains(Players.getLocal()), 15000);
            }

            return;
        }

        if(ItemUtils.inventoryContains("Maple logs", 1, false)) {
            if(!Inventory.drop("Maple logs")) {
                Logger.log("didnt drop");
                return;
            }

            Sleep.sleepUntil(() -> !Inventory.contains("Maple logs"), 3000);
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
        return Skill.RANGED;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Ring of wealth (", 1);
        return ret;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Maple logs", 1);
        return ret;
    }

    @Override
    public boolean requiresMembers() {
        return true;
    }
}
