package org.lolwat.tasks.misc;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.Location;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.blastmine.BlastArea;
import org.lolwat.misc.utils.GenericUtils;

import java.util.HashMap;

public class HopperTask implements WatTask {
    private final WatTask postTask;
    private final int world;
    private final int startWorld;
    private final BlastArea area;

    @Override
    public String getName() {
        return "World Hopping";
    }

    public HopperTask(int toWorld, WatTask post, BlastArea area) {
        Logger.log("set up hopper task");
        startWorld = Worlds.getCurrentWorld();
        postTask = post;
        world = toWorld;
        this.area = area;
    }

    public HopperTask(int toWorld, WatTask post) {
        this(toWorld, post, null);
    }

    @Override
    public void execute() {
        if (postTask instanceof BondingTask || postTask instanceof MulingTask) {
            if (Bank.isOpen()) {
                Bank.close();
                Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
            }

            if (startWorld == world || Worlds.getCurrentWorld() == world) {
                TaskManager.getInstance().setCurrentTask(postTask);
                return;
            }
        }

        if (Worlds.getCurrentWorld() == startWorld) {
            if (!Tab.LOGOUT.isOpen()) {
                Tab.LOGOUT.open();
                Sleep.sleep(500, 1000);
            }

            if (!WorldHopper.isWorldHopperOpen()) {
                WorldHopper.openWorldHopper();
                Sleep.sleep(300, 800);
            }

            if (world == 0) {
                WorldHopper.hopWorld(Worlds.getRandomWorld((w) ->
                        !w.isPVP()
                                && w.isMembers()
                                && !w.isDeadmanMode()
                                && !w.isHighRisk()
                                && w.getLocation().equals(Location.USA)
                                && !w.isPvpArena() && w.isNormal()
                                && w.getMinimumLevel() <= 100));
            } else {
                WorldHopper.hopWorld(world);
            }

            Sleep.sleepUntil(() -> Worlds.getCurrentWorld() != startWorld && Client.getGameState().equals(GameState.LOGGED_IN), 10000);
        }

        if(Worlds.getCurrentWorld() != startWorld && Client.getGameState().equals(GameState.LOGGED_IN)) {
            if(area != null && GenericUtils.tooManyPlayers(area.getHopArea(), 1, true)) {
                TaskManager.getInstance().setCurrentTask(new HopperTask(0, postTask));
                return;
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
