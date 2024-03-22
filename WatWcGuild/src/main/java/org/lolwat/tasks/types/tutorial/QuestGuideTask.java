package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.dreambot.api.methods.map.Area;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.HashMap;

public class QuestGuideTask implements WatTask {
    Area area = new Area(3083, 3125, 3089, 3119);
    boolean clickedTab = false;

    @Override
    public String getName() {
        return "Tutorial: Quest Guide";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        if(!area.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
            return;
        }

        if (TutorialUtils.needsOpenTab()) {
            TutorialUtils.handleTab();
            return;
        }

        if(!clickedTab && !Tabs.isDisabled(Tab.QUEST)) {
            clickedTab = true;
            if(!Tabs.isOpen(Tab.QUEST)) {
                Tabs.openWithMouse(Tab.QUEST);
                Sleep.sleep(100, 200);
                return;
            }
        }

        if(Dialogues.canContinue()) {
            Dialogues.continueDialogue();
            return;
        }

        if(HintArrow.exists()) {
            if(HintArrow.getType().equals(HintArrowType.NPC)) {
                DialogueUtils.talkTo("Quest Guide");
            }
            else {
                if(GameObjects.closest("Ladder").interact("Climb-down")) {
                    Sleep.sleep(300, 600);
                    TaskManager.getInstance().setCurrentTask(new MiningInstructorTask());
                }
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return Calculations.random(500, 800);
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

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
