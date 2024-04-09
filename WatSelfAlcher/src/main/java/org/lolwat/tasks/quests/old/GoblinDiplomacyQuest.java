package org.lolwat.tasks.quests.old;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GoblinDiplomacyQuest implements WatTask {
    private final Area startLocation = new Area(
            new Tile(2954, 3512, 0),
            new Tile(2954, 3510, 0),
            new Tile(2962, 3510, 0),
            new Tile(2962, 3514, 0),
            new Tile(2956, 3514, 0));

    private final List<String> startDialogue = Arrays.asList(
            "Do you want me to pick an armour colour for you?",
            "Yes.",
            "You should wear red.",
            "What about a different colour?",
            "Okay, I'll be back soon.",
            "I'll leave you to it.");
    private final List<String> completeDialogue = Arrays.asList(
            "Do you want me to pick an armour colour for you?",
            "What about a different colour?",
            "Yes.",
            "You should wear red.",
            "No, he doesn't look fat.",
            "I have some orange armour here.",
            "I have some blue armour here.",
            "I have some brown armour here.");

    boolean traversed = false;

    public GoblinDiplomacyQuest() { }

    @Override
    public void execute() {
        if (Bank.isOpen()) {
            Bank.close();
            Sleep.sleep(300, 600);
        }

        if(GenericUtils.notedOrNull("Orange goblin mail") && !traversed) {
            if(GenericUtils.notedOrNull("Goblin mail") && GenericUtils.notedOrNull("Orange dye")) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                    {
                        put("Goblin mail", 1);
                        put("Orange dye", 1);
                    }
                }, null, 1, this));
                return;
            } else {
                if(Inventory.interact("Orange dye")) {
                    Sleep.sleep(50, 120);
                    if(!Inventory.interact("Goblin mail")) {
                        Logger.error("problem dying goblin mail orange");
                        return;
                    }
                } else {
                    return;
                }
            }
        }

        if(GenericUtils.notedOrNull("Orange goblin mail") && !traversed) return;
        if(GenericUtils.notedOrNull("Blue goblin mail") && !traversed) {
            if(GenericUtils.notedOrNull("Goblin mail") && GenericUtils.notedOrNull("Blue dye")) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                    {
                        put("Orange goblin mail", 1);
                        put("Goblin mail", 1);
                        put("Blue dye", 1);
                    }
                }, null, 1, this));
                return;
            } else {
                if(Inventory.interact("Blue dye")) {
                    Sleep.sleep(50, 120);
                    if(!Inventory.interact("Goblin mail")) {
                        Logger.error("problem dying goblin mail blue");
                        return;
                    }
                }
            }
        }

        Sleep.sleep(200, 500);
        if(GenericUtils.notedOrNull("Blue goblin mail") && !traversed) return;
        if(GenericUtils.notedOrNull("Goblin mail") && !traversed) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                {
                    put("Orange goblin mail", 1);
                    put("Blue goblin mail", 1);
                    put("Goblin mail", 1);
                }
            }, null, 1, this));
            return;
        }

        if (NPCs.closest("General Bentnoze") == null) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, this));
        } else {
            if (!Dialogues.inDialogue()) {
                NPCs.closest("General Bentnoze").interact();
                Sleep.sleepUntil(Dialogues::inDialogue, Calculations.random(1000, 1500));
            } else {
                if(!traversed)
                    traversed = true;

                DialogueUtils.continueWhilePossible();
                if (Dialogues.getOptions() != null) {
                    DialogueUtils.solve(Quests.isStarted(FreeQuest.GOBLIN_DIPLOMACY) ? completeDialogue : startDialogue);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Goblin Diplomacy";
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(FreeQuest.GOBLIN_DIPLOMACY);
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
        // Method implementation
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
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
