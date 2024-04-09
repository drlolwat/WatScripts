package org.lolwat.tasks.misc;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.WatAIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScavengingTask implements WatTask {
    private final Area scavengingZone = new Area(
            new Tile(3154, 3478, 0),
            new Tile(3175, 3478, 0),
            new Tile(3175, 3503, 0),
            new Tile(3154, 3503, 0));

    private final List<String> avoidItems;
    private List<String> itemsTaken;
    private int maxScavenge = 50000;

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
    }

    @Override
    public void execute() {
        if(!scavengingZone.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(scavengingZone, this));
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

        if(Inventory.isFull() || worth > maxScavenge) {
            itemsTaken.clear();
            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<>(), sellList, 1, (worth > maxScavenge) ? null : this));
            return;
        }

        for(GroundItem item : GroundItems.all(x -> !avoidItems.contains(x.getName().toLowerCase()) && scavengingZone.contains(x))) {
            if(item == null || !item.exists()) {
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
}
