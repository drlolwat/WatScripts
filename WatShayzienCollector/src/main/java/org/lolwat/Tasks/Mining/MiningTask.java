package org.lolwat.Tasks.Mining;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.Tasks.Misc.BankingTask;
import org.lolwat.Tasks.Misc.HopperTask;
import org.lolwat.Tasks.Misc.TraversalTask;
import org.lolwat.Tasks.WatTask;
import org.lolwat.Utils.ItemUtils;
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
    private WatAIO instance;
    private boolean usingAlternateRocks = false;
    private long lastSuccessfulRock = 0;
    private boolean gotRock;
    private GameObject rock;
    private final HashMap<Skill, Integer> levelRequirements = new HashMap<>();

    public MiningTask(int miningLevel, Tile startPosition, String pRockName, HashMap<String, Integer> sellableProduct, WatAIO core) {
        setRequirements(new HashMap<Skill, Integer>() {{
            put(Skill.MINING, miningLevel);
        }}, new ArrayList<>());

        defaultSquare = startPosition;
        rockName = pRockName;
        sellingItems = sellableProduct;
        instance = core;
    }

    public MiningTask(int miningLevel, Tile startPosition, List<List<Tile>> rockLists, HashMap<String, Integer> sellableProduct, WatAIO core) {
        setRequirements(new HashMap<Skill, Integer>() {{
            put(Skill.MINING, miningLevel);
        }}, new ArrayList<>());

        alternateRockList = new ArrayList<>();

        if(rockLists.size() > 0) {
            defaultRocks = rockLists.get(0);
            for (List<Tile> rockTiles : rockLists) {
                if(rockTiles.equals(defaultRocks))
                    continue;

                alternateRockList.add(rockTiles);
            }

            Logger.log("Set up a mining task with " + defaultRocks.size() + " default rock(s) and " + alternateRockList.size() + " alternate rock list(s)");
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
        }

        sellingItems = sellableProduct;
        instance = core;
        rockName = "";
    }

    @Override
    public boolean canPerformTask() {
        for (java.util.Map.Entry<Skill, Integer> map : levelRequirements.entrySet()) {
            if (map.getValue() > Skills.getRealLevel(map.getKey())) {
                return false;
            }
        }

        return Quests.getQuestPoints() >= 10 && Skills.getTotalLevel() >= 100;
    }

    public void setRequirements(HashMap<Skill, Integer> skills, List<Quest> quests) {
        levelRequirements.putAll(skills);
    }

    @Override
    public String getName() {
        return "Mining";
    }

    @Override
    public void execute(WatAIO instance) {
        String pickaxe = ItemUtils.getBestPickaxeForLevel();

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
            instance.currentTask = new BankingTask("Grabbing Pickaxe", bankItems, true, this, true, null);
        } else {
            if (!Tab.INVENTORY.isOpen()) {
                Tab.INVENTORY.open();
            }

            // Check if we need to bank, and tell the Banking task to check if we have over 1000 ore, in which case sell it.
            // We use -1000 so the script knows to check for 1000, but also to sell all of it.
            // If we checked for 1000, then it would only withdraw 1000.
            if (Inventory.isFull()) {
                Logger.log("My inventory is full, to the bank!");
                instance.currentTask = new BankingTask("Banking Ore", bankItems, true, this, true, sellingItems);
                lastSuccessfulRock = 0;
                return;
            }

            if (!Map.isTileOnScreen(defaultSquare)) {
                Logger.log("I need to traverse to the location.");
                instance.currentTask = new TraversalTask(defaultSquare, false, this);
                return;
            }

            List<Tile> currentlyUsing = new ArrayList<>();
            if (rockName == null || rockName.isEmpty()) {
                if (!usingAlternateRocks) {
                    currentlyUsing = defaultRocks;

                    if (!Map.isTileOnScreen(defaultSquare)) {
                        Camera.rotateToTile(defaultSquare);
                    }

                    if (Players.getLocal().getLevel() >= 15) {
                        if (lastSuccessfulRock > 0 && (Instant.now().getEpochSecond() - lastSuccessfulRock) > 20) {
                            currentlyUsing = alternateRockList.get(new Random().nextInt(alternateRockList.size()));
                            usingAlternateRocks = true;
                            lastSuccessfulRock = Instant.now().getEpochSecond();
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


            if (!gotRock || (lastSuccessfulRock <= 0 || (Instant.now().getEpochSecond() - lastSuccessfulRock) > 10) || (rock != null && GameObjects.getTopObjectOnTile(rock.getTile()).getModelColors() == null)) {
                if (Players.getLocal().isMoving()) {
                    Sleep.sleep(1000, 1500);
                }

                if(rockName == null || rockName.isEmpty()) {
                    for (Tile tile : currentlyUsing) {
                        GameObject obj = GameObjects.getTopObjectOnTile(tile);
                        if (obj.getModelColors() != null) {
                            rock = obj;

                            if (!obj.isOnScreen()) {
                                Camera.rotateToEntity(obj);
                            }

                            obj.interact();
                            gotRock = true;
                            break;
                        }
                    }
                } else {
                    GameObject obj = GameObjects.closest(rockName);
                    if (obj.getModelColors() != null) {
                        rock = obj;

                        if (!obj.isOnScreen()) {
                            Camera.rotateToEntity(obj); // lol
                        }

                        obj.interact();
                        gotRock = true;
                    }
                }

                Sleep.sleepUntil(() -> (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) || (rock != null && GameObjects.getTopObjectOnTile(rock.getTile()).getModelColors() == null), 8000);
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
        lastSuccessfulRock = Instant.now().getEpochSecond();
    }

    @Override
    public Skill trainsSkill() {
        return Skill.MINING;
    }
}
