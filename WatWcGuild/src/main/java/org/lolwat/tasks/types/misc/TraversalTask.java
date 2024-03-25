package org.lolwat.tasks.types.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.TeleportItemUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;
import org.lolwat.tasks.types.combat.warriorguild.FightArmorSetTask;
import org.lolwat.tasks.types.combat.warriorguild.FightCyclopsTask;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TraversalTask implements WatTask {
    WatTask postTask;
    boolean mustBeOnTile;
    Tile target;
    double lastWalk;
    boolean usingArea;
    Area area;

    @Override
    public String getName() {
        if(postTask != null) {
            return postTask.getName();
        }

        return "Walking";
    }

    public TraversalTask(Tile tile, boolean tileOnly, WatTask post) {
        target = tile;
        mustBeOnTile = tileOnly;
        postTask = post;
        lastWalk = 0;
        usingArea = false;

        Logger.log("Walking to coords");
    }

    public TraversalTask(Area using, WatTask post) {
        area = using;
        postTask = post;
        usingArea = true;
        lastWalk = 0;

        Logger.log("Walking to area for task " + post.getName());
    }

    @Override
    public void execute(WatAIO instance) {
        if (TutorialUtils.needsOpenTab()) {
            TutorialUtils.handleTab();
        }

        boolean needsTeleport = Players.getLocal().walkingDistance(postTask.favoredBank().getCenter()) >= 1500;

        if(postTask != null && needsTeleport) {
            if(postTask.favoredBank() != BankLocation.GRAND_EXCHANGE) {
                String teleportItem = TeleportItemUtils.getTeleportForBank(postTask.favoredBank());
                if(!teleportItem.isEmpty()) {
                    Logger.log("Needs to teleport");
                    if(!Inventory.contains(x -> x.getName().contains(teleportItem))) {
                        if(Equipment.contains(x -> x.getName().contains(teleportItem))) {
                            if(Bank.isOpen()) {
                                Bank.close();
                                Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
                            }

                            if(!Tabs.isOpen(Tab.EQUIPMENT)) {
                                Tabs.open(Tab.EQUIPMENT);
                                Sleep.sleepUntil(() -> Tabs.isOpen(Tab.EQUIPMENT), Calculations.random(200, 300));
                            }

                            if(!Equipment.interact(Equipment.getSlotForItem(f -> f.getName().contains(teleportItem)),
                                    TeleportItemUtils.getDialogueOption(teleportItem, true))) {

                                Logger.log("Traversal: failed to use equipped teleport item");
                            }

                            Tile currentTile = Players.getLocal().getTile();
                            Sleep.sleepUntil(() -> Players.getLocal().getTile() != currentTile && !Players.getLocal().isAnimating(), Calculations.random(1500, 3000));
                            return;
                        } else {
                            Logger.log("Traversal: missing teleport item " + teleportItem);
                            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                                {
                                    put(TeleportItemUtils.getChargedItemName(teleportItem), Calculations.random(2, 6));
                                }
                            }, new HashMap<>(), 1, this));

                            return;
                        }
                    } else {
                        if(Bank.isOpen()) {
                            Bank.close();
                            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
                        }

                        if(!Tabs.isOpen(Tab.INVENTORY)) {
                            Tabs.open(Tab.INVENTORY);
                            Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), Calculations.random(200, 300));
                        }

                        if(!Inventory.interact(x -> x != null && x.getName().contains(teleportItem), "Rub")) {
                            Logger.log("Traversal: failed to use teleport item: " + teleportItem);
                            return;
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);

                        if(!Dialogues.inDialogue()) {
                            Logger.log("Traversal: did not find dialogue for teleport item: " + teleportItem);
                            return;
                        }
                        else {
                            Dialogues.clickOption(TeleportItemUtils.getDialogueOption(teleportItem, false));
                        }

                        Tile currentTile = Players.getLocal().getTile();
                        Sleep.sleepUntil(() -> Players.getLocal().getTile() != currentTile && !Players.getLocal().isAnimating(), Calculations.random(1500, 3000));
                        return;
                    }
                }
            } else {
                Sleep.sleep(Calculations.random(1500, 3000));
                return;
            }
        }

        boolean completedTile = !mustBeOnTile || Players.getLocal().getTile().equals(target);

        List<String> types = Arrays.asList("Web");
        for (String t : types) {
            if(GameObjects.closest(t) != null && GameObjects.closest(t).distance(Players.getLocal().getTile()) <= 10 && GameObjects.closest(t).interact()) {
                Logger.log("Traversal: slashed " + t);
                Sleep.sleepUntil(() -> GameObjects.closest(t) == null || !GameObjects.closest(t).exists(), 5000);
                return;
            }
        }

        if(Worlds.getCurrent().isF2P()) {
            Area castleWars = new Area(2435, 3099, 2446, 3080);
            if(castleWars.contains(Players.getLocal())) {
                GameObject obj = GameObjects.closest("Large door");
                if(obj != null && obj.interact()) {
                    Sleep.sleep(2000, 3000);

                    if(Dialogues.inDialogue() && Dialogues.getOptions() != null && Dialogues.chooseOption("Yes")) {
                        Sleep.sleep(1000, 2000);
                        TaskManager.getInstance().setCurrentTask(postTask);
                        return;
                    }
                }
            }
        }

        if(postTask instanceof FightCyclopsTask) {
            if(!Inventory.contains("Warrior guild token") || Inventory.count("Warrior guild token") < 100) {
                String defender = "";
                if(Equipment.slotContains(EquipmentSlot.SHIELD, x -> x.getName().contains("defender"))) {
                    defender = Equipment.getItemInSlot(EquipmentSlot.SHIELD).getName();
                } else if(Inventory.contains(x -> x.getName().contains("defender"))) {
                    defender = Inventory.get(x -> x.getName().contains("defender")).getName();
                }

                TaskManager.getInstance().setCurrentTask(new FightArmorSetTask(trainsSkill(), new HashMap<String, Integer>() {
                    {
                        put("Lobster", 20);
                    }
                }, defender));
                return;
            }
        }

        if(!usingArea) {
            if (completedTile && Map.isTileOnMap(target)) {
                if (!Map.isTileOnScreen(target)) {
                    Camera.rotateToTile(target);
                    Sleep.sleepUntil(() -> Map.isTileOnScreen(target), 3000);
                }

                Logger.log("Reached target: X:" + target.getX() + ", Y:" + target.getY());
                TaskManager.getInstance().setCurrentTask(postTask);
                return;
            }
        } else {
            if(area.contains(Players.getLocal())) {
                Logger.log("Reached target area for task " + postTask.getName());
                TaskManager.getInstance().setCurrentTask(postTask);
                return;
            }
        }

        if (Walking.shouldWalk(5) || (lastWalk > 0 && (Instant.now().getEpochSecond() - lastWalk) >= (Walking.isRunEnabled() ? 1 : 2))) {
            if(target == null && usingArea)
                Walking.walk(area);
            else
                Walking.walk(target);

            lastWalk = Instant.now().getEpochSecond();
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 400;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
