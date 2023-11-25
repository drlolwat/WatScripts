package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.WatAIO;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MagicInstructorTask implements WatTask {
    @Override
    public String getName() {
        return "Tutorial: Magic Instructor";
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

        List<String> answers = new ArrayList<String>() {
            {
                add("Yes.");
                add("No, I'm not planning to do that.");
            }
        };

        if(HintArrow.exists()) {
            if(HintArrow.getType().equals(HintArrowType.NPC)) {
                NPC n = getNpcOnTile(HintArrow.getTile());
                if(n != null) {
                    if(n.getName().equals("Magic Instructor")) {
                        if (Dialogues.getOptions() == null) {
                            DialogueUtils.talkTo("Magic Instructor");
                        } else {
                            if(Dialogues.getOptions()[0].equals("Yes.")) {
                                Dialogues.chooseOption("Yes.");
                            } else {
                                Dialogues.chooseOption("No, I'm not planning to do that.");
                            }
                        }
                    } else if(n.getName().equals("Chicken")) {
                        if(!Tabs.isOpen(Tab.MAGIC)) {
                            Tabs.open(Tab.MAGIC);
                            Sleep.sleep(120, 200);
                        }

                        if(Magic.castSpellOn(Normal.WIND_STRIKE, n)) {
                            Sleep.sleep(100, 200);
                        }
                    }
                }
            }
        } else {
            instance.currentTask = new TraversalTask(new Tile(3141, 3087).getArea(2), this);
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

    public static NPC getNpcOnTile(Tile tile) {
        return NPCs.closest(n -> n.getTile().equals(tile));
    }
}
