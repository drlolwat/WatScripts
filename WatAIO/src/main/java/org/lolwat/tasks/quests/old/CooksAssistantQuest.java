package org.lolwat.tasks.quests.old;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
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

public class CooksAssistantQuest implements WatTask {
    private final Area startLocation = new Area(3205, 3217, 3212, 3212);
    private final List<String> startDialogue = Arrays.asList("What's wrong?", "Yes.");

    public CooksAssistantQuest() {
    }

    @Override
    public void execute(WatAIO instance) {
        //check for items
        for (String i : inventoryRequired().keySet()) {
            if (!Inventory.contains(i) || (Inventory.contains(i) && Inventory.get(i).isNoted()) || (Inventory.contains(i) && Inventory.count(i) < inventoryRequired().get(i))) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryRequired(), null, 1, this));
                return;
            }
        }

        //go to start location
        if (!startLocation.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, this));
            return;
        }

        if (NPCs.closest("Cook") != null) {
            // handle the dialogue here
            if (Dialogues.inDialogue()) {
                while (Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(startDialogue);
                }
            } else {
                NPCs.closest("Cook").interact();
            }
        }
    }

    @Override
    public String getName() {
        return "Cooks Assistant";
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(FreeQuest.COOKS_ASSISTANT);
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
        return FreeQuest.COOKS_ASSISTANT;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
    }

    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {{
            put("Egg", 1);
            put("Bucket of milk", 1);
            put("Pot of flour", 1); }};
    }
}
