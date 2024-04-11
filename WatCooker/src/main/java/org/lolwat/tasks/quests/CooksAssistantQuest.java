package org.lolwat.tasks.quests;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
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
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CooksAssistantQuest implements QuestTask {
    private final Area cookLocation = new Area(3205, 3216, 3210, 3212);
    private final List<String> startDialogue = Arrays.asList("What's wrong?", "Yes.", "Actually, I know where to find this stuff.", "I'll get right on it.");

    @Override
    public int getState() {
        return PlayerSettings.getBitValue(29);
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled value: " + getState());
                break;
            }

            case 0: {
                if(Inventory.contains("Pot of flour", "Bucket of milk", "Egg")) {
                    NPC cook = NPCs.closest("Cook");
                    if (!Dialogues.inDialogue() && (cook == null || !cook.exists() || !cook.canReach()) && !cookLocation.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(cookLocation, wrapper));
                        return;
                    }

                    if (Dialogues.inDialogue()) {
                        DialogueUtils.continueWhilePossible();
                        DialogueUtils.solve(startDialogue);
                    } else {
                        if (cook != null && cook.exists() && cook.canReach()) {
                            if (!cook.interact()) {
                                Logger.log("Failed to interact with cook");
                            }

                            Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                        }
                    }
                } else {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, wrapper));
                }

                break;
            }
        }
    }

    @Override
    public Quest completes() {
        return FreeQuest.COOKS_ASSISTANT;
    }

    @Override
    public boolean canPerformTask() {
        return true; // no requirements
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {{
            put("Pot of flour", 1);
            put("Bucket of milk", 1);
            put("Egg", 1);
        }};
    }
}
