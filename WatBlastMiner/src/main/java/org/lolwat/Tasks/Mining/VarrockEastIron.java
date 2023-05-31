package org.lolwat.Tasks.Mining;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.Player;
import org.lolwat.Tasks.Misc.BankingTask;
import org.lolwat.Tasks.Misc.HopperTask;
import org.lolwat.Tasks.Misc.TraversalTask;
import org.lolwat.Tasks.WatTask;
import org.lolwat.Utils.ItemUtils;
import org.lolwat.WatAIO;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class VarrockEastIron implements WatTask {
    private final Tile defaultSquare = new Tile(3286, 3368);
    private final Tile alternateSquare = new Tile(3285, 3370);
    private final List<Tile> defaultRocks;
    private final List<Tile> alternateRocks;
    private boolean usingAlternateRocks = false;
    private long lastSuccessfulRock = 0;
    private boolean gotRock;
    private GameObject rock;
    private final HashMap<Skill, Integer> levelRequirements = new HashMap<>();

    @Override
    public String getName() {
        return "Varrock East: Iron";
    }

    public VarrockEastIron() {
        setRequirements(new HashMap<Skill, Integer>() {{
            put(Skill.MINING, 15);
        }});

        defaultRocks = Arrays.asList(new Tile(3286, 3369), new Tile(3285, 3368));
        alternateRocks = Arrays.asList(new Tile(3288, 3370), new Tile(3285, 3369));
    }

    public void setRequirements(HashMap<Skill, Integer> skills) {
        levelRequirements.putAll(skills);
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

    @Override
    public void execute(WatAIO instance) {
        String pickaxe = ItemUtils.getBestPickaxeForLevel();

        HashMap<String, Integer> bankItems = new HashMap<String, Integer>() {
            {
                put(pickaxe, 1);
            }
        };

        HashMap<String, Integer> alwaysSell = new HashMap<String, Integer>() {
            {
                put("Iron ore", -1000);
                put("Coal", -1);
                put("Tin ore", -1);
                put("Copper ore", -1);
                put("Logs", -1);
                put("Oak logs", -1);
                put("Gold bar", -1);
            }
        };

        if(WorldHopper.isWorldHopperOpen()) {
            WorldHopper.closeWorldHopper();
        }

        if(!Inventory.contains(pickaxe) && !Equipment.contains(pickaxe)) {
            Logger.log("I don't own the best pickaxe available for me: " + pickaxe);
            instance.currentTask = new BankingTask("Grabbing Pickaxe", bankItems, true, this, true, alwaysSell);
        } else {
            if(!Tab.INVENTORY.isOpen()) {
                Tab.INVENTORY.open();
            }

            // Check if we need to bank, and tell the Banking task to check if we have over 1000 ore, in which case sell it.
            // We use -1000 so the script knows to check for 1000, but also to sell all of it.
            // If we checked for 1000, then it would only withdraw 1000.
            if (Inventory.isFull()) {
                Logger.log("My inventory is full, to the bank!");
                instance.currentTask = new BankingTask("Banking Ore", bankItems, true, null, true, new HashMap<String, Integer>() {
                    {
                        put("Iron ore", -1000);
                        putAll(alwaysSell);
                    }
                });
                lastSuccessfulRock = 0;
                return;
            }

            if (!Map.isTileOnScreen(defaultSquare)) {
                if(Map.isTileOnMap(defaultSquare)) {
                    Camera.rotateToTile(defaultSquare);
                }

                Logger.log("I need to traverse to the location.");
                instance.currentTask = new TraversalTask(defaultSquare, false, this);
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

                if(!Map.isTileOnScreen(alternateSquare)) {
                    Camera.rotateToTile(alternateSquare);
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
                    currentlyUsing = alternateRocks;
                }
            }

            if(!Map.isTileOnScreen(currentlyUsing.get(0))) {
                Camera.rotateToTile(currentlyUsing.get(0));
                Sleep.sleep(100, 300);
            }

            if (!gotRock || (lastSuccessfulRock <= 0 || (Instant.now().getEpochSecond() - lastSuccessfulRock) > 10) || (rock != null && GameObjects.getTopObjectOnTile(rock.getTile()).getModelColors() == null)) {
                if (Players.getLocal().isMoving()) {
                    Sleep.sleep(1000, 1500);
                }

                for (Tile tile : currentlyUsing) {
                    GameObject obj = GameObjects.getTopObjectOnTile(tile);
                    if (obj.getModelColors() != null) {
                        rock = obj;

                        if (!Map.isTileOnScreen(obj.getTile())) {
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
