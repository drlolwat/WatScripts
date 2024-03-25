package org.lolwat.tasks.types.agility;

import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.GroundItem;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.agility.types.Obstacle;
import org.lolwat.tasks.types.misc.TraversalTask;

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

    public AgilityCourseTask(String course, Area start, List<Obstacle> obstacles, int min, int max, BankLocation bankLoc) {
        courseName = course;
        startingArea = start;
        obs = obstacles;
        minimumLevel = min;
        maximumLevel = max;
        favoredBank = bankLoc;
    }

    @Override
    public String getName() {
        return courseName + " Agility";
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.AGILITY) >= minimumLevel;
    }

    @Override
    public void execute(WatAIO instance) {
        if (!started && !startingArea.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(startingArea, this), 0);
            return;
        }

        if (!started) {
            Logger.log("Starting agility course");
            started = true;
        }

        for (Obstacle ob : obs) {
            GroundItem mark = GroundItems.closest(x -> x.getName().equalsIgnoreCase("Mark of grace"));
            if(mark != null) {
                if(mark.canReach() && mark.interact()) {
                    Sleep.sleepUntil(() -> !mark.exists(), 5000);
                } else {
                    Logger.log("Could not pick up mark of grace");
                    continue;
                }
            }

            nextObstacle = false;
            GameObject obstacle = GameObjects.closest(x -> x.getName().equalsIgnoreCase(ob.getName()));
            if (obstacle == null || !obstacle.canReach()) {
                Logger.log("Could not find obstacle: " + ob.getName() + " with action: " + ob.getAction());
                break;
            }

            if (obstacle.interact(ob.getAction())) {
                Sleep.sleepUntil(() -> nextObstacle && !Players.getLocal().isMoving()
                        && !Players.getLocal().isAnimating() && Players.getLocal().isStandingStill(), 15000);
            }
        }

        Sleep.sleepUntil(() -> nextObstacle && !Players.getLocal().isMoving()
                && !Players.getLocal().isAnimating() && Players.getLocal().isStandingStill(), 15000);
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
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }

    @Override
    public BankLocation favoredBank() {
        return favoredBank;
    }
}
