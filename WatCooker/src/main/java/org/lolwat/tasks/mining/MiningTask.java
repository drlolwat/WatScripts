package org.lolwat.tasks.mining;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatConfig;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.mining.MiningUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MiningTask implements WatTask {
    private final Tile defaultSquare;
    private List<Tile> defaultRocks;
    private List<List<Tile>> alternateRockList;
    private final HashMap<String, Integer> sellingItems;
    private final String rockName;
    private final boolean usingAlternateRocks = false;
    private long lastSuccessfulRock = 0;
    private boolean gotRock = false;
    private Tile rockTile;
    private final int minLevel;
    private Integer maxMiningLevel = 0;
    private Tile lastTile; // lets try and hover the previous rock for speed

    public MiningTask(int miningLevel, int maxMining, Tile startPosition, String pRockName, HashMap<String, Integer> sellableProduct) {
        minLevel = miningLevel;
        defaultSquare = startPosition;
        rockName = pRockName;
        sellingItems = sellableProduct;
        maxMiningLevel = maxMining;
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.MINING) >= minLevel && Skills.getRealLevel(Skill.MINING) <= maxMiningLevel;
    }

    @Override
    public String getName() {
        return "Mining";
    }

    @Override
    public void execute() {
        String pickaxe = MiningUtils.getBestPickaxeForLevel();

        if (WorldHopper.isWorldHopperOpen()) {
            WorldHopper.closeWorldHopper();
        }

        if ((!Inventory.contains(pickaxe) && !Equipment.contains(pickaxe)) || (Inventory.contains(pickaxe) && Inventory.get(pickaxe).isNoted())) {
            WatConfig.incrementToolFailures();
            Logger.log("I don't own the best pickaxe available for me: " + pickaxe);
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, sellingItems, 1, this));
        } else {
            if (!Tab.INVENTORY.isOpen()) {
                Tab.INVENTORY.open();
            }

            Item old = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
            if(!Equipment.contains(pickaxe) && GenericUtils.canEquipTool(pickaxe) && GenericUtils.equipItem(pickaxe, old)) {
                Sleep.sleep(30, 90);
            }

            // Check if we need to bank, and tell the Banking task to check if we have over 1000 ore, in which case sell it.
            // We use -1000 so the script knows to check for 1000, but also to sell all of it.
            // If we checked for 1000, then it would only withdraw 1000.
            if (Inventory.isFull()) {
                Logger.log("My inventory is full, to the bank!");
                TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<>(), sellingItems, 1, this));
                lastSuccessfulRock = 0;
                return;
            }

            if (!Map.isTileOnScreen(defaultSquare)) {
                Logger.log("I need to traverse to the location.");
                TaskManager.getInstance().setCurrentTask(new TraversalTask(defaultSquare.getArea(3), this));
                return;
            }

            if (Dialogues.canContinue()) {
                Dialogues.continueDialogue();
            }

            if(GenericUtils.tooManyPlayers(3, 3)) {
                TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
                return;
            }

            if (!gotRock || (lastSuccessfulRock <= 0 || (Instant.now().getEpochSecond() - lastSuccessfulRock) > 10) || (rockTile != null && GameObjects.getTopObjectOnTile(rockTile.getTile()).getModelColors() == null)) {
                if (Players.getLocal().isMoving()) {
                    Sleep.sleep(1000, 1500);
                }

                GameObject obj = GameObjects.closest(rockName);
                if (obj != null && obj.getModelColors() != null && obj.canReach()) {
                    rockTile = obj.getTile();

                    if (!obj.isOnScreen()) {
                        Camera.rotateToEntity(obj);
                    }

                    if(!Map.isTileOnScreen(rockTile)) {
                        Camera.rotateToTile(rockTile);
                    }

                    if(!obj.interact()) {
                        Logger.error("error interacting with rock");
                    }

                    gotRock = true;
                } else {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(GameObjects.all(n -> n.exists() &&
                            n.canReach() && n.getName().equals(rockName)).get(0).getTile().getArea(3), this));

                    Logger.log("traversing to closer rocks");
                    return;
                }

                if (lastTile != null) {
                    Mouse.move(lastTile.getRandomized());
                }
            }

            Sleep.sleepUntil(() -> Dialogues.canContinue() || !gotRock || (rockTile != null && GameObjects.getTopObjectOnTile(rockTile).getModelColors() == null), 16000);
        }
    }


    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {
        gotRock = false;
        lastTile = rockTile;
        lastSuccessfulRock = Instant.now().getEpochSecond();
    }

    @Override
    public Skill trainsSkill() {
        return Skill.MINING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return maxMiningLevel;
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
                put(MiningUtils.getBestPickaxeForLevel(), 1);
            }
        };
    }

    @Override
    public List<String> inventoryTolerated() {
        return new ArrayList<String>() { { add(MiningUtils.getBestPickaxeForLevel()); } };
    }
}
