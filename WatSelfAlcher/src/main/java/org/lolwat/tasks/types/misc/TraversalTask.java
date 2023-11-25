package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TraversalTask implements WatTask {
    WatTask postTask;
    boolean mustBeOnTile;
    Tile target;
    double lastWalk;
    boolean usingArea;
    Area area;

    @Override
    public String getName() {
        if(postTask != null) {
            return postTask.getName();
        }

        return "Traversing";
    }

    public TraversalTask(Tile tile, boolean tileOnly, WatTask post) {
        target = tile;
        mustBeOnTile = tileOnly;
        postTask = post;
        lastWalk = 0;
        usingArea = false;

        Logger.log("Traversing to coords");
    }

    public TraversalTask(Area using, WatTask post) {
        area = using;
        postTask = post;
        usingArea = true;
        lastWalk = 0;

        Logger.log("Traversing to area");
    }

    @Override
    public void execute(WatAIO instance) {
        if (TutorialUtils.needsOpenTab()) {
            TutorialUtils.handleTab();
        }

        boolean completedTile = !mustBeOnTile || Players.getLocal().getTile().equals(target);

        List<String> types = Arrays.asList("Web");
        for (String t : types) {
            if(GameObjects.closest(t) != null && GameObjects.closest(t).distance(Players.getLocal().getTile()) <= 10 && GameObjects.closest(t).interact()) {
                Logger.log("Traversal: slashed " + t);
                Sleep.sleepUntil(() -> GameObjects.closest(t) == null || !GameObjects.closest(t).exists(), 5000);
                return;
            }
        }

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

        if (Walking.shouldWalk(5) || (lastWalk > 0 && (Instant.now().getEpochSecond() - lastWalk) >= (Walking.isRunEnabled() ? 1 : 2))) {
            if(target == null && usingArea)
                Walking.walk(area);
            else
                Walking.walk(target);

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

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

}
