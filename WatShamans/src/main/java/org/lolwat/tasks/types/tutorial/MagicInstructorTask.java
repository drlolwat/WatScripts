package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.dreambot.api.methods.hint.HintArrow.getTile;

public class MagicInstructorTask implements WatTask {
    boolean started = false;
    Area loc = new Tile(3141, 3087).getArea(2);
    private boolean spellCasted = false;

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

        if(!started && !loc.contains(Players.getLocal())) {
            started = true;
            TaskManager.getInstance().setCurrentTask(new TraversalTask(loc, this));
        }

        List<String> answers = new ArrayList<String>() {
            {
                add("Yes.");
                add("No, I'm not planning to do that.");
            }
        };

        if(HintArrow.exists()) {
            if(HintArrow.getType().equals(HintArrowType.NPC)) {
                NPC n = getNpcOnTile(getTile());
                if(n != null) {
                    if(n.getName().equals("Magic Instructor")) {
                        DialogueUtils.talkTo("Magic Instructor", answers);
                    } else if(n.getName().equals("Chicken")) {
                        if (!spellCasted) {
                            if (!Tabs.isOpen(Tab.MAGIC)) {
                                Tabs.open(Tab.MAGIC);
                                Sleep.sleep(120, 200);
                            }

                            Area location = new Area(3138, 3091, 3141, 3091);

                            if (!location.contains(Players.getLocal())) {
                                TaskManager.getInstance().setCurrentTask(new TraversalTask(location, this));
                                return;
                            }

                            if (Magic.castSpellOn(Normal.WIND_STRIKE, NPCs.closest(x -> !x.isInCombat() && x.getName().equals("Chicken")))) {
                                Sleep.sleep(100, 200);
                                spellCasted = true;
                            } else {
                                Logger.log("error castin' spell on the chucken");
                            }
                        }
                    }
                }
            }
        }

        if (PlayerSettings.getConfig(281) == 1000) {
            TaskManager.getInstance().getNewTask();
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

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
