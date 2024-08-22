package org.lolwat.tasks.misc;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.GroundItem;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.agility.types.Obstacle;

import java.util.ArrayList;
import java.util.List;

public class ErnestTheChickenSolver implements WatTask {
    private final List<Obstacle> entry;
    private final List<Obstacle> exit;
    private boolean hasStarted = false;
    private final WatTask next;

    public ErnestTheChickenSolver(WatTask post) {
        entry = new ArrayList<Obstacle>() {
            {
                add(new Obstacle(156, "Search"));
                add(new Obstacle(133, "Climb-down"));
                add(new Obstacle(147, "Pull"));
                add(new Obstacle(146, "Pull", new Tile(3108, 9746, 0).getArea(2)));
                add(new Obstacle(144, "Open"));
                add(new Obstacle(149, "Pull", new Tile(3108, 9766, 0).getArea(2)));
                add(new Obstacle(144, "Open"));
                add(new Obstacle(147, "Pull"));
                add(new Obstacle(146, "Pull", new Tile(3108, 9746, 0).getArea(2)));
                add(new Obstacle(145, "Open", new Tile(3102, 9756, 0).getArea(2)));
                add(new Obstacle(140, "Open"));
                add(new Obstacle(143, "Open"));
                add(new Obstacle(150, "Pull"));
                add(new Obstacle(151, "Pull"));
                add(new Obstacle(138, "Open"));
                add(new Obstacle(137, "Open"));
                add(new Obstacle(148, "Pull"));
                add(new Obstacle(137, "Open"));
                add(new Obstacle(138, "Open"));
                add(new Obstacle(150, "Pull"));
                add(new Obstacle(138, "Open"));
                add(new Obstacle(142, "Open"));
                add(new Obstacle(145, "Open"));
                add(new Obstacle(141, "Open"));
            }
        };

        exit = new ArrayList<Obstacle>() {
            {
                add(new Obstacle(141, "Open"));
                add(new Obstacle(132, "Climb-up", new Tile(3117, 9753, 0).getArea(2)));
                add(new Obstacle(160, "Pull"));
            }
        };

        next = post;
    }

    @Override
    public String getName() {
        return "Ernest The Chicken Solver";
    }

    @Override
    public void execute() {
        Area startZone = new Area(3097, 3363, 3103, 3354);
        if (!hasStarted) {
            if (!startZone.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(startZone, this));
                return;
            }

            hasStarted = true;
        } else {
            for (Obstacle o : entry) {
                if(Inventory.contains(x -> x != null && x.getName().equals("Oil can"))) {
                    break;
                }

                GameObject ob = GameObjects.closest(x -> x != null && x.exists() && x.getID() == o.getId()
                        && x.hasAction(o.getAction()));

                if (ob != null) {
                    if(!ob.isOnScreen()) {
                        Camera.rotateToEntity(ob);
                        Sleep.sleepUntil(ob::isOnScreen, 5000);
                    }

                    //Mouse.move(ob.getClickablePoint());
                    Sleep.sleepUntil(() -> ob.getClickablePoint().distance(Mouse.getPosition()) < 5, 2000);

                    if (Players.getLocal().isMoving() || Players.getLocal().isAnimating() || !Players.getLocal().isStandingStill()) {
                        Sleep.sleep(1000, 4000);
                        Sleep.sleepUntil(() -> !Players.getLocal().isMoving() &&
                                !Players.getLocal().isAnimating()
                                && Players.getLocal().isStandingStill(), 10000);
                        Sleep.sleep(1000, 4000);
                    }

                    if (!ob.interact(o.getAction())) {
                        Logger.log("Failed to interact with object: " + o.getName() + " (" + o.getAction() + ")");
                    }

                    Sleep.sleep(1000, 2000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving() &&
                            !Players.getLocal().isAnimating()
                            && Players.getLocal().isStandingStill(), 10000);
                    Sleep.sleep(1000, 2000);
                }

                GroundItem oilCan = GroundItems.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Oil can"));
                if (oilCan != null) {
                    if (!oilCan.interact("Take")) {
                        Logger.log("Failed to interact with Oil can");
                    }

                    Sleep.sleepUntil(() -> !oilCan.exists() && Inventory.contains(x -> x != null && x.getName().equals("Oil can")), 5000);
                }
            }

            if (Inventory.contains(x -> x != null && x.getName().equals("Oil can"))) {
                for (Obstacle o : exit) {
                    if (hasStarted && startZone.contains(Players.getLocal())) {
                        Logger.log("ErnestTheChickenSolver: complete");
                        TaskManager.getInstance().setCurrentTask(next);
                        break;
                    }

                    if (Players.getLocal().isMoving() || Players.getLocal().isAnimating() || !Players.getLocal().isStandingStill()) {
                        Sleep.sleep(1000, 2000);
                        Sleep.sleepUntil(() -> !Players.getLocal().isMoving() &&
                                !Players.getLocal().isAnimating()
                                && Players.getLocal().isStandingStill(), 10000);
                        Sleep.sleep(1000, 2000);
                    }

                    if(o.getBackupArea() != null) {
                        Tile to = o.getBackupArea().getRandomTile();
                        Walking.walk(o.getBackupArea().getRandomTile());
                        Sleep.sleepUntil(() -> Players.getLocal().getTile().equals(to), 3000);
                    }

                    GameObject ob = GameObjects.closest(x -> x != null && x.exists() && x.getID() == o.getId()
                            && x.hasAction(o.getAction()));

                    if (ob != null) {
                        Sleep.sleep(1000, 2000);
                        Sleep.sleepUntil(() -> !Players.getLocal().isMoving() &&
                                !Players.getLocal().isAnimating()
                                && Players.getLocal().isStandingStill(), 10000);
                        Sleep.sleep(1000, 2000);

                        if (!ob.interact(o.getAction())) {
                            Logger.log("Failed to interact with object: " + o.getName() + " (" + o.getAction() + ")");
                            break;
                        }

                        Sleep.sleep(1000, 2000);
                        Sleep.sleepUntil(() -> !Players.getLocal().isMoving() &&
                                !Players.getLocal().isAnimating()
                                && Players.getLocal().isStandingStill(), 10000);
                        Sleep.sleep(1000, 2000);
                    }
                }
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return false;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }
}
