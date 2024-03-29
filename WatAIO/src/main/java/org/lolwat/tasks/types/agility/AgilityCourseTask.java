package org.lolwat.tasks.types.agility;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.GroundItem;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.agility.types.Obstacle;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AgilityCourseTask implements WatTask {
    String courseName;
    Area startingArea;
    boolean started = false;
    List<Obstacle> obs;
    boolean nextObstacle = false;
    int minimumLevel;
    int maximumLevel;
    BankLocation favoredBank;
    boolean needsEat;

    List<Integer> safelyIgnore = new ArrayList<Integer>() { {
        add(14923);
    }};

    public AgilityCourseTask(String course, Area start, List<Obstacle> obstacles, int min, int max, boolean eat, BankLocation bankLoc) {
        courseName = course;
        startingArea = start;
        obs = obstacles;
        minimumLevel = min;
        maximumLevel = max;
        needsEat = eat;
        favoredBank = bankLoc;
    }

    @Override
    public String getName() {
        return courseName + " Agility";
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.AGILITY) >= minimumLevel && Skills.getRealLevel(Skill.AGILITY) < maximumLevel;
    }

    @Override
    public void execute(WatAIO instance) {
        if(needsEat) {
            if(Inventory.count(x -> x != null && x.hasAction("Eat")) > 0) {
                if(Combat.getHealthPercent() <= 50 && !Inventory.interact(x -> x != null && x.hasAction("Eat"))) {
                    Logger.log("Issue eating food during agility task");
                    return;
                }
            } else {
                TaskManager.getInstance().setCurrentTask(new BankingTask(null, new HashMap<>(), 1, this));
                return;
            }
        }

        if (!started && !startingArea.contains(Players.getLocal()) && !startingArea.contains(Walking.getDestination())) {
            Obstacle first = obs.get(0);
            GameObject ob = GameObjects.closest(x -> x.exists() && x.distance() <= 7 && !safelyIgnore.contains(x.getRealID())
                    && x.getName().equalsIgnoreCase(first.getName()) && x.hasAction(first.getAction()));

            if(ob == null || !ob.exists() || !ob.canReach()) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(startingArea, this));
                return;
            }
        }

        if (!started) {
            started = true;
        }

        List<Integer> clicked = new ArrayList<>();
        for (Obstacle ob : obs) {
            if(Players.getLocal().isHealthBarVisible()) {
                nextObstacle = true;
                Logger.log("We fell off the course, starting again");
                Sleep.sleep(100, 400);
                break;
            }

            GroundItem mark = GroundItems.closest(x -> x.getName().equalsIgnoreCase("Mark of grace"));
            if(mark != null) {
                if(mark.canReach() && mark.interact()) {
                    Logger.log("Picking up mark of grace");
                    Sleep.sleepUntil(() -> !mark.exists() && (!Players.getLocal().isMoving()
                            && !Players.getLocal().isAnimating() && Players.getLocal().isStandingStill()), 5000);
                    Sleep.sleep(100, 200);
                }
            }

            if (ob.getBackupArea() != null) {
                Walking.walk(ob.getBackupArea().getRandomTile());
                Sleep.sleep(100, 200);
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving()
                        && !Players.getLocal().isAnimating() && Players.getLocal().isStandingStill(), 15000);
            }

            nextObstacle = false;
            long startTime = System.currentTimeMillis();
            while(!nextObstacle && !Players.getLocal().isHealthBarVisible()) {
                if ((System.currentTimeMillis() - startTime) >= 30000) {
                    Logger.log("Couldn't find obstacle in a reasonable time, moving on");
                    break;
                }
                GameObject obstacle = GameObjects.closest(x -> x.exists() && !safelyIgnore.contains(x.getRealID()) && !clicked.contains(x.getRealID())
                        && x.getName().equalsIgnoreCase(ob.getName()) && x.hasAction(ob.getAction()));

                if (obstacle == null) {
                    Logger.log("Could not find obstacle: " + ob.getName() + ", no backup available");
                    continue;
                }

                if (obstacle.interact(ob.getAction())) {
                    Sleep.sleepUntil(() -> Players.getLocal().isHealthBarVisible() || (nextObstacle && !Players.getLocal().isMoving()
                            && !Players.getLocal().isAnimating() && Players.getLocal().isStandingStill()), 15000);
                    Sleep.sleep(100, 200);
                    clicked.add(obstacle.getRealID());
                }
            }
        }

        Sleep.sleepUntil(() -> Players.getLocal().isHealthBarVisible() || (nextObstacle && !Players.getLocal().isMoving()
                && !Players.getLocal().isAnimating() && Players.getLocal().isStandingStill()), 15000);

        started = false;
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
        nextObstacle = true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.AGILITY;
    }

    @Override
    public Integer avoidAfterLevel() {
        return maximumLevel;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<String, Integer>() { {
            put("Graceful hood", 1);
            put("Graceful top", 1);
            put("Graceful legs", 1);
            put("Graceful gloves", 1);
            put("Graceful boots", 1);
            put("Graceful cape", 1);
        } };
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        HashMap<String, Integer> ret = new HashMap<>();

        if(needsEat) {
            ret.put("Monkfish", -10);
        }

        return ret;
    }
}
