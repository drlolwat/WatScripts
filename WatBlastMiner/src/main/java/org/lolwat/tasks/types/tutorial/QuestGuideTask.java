package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.dreambot.api.methods.map.Area;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.HashMap;

public class QuestGuideTask implements WatTask {
    Area area = new Area(3083, 3125, 3089, 3119);

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
            instance.currentTask = new TraversalTask(area, this);
            return;
        }

        if(Dialogues.canContinue()) {
            Dialogues.continueDialogue();
            return;
        }

        if(HintArrow.exists()) {
            if(HintArrow.getType().equals(HintArrowType.NPC)) {
                DialogueUtils.talkTo("Quest Guide");
                Sleep.sleep(700, 1400);
            }
            else {
                if(GameObjects.closest("Ladder").interact("Climb-down")) {
                    instance.currentTask = new MiningInstructorTask();
                }
            }
        }

        if (TutorialUtils.needsOpenTab()) {
            TutorialUtils.handleTab();
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
}
