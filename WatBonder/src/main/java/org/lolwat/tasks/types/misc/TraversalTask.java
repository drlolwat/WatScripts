package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.time.Instant;

public class TraversalTask implements WatTask {
    WatTask postTask;
    boolean mustBeOnTile;
    Tile target;
    double lastWalk;
    boolean usingArea;
    Area area;

    @Override
    public String getName() {
        return "Traversing";
    }

    public TraversalTask(Tile tile, boolean tileOnly, WatTask post) {
        target = tile;
        mustBeOnTile = tileOnly;
        postTask = post;
        lastWalk = 0;
        usingArea = false;
    }

    public TraversalTask(Area using, WatTask post) {
        area = using;
        postTask = post;
        usingArea = true;
        lastWalk = 0;
    }

    @Override
    public void execute(WatAIO instance) {
        boolean completedTile = !mustBeOnTile || Players.getLocal().getTile().equals(target);

        if(!usingArea) {
            if (completedTile && Map.isTileOnMap(target)) {
                if (!Map.isTileOnScreen(target)) {
                    Camera.rotateToTile(target);
                    Sleep.sleepUntil(() -> Map.isTileOnScreen(target), 3000);
                }

                Logger.log("Reached target: X:" + target.getX() + ", Y:" + target.getY());
                instance.currentTask = postTask;
                return;
            }
        } else {
            if(area.contains(Players.getLocal())) {
                Logger.log("Reached target area");
                instance.currentTask = postTask;
                return;
            }
        }

        if (Walking.getDestinationDistance() <= 5 || (lastWalk > 0 && (Instant.now().getEpochSecond() - lastWalk) >= (Walking.isRunEnabled() ? 1 : 2))) {
            Walking.walk(usingArea ? area.getRandomTile() : target);
            lastWalk = Instant.now().getEpochSecond();
        }

    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 400;
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
}
