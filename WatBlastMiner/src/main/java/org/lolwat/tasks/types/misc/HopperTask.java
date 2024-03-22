package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

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

    @Override
    public void execute(WatAIO instance) {
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
                WorldHopper.hopWorld(Worlds.getRandomWorld((w) -> !w.isPVP() && !w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && w.getMinimumLevel() <= 100));
            } else {
                WorldHopper.hopWorld(Worlds.getRandomWorld((w) -> !w.isPVP() && w.isMembers() && !w.isDeadmanMode() && !w.isHighRisk() && w.getMinimumLevel() <= 100));
            }
        }
        else {
            WorldHopper.hopWorld(world);
        }

        TaskManager.getInstance().setCurrentTask(postTask);
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
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return null;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return null;
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
