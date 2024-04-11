package org.lolwat.tasks.quests;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.QuestTask;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TheRestlessGhostQuest implements QuestTask {
    private final Area lumbridgeChurch = new Area(3240, 3211, 3247, 3204);
    private final List<String> aereckDialogue = Arrays.asList("I'm looking for a quest!", "Ok, let me help then.", "Yes.");

    @Override
    public Quest completes() {
        return FreeQuest.THE_RESTLESS_GHOST;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }

            case 0: {
                NPC aereck = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Father Aereck"));
                if(!lumbridgeChurch.contains(Players.getLocal()) && aereck == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(lumbridgeChurch, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(aereckDialogue);
                } else {
                    if(aereck != null) {
                        if(!aereck.interact()) {
                            Logger.log("Failed to interact with Father Aereck");
                            return;
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }
                break;
            }

            case 1: {
                Area zone = new Area(3144, 3177, 3151, 3173);
                NPC urhney = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Father Urhney"));

                if(!zone.contains(Players.getLocal()) && urhney == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(zone, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Arrays.asList("Father Aereck sent me to talk to you.", "He's got a ghost haunting his graveyard."));
                } else {
                    if(urhney != null) {
                        if(!urhney.interact()) {
                            Logger.log("Failed to interact with Father Urhney");
                            return;
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case 4:
            case 2: {
                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Ghostspeak amulet"))
                    && !Equipment.contains(x -> x != null && !x.isNoted() && x.getName().equals("Ghostspeak amulet"))) {

                    TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {{
                        put("Ghostspeak amulet", 1);
                    }}, null, 1, wrapper));
                    return;
                }

                if(!Equipment.contains("Ghostspeak amulet")) {
                    if(!Inventory.interact("Ghostspeak amulet", "Wear")) {
                        Logger.log("Failed to wear Ghostspeak amulet");
                        return;
                    }
                }

                Area graveyard = new Area(3247, 3195, 3251, 3190);
                NPC ghost = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Restless ghost"));

                if(!graveyard.contains(Players.getLocal()) && ghost == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(graveyard, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Collections.singletonList("Yep, now tell me what the problem is."));
                } else {
                    if(ghost != null) {
                        if(!ghost.interact()) {
                            Logger.log("Failed to interact with Restless ghost");
                            return;
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    } else {
                        GameObject coffin = GameObjects.closest(x -> x != null && x.canReach() && x.getName().equals("Coffin"));
                        if(coffin != null) {
                            if(!coffin.interact()) {
                                Logger.log("Failed to interact with Coffin");
                                return;
                            }

                            Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                        }
                    }
                }

                break;
            }

            case 3: {
                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Ghost's skull"))) {
                    Area skullZone = new Area(3111, 9569, 3121, 9564);
                    GameObject altar = GameObjects.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Altar"));

                    if (!skullZone.contains(Players.getLocal()) && altar == null) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(skullZone, wrapper));
                        return;
                    }

                    if (altar != null) {
                        if (!altar.interact()) {
                            Logger.log("Failed to interact with Altar");
                            return;
                        }

                        Sleep.sleepUntil(() -> Inventory.contains("Ghost's skull"), 5000);
                    }
                }

                break;
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(FreeQuest.THE_RESTLESS_GHOST);
    }

    @Override
    public int getState() {
        return PlayerSettings.getConfig(107);
    }
}
