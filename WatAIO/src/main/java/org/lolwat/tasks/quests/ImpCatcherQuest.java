package org.lolwat.tasks.quests;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
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

public class ImpCatcherQuest implements QuestTask {
    private final Area wizardLocation = new Area(
            new Tile(3102, 3163, 2),
            new Tile(3103, 3162, 2),
            new Tile(3105, 3162, 2),
            new Tile(3106, 3162, 2),
            new Tile(3105, 3166, 2),
            new Tile(3102, 3165, 2));

    private final List<String> dialogue = Arrays.asList(
            "Give me a quest please.",
            "Give me a quest or else!",
            "Just stop messing around and give me a quest!",
            "Yes.");

    @Override
    public Quest completes() {
        return FreeQuest.IMP_CATCHER;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }

            case 1:
            case 0: {
                if(!Inventory.contains("Black bead", "Yellow bead", "Red bead", "White bead")) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, wrapper));
                    return;
                }

                NPC wizard = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Wizard Mizgog"));
                if(!wizardLocation.contains(Players.getLocal()) && wizard == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(wizardLocation, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(dialogue);
                }
                else {
                    if (wizard != null) {
                        if (!wizard.interact("Talk-to")) {
                            Logger.log("Failed to interact with Wizard Mizgog");
                            return;
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(completes());
    }

    @Override
    public int getState() {
        return PlayerSettings.getBitValue(160);
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {{
            put("Black bead", 1);
            put("Yellow bead", 1);
            put("Red bead", 1);
            put("White bead", 1); }};
    }
}
