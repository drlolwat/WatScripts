package org.lolwat.tasks.misc;

import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;

import java.util.HashMap;

public class HopperTask implements WatTask {
    private final WatTask postTask;
    private final int world;

    @Override
    public String getName() {
        return "World Hopping";
    }

    public HopperTask(int toWorld, WatTask post) {
        postTask = post;
        world = toWorld;
    }

    Area monsterArea = new Area(1289, 10100, 1296, 10093);

    @Override
    public void execute() {
        if(Players.getLocal().isInCombat()) {
            return;
        }

        GameObject gate = GameObjects.closest(34642);

        if(Worlds.getCurrentWorld() != world) {
            if(monsterArea.contains(Players.getLocal())) {
                if (gate != null) {
                    if (!gate.interact()) {
                        Logger.log("failed to interact w exit gate onHop");
                        return;
                    }

                    Sleep.sleepUntil(() -> !monsterArea.contains(Players.getLocal()) && !Players.getLocal().isInCombat(), 20000);
                }
            }

            if (!Tab.LOGOUT.isOpen()) {
                Tab.LOGOUT.open();
                Sleep.sleep(500, 1000);
            }

            if (!WorldHopper.isWorldHopperOpen()) {
                WorldHopper.openWorldHopper();
                Sleep.sleep(300, 800);
            }

            if (world == 0) {
                if (!GenericUtils.isMember()) {
                    WorldHopper.hopWorld(Worlds.getRandomWorld((w) -> !w.isPVP() && !w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && w.getMinimumLevel() <= 100));
                } else {
                    WorldHopper.hopWorld(Worlds.getRandomWorld((w) -> !w.isPVP() && w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && w.getMinimumLevel() <= 100));
                }
            } else {
                WorldHopper.hopWorld(world);
            }
        } else {
            if(!monsterArea.contains(Players.getLocal())) {
                if (gate != null) {
                    if (!gate.interact()) {
                        Logger.log("failed to interact w entry gate onHopSuccess");
                        return;
                    }

                    Sleep.sleepUntil(() -> monsterArea.contains(Players.getLocal()), 20000);
                }
            }

            TaskManager.getInstance().setCurrentTask(postTask);
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 500;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatScript instance) {

    }

    @Override
    public boolean canPerformTask() {
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

    

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
