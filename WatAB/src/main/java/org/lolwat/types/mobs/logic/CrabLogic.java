package org.lolwat.types.mobs.logic;

import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.MobLogic;
import org.lolwat.types.mobs.Mob;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class CrabLogic implements MobLogic {
    List<Integer> rockIds = Arrays.asList(101, 103);

    @Override
    public HashMap<WatItem, Integer> inventoryLoadout() {
        return new HashMap<WatItem, Integer>() {
            {
                put(ItemManager.getInstance().getItem("Lobster"), 20);
                put(ItemManager.getInstance().getItem("Camelot teleport"), 2);
                put(ItemManager.getInstance().getItem("Varrock teleport"), 2);
            }
        };
    }

    @Override
    public void execute(Mob mob, Skill skill) {
        if(!Players.getLocal().isInCombat() && !Players.getLocal().isHealthBarVisible()) {
            Collections.shuffle(rockIds);
            for(int i : rockIds) {
                NPC rock = NPCs.closest(x -> x != null && x.exists() && x.getID() == i);
                if(rock != null) {
                    if(Walking.shouldWalk(5)) {
                        Walking.walk(rock.getTile());

                        Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 15000);

                        if(!Players.getLocal().isInCombat()) {
                            TaskManager.getInstance().setCurrentTask(new WalkingTask(new Area(
                                    new Tile(2708, 3663, 0),
                                    new Tile(2717, 3667, 0),
                                    new Tile(2725, 3665, 0),
                                    new Tile(2727, 3658, 0),
                                    new Tile(2714, 3650, 0)), TaskManager.getInstance().getCurrentTask()));
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
