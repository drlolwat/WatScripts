package org.lolwat.tasks.woodcutting;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatConfig;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.types.mixed.TreeType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.woodcutting.WoodcuttingUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WoodcuttingTask implements WatTask {
    private final HashMap<Skill, Integer> levelRequirements = new HashMap<>();
    private final TreeType treeType;
    private final Area area;
    private final int minimumLevel;
    private final int avoidAfterLevel;
    private final HashMap<String, Integer> sellList;
    private long lastGotLog;
    private final boolean dropping;

    public WoodcuttingTask(TreeType type, Tile startingLocation, int minLevel, int maxLevel, HashMap<String, Integer> sellingList, boolean drop) {
        treeType = type;
        area = startingLocation.getArea(15);
        avoidAfterLevel = maxLevel;
        minimumLevel = minLevel;
        sellList = sellingList;
        lastGotLog = 0;
        dropping = drop;
    }

    @Override
    public void execute() {
        String hatchet = WoodcuttingUtils.getBestHatchetForLevel();
        if ((!Inventory.contains(hatchet) && !Equipment.contains(hatchet)) || (Inventory.contains(hatchet) && Inventory.get(hatchet).isNoted())) {
            WatConfig.incrementToolFailures();
            Logger.log("I don't own the best hatchet available for me: " + hatchet);
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, sellList, 1, this));
        } else {
            if (!Tab.INVENTORY.isOpen()) {
                Tab.INVENTORY.open();
            }

            Item old = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
            if (!Equipment.contains(hatchet) && GenericUtils.canEquipTool(hatchet) && GenericUtils.equipItem(hatchet, old)) {
                Sleep.sleep(30, 90);
            }

            // its all very similar to mining isn't it
            if (Inventory.isFull()) {
                if (!dropping) {
                    Logger.log("My inventory is full, to the bank!");
                    lastGotLog = 0;
                    TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<>(), sellList, 1, this));
                    return;
                } else {
                    for (Item it : Inventory.all()) {
                        if (it.getName().equals(WoodcuttingUtils.getLogName(treeType))) {
                            Inventory.drop(it.getName());
                            Sleep.sleep(800, 1200);
                        }
                    }
                }
            }

            if (!area.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
                return;
            }

            if (Players.getLocal().isAnimating())
                return;

            if (treeType.equals(TreeType.TREE) && GenericUtils.tooManyPlayers(5, 4)) {
                TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
                return;
            }

            GameObject tree = GameObjects.closest(x -> x.getName().equalsIgnoreCase(WoodcuttingUtils.getTreeName(treeType)) && area.contains(x));
            if (tree != null && tree.interact()) {
                Mouse.move();
                Sleep.sleepUntil(() -> !tree.exists() || Inventory.isFull() || Dialogues.canContinue(), treeType.equals(TreeType.TREE) ? 5000 : 60000);
            } else {
                Sleep.sleep(5000, 10000);
                if (lastGotLog > 0 && (Instant.now().getEpochSecond() - lastGotLog) > 30) {
                    TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
                    lastGotLog = 0;
                }
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
        return true;
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

    

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<String, Integer>() {
            {
                putAll(GenericUtils.getSkillingGear());
            }
        };
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {
            {
                put(WoodcuttingUtils.getBestHatchetForLevel(), 1);
            }
        };
    }

    @Override
    public List<String> inventoryTolerated() {
        return new ArrayList<String>() {
            {
                add(WoodcuttingUtils.getBestHatchetForLevel());
            }
        };
    }
}
