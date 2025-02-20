package org.lolwat.tasks.woodcutting;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.misc.types.mixed.TreeType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.woodcutting.WoodcuttingUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.WalkingTask;

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
    private HashMap<String, Integer> sellList;
    private long lastGotLog;
    private final boolean dropping;
    private final boolean moneyMaking;
    private final HashMap<String, Object> data;
    private boolean gotLog = false;

    public WoodcuttingTask(TreeType type, Area startingLocation, int minLevel, int maxLevel, HashMap<String, Integer> sellingList, boolean drop) {
        treeType = type;
        area = startingLocation;
        avoidAfterLevel = maxLevel;
        minimumLevel = minLevel;
        sellList = sellingList;
        lastGotLog = 0;
        dropping = drop;
        moneyMaking = false;
        data = new HashMap<>();
    }

    @Override
    public void execute() {
        if(data.containsKey("gp_to_generate")) {
            int logsNeeded = (int)data.get("gp_to_generate") / LivePrices.get(WoodcuttingUtils.getLogName(treeType));

            if(Bank.isOpen()) {
                int coins = (Bank.contains("Coins") ? Bank.count("Coins") : 0)
                        + (Inventory.contains("Coins") ? Inventory.count("Coins") : 0);

                if(coins >= (int)data.get("gp_to_generate")) {
                    WatTask t = (WatTask)data.get("previous_task");
                    data.remove("gp_to_generate");
                    data.remove("previous_task");
                    TaskManager.getInstance().setCurrentTask(t);
                    return;
                }

                Logger.log("We need to generate " + data.get("gp_to_generate") + "gp, which is " + logsNeeded + " logs");
            }

            sellList = new HashMap<String, Integer>() {
                {
                    put(WoodcuttingUtils.getLogName(treeType), -logsNeeded);
                }
            };
        }

        String hatchet = WoodcuttingUtils.getBestHatchetForLevel();
        if ((!Inventory.contains(hatchet) && !Equipment.contains(hatchet)) || (Inventory.contains(hatchet) && Inventory.get(hatchet).isNoted())) {
            ConfigManager.getInstance().incrementToolFailures();
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
                        if(it == null) continue;
                        if (it.getName().equals(WoodcuttingUtils.getLogName(treeType))) {
                            int logCount = Inventory.count(it.getName());
                            if(!Inventory.drop(it.getName())) {
                                Logger.log("Failed to drop log");
                                continue;
                            }

                            Sleep.sleepUntil(() -> Inventory.count(it.getName()) < logCount, 2000);
                        }
                    }
                }
            }

            if (!area.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new WalkingTask(area, this));
                return;
            }

            if (Players.getLocal().isAnimating())
                return;

            if (treeType.equals(TreeType.TREE) && GenericUtils.tooManyPlayers(5, 4)) {
                TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
                return;
            }

            GameObject tree = GameObjects.closest(x ->
                    (x.getName().equalsIgnoreCase(WoodcuttingUtils.getTreeName(treeType)) || treeType.equals(TreeType.TREE) && x.getName().contains("Evergreen"))
                            && area.contains(x));
            if (tree != null && tree.interact()) {
                gotLog = false;
                Sleep.sleepUntil(() -> (treeType.equals(TreeType.TREE) && gotLog) ||
                        (!tree.exists() || Inventory.isFull() || Dialogues.canContinue()),
                        treeType.equals(TreeType.TREE) ? 5000 : 60000);
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
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.WOODCUTTING) >= minimumLevel;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {
        lastGotLog = Instant.now().getEpochSecond();
        gotLog = true;
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
    public HashMap<WatItem, Integer> loadout() {
        return new HashMap<WatItem, Integer>() {
            {
                if(!moneyMaking) {
                    putAll(GenericUtils.getSkillingGear());
                }
            }
        };
    }

    @Override
    public HashMap<String, Integer> inventory() {
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

    @Override
    public HashMap<String, Object> data() {
        return data;
    }
}
