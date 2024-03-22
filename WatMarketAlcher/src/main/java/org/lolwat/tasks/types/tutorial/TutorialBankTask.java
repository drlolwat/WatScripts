package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.HashMap;

public class TutorialBankTask implements WatTask {
    boolean guide = false;
    boolean booth = false;

    @Override
    public String getName() {
        return "Tutorial: Banking/Poll";
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

        if(HintArrow.exists()) {
            if(!HintArrow.getType().equals(HintArrowType.NPC)) {
                GameObject obj = GameObjects.getTopObjectOnTile(HintArrow.getTile());
                if(obj != null) {
                    if(obj.getName().equals("Bank booth")) {
                        if(obj.interact("Use")) {
                            Sleep.sleep(1000, 3000);

                            if(Bank.isOpen()) {
                                Sleep.sleep(100, 200);
                                Bank.close();
                                Sleep.sleep(50, 100);
                            }
                        }
                    }
                    else if(obj.getName().equals("Poll booth")) {
                        if(!booth) {
                            if (obj.interact("Use")) {
                                booth = true;
                                Sleep.sleep(100, 200);
                            }
                        }
                    }
                    else if(obj.getName().equals("Door")) {
                        if(guide) {
                            TaskManager.getInstance().setCurrentTask(new BrotherBraceTask());
                            return;
                        }

                        if(obj.interact("Open")) {
                            Sleep.sleep(50, 100);
                        }
                    }
                }
            } else {
                guide = true;
                DialogueUtils.talkTo("Account Guide");
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
