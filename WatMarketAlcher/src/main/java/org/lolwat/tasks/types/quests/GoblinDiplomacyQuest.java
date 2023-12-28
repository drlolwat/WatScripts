package org.lolwat.tasks.types.quests;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
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

    private boolean hasOrange = false;
    private boolean hasBlue = false;

    private final HashMap<String, Integer> needed = new HashMap<String, Integer>() {{
        put("Goblin mail", 3);
        put("Blue dye", 1);
        put("Orange dye", 1);
    }};

    public GoblinDiplomacyQuest() {
    }

    @Override
    public void execute(WatAIO instance) {
        //

        // Check if the bank window is open and close it if necessary
        if (Bank.isOpen()) {
            Bank.close();
            Sleep.sleep(300, 600); // Wait for the bank to close
        }

        boolean hasOrangeMail = Inventory.contains("Orange goblin mail");
        boolean hasBlueMail = Inventory.contains("Blue goblin mail");

        // Update flags based on the presence of dyed goblin mails
        hasOrange = hasOrange || hasOrangeMail;
        hasBlue = hasBlue || hasBlueMail;

        if (!hasDoneDyes()) {
            for (java.util.Map.Entry<String, Integer> kv : needed.entrySet()) {
                if (hasBlue && hasOrange) {
                    continue;
                }

                if (!Inventory.contains(kv.getKey()) ||
                        (Inventory.contains(kv.getKey()) && Inventory.get(kv.getKey()).isNoted()) ||
                        (Inventory.contains(kv.getKey()) && Inventory.count(kv.getKey()) < kv.getValue())) {
                    instance.currentTask = new BankingTask(null, needed, null, 1, this);
                    return;
                }
            }

            // Perform dyeing if necessary
            if(!hasBlue && Inventory.contains("Blue dye")) {
                dyeMail("Blue");
            }
            if(!hasOrange && Inventory.contains("Orange dye")) {
                dyeMail("Orange");
            }
        }

        // Proceed with the quest if it's started, or the dyes are done
        if (Quests.isStarted(FreeQuest.GOBLIN_DIPLOMACY) || hasDoneDyes()) {
            proceedWithQuestDialogue(instance);
            return;
        }

        if (!startLocation.contains(Players.getLocal())) {
            instance.currentTask = new TraversalTask(startLocation, this);
            return;
        }

        if (!Quests.isStarted(FreeQuest.GOBLIN_DIPLOMACY) && NPCs.closest("General Bentnoze") != null) {
            if (Dialogues.inDialogue()) {
                while (Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(startDialogue);
                }
            } else {
                NPCs.closest("General Bentnoze").interact();
            }
        }
    }

    private void proceedWithQuestDialogue(WatAIO instance) {
        if (!startLocation.contains(Players.getLocal())) {
            instance.currentTask = new TraversalTask(startLocation, this);
        } else if (NPCs.closest("General Bentnoze") != null) {
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

    private boolean dyeMail(String color) {
        if (Inventory.count("Goblin mail") > 1 && Inventory.contains(color + " dye")) {
            if (Inventory.get(color + " dye").interact("Use")) {
                Sleep.sleep(50, 200);
                if (Inventory.get("Goblin mail").interact()) {
                    Sleep.sleep(50, 200);
                    boolean hasOrangeMail = Inventory.contains("Orange goblin mail");
                    boolean hasBlueMail = Inventory.contains("Blue goblin mail");
                    if ((color.equals("Orange") && hasOrangeMail) || (color.equals("Blue") && hasBlueMail)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasDoneDyes() {
        return hasOrange && hasBlue;
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
