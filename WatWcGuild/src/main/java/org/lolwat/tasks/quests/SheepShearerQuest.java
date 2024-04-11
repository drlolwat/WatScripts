package org.lolwat.tasks.quests;

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
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SheepShearerQuest implements QuestTask {
    private final Area startLocation = new Area(3188, 3275, 3191, 3270);
    private final List<String> startDialogue = Arrays.asList("I'm looking for a quest.", "Yes.");

    @Override
    public Quest completes() {
        return FreeQuest.SHEEP_SHEARER;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }

            case 0: {
                if(Inventory.count(x -> x != null && x.getName().equals("Ball of wool") && !x.isNoted()) < 20) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, wrapper));
                    return;
                }

                NPC fred = NPCs.closest("Fred the Farmer");
                if(!startLocation.contains(Players.getLocal()) && fred == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(startDialogue);
                } else {
                    if(fred != null) {
                        if(!fred.interact()) {
                            Logger.log("Failed to interact with Fred");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
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
        return PlayerSettings.getConfig(179);
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {{
            put("Ball of wool", 20);
        }};
    }
}
