package org.lolwat.tasks.quests.wip;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.QuestTask;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.misc.ErnestTheChickenSolver;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ErnestTheChickenQuest implements QuestTask {
    @Override
    public Quest completes() {
        return FreeQuest.ERNEST_THE_CHICKEN;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }

            case 0: {
                NPC veronica = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Veronica"));
                Area veronicaArea = new Area(3110, 3330, 3112, 3328);

                if(!veronicaArea.contains(Players.getLocal()) && veronica == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(veronicaArea, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Collections.singletonList("Yes."));
                }
                else {
                    if (veronica != null) {
                        if (!veronica.interact("Talk-to")) {
                            Logger.log("Failed to interact with Veronica");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case 1: {
                NPC professor = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Professor Oddenstein"));
                Area professorArea = new Area(3108, 3368, 3111, 3362, 2);

                if(!professorArea.contains(Players.getLocal()) && professor == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(professorArea, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Arrays.asList("I'm looking for a guy called Ernest.", "Change him back this instant!"));
                }
                else {
                    if (professor != null) {
                        if (!professor.interact("Talk-to")) {
                            Logger.log("Failed to interact with Professor Oddenstein");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }
                break;
            }

            case 2: {
                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Oil can"))) {
                    Logger.log("Need oil can, handing off to ErnestTheChickenSolver");
                    TaskManager.getInstance().setCurrentTask(new ErnestTheChickenSolver(wrapper));
                    return;
                }

                break;
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(completes());
    }

    @Override
    public int getState() {
        return PlayerSettings.getConfig(32);
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return QuestTask.super.inventoryRequired();
    }

    @Override
    public List<String> inventoryTolerated() {
        return QuestTask.super.inventoryTolerated();
    }
}
