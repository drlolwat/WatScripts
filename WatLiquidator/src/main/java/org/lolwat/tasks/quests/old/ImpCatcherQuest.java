package org.lolwat.tasks.quests.old;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.skills.Skill;
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

public class ImpCatcherQuest implements WatTask {
    private Area startLocation = new Area(
            new Tile(3102, 3163, 2),
            new Tile(3103, 3162, 2),
            new Tile(3105, 3162, 2),
            new Tile(3106, 3162, 2),
            new Tile(3105, 3166, 2),
            new Tile(3102, 3165, 2));

    List<String> startDialogue = Arrays.asList("Give me a quest please.", "Give me a quest or else!", "Just stop messing around and give me a quest!", "Yes.");

    private HashMap<String, Integer> needed = new HashMap<String, Integer>() { {
        put("Black bead", 1);
        put("Yellow bead", 1);
        put("Red bead", 1);
        put("White bead", 1);
    } };

    public ImpCatcherQuest() {
        // Wizard Mizgog
    }

    @Override
    public void execute(WatAIO instance) {
        //check for items
        for (String i : needed.keySet()) {
            if (!Inventory.contains(i) || (Inventory.contains(i) && Inventory.get(i).isNoted()) || (Inventory.contains(i) && Inventory.count(i) < needed.get(i))) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(needed, null, 1, this));
                return;
            }
        }

        //go to start location
        if (!startLocation.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, this));
            return;
        }

        if (NPCs.closest("Wizard Mizgog") != null) {
            // handle the dialogue here
            if (Dialogues.inDialogue()) {
                while (Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(startDialogue);
                }
            } else {
                NPCs.closest("Wizard Mizgog").interact();
            }
        }
    }

    @Override
    public String getName() {
        return "Imp Catcher";
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(FreeQuest.IMP_CATCHER);
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
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
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
