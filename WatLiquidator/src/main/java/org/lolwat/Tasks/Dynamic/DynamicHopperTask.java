package org.lolwat.Tasks.Dynamic;

import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.Tasks.WatTask;
import org.lolwat.WatMiner;

public class DynamicHopperTask implements WatTask {
    private final WatTask postTask;
    private final int world;

    public String getName() {
        return "World Hopping";
    }

    public DynamicHopperTask(int toWorld, WatTask post) {
        postTask = post;
        world = toWorld;
    }

    public void execute(WatMiner instance) {
        if (!Tab.LOGOUT.isOpen()) {
            Tab.LOGOUT.open();
            Sleep.sleep(500, 1000);
        }

        if (!WorldHopper.isWorldHopperOpen()) {
            WorldHopper.openWorldHopper();
            Sleep.sleep(300, 800);
        }

        if(world == 0) {
            WorldHopper.hopWorld(Worlds.getRandomWorld((w) -> !w.isPVP() && !w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && w.getMinimumLevel() <= 100));
        }
        else {
            WorldHopper.hopWorld(world);
        }

        instance.currentTask = postTask;
    }

    @Override
    public int loopTime() {
        return 500;
    }

    @Override
    public boolean hasLevelRequirements() {
        return false;
    }

    @Override
    public boolean hasQuestRequirements() {
        return false;
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }
}
