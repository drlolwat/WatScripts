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

public class DoricsQuest implements WatTask {
    private final Area startLocation = new Area(2950, 3449, 2953, 3452);
    private final List<String> startDialogue = Arrays.asList("I wanted to use your anvils.", "Yes.");

    private final HashMap<String, Integer> needed = new HashMap<String, Integer>() { {
        put("Clay", 6);
        put("Copper ore", 4);
        put("Iron ore", 2);
    } };

    public DoricsQuest() {
    }

    @Override
    public void execute(WatAIO instance) {
        //check for items
        for (java.util.Map.Entry<String, Integer> kv : needed.entrySet()) {
            if (!Inventory.contains(kv.getKey()) ||
                    (Inventory.contains(kv.getKey()) && Inventory.get(kv.getKey()).isNoted()) ||
                    (Inventory.contains(kv.getKey()) && Inventory.count(kv.getKey()) < kv.getValue())) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(needed, null, 1, this));
                return;
            }
        }

        //go to start location
        if (!startLocation.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, this));
            return;
        }

        if (NPCs.closest("Doric") != null) {
            // handle the dialogue here
            if (Dialogues.inDialogue()) {
                while (Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(startDialogue);
                }
            } else {
                NPCs.closest("Doric").interact();
            }
        }
    }

    @Override
    public String getName() {
        return "Doric's Quest";
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(FreeQuest.DORICS_QUEST);
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
        return FreeQuest.DORICS_QUEST;
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
