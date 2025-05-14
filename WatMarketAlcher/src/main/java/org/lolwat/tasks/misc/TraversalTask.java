package org.lolwat.tasks.misc;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.TeleportManager;
import org.lolwat.managers.types.Teleport;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TraversalTask implements WatTask {
    WatTask postTask;
    boolean mustBeOnTile;
    Tile target;
    double lastWalk;
    boolean usingArea;
    Area area;
    boolean hasTeleported = false;
    double taskStartedAt;
    Tile startedOnTile;

    @Override
    public String getName() {
        if (postTask != null) {
            return "W-" + postTask.getName();
        }

        return "Walking";
    }

    public TraversalTask(Area using, WatTask post) {
        area = using;
        postTask = post;
        usingArea = true;
        lastWalk = 0;
        taskStartedAt = Instant.now().getEpochSecond();
        startedOnTile = Players.getLocal().getTile();
        Logger.log("Walking to area for task " + post.getName());
    }

    @Override
    public void execute() {
        Widget w = Widgets.getWidget(579);
        if (w != null && w.isVisible()) {
            WidgetChild c = w.getChild(17);
            if (c != null && c.isVisible() && c.hasAction("Yes")) {
                if (!c.interact("Yes")) {
                    Logger.log("Traversal: failed to click yes on widget");
                }

                Tile t = Players.getLocal().getTile();
                Sleep.sleepUntil(() -> !Players.getLocal().getTile().equals(t), 5000);
                return;
            }
        }

        if (GenericUtils.isMember() && postTask != null) {
            double targetDistance = Players.getLocal().walkingDistance(area.getRandomTile());
            double exchangeDistance = Players.getLocal().walkingDistance(BankLocation.GRAND_EXCHANGE.getCenter());

            if (targetDistance >= 1000 && !hasTeleported) {
                Teleport teleport = TeleportManager.getInstance().getBestOption(area.getRandomTile());
                if (teleport != null) {
                    Logger.log("Traversal: selected teleport " + teleport.getName() + " for destination "
                            + area.getRandomTile() + " for task " + postTask.getName());

                    String teleportItem = teleport.getSearchFor();
                    if (!Inventory.contains(x -> x.getName().contains(teleportItem))) {
                        if (Equipment.contains(x -> x.getName().contains(teleportItem))) {
                            if (Bank.isOpen()) {
                                Bank.close();
                                Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
                            }

                            if (!Tabs.isOpen(Tab.EQUIPMENT)) {
                                Tabs.open(Tab.EQUIPMENT);
                                Sleep.sleepUntil(() -> Tabs.isOpen(Tab.EQUIPMENT), Calculations.random(200, 300));
                            }

                            if (!Equipment.interact(Equipment.getSlotForItem(f -> f.getName().contains(teleportItem)),
                                    teleport.getOption())) {

                                Logger.log("Traversal: failed to use equipped teleport item");
                            }

                            Tile currentTile = Players.getLocal().getTile();
                            Sleep.sleepUntil(() -> Players.getLocal().getTile() != currentTile
                                    && !Players.getLocal().isAnimating(), Calculations.random(1500, 3000));
                            hasTeleported = true;
                            return;
                        } else {
                            if (targetDistance > exchangeDistance
                                    && (!(postTask instanceof BankingTask) && !(postTask instanceof GrandExchangeTask))) {

                                TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                                    {
                                        put(teleportItem, 1);
                                    }
                                }, this));

                                Logger.log("Traversal: missing teleport item " + teleportItem);
                                return;
                            }
                        }
                    } else {
                        if (Bank.isOpen()) {
                            Bank.close();
                            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
                        }

                        if (!Tabs.isOpen(Tab.INVENTORY)) {
                            Tabs.open(Tab.INVENTORY);
                            Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), Calculations.random(200, 300));
                        }

                        if (teleportItem.contains("teleport")) {
                            if (!Inventory.interact(x -> x != null && x.getName().contains(teleportItem), "Break")) {
                                Logger.log("Traversal: failed to use teleport tab: " + teleportItem);
                                return;
                            }
                        } else {
                            if (!Inventory.interact(x -> x != null && x.getName().contains(teleportItem), "Rub")) {
                                Logger.log("Traversal: failed to use teleport item: " + teleportItem);
                                return;
                            }

                            Sleep.sleepUntil(Dialogues::inDialogue, 2000);

                            if (!Dialogues.inDialogue()) {
                                Widget scroll = Widgets.getWidget(187);
                                if (scroll != null && scroll.isVisible()) {
                                    WidgetChild c = scroll.getChild(3);
                                    if (c != null && c.isVisible()) {
                                        WidgetChild option = null;
                                        for (WidgetChild child : c.getChildren()) {
                                            if (child == null) continue;
                                            if (child.getText().contains(teleport.getOption())) {
                                                option = child;
                                                break;
                                            }
                                        }

                                        if (option != null) {
                                            if (!option.interact("Continue")) {
                                                Logger.log("Traversal: failed to use teleport item " + teleportItem);
                                                return;
                                            }
                                        }
                                    }
                                }
                            } else {
                                Dialogues.clickOption(teleport.getOption());
                            }
                        }

                        Tile currentTile = Players.getLocal().getTile();
                        Sleep.sleepUntil(() -> Players.getLocal().getTile() != currentTile && !Players.getLocal().isAnimating(), Calculations.random(1500, 3000));
                        hasTeleported = true;
                        return;
                    }
                } else {
                    hasTeleported = true;
                    Logger.log("Traversal: no teleport item found for destination, walking");
                }
            }
        }

        boolean completedTile = !mustBeOnTile || Players.getLocal().getTile().equals(target);

        List<String> types = Collections.singletonList("Web");
        for (String t : types) {
            if (GameObjects.closest(t) != null && GameObjects.closest(t).distance(Players.getLocal().getTile()) <= 10 && GameObjects.closest(t).interact()) {
                Logger.log("Traversal: slashed " + t);
                Sleep.sleepUntil(() -> GameObjects.closest(t) == null || !GameObjects.closest(t).exists(), 5000);
                return;
            }
        }

        if (Worlds.getCurrent().isF2P()) {
            Area castleWars = new Area(2435, 3099, 2446, 3080);
            if (castleWars.contains(Players.getLocal())) {
                GameObject obj = GameObjects.closest("Large door");
                if (obj != null && obj.interact()) {
                    Sleep.sleep(2000, 3000);

                    if (Dialogues.inDialogue() && Dialogues.getOptions() != null && Dialogues.chooseOption("Yes")) {
                        Sleep.sleep(1000, 2000);
                        TaskManager.getInstance().setCurrentTask(postTask);
                        return;
                    }
                }
            }
        }

        if (!usingArea) {
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
            if (area.contains(Players.getLocal())) {
                Tile check = area.getRandomTile();
                if (Map.isTileOnMap(check) && !Map.isTileOnScreen(check)) {
                    Camera.rotateToTile(check);
                    Sleep.sleepUntil(() -> Map.isTileOnScreen(check), 3000);
                }

                Logger.log("Reached target area for task " + postTask.getName());
                TaskManager.getInstance().setCurrentTask(postTask);
                return;
            }
        }

        if (Walking.shouldWalk(5) || (lastWalk > 0 && (Instant.now().getEpochSecond() - lastWalk) >= (Walking.isRunEnabled() ? 1 : 2))) {
            if (target == null && usingArea)
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
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        if (postTask != null) {
            return postTask.clothesRequired();
        }

        HashMap<String, Integer> ret = new HashMap<>();
        for (Item i : Equipment.all()) {
            if (i == null)
                continue;

            ret.put(i.getName(), 1);
        }

        return ret;
    }

    @Override
    public List<String> inventoryTolerated() {
        List<String> ret = new ArrayList<>();
        for (Item i : Inventory.all()) {
            if (i == null)
                continue;

            ret.add(i.getName());
        }

        return ret;
    }
}