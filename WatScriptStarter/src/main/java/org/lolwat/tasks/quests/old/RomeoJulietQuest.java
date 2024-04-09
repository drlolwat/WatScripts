package org.lolwat.tasks.quests.old;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RomeoJulietQuest implements WatTask {
    private enum State {
        STARTING,
        JULIET,
        ROMEO_LETTER,
        FATHER_LAWRENCE,
        POTION,
        JULIET_POTION,
        JULIET_CUTSCENE,
        CRYPT,
        CRYPT_CUTSCENE,
    }

    private State state;

    private Area romeoArea = new Area(
            new Tile(3205, 3437, 0),
            new Tile(3205, 3420, 0),
            new Tile(3211, 3420, 0),
            new Tile(3213, 3416, 0),
            new Tile(3222, 3416, 0),
            new Tile(3222, 3437, 0));

    private Area julietArea = new Area(3155, 3426, 3161, 3425, 1);

    private Area lawrenceArea = new Area(
            new Tile(3259, 3481, 0),
            new Tile(3252, 3481, 0),
            new Tile(3252, 3488, 0),
            new Tile(3252, 3489, 0),
            new Tile(3256, 3489, 0),
            new Tile(3256, 3485, 0));

    Area potionArea = new Area(3192, 3406, 3198, 3402);

    List<String> romeoDialogue = Arrays.asList("Yes, I have seen her actually!", "Yes, ok, I'll let her know.", "Ok, thanks.", "Yes.");
    List<String> potionDialogue = Arrays.asList("Talk about something else.", "Talk about Romeo & Juliet.");

    public RomeoJulietQuest() {
        if(Quests.isStarted(FreeQuest.ROMEO_AND_JULIET)) {
            state = State.JULIET;
        }
        else {
            state = State.STARTING;
        }
    }

    @Override
    public void execute(WatAIO instance) {
        if(Inventory.isFull()) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, this));
            return;
        }

        switch(state) {
            default: return;
            case STARTING: {
                if(Quests.isStarted(FreeQuest.ROMEO_AND_JULIET)) {
                    state = State.JULIET;
                    return;
                }

                if (!romeoArea.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(romeoArea, this));
                    return;
                }

                if (NPCs.closest("Romeo") != null) {
                    // handle the dialogue here
                    if (Dialogues.inDialogue()) {
                        while (Dialogues.inDialogue()) {
                            DialogueUtils.continueWhilePossible();
                            DialogueUtils.solve(romeoDialogue);
                        }
                    } else {
                        NPCs.closest("Romeo").interact();
                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case JULIET: {
                if (!julietArea.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(julietArea, this));
                    return;
                }

                if(Inventory.contains("Message")) {
                    state = State.ROMEO_LETTER;
                    return;
                }

                if (NPCs.closest("Juliet") != null) {
                    if (Dialogues.inDialogue()) {
                        while (Dialogues.inDialogue()) {
                            DialogueUtils.continueWhilePossible();
                        }
                    } else {
                        NPCs.closest("Juliet").interact();
                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case ROMEO_LETTER: {
                if (!romeoArea.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(romeoArea, this));
                    return;
                }

                if(!Inventory.contains("Message")) {
                    state = State.FATHER_LAWRENCE;
                    return;
                }

                if (NPCs.closest("Romeo") != null) {
                    // handle the dialogue here
                    if (Dialogues.inDialogue()) {
                        while (Dialogues.inDialogue()) {
                            DialogueUtils.continueWhilePossible();
                            DialogueUtils.solve(romeoDialogue);
                        }
                    } else {
                        NPCs.closest("Romeo").interact();
                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case FATHER_LAWRENCE: {
                if (!lawrenceArea.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(lawrenceArea, this));
                    return;
                }

                if (NPCs.closest("Father Lawrence") != null) {
                    // handle the dialogue here
                    if (Dialogues.inDialogue()) {
                        while (Dialogues.inDialogue()) {
                            DialogueUtils.continueWhilePossible();
                        }
                        state = State.POTION;
                        return;
                    } else {
                        NPCs.closest("Father Lawrence").interact();
                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case POTION: {
                HashMap<String, Integer> req = new HashMap<String, Integer>() { { put("Cadava berries", 1); } };
                if(!Inventory.contains("Cadava potion")) {
                    if (!Inventory.contains("Cadava berries") || (Inventory.contains("Cadava berries") && Inventory.get("Cadava berries").isNoted())) {
                        TaskManager.getInstance().setCurrentTask(new BankingTask(req, null, 1,this));
                        return;
                    }

                    if (!potionArea.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(potionArea, this));
                        return;
                    }

                    if (NPCs.closest("Apothecary") != null) {
                        // handle the dialogue here
                        if (Dialogues.inDialogue()) {
                            while (Dialogues.inDialogue()) {
                                DialogueUtils.continueWhilePossible();
                                DialogueUtils.solve(potionDialogue);
                            }
                        } else {
                            NPCs.closest("Apothecary").interact();
                            Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                        }
                    }
                } else {
                    state = State.JULIET_POTION;
                    return;
                }

                break;
            }

            case JULIET_POTION: {
                if (!julietArea.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(julietArea, this));
                    return;
                }

                if (NPCs.closest("Juliet") != null) {
                    if (Dialogues.inDialogue()) {
                        DialogueUtils.continueWhilePossible();
                        state = State.JULIET_CUTSCENE;
                    } else {
                        NPCs.closest("Juliet").interact();
                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                } else {
                    state = State.JULIET_CUTSCENE;
                }

                break;
            }

            case JULIET_CUTSCENE: {
                Sleep.sleep(2000, 3000);
                DialogueUtils.continueWhilePossible();

                if(NPCs.closest("Juliet") == null) {
                    state = State.CRYPT;
                    return;
                }

                break;
            }

            case CRYPT: {
                if (!romeoArea.contains(Players.getLocal())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(romeoArea, this));
                    return;
                }

                if (NPCs.closest("Romeo") != null) {
                    if (Dialogues.inDialogue()) {
                        DialogueUtils.continueWhilePossible();
                        state = State.CRYPT_CUTSCENE;
                        return;
                    } else {
                        NPCs.closest("Romeo").interact();
                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case CRYPT_CUTSCENE: {
                DialogueUtils.continueWhilePossible();
                break;
            }
        }
    }

    @Override
    public String getName() {
        return "Romeo and Juliet";
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(FreeQuest.ROMEO_AND_JULIET) || Quests.isStarted(FreeQuest.ROMEO_AND_JULIET);
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 650;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return FreeQuest.ROMEO_AND_JULIET;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
