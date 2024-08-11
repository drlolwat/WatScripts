package org.lolwat.tasks.misc;

import com.google.common.collect.Lists;
import org.dreambot.api.methods.Calculations;
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

import java.util.Collections;
import java.util.List;

public class AeglenModeTask implements WatTask {
    @Override
    public String getName() {
        return "Aeglen (Noob)";
    }

    List<String> examined;
    WatTask post;
    int secondsToRun;
    long startedAt;

    public AeglenModeTask(WatTask postTask, int seconds) {
        examined = Lists.newArrayList();
        post = postTask;
        secondsToRun = seconds;
        startedAt = System.currentTimeMillis();
    }

    @Override
    public void execute() {
        if ((System.currentTimeMillis()) - startedAt > (secondsToRun * 1000L)) {
            TaskManager.getInstance().setCurrentTask(post);
            return;
        }

        int n = Calculations.random(5);
        if(n == 1) {
            for(GameObject o : GameObjects.all(x -> x != null && x.isOnScreen() && !examined.contains(x.getName())
                    && !x.getName().equalsIgnoreCase("null"))) {

                if(!examined.contains(o.getName())) {
                    if(!o.interact("Examine")) {
                        Logger.log("Issue examining object: " + o.getName());
                    } else {
                        examined.add(o.getName());
                        break;
                    }
                }
            }

            Sleep.sleep(Calculations.random(1000, 5000));
        } else {
            List<String> openable = Collections.singletonList("Gate");
            for(GameObject o : GameObjects.all(x -> x != null && x.isOnScreen() && openable.contains(x.getName())
                    && x.hasAction("Open") && x.distance(Players.getLocal()) <= 10)) {

                if(o.interact("Open")) {
                    Sleep.sleepUntil(() -> !o.hasAction("Open"), 5000);
                    break;
                }
            }

            Area a = Players.getLocal().getTile().getArea(20);
            Tile tile = null;
            for (Tile t : a.getTiles()) {
                if(t.canReach() && !t.equals(Players.getLocal().getTile())) {
                    tile = t;
                    break;
                }
            }

            if(tile != null) {
                TaskManager.getInstance().
                        setCurrentTask(new TraversalTask(tile.getArea(1), this));
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
