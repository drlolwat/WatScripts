package org.lolwat.tasks.fishing;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.types.mixed.FishType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.fishing.FishingUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;
import org.lolwat.managers.types.WatTask;
import org.lolwat.WatAIO;

import java.time.Instant;
import java.util.HashMap;

public class FishingTask implements WatTask {
    private final FishType fishType;
    private final Area area;
    private final HashMap<String, Integer> sellingItems;
    private final int minimumLevel;
    private final int maximumLevel;
    private long lastCatch;
    private Tile currentSpot;

    public FishingTask(FishType type, int minLevel, int maxLevel, Tile startingLocation, HashMap<String, Integer> sellItems) {
        fishType = type;
        area = startingLocation.getArea(10);
        sellingItems = sellItems;
        minimumLevel = minLevel;
        maximumLevel = maxLevel;
    }

    @Override
    public String getName() {
        return "Fishing " + fishType.toString().toLowerCase();
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.FISHING) >= minimumLevel && Skills.getRealLevel(Skill.FISHING) <= maximumLevel;
    }

    @Override
    public void execute() {
        String tool = FishingUtils.getToolByFishType(fishType);
        HashMap<String, Integer> requiredItems = new HashMap<String, Integer>() {{ put(tool, 1); }};

        if(!FishingUtils.getExtraFishingItems(fishType).isEmpty()) {
            requiredItems.putAll(FishingUtils.getExtraFishingItems(fishType));
        }

        boolean hasItems = true;
        for(String name : requiredItems.keySet()) {
            if(!Inventory.contains(name)) {
                hasItems = false;
                break;
            }
        }

        if(!hasItems) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(requiredItems, sellingItems, 1, this));
        }
        else {
            if (!Tab.INVENTORY.isOpen()) {
                Tab.INVENTORY.open();
            }

            if(Inventory.isFull()) {
                Logger.log("My inventory is full, to the bank!");
                TaskManager.getInstance().setCurrentTask(new BankingTask(requiredItems, sellingItems, 1, this));
                return;
            }

            if(Dialogues.canContinue()) {
                Dialogues.continueDialogue();
                Sleep.sleep(100, 300);
            }

            if(!area.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
                return;
            }

            if(currentSpot == null || getNpcOnTile(currentSpot) == null) {
                currentSpot = NPCs.closest(n -> n != null && n.getTile() != null && n.hasAction(FishingUtils.getMenuItemByFishType(fishType))).getTile();
                if(currentSpot == null)
                    return;
            }

            if(currentSpot != null && getNpcOnTile(currentSpot) != null) {
                if(getNpcOnTile(currentSpot) != null && getNpcOnTile(currentSpot).interact(FishingUtils.getMenuItemByFishType(fishType))) {
                    Sleep.sleep(1200, 2000);
                    GenericUtils.moveMouseInOrOut();
                    lastCatch = Instant.now().getEpochSecond();
                    Sleep.sleepUntil(() -> getNpcOnTile(currentSpot) == null || !getNpcOnTile(currentSpot).exists() || (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) || Inventory.isFull() || Dialogues.canContinue() || !hasRequiredItems(), 60000);
                }
            }
        }
    }

    private NPC getNpcOnTile(Tile tile) {
        return NPCs.closest(n -> n.getName().contains("Fishing") && n.getTile().equals(tile));
    }

    private boolean hasRequiredItems() {
        for(String it : FishingUtils.getExtraFishingItems(fishType).keySet()) {
            if(!Inventory.contains(it)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 650;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {
        lastCatch = Instant.now().getEpochSecond();
    }

    @Override
    public Skill trainsSkill() {
        return Skill.FISHING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return maximumLevel;
    }

    

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
