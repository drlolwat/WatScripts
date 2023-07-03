package org.lolwat.tasks.types.mining;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.lolwat.misc.utils.mining.MiningUtils;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.HopperTask;
import org.lolwat.tasks.types.misc.TraversalTask;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MiningTask implements WatTask {
    private Tile defaultSquare;
    private List<Tile> defaultRocks;
    private List<List<Tile>> alternateRockList;
    private HashMap<String, Integer> sellingItems;
    private String rockName;
    private boolean usingAlternateRocks = false;
    private long lastSuccessfulRock = 0;
    private boolean gotRock;
    private Tile rockTile;
    private final int minLevel;
    private Integer maxMiningLevel = 0;
    private Tile lastTile; // lets try and hover the previous rock for speed

    public MiningTask(int miningLevel, int maxMining, Tile startPosition, String pRockName, HashMap<String, Integer> sellableProduct, WatAIO core) {
        minLevel = miningLevel;
        defaultSquare = startPosition;
        rockName = pRockName;
        sellingItems = sellableProduct;
        maxMiningLevel = maxMining;
    }

    public MiningTask(int miningLevel, int maxMining, Tile startPosition, List<List<Tile>> rockLists, HashMap<String, Integer> sellableProduct, WatAIO core) {
        minLevel = miningLevel;
        alternateRockList = new ArrayList<>();

        if(rockLists.size() > 0) {
            defaultRocks = rockLists.get(0);
            for (List<Tile> rockTiles : rockLists) {
                if(rockTiles.equals(defaultRocks))
                    continue;

                alternateRockList.add(rockTiles);
            }
        } else {
            // no rocks? kill task
            Logger.error("Mining task had no rocks setup");
            core.fatalError = true;
            core.currentTask = null;
            return;
        }

        if(startPosition != null) {
            defaultSquare = startPosition;
        }
        else {
            Logger.error("Mining task had no default location setup");
            core.fatalError = true;
            core.currentTask = null;
            return;
        }

        sellingItems = sellableProduct;
        rockName = "";
        maxMiningLevel = maxMining;
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.MINING) >= minLevel && Skills.getRealLevel(Skill.MINING) < maxMiningLevel;
    }

    @Override
    public String getName() {
        return "Mining";
    }

    @Override
    public void execute(WatAIO instance) {
        String pickaxe = MiningUtils.getBestPickaxeForLevel();

        HashMap<String, Integer> bankItems = new HashMap<String, Integer>() {
            {
                put(pickaxe, 1);
            }
        };

        if(WorldHopper.isWorldHopperOpen()) {
            WorldHopper.closeWorldHopper();
        }

        if(!Inventory.contains(pickaxe) && !Equipment.contains(pickaxe)) {
            Logger.log("I don't own the best pickaxe available for me: " + pickaxe);
            instance.currentTask = new BankingTask(null, bankItems, sellingItems, 1, this);
        } else {
            if (!Tab.INVENTORY.isOpen()) {
                Tab.INVENTORY.open();
            }

            // Check if we need to bank, and tell the Banking task to check if we have over 1000 ore, in which case sell it.
            // We use -1000 so the script knows to check for 1000, but also to sell all of it.
            // If we checked for 1000, then it would only withdraw 1000.
            if (Inventory.isFull()) {
                Logger.log("My inventory is full, to the bank!");
                instance.currentTask = new BankingTask(null, bankItems, sellingItems, 1, this);
                lastSuccessfulRock = 0;
                return;
            }

            if (!Map.isTileOnScreen(defaultSquare)) {
                Logger.log("I need to traverse to the location.");
                instance.currentTask = new TraversalTask(defaultSquare, false, this);
                return;
            }

            if(Dialogues.canContinue()) {
                Dialogues.continueDialogue();
            }

            List<Tile> currentlyUsing = new ArrayList<>();
            if (rockName == null || rockName.isEmpty()) {
                if (!usingAlternateRocks) {
                    currentlyUsing = defaultRocks;

                    if (!Map.isTileOnScreen(defaultSquare)) {
                        Camera.rotateToTile(defaultSquare);
                    }

                    if (Players.getLocal().getLevel() >= 15) {
                        if (lastSuccessfulRock > 0 && (Instant.now().getEpochSecond() - lastSuccessfulRock) > 20) { // TODO move them to the alternate spot correctly
                            currentlyUsing = alternateRockList.get(new Random().nextInt(alternateRockList.size()));
                            usingAlternateRocks = true;
                            lastSuccessfulRock = Instant.now().getEpochSecond();
                        } else if (lastSuccessfulRock > 0 && (Instant.now().getEpochSecond() - lastSuccessfulRock) > 45) { // TODO move them to the alternate spot correctly
                            usingAlternateRocks = false; // TODO
                            lastSuccessfulRock = 0; // TODO
                            instance.currentTask = new HopperTask(0, this); //TODO REMOVE THIS WHEN WE SEND EM TO THE ALT SPOT GOOD
                        }
                    } else {
                        if (lastSuccessfulRock > 0 && (Instant.now().getEpochSecond() - lastSuccessfulRock) > 20) {
                            usingAlternateRocks = false;
                            lastSuccessfulRock = 0;
                            instance.currentTask = new HopperTask(0, this);
                            return;
                        }
                    }
                } else { // Using the alternate spot
                    boolean returnBack = true;
                    for (Player pl : Players.all()) {
                        if (pl.getTile().equals(defaultSquare)) {
                            returnBack = false;
                        }
                    }

                    if (!Map.isTileOnScreen(defaultSquare)) {
                        Camera.rotateToTile(defaultSquare);
                    }

                    if (lastSuccessfulRock > 0 && (Instant.now().getEpochSecond() - lastSuccessfulRock) >= 20) {
                        lastSuccessfulRock = 0;
                        instance.currentTask = new HopperTask(0, this);
                        return;
                    }

                    if (returnBack) {
                        currentlyUsing = defaultRocks;
                        usingAlternateRocks = false;
                    } else {
                        currentlyUsing = alternateRockList.get(new Random().nextInt(alternateRockList.size()));
                    }
                }
            }


            if (!gotRock || (lastSuccessfulRock <= 0 || (Instant.now().getEpochSecond() - lastSuccessfulRock) > 10) || (rockTile != null && GameObjects.getTopObjectOnTile(rockTile.getTile()).getModelColors() == null)) {
                if (Players.getLocal().isMoving()) {
                    Sleep.sleep(1000, 1500);
                }

                if(rockName == null || rockName.isEmpty()) {
                    for (Tile tile : currentlyUsing) {
                        GameObject obj = GameObjects.getTopObjectOnTile(tile);
                        if (obj.getModelColors() != null) {
                            rockTile = obj.getTile();

                            if (!Map.isTileOnScreen(rockTile)) {
                                Camera.rotateToTile(rockTile);
                                Sleep.sleep(100, 300);
                            }

                            obj.interact();
                            gotRock = true;
                            break;
                        }
                    }
                } else {
                    GameObject obj = GameObjects.closest(rockName);
                    if (obj.getModelColors() != null) {
                        rockTile = obj.getTile();

                        if (!obj.isOnScreen()) {
                            Camera.rotateToEntity(obj); // lol
                        }

                        obj.interact();
                        gotRock = true;
                    }

                    if(lastTile != null) {
                        Mouse.move(lastTile.getRandomized());
                    }
                }

                Sleep.sleepUntil(() -> Dialogues.canContinue() || !gotRock || (rockTile != null && GameObjects.getTopObjectOnTile(rockTile).getModelColors() == null), 16000);
            }
        }
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
    public Quest completesQuest() {
        return null;
    }
}
