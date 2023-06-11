package org.lolwat.tasks.types.fishing;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.misc.types.mixed.FishType;
import org.lolwat.misc.utils.fishing.FishingUtils;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.time.Instant;
import java.util.HashMap;

public class FishingTask implements WatTask {
    private final FishType fishType;
    private final Tile startTile;
    private final HashMap<String, Integer> sellingItems;
    private final int minimumLevel;
    private final int maximumLevel;
    private long lastCatch;
    private Tile currentSpot;

    public FishingTask(FishType type, int minLevel, int maxLevel, Tile startingLocation, HashMap<String, Integer> sellItems) {
        fishType = type;
        startTile = startingLocation;
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
        return Skills.getRealLevel(Skill.FISHING) >= minimumLevel;
    }

    @Override
    public void execute(WatAIO instance) {
        String tool = FishingUtils.getToolByFishType(fishType);
        HashMap<String, Integer> requiredItems = new HashMap<String, Integer>() {{ put(tool, 1); }};

        if(FishingUtils.getExtraFishingItems(fishType).size() > 0) {
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
            instance.currentTask = new BankingTask("Grabbing tool", requiredItems, true, this, true, sellingItems, 1);
        }
        else {
            if (!Tab.INVENTORY.isOpen()) {
                Tab.INVENTORY.open();
            }

            if(Inventory.isFull()) {
                Logger.log("My inventory is full, to the bank!");
                instance.currentTask = new BankingTask("Banking fish", requiredItems, true, this, false, sellingItems, 1);
                return;
            }

            if(!Map.isTileOnMap(startTile)) {
                instance.currentTask = new TraversalTask(startTile, false, this);
                return;
            }

            if(Map.isTileOnMap(startTile) && !Map.isTileOnScreen(startTile)) {
                Camera.rotateToTile(startTile);
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
                    Mouse.moveOutsideScreen();
                    lastCatch = Instant.now().getEpochSecond();
                    Sleep.sleepUntil(() -> getNpcOnTile(currentSpot) == null || Inventory.isFull() || Dialogues.canContinue() || !hasRequiredItems(), 45000);
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
    public Quest completesQuest() {
        return null;
    }
}
