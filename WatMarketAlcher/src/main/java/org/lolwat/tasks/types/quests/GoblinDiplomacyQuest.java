package org.lolwat.tasks.types.quests;

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
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;

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

    private final List<String> startDialogue = Arrays.asList("Do you want me to pick an armour colour for you?", "Yes.", "You should wear red.", "What about a different colour?", "Okay, I'll be back soon.", "I'll leave you to it.");
    private final List<String> completeDialogue = Arrays.asList("No, he doesn't look fat.", "I have some orange armour here.", "I have some blue armour here.", "I have some brown armour here.");
    private boolean hasOrange = false;
    private boolean hasBlue = false;

    private final HashMap<String, Integer> needed = new HashMap<String, Integer>() { {
        put("Goblin mail", 3);
        put("Blue dye", 1);
        put("Orange dye", 1);
    } };

    private boolean hasDoneDyes() {
        return hasOrange && hasBlue;
    }

    public GoblinDiplomacyQuest() {
    }

    @Override
    public void execute(WatAIO instance) {
        if(!hasDoneDyes()) {
            for (java.util.Map.Entry<String, Integer> kv : needed.entrySet()) {
                if (!Inventory.contains(kv.getKey()) ||
                        (Inventory.contains(kv.getKey()) && Inventory.get(kv.getKey()).isNoted()) ||
                        (Inventory.contains(kv.getKey()) && Inventory.count(kv.getKey()) < kv.getValue())) {
                    instance.currentTask = new BankingTask(null, needed, null, 1, this);
                    return;
                }
            }
        }

        //go to start location
        if (!startLocation.contains(Players.getLocal())) {
            instance.currentTask = new TraversalTask(startLocation, this);
            return;
        }

        if(!Quests.isStarted(FreeQuest.GOBLIN_DIPLOMACY)) {
            if (NPCs.closest("General Bentnoze") != null) {
                // handle the dialogue here
                if (Dialogues.inDialogue()) {
                    while (Dialogues.inDialogue()) {
                        DialogueUtils.continueWhilePossible();
                        DialogueUtils.solve(startDialogue);
                    }
                } else {
                    NPCs.closest("General Bentnoze").interact();
                }
            }
        } else {
            if(!hasBlue && dyeMail("Blue"))
                hasBlue = true;

            if(!hasOrange && dyeMail("Orange"))
                hasOrange = true;

            if(hasDoneDyes()) {
                if (NPCs.closest("General Bentnoze") != null) {
                    // handle the dialogue here
                    if (Dialogues.inDialogue()) {
                        while (Dialogues.inDialogue()) {
                            DialogueUtils.continueWhilePossible();
                            DialogueUtils.solve(completeDialogue);
                        }
                    } else {
                        NPCs.closest("General Bentnoze").interact();
                    }
                }
            }
        }
    }

    private boolean dyeMail(String color) {
        if(Inventory.count("Goblin mail") == 3) {
            while(Inventory.count("Goblin mail") > 1) {
                if(Inventory.contains(color + " dye")) {
                    if(Inventory.get(color + " dye").interact("Use")) {
                        Sleep.sleep(50, 200);
                        if(Inventory.get("Goblin mail").interact()) {
                            Sleep.sleep(50, 200);
                        }
                    }
                }
            }
            return Inventory.count("Goblin mail") == 1;
        }
        return false;
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

    }

    @Override
    public Skill trainsSkill() {
        return null;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public Quest completesQuest() {
        return FreeQuest.GOBLIN_DIPLOMACY;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
    }
}
