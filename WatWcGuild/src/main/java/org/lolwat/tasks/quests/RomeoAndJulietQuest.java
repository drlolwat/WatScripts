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
import org.lolwat.tasks.misc.GatheringTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RomeoAndJulietQuest implements QuestTask {
    private final Area romeoArea = new Area(
            new Tile(3205, 3437, 0),
            new Tile(3205, 3420, 0),
            new Tile(3211, 3420, 0),
            new Tile(3213, 3416, 0),
            new Tile(3222, 3416, 0),
            new Tile(3222, 3437, 0));

    private final Area julietArea = new Area(3155, 3426, 3161, 3425, 1);

    private final Area lawrenceArea = new Area(
            new Tile(3259, 3481, 0),
            new Tile(3252, 3481, 0),
            new Tile(3252, 3488, 0),
            new Tile(3252, 3489, 0),
            new Tile(3256, 3489, 0),
            new Tile(3256, 3485, 0));

    private final Area potionArea = new Area(3192, 3406, 3194, 3402);
    private final List<String> romeoDialogue = Arrays.asList("Yes, I have seen her actually!", "Yes, ok, I'll let her know.", "Ok, thanks.", "Yes.");
    private final List<String> potionDialogue = Arrays.asList("Talk about something else.", "Talk about Romeo & Juliet.");
    private final int cutsceneId = 19136;

    @Override
    public Quest completes() {
        return FreeQuest.ROMEO_AND_JULIET;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }

            case 60:
            case 20:
            case 0: {
                NPC romeo = NPCs.closest(x -> x != null && x.exists() && x.getName().equals("Romeo") && x.canReach());
                if (!romeoArea.contains(romeo) && romeo == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(romeoArea, wrapper));
                    return;
                }

                if(getState() == 20) {
                    if(!Inventory.contains("Message")) {
                        TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, wrapper));
                        return;
                    }
                }

                if(Dialogues.inDialogue() || PlayerSettings.getConfig(1021) == cutsceneId){
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(romeoDialogue);
                } else {
                    if (romeo != null) {
                        if (!romeo.interact("Talk-to")) {
                            Logger.log("Failed to interact with Romeo");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case 50:
            case 10: {
                NPC juliet = NPCs.closest(x -> x != null && x.exists() && x.getName().equals("Juliet") && x.canReach());
                if (!Inventory.contains(x -> x != null && x.getName().equals("Cadava berries"))
                        && !julietArea.contains(juliet) && juliet == null) {

                    TaskManager.getInstance().setCurrentTask(new TraversalTask(julietArea, wrapper));
                    return;
                }

                if(Dialogues.inDialogue() || PlayerSettings.getConfig(1021) == cutsceneId) {
                    DialogueUtils.continueWhilePossible();
                } else {
                    if (juliet != null) {
                        if (!juliet.interact("Talk-to")) {
                            Logger.log("Failed to interact with Juliet");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }
                break;
            }

            case 30: {
                NPC lawrence = NPCs.closest(x -> x != null && x.exists() && x.getName().equals("Father Lawrence") && x.canReach());
                if (!lawrenceArea.contains(lawrence) && lawrence == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(lawrenceArea, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                } else {
                    if (lawrence != null) {
                        if (!lawrence.interact("Talk-to")) {
                            Logger.log("Failed to interact with Father Lawrence");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }
                break;
            }

            case 40: {
                if(!Inventory.contains(x -> x != null && x.getName().equals("Cadava berries") && !x.isNoted())) {
                    TaskManager.getInstance().setCurrentTask(new GatheringTask("Cadava bush", "Cadava berries", 1, new Area(
                            new Tile(3260, 3374, 0),
                            new Tile(3261, 3364, 0),
                            new Tile(3273, 3363, 0),
                            new Tile(3283, 3375, 0)), wrapper));
                    return;
                }

                NPC apothecary = NPCs.closest(x -> x != null && x.exists() && x.getName().equals("Apothecary") && x.canReach());
                if(!potionArea.contains(Players.getLocal()) && apothecary == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(new Area(3190, 3400, 3195, 3396), wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(potionDialogue);
                } else {
                    if (apothecary != null) {
                        if (!apothecary.interact("Talk-to")) {
                            Logger.log("Failed to interact with Apothecary");
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
        int state = PlayerSettings.getConfig(144);
        if(state == 50 && !Inventory.contains(x -> x != null && x.getName().equals("Cadava potion") && !x.isNoted())) {
            return 40;
        }
        return state;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {{
            put("Message", 1);
        }};
    }
}
