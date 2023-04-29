package org.lolwat.Tasks.Mining;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.lolwat.Tasks.Dynamic.DynamicBankingTask;
import org.lolwat.Tasks.Dynamic.DynamicHopperTask;
import org.lolwat.Tasks.Dynamic.DynamicTraversalTask;
import org.lolwat.Tasks.WatTask;
import org.lolwat.Utils.ItemUtils;
import org.lolwat.WatMiner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FaladorGuild implements WatTask {
    private Tile defaultSquare = new Tile(3033, 9738);
    private List<Tile> defaultRocks;
    private List<Tile> alternateRocks;
    private boolean usingAlternateRocks = false;
    private long lastSuccessfulRock = 0;
    private boolean gotRock;
    private GameObject rock;


    public FaladorGuild() {
        levelRequirements.put(Skill.MINING, 60);
        defaultRocks = new ArrayList<Tile>(){
            {
                add(new Tile(3033, 9737));
                add(new Tile(3034, 9738));
            }
        };

        alternateRocks = new ArrayList<Tile>() {
            {
                add(new Tile(3032, 9737));
                add(new Tile(3032, 9739));
            }
        };
    }

    @Override
    public String getName() {
        return "Falador Mining Guild";
    }

    @Override
    public void execute(WatMiner instance) {
        String pickaxe = ItemUtils.getBestPickaxeForLevel();

        HashMap<String, Integer> bankItems = new HashMap<String, Integer>() {
            {
                put(pickaxe, 1);
            }
        };

        HashMap<String, Integer> alwaysSell = new HashMap<String, Integer>() {
            {
                put("Coal", -1);
                put("Tin ore", -1);
                put("Copper ore", -1);
                put("Logs", -1);
                put("Oak logs", -1);
                put("Gold bar", -1);
                put("Bronze bar", -1);
                put("Feather", -1);
                put("Fire rune", -1);
                put("Iron arrow", -1);
                put("Salmon", -1);
                put("Trout", -1);
                put("Iron bar", -1);
            }
        };

        //todo make this instanceof in watminer global task
        if(WorldHopper.isWorldHopperOpen()) {
            WorldHopper.closeWorldHopper();
        }

        if(!Inventory.contains(pickaxe) && !Equipment.contains(pickaxe)) {
            Logger.log("I don't own the best pickaxe available for me: " + pickaxe);
            instance.currentTask = new DynamicBankingTask("Grabbing Pickaxe", bankItems, true, this, true, alwaysSell);
        } else {
            if(!Tab.INVENTORY.isOpen()) {
                Tab.INVENTORY.open();
            }

            // Check if we need to bank, and tell the Banking task to check if we have over 1000 ore, in which case sell it.
            // We use -1000 so the script knows to check for 1000, but also to sell all of it.
            // If we checked for 1000, then it would only withdraw 1000.
            if (Inventory.isFull()) {
                Logger.log("My inventory is full, to the bank!");
                instance.currentTask = new DynamicBankingTask("Banking Ore", bankItems, true, this, true, new HashMap<String, Integer>() {
                    {
                        put("Iron ore", -1000);
                        putAll(alwaysSell);
                    }
                });
                lastSuccessfulRock = 0;
                return;
            }

            if (!Map.isTileOnScreen(defaultSquare)) {
                Logger.log("I need to traverse to the location.");
                instance.currentTask = new DynamicTraversalTask(defaultSquare, false, this);
                return;
            }

            List<Tile> currentlyUsing;
            if (!usingAlternateRocks) {
                currentlyUsing = defaultRocks;

                if(!Map.isTileOnScreen(defaultSquare)) {
                    Camera.rotateToTile(defaultSquare);
                }

                if(Players.getLocal().getLevel() >= 15) {
                    if (lastSuccessfulRock > 0 && (Instant.now().getEpochSecond() - lastSuccessfulRock ) > 20) {
                        currentlyUsing = alternateRocks;
                        usingAlternateRocks = true;
                        lastSuccessfulRock = Instant.now().getEpochSecond();
                    }
                } else {
                    if (lastSuccessfulRock > 0 && (Instant.now().getEpochSecond() - lastSuccessfulRock ) > 20) {
                        usingAlternateRocks = false;
                        lastSuccessfulRock = 0;
                        instance.currentTask = new DynamicHopperTask(0, this);
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

                if(!Map.isTileOnScreen(defaultSquare)) {
                    Camera.rotateToTile(defaultSquare);
                }

                if (lastSuccessfulRock > 0 && (Instant.now().getEpochSecond() - lastSuccessfulRock) >= 20) {
                    lastSuccessfulRock = 0;
                    instance.currentTask = new DynamicHopperTask(0, this);
                    return;
                }

                if (returnBack) {
                    currentlyUsing = defaultRocks;
                    usingAlternateRocks = false;
                } else {
                    currentlyUsing = alternateRocks;
                }
            }

            if (!gotRock || (lastSuccessfulRock <= 0 || (Instant.now().getEpochSecond() - lastSuccessfulRock) > 10) || (rock != null && GameObjects.getTopObjectOnTile(rock.getTile()).getModelColors() == null)) {
                if (Players.getLocal().isMoving()) {
                    Sleep.sleep(1000, 1500);
                }

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

                Sleep.sleepUntil(() -> (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) || (rock != null && GameObjects.getTopObjectOnTile(rock.getTile()).getModelColors() == null), 8000);
            }
        }
    }

    @Override
    public boolean requiresTradeUnrestricted() {
        return true;
    }

    @Override
    public int loopTime() {
        return 650;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatMiner instance) {
        gotRock = false;
        lastSuccessfulRock = Instant.now().getEpochSecond();

        instance.expGained += amount;
        instance.rocksMined += 1;
    }
}
