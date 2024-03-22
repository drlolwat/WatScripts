package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.HashMap;

public class BrotherBraceTask implements WatTask {
    boolean door = false;
    boolean started = false;
    Area loc = new Tile(3124, 3107).getArea(3);

    @Override
    public String getName() {
        return "Tutorial: Brother Brace";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        if(Dialogues.canContinue()) {
            Dialogues.continueDialogue();
        }

        if (TutorialUtils.needsOpenTab()) {
            TutorialUtils.handleTab();
        }

        if(!started && !loc.contains(Players.getLocal())) {
            started = true;
            TaskManager.getInstance().setCurrentTask(new TraversalTask(loc, this));
        }

        if(HintArrow.exists()) {
            if(HintArrow.getType().equals(HintArrowType.NPC)) {
                DialogueUtils.talkTo("Brother Brace");
            } else {
                GameObject obj = GameObjects.getTopObjectOnTile(HintArrow.getTile());
                if(obj != null && obj.getName().equals("Door")) {
                    if(obj.interact("Open")) {
                        Sleep.sleep(300, 600);

                        if(door)
                            TaskManager.getInstance().setCurrentTask(new MagicInstructorTask());
                        else
                            door = true;
                    }
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

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
