package org.lolwat.Tasks.Dynamic;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.lolwat.Tasks.WatTask;
import org.lolwat.WatAIO;

import java.time.Instant;

public class DynamicTraversalTask implements WatTask {
    WatTask postTask;
    boolean mustBeOnTile;
    Tile target;
    double lastWalk;

    @Override
    public String getName() {
        return "Traversing Map";
    }

    public DynamicTraversalTask(Tile tile, boolean tileOnly, WatTask post) {
        target = tile;
        mustBeOnTile = tileOnly;
        postTask = post;
        lastWalk = 0;
    }

    @Override
    public void execute(WatAIO instance) {
        if(Players.getLocal().getTile().equals(target) || (!mustBeOnTile && target.distance() <= 5)) {
            Logger.log("Reached target: X:" + target.getX() + ", Y:" + target.getY());
            instance.currentTask = postTask;
        }
        else {
            if(Walking.getDestinationDistance() <= 5 || (lastWalk > 0 && (Instant.now().getEpochSecond() - lastWalk) >= 2)) {
                Walking.walk(target);
                lastWalk = Instant.now().getEpochSecond();
            }
            //Sleep.sleepUntil(()-> Walking.getDestinationDistance() <= 5, Calculations.random(3000, 4500));
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
}
