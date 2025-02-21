package org.lolwat.tasks.firemaking;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.types.mixed.TreeType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.misc.utils.firemaking.FiremakingUtils;
import org.lolwat.misc.utils.woodcutting.WoodcuttingUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class FiremakingTask implements WatTask {
    private final Area selectedLocation;
    private final TreeType logType;
    private final int minLevel;
    private final int avoidAtLevel;
    private final HashMap<String, Integer> toSell;
    private final int inventoryLoads;

    public FiremakingTask(TreeType type, int firemakingLevel, int pAvoidAtLevel, int totalInventories, HashMap<String, Integer> sellList) {
        List<Area> areas = Arrays.asList(
                new Area(3149, 3475, 3176, 3478),
                new Area(3180, 3501, 3148, 3507));

        minLevel = firemakingLevel;
        selectedLocation = areas.get(new Random().nextInt(areas.size()));
        logType = type;
        avoidAtLevel = pAvoidAtLevel;
        toSell = sellList;
        inventoryLoads = Calculations.random(5, totalInventories);
    }

    @Override
    public void execute() {
        if(!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
        }

        if(!ItemUtils.hasInventory()) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(
                    FiremakingUtils.getMaterialsForFiremaking(logType, true, 1),
                    toSell, inventoryLoads, this));
            return;
        }

        if(!selectedLocation.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new WalkingTask(selectedLocation, this));
            return;
        }

        if(getFireOnPlayer()) {
            TaskManager.getInstance().setCurrentTask(new WalkingTask(selectedLocation.getRandomTile().getArea(3), this));
            return;
        }

        if(Dialogues.canContinue()) {
            Dialogues.continueDialogue();
            Sleep.sleep(100, 400);
        }

        if(Inventory.contains("Tinderbox") && Inventory.use("Tinderbox")) {
            Tile t = Players.getLocal().getTile();
            if(Inventory.contains(WoodcuttingUtils.getLogName(logType)) && Inventory.use(WoodcuttingUtils.getLogName(logType))) {
                Sleep.sleepUntil(() -> Dialogues.canContinue() || (Players.getLocal().getTile() != t && !Players.getLocal().isMoving() && !Players.getLocal().isAnimating()), 10000);
            } else {
                Logger.log("Failed to use tinderbox on log");
            }
        } else {
            Logger.log("Failed to select tinderbox");
        }
    }

    public static boolean getFireOnPlayer() {
        GameObject fire = GameObjects.closest("Fire");
        return fire != null && fire.getTile().equals(Players.getLocal().getTile());
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.FIREMAKING) >= minLevel && Skills.getRealLevel(Skill.FIREMAKING) <= avoidAtLevel;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.FIREMAKING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAtLevel;
    }

    @Override
    public HashMap<WatItem, Integer> loadout() {
        return GenericUtils.getSkillingGear();
    }
}
