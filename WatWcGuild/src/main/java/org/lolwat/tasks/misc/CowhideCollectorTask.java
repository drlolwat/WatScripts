package org.lolwat.tasks.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.GroundItem;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;

import java.util.HashMap;

public class CowhideCollectorTask implements WatTask {
    private final Area cowPastureEast = new Area(
            new Tile(3253, 3255, 0),
            new Tile(3253, 3272, 0),
            new Tile(3251, 3274, 0),
            new Tile(3251, 3276, 0),
            new Tile(3249, 3278, 0),
            new Tile(3248, 3279, 0),
            new Tile(3245, 3279, 0),
            new Tile(3244, 3280, 0),
            new Tile(3244, 3281, 0),
            new Tile(3240, 3285, 0),
            new Tile(3240, 3287, 0),
            new Tile(3241, 3288, 0),
            new Tile(3241, 3289, 0),
            new Tile(3242, 3290, 0),
            new Tile(3242, 3293, 0),
            new Tile(3241, 3294, 0),
            new Tile(3241, 3295, 0),
            new Tile(3240, 3296, 0),
            new Tile(3240, 3298, 0),
            new Tile(3241, 3299, 0),
            new Tile(3256, 3299, 0),
            new Tile(3257, 3300, 0),
            new Tile(3260, 3300, 0),
            new Tile(3261, 3299, 0),
            new Tile(3263, 3299, 0),
            new Tile(3265, 3297, 0),
            new Tile(3265, 3255, 0));
    private final Area cowPastureWest = new Area(
            new Tile(3211, 3284, 0),
            new Tile(3195, 3284, 0),
            new Tile(3193, 3286, 0),
            new Tile(3193, 3301, 0),
            new Tile(3197, 3303, 0),
            new Tile(3200, 3302, 0),
            new Tile(3205, 3302, 0),
            new Tile(3208, 3303, 0),
            new Tile(3210, 3302, 0),
            new Tile(3210, 3297, 0),
            new Tile(3211, 3296, 0),
            new Tile(3213, 3292, 0));
    private final Area lumbridgeBankArea = new Area(new Tile(3207, 3220, 2), new Tile(3210, 3210, 2));
    private long lastCowhideCollectionTime;
    private boolean hasCollectedCowhide = false;
    private boolean hasHopped = false;

    @Override
    public void execute() {
        if (Inventory.isFull()) {
            depositCowhides();
            lastCowhideCollectionTime = 0;
            hasCollectedCowhide = false;
            hasHopped = false;
        } else {
            Area nearestPasture = cowPastureEast.getCenter().distance(Players.getLocal()) < cowPastureWest.getCenter().distance(Players.getLocal()) ? cowPastureEast : cowPastureWest;
            if (nearestPasture.contains(Players.getLocal())) {
                if (hasCollectedCowhide && !hasHopped && System.currentTimeMillis() - lastCowhideCollectionTime > 2 * 60 * 1000) { // 2 minutes in milliseconds
                    hopWorld();
                    hasHopped = true;
                } else {
                    collectCowhides();
                }
            } else {
                Walking.walk(nearestPasture.getCenter());
                Sleep.sleepUntil(() -> nearestPasture.contains(Players.getLocal()), 5000, 600);
            }
        }
    }

    private void collectCowhides() {
        Area nearestPasture = cowPastureEast.getCenter().distance(Players.getLocal()) < cowPastureWest.getCenter().distance(Players.getLocal()) ? cowPastureEast : cowPastureWest;
        if (!nearestPasture.contains(Players.getLocal())) {
            Walking.walk(nearestPasture.getCenter());
            Sleep.sleepUntil(() -> nearestPasture.contains(Players.getLocal()), 5000, 600);
        } else {
            GroundItem cowhide = GroundItems.closest("Cowhide");
            if (cowhide != null && cowhide.interact("Take")) {
                Sleep.sleepUntil(() -> !cowhide.exists() || Inventory.isFull(), 5000, 600);
                lastCowhideCollectionTime = System.currentTimeMillis();
                hasCollectedCowhide = true;
            }
        }
    }

    private void depositCowhides() {
        if (Bank.open()) {
            Sleep.sleepUntil(Bank::isOpen, 5000, 600);
            Bank.depositAllItems();
            Sleep.sleepUntil(() -> Inventory.emptySlotCount() == 28, 5000, 600);
            Bank.close();

            if (!cowPastureEast.contains(Players.getLocal()) && !cowPastureWest.contains(Players.getLocal())) {
                hasCollectedCowhide = false;
            }
        }
    }

    private void hopWorld() {
        TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
    }

    @Override
    public String getName() {
        return "Cowhide Collector";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return Calculations.random(600, 1200);
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {
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