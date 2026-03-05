package org.lolwat.tasks.misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.NumUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ScavengingTask implements WatTask {
    private final Area scavengingZone = new Area(
            new Tile(3154, 3478, 0),
            new Tile(3175, 3478, 0),
            new Tile(3175, 3503, 0),
            new Tile(3154, 3503, 0));

    private final List<String> avoidItems;
    private final List<String> itemsTaken;
    private final HashMap<String, Object> data;
    private final int originalWorld;
    private final List<Integer> acceptableWorlds;

    @Override
    public String getName() {
        return "Scavenging";
    }

    public ScavengingTask() {
        avoidItems = new ArrayList<String>() {
            {
                add("ashes");
                add("bones");
            }
        };

        itemsTaken = new ArrayList<>();
        data = new HashMap<>();
        originalWorld = Worlds.getCurrentWorld();
        acceptableWorlds = Arrays.asList(301, 308);
    }

    @Override
    public void execute() {
        if(!scavengingZone.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(scavengingZone, this));
            return;
        }

        if(!acceptableWorlds.contains(Worlds.getCurrentWorld())) {
            TaskManager.getInstance().setCurrentTask(new HopperTask(301, this));
            return;
        }

        HashMap<String, Integer> sellList = new HashMap<>();
        int worth = 0;

        for(Item i : Inventory.all()) {
            if(i == null) {
                continue;
            }

            if(itemsTaken.contains(i.getName())) {
                worth += LivePrices.get(i);
                sellList.put(i.getName(), Inventory.count(i.getName()));
            } else {
                sellList.remove(i.getName());
            }
        }

        int maxScavenge = data().containsKey("gp_to_generate") ? (int)data().get("gp_to_generate") : 50000;
        if(Inventory.isFull() || worth > maxScavenge) {
            itemsTaken.clear();
            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<>(), sellList, 1,
                    (worth > maxScavenge) ? new HopperTask(originalWorld, null) : this));
            return;
        }

        for(GroundItem item : GroundItems.all(x -> !avoidItems.contains(x.getName().toLowerCase()) && scavengingZone.contains(x))) {
            if(item == null || !item.exists() || NumUtils.getItemPrice(item.getName()) < 100) {
                continue;
            }

            if(item.interact("Take")) {
                Sleep.sleepUntil(() -> !item.exists(), 5000);
                itemsTaken.add(item.getName());
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
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

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

    @Override
    public HashMap<String, Object> data() {
        return data;
    }
}
