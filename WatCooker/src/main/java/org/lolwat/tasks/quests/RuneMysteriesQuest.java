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
import org.lolwat.tasks.misc.TalkToNPC;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class RuneMysteriesQuest implements QuestTask {
    private final Area aubury = new Area(
            new Tile(3250, 3402, 0),
            new Tile(3252, 3399, 0),
            new Tile(3253, 3399, 0),
            new Tile(3255, 3401, 0),
            new Tile(3255, 3403, 0));

    @Override
    public Quest completes() {
        return FreeQuest.RUNE_MYSTERIES;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }

            case 0: {
                Area horatio = new Area(3208, 3225, 3209, 3218, 1);
                NPC horatioNPC = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Duke Horacio"));

                if(!horatio.contains(Players.getLocal()) && horatioNPC == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(horatio, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Arrays.asList("Have you any quests for me?", "Yes."));
                } else {
                    if(horatioNPC != null) {
                        if(!horatioNPC.interact("Talk-to")) {
                            Logger.log("Failed to interact with Duke Horatio");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }
                break;
            }

            case 5:
            case 2:
            case 1: {
                if(getState() == 1 && !Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Air talisman"))) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {{
                        put("Air talisman", 1);
                    }}, null, 1, wrapper));
                    return;
                }

                if(!Dialogues.inDialogue() && getState() == 5 && !Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Research notes"))) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {{
                        put("Research notes", 1);
                    }}, null, 1, wrapper, new HashMap<String, WatTask>() {{
                        put("Research notes", new TalkToNPC(aubury,
                                "Aubury", wrapper, new ArrayList<>(),"Research notes"));
                    }}));
                    return;
                }

                Area sedridor = new Area(3104, 9570, 3106, 9568, 0);
                NPC sedridorNPC = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Archmage Sedridor"));

                if(!sedridor.contains(Players.getLocal()) && sedridorNPC == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(sedridor, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Arrays.asList("Okay, here you are.", "Go ahead.", "Yes, certainly."));
                } else {
                    if(sedridorNPC != null) {
                        if(!sedridorNPC.interact("Talk-to")) {
                            Logger.log("Failed to interact with Archmage Sedridor");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }
                break;
            }

            case 3: {
                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Research package"))) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {{
                        put("Research package", 1);
                    }}, null, 1, wrapper, new HashMap<String, WatTask>() {{
                        put("Research package", new TalkToNPC(new Area(3104, 9570, 3106, 9568, 0),
                                "Archmage Sedridor", wrapper, new ArrayList<>(),"Research package"));
                    }}));
                    return;
                }

                NPC auburyNPC = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Aubury"));

                if(!aubury.contains(Players.getLocal()) && auburyNPC == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(aubury, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Arrays.asList("I've been sent here with a package for you."));
                } else {
                    if(auburyNPC != null) {
                        if(!auburyNPC.interact("Talk-to")) {
                            Logger.log("Failed to interact with Aubury");
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
        return PlayerSettings.getConfig(63);
    }
}
