package org.lolwat.Tasks.Types.Fishing;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.Enums.FishType;
import org.lolwat.Tasks.Types.Misc.BankingTask;
import org.lolwat.Tasks.Types.Misc.TraversalTask;
import org.lolwat.Tasks.WatTask;
import org.lolwat.Utils.SkillUtils;
import org.lolwat.WatAIO;

import java.time.Instant;
import java.util.HashMap;

public class FishingTask implements WatTask {
    private FishType fishType;
    private Tile startTile;
    private HashMap<String, Integer> sellingItems;
    private int minimumLevel;
    private int maximumLevel;
    private long lastCatch;
    private NPC currentSpot;

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
        return Skills.getRealLevel(Skill.FISHING) > minimumLevel;
    }

    @Override
    public void execute(WatAIO instance) {
        String tool = SkillUtils.getToolByFishType(fishType);
        HashMap<String, Integer> requiredItems = new HashMap<String, Integer>() {{ put(tool, 1); }};

        if(SkillUtils.getExtraFishingItems(fishType).size() > 0) {
            requiredItems.putAll(SkillUtils.getExtraFishingItems(fishType));
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

            if(currentSpot == null || !currentSpot.exists()) {
                currentSpot = NPCs.closest(n -> n != null && n.hasAction(SkillUtils.getMenuItemByFishType(fishType)));
            }

            if(currentSpot != null && currentSpot.exists()) {
                if(currentSpot.interact(SkillUtils.getMenuItemByFishType(fishType))) {
                    Sleep.sleep(1200, 2000);
                    Mouse.moveOutsideScreen();
                    lastCatch = Instant.now().getEpochSecond();
                    Sleep.sleepUntil(() -> !currentSpot.exists() || Inventory.isFull() || Dialogues.canContinue() || !Players.getLocal().isAnimating() || (Instant.now().getEpochSecond() - lastCatch) >= 45, 45000); // future check for no bait
                }
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return false;
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
}
