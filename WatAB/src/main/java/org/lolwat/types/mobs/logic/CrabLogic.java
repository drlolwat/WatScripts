package org.lolwat.types.mobs.logic;

import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.types.interfaces.MobLogic;
import org.lolwat.types.mobs.Mob;

import java.util.Arrays;
import java.util.List;

public class CrabLogic implements MobLogic {
    List<Integer> rockIds = Arrays.asList(101, 103);

    @Override
    public void execute(Mob mob, Skill skill) {
        if(!Players.getLocal().isInCombat() && !Players.getLocal().isHealthBarVisible()) {
            for(int i : rockIds) {
                NPC rock = NPCs.closest(x -> x != null && x.exists() && x.getID() == i);
                if(rock != null) {
                    if(Walking.shouldWalk(5)) {
                        Walking.walk(rock.getTile());

                        Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 15000);

                        if(!Players.getLocal().isInCombat()) {
                            TaskManager.getInstance().setCurrentTask(new WalkingTask(new Area(
                                    new Tile(2773, 10163, 0),
                                    new Tile(2774, 10160, 0),
                                    new Tile(2776, 10161, 0),
                                    new Tile(2776, 10162, 0),
                                    new Tile(2775, 10163, 0),
                                    new Tile(2774, 10164, 0)), TaskManager.getInstance().getCurrentTask()));
                            return;
                        } else {
                            break;
                        }
                    }
                }
            }
        }
    }
}
