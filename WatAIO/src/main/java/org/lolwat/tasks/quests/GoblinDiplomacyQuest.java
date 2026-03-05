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

public class GoblinDiplomacyQuest implements QuestTask {
    private final Area goblinArea = new Area(2954, 3512, 2961, 3510);

    private final List<String> startDialogue = Arrays.asList(
            "Do you want me to pick an armour colour for you?",
            "Yes.",
            "You should wear red.",
            "What about a different colour?",
            "Okay, I'll be back soon.",
            "I'll leave you to it.");

    private final List<String> otherDialogue = Arrays.asList(
            "Do you want me to pick an armour colour for you?",
            "What about a different colour?",
            "Yes.",
            "You should wear red.",
            "No, he doesn't look fat.",
            "I have some orange armour here.",
            "I have some blue armour here.",
            "I have some brown armour here.",
            "How am I meant to get blue armour?",
            "How am I meant to get orange armour?");

    @Override
    public Quest completes() {
        return FreeQuest.GOBLIN_DIPLOMACY;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }

            case 0: {
                if(Inventory.count(x -> x != null && !x.isNoted() && x.getName().equals("Goblin mail")) < 3
                        || !Inventory.contains("Orange dye", "Blue dye")) {

                    TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, wrapper));
                    return;
                }

                NPC bentnoze = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("General Bentnoze"));
                if(!goblinArea.contains(Players.getLocal()) && bentnoze == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(goblinArea, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(startDialogue);
                }
                else {
                    if (bentnoze != null) {
                        if (!bentnoze.interact("Talk-to")) {
                            Logger.log("Failed to interact with General Bentnoze");
                        }
                    }

                    Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                }

                break;
            }

            case 3: {
                handleDye("Orange", wrapper);
                break;
            }

            case 4: {
                handleDye("Blue", wrapper);
                break;
            }

            case 5: {
                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Goblin mail"))) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {{
                        put("Goblin mail", 1);
                    }}, null, 1, wrapper));
                    return;
                }

                NPC bentnoze = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("General Bentnoze"));
                if(!goblinArea.contains(Players.getLocal()) && bentnoze == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(goblinArea, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(otherDialogue);
                } else {
                    if (bentnoze != null) {
                        if (!bentnoze.interact("Talk-to")) {
                            Logger.log("Failed to interact with General Bentnoze");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }
            }
        }
    }

    private void handleDye(String color, WatTask wrapper) {
        if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Goblin mail"))
                || !Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals(color + " dye"))) {

            if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals(color + " goblin mail"))) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, wrapper));
                return;
            }
        }

        if(Inventory.contains(color + " dye")) {
            if (!Inventory.use(color + " dye")) {
                Logger.log("Failed to use " + color + " dye");
                return;
            }

            if (!Inventory.use("Goblin mail")) {
                Logger.log("Failed to use dye on goblin mail");
                return;
            }

            Sleep.sleepUntil(() -> Inventory.contains(color + " goblin mail"), 5000);
        }

        NPC bentnoze = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("General Bentnoze"));
        if(!goblinArea.contains(Players.getLocal()) && bentnoze == null) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(goblinArea, wrapper));
            return;
        }

        if(Dialogues.inDialogue()) {
            DialogueUtils.continueWhilePossible();
            DialogueUtils.solve(otherDialogue);
        } else {
            if (bentnoze != null) {
                if (!bentnoze.interact("Talk-to")) {
                    Logger.log("Failed to interact with General Bentnoze");
                }
            }

            Sleep.sleepUntil(Dialogues::inDialogue, 5000);
        }
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(completes());
    }

    @Override
    public int getState() {
        if(!Quests.isStarted(completes())) {
            return 0;
        }

        return PlayerSettings.getBitValue(2378);
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() { {
            put("Goblin mail", 3);
            put("Orange dye", 1);
            put("Blue dye", 1);
        }};
    }
}
