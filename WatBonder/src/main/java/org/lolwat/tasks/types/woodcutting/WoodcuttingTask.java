package org.lolwat.tasks.types.woodcutting;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.utils.types.TreeType;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.HopperTask;
import org.lolwat.tasks.types.misc.TraversalTask;
import org.lolwat.tasks.WatTask;
import org.lolwat.utils.ItemUtils;
import org.lolwat.WatAIO;

import java.time.Instant;
import java.util.HashMap;

public class WoodcuttingTask implements WatTask {
    private final HashMap<Skill, Integer> levelRequirements = new HashMap<>();
    private final TreeType treeType;
    private final Tile startLocation;
    private final int minimumLevel;
    private final int avoidAfterLevel;
    private final HashMap<String, Integer> sellList;
    private long lastGotLog;
    private final boolean dropping;

    public WoodcuttingTask(TreeType type, Tile startingLocation, int minLevel, int maxLevel, HashMap<String, Integer> sellingList, boolean drop) {
        treeType = type;
        startLocation = startingLocation;
        avoidAfterLevel = maxLevel;
        minimumLevel = minLevel;
        sellList = sellingList;
        lastGotLog = 0;
        dropping = drop;
    }

    @Override
    public void execute(WatAIO instance) {
        String hatchet = ItemUtils.getBestHatchetForLevel();

        HashMap<String, Integer> bankItems = new HashMap<String, Integer>() {
            {
                put(hatchet, 1);
            }
        };

        if(!Inventory.contains(hatchet) && !Equipment.contains(hatchet)) {
            Logger.log("I don't own the best hatchet available for me: " + hatchet);
            instance.currentTask = new BankingTask("Grabbing Hatchet", bankItems, true, this, true, null, 1);
        }
        else {
            if (!Tab.INVENTORY.isOpen()) {
                Tab.INVENTORY.open();
            }

            // its all very similar to mining isn't it
            if (Inventory.isFull()) {
                if(!dropping) {
                    Logger.log("My inventory is full, to the bank!");
                    lastGotLog = 0;
                    instance.currentTask = new BankingTask("Banking logs", bankItems, true, this, true, sellList, 1);
                    return;
                } else {
                    for(Item it : Inventory.all()) {
                        if(it.getName().equals(ItemUtils.getLogName(treeType))) {
                            Inventory.drop(it.getName());
                            Sleep.sleep(800, 1200);
                        }
                    }
                }
            }

            if (!Map.isTileOnMap(startLocation)) {
                instance.currentTask = new TraversalTask(startLocation, false, this);
                return;
            }

            if(lastGotLog > 0 && (Instant.now().getEpochSecond() - lastGotLog) > 30) {
                instance.currentTask = new HopperTask(0, this);
                lastGotLog = 0;
                return;
            }

            GameObject tree = GameObjects.closest(ItemUtils.getTreeName(treeType));
            if(tree != null && tree.interact()) {
                Mouse.moveOutsideScreen();
                Sleep.sleepUntil(() -> !tree.exists() || Inventory.isFull() || Dialogues.canContinue(), treeType.equals(TreeType.TREE) ? 5000 : 30000);
            }
        }
    }

    @Override
    public String getName() {
        return "Chopping " + treeType.toString().toLowerCase() + "s";
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.WOODCUTTING) >= minimumLevel;
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
        lastGotLog = Instant.now().getEpochSecond();
    }

    @Override
    public Skill trainsSkill() {
        return Skill.WOODCUTTING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAfterLevel;
    }
}
