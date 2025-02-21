package org.lolwat.tasks.misc;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.types.interfaces.WatTask;

public class HopperTask implements WatTask {
    private final WatTask postTask;
    private final int world;


    public HopperTask(int toWorld, WatTask post) {
        postTask = post;
        world = toWorld;
    }

    @Override
    public void execute() {
        if(Players.getLocal().isInCombat()) {
            return;
        }

        if (!Tab.LOGOUT.isOpen()) {
            Tab.LOGOUT.open();
            Sleep.sleep(500, 1000);
        }

        if (!WorldHopper.isWorldHopperOpen()) {
            WorldHopper.openWorldHopper();
            Sleep.sleep(300, 800);
        }

        if(world == 0) {
            if(!GenericUtils.isMember()) {
                WorldHopper.hopWorld(Worlds.getRandomWorld((w) -> !w.isPVP() && !w.isMembers() && w.isNormal() && !w.isDeadmanMode() && !w.isHighRisk() && w.getMinimumLevel() <= 100));
            } else {
                WorldHopper.hopWorld(Worlds.getRandomWorld((w) -> !w.isPVP() && w.isNormal() && w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && w.getMinimumLevel() <= 100));
            }
        }
        else {
            WorldHopper.hopWorld(world);
        }

        Sleep.sleep(1000, 3000);
        TaskManager.getInstance().setCurrentTask(postTask);
    }

    @Override
    public int loopTime() {
        return 500;
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
}
