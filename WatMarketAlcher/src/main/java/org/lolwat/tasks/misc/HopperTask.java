package org.lolwat.tasks.misc;

import org.dreambot.api.Client;
import org.dreambot.api.data.GameState;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;

public class HopperTask implements WatTask {
    private final WatTask postTask;
    private final int world;
    private final int startWorld;

    @Override
    public String getName() {
        return "World Hopping";
    }

    public HopperTask(int toWorld, WatTask post) {
        Logger.log("set up hopper task");
        startWorld = Worlds.getCurrentWorld();
        postTask = post;
        world = toWorld;
    }

    @Override
    public void execute() {
        if (Bank.isOpen()) {
            if (!Bank.close()) {
                Logger.error("problem closing bank during hop");
                return;
            }

            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
        }

        if (GrandExchange.isOpen()) {
            if (!GrandExchange.close()) {
                Logger.error("problem closing GE during hop");
                return;
            }

            Sleep.sleepUntil(() -> !GrandExchange.isOpen(), 5000);
        }

        if (startWorld == world || Worlds.getCurrentWorld() == world) {
            Logger.log("no need to hop, already here");
            TaskManager.getInstance().setCurrentTask(postTask);
            return;
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
                                && !w.isPvpArena() && w.isNormal()
                                && w.getMinimumLevel() <= 100));
            } else {
                WorldHopper.hopWorld(world);
            }

            Sleep.sleepUntil(() -> Worlds.getCurrentWorld() != startWorld && Client.getGameState().equals(GameState.LOGGED_IN), 10000);
        } else {
            if (Client.getGameState().equals(GameState.LOGGED_IN)) {
                TaskManager.getInstance().setCurrentTask(postTask);
            } else {
                if (Client.getGameState().equals(GameState.LOADING)) {
                    Logger.log("hopper still loading");
                }
            }
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
