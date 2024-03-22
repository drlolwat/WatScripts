package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.HashMap;

public class CookingInstructorTask implements WatTask {
    private Area area = new Area(3073, 3086, 3078, 3083);
    boolean ready = false;
    @Override
    public String getName() {
        return "Tutorial: Cooking Instructor";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        if (!area.contains(Players.getLocal()) && !Inventory.contains("Bread")) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
            return;
        }

        if (!Inventory.contains("Bread")) {
            if (HintArrow.exists()) {
                if (HintArrow.getType().equals(HintArrowType.NPC)) {
                    DialogueUtils.talkTo("Master Chef");
                } else {
                    if (Inventory.contains("Bread dough")) {
                        GameObject range = GameObjects.closest("Range");
                        if (range != null && range.interact("Cook")) {
                            Sleep.sleepUntil(() -> (ready && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving()), 10000);
                            Sleep.sleep(50, 120);
                        }
                    }
                }
            } else {
                if (Inventory.contains("Pot of flour") && Inventory.contains("Bucket of water")) {
                    if (Inventory.interact("Pot of flour") && Inventory.interact("Bucket of water")) {
                        Sleep.sleepUntil(() -> !Inventory.contains("Pot of flour") && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 10000);
                        Sleep.sleep(50, 120);
                    }
                }
            }
        } else {
            TaskManager.getInstance().setCurrentTask(new QuestGuideTask());
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return Calculations.random(350, 450);
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {
        ready = true;
    }

    @Override
    public Skill trainsSkill() {
        return null;
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
