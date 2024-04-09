package org.lolwat.tasks.quests.old;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
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

public class DruidicRitualQuest implements WatTask {
    private final Area stoneCircle = new Area(2923, 3486, 2928, 3481);
    private final Area sanfewDownstairs = new Area(2896, 3431, 2900, 3426);
    private final Area sanfewUpstairs = new Area(2895, 3431, 2900, 3426, 1);
    private final Area cauldronDungeon = new Area(2809, 9859, 2962, 9736);
    private final Area cauldronOfThunder = new Area(2891, 9833, 2895, 9828);
    private final List<String> kaqemeexDialogue = Arrays.asList("Who are you?", "What about the stone circle full of dark wizards?", "Yes.");
    private final List<String> sanfewFirstDialogue = Arrays.asList("I've been sent to help purify the Varrock stone circle.", "Ok, I'll do that then.");
    private boolean geItemsAcquired = false;
    private boolean questStarted = false;
    private boolean spokeToSanfew = false;
    private boolean enteredDungeon = false;
    private boolean meatDipped = false;
    private boolean spokeToSanfewAfterEnchantment = false;
    private boolean questCompleted = false;
    private HashMap<String, Integer> needed = new HashMap<String, Integer>() {{
        put("Raw rat meat", 1);
        put("Raw bear meat", 1);
        put("Raw beef", 1);
        put("Raw chicken", 1);
    }};

    @Override
    public void execute(WatAIO instance) {
        if (!geItemsAcquired) {
            acquireGEItems(instance);
        } else if (!questStarted) {
            startQuest(instance);
        } else if (!spokeToSanfew) {
            talkToSanfew(instance);
        } else if (!enteredDungeon) {
            enterCauldronDungeon(instance);
        } else if (!meatDipped) {
            dipMeat(instance);
        } else if (!spokeToSanfewAfterEnchantment) {
                talkToSanfewAfterEnchantment(instance);
        } else {
            completeQuest(instance);
        }
    }

    private void acquireGEItems(WatAIO instance) {
        if (!Inventory.containsAll(needed.keySet())) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(needed, new HashMap<>(), 1, this));
            return;
        }

        if (Inventory.containsAll(needed.keySet())) {
            geItemsAcquired = true;
        }

        geItemsAcquired = Inventory.containsAll(needed.keySet());
    }

    private void startQuest(WatAIO instance) {
        if (Quests.isFinished(PaidQuest.DRUIDIC_RITUAL)) {
            questStarted = true;
            return;
        }

        if (!stoneCircle.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(stoneCircle, this));
            return;
        }

        NPC kaqemeex = NPCs.closest("Kaqemeex");
        if (kaqemeex != null) {
            if (!Dialogues.inDialogue()) {
                kaqemeex.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), Calculations.random(3000, 5000));
            }

            if (Dialogues.inDialogue()) {
                while (Dialogues.inDialogue()) {
                    if (Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    } else {
                        DialogueUtils.solve(kaqemeexDialogue);
                    }
                }
                questStarted = Quests.isStarted(PaidQuest.DRUIDIC_RITUAL);
            }
        }
    }

    private void talkToSanfew(WatAIO instance) {
        if (!sanfewDownstairs.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(sanfewDownstairs, this));
            return;
        }

        if (GameObjects.closest("Staircase") != null) {
            GameObjects.closest("Staircase").interact("Climb-up");
            Sleep.sleepUntil(() -> sanfewUpstairs.contains(Players.getLocal()), 5000);
            NPC sanfew = NPCs.closest("Sanfew");
            if (sanfew != null) {
                if (!Dialogues.inDialogue()) {
                    sanfew.interact("Talk-to");
                    Sleep.sleepUntil(() -> Dialogues.inDialogue(), Calculations.random(3000, 5000));
                }

                if (Dialogues.inDialogue()) {
                    while (Dialogues.inDialogue()) {
                        if (Dialogues.canContinue()) {
                            Dialogues.spaceToContinue();
                        } else {
                            DialogueUtils.solve(sanfewFirstDialogue);
                        }
                    }
                    spokeToSanfew = true;
                }
            }
        }
    }

    private void enterCauldronDungeon(WatAIO instance) {
        if (!Inventory.contains("Enchanted beef", "Enchanted rat", "Enchanted bear", "Enchanted chicken")) {
            if (!cauldronDungeon.contains(Players.getLocal()) && !cauldronOfThunder.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(cauldronDungeon, this));
            } else if (cauldronDungeon.contains(Players.getLocal())) {
                GameObjects.closest("Ladder").interact("Climb-down");
                TaskManager.getInstance().setCurrentTask(new TraversalTask(cauldronOfThunder, this));
                Sleep.sleepUntil(() -> cauldronOfThunder.contains(Players.getLocal()), 5000);
                enteredDungeon = true;
            }
            else if (cauldronOfThunder.contains(Players.getLocal())) {
                enteredDungeon = true;
            }
        }
    }

    private void dipMeat(WatAIO instance) {
        GameObject cauldron = GameObjects.closest("Cauldron of Thunder");
        if (cauldron != null && cauldronOfThunder.contains(Players.getLocal())) {
            // Array of all meats to attempt dipping
            String[] meats = {"Raw beef", "Raw rat meat", "Raw bear meat", "Raw chicken"};
            boolean allMeatDipped = true;

            for (String meat : meats) {
                if (Inventory.contains(meat)) {
                    allMeatDipped &= attemptDipMeat(cauldron, meat);
                }
            }

            // Only mark as complete if all meats have been successfully dipped
            meatDipped = allMeatDipped;
        }
    }

    private boolean attemptDipMeat(GameObject cauldron, String meat) {
        final long startTime = System.currentTimeMillis();
        final long timeout = 15000; // 15 seconds timeout for each attempt

        while (System.currentTimeMillis() - startTime < timeout) {
            if (!Inventory.contains(meat)) {
                // Meat has been dipped successfully or was lost, stop attempting
                return true;
            }

            Inventory.interact(meat, "Use");
            cauldron.interact("Use");

            Sleep.sleepUntil(() -> !Inventory.contains(meat), 60000);

            if (!Inventory.contains(meat)) {
                // Confirm meat has been dipped before moving to next meat
                return true;
            } else {
                // Wait a bit before trying again if meat is still in inventory
                Sleep.sleep(1000);
            }
        }

        // Return false if timeout reached and meat could still be in the inventory
        return !Inventory.contains(meat);
    }

    private void talkToSanfewAfterEnchantment(WatAIO instance) {
        if (!sanfewDownstairs.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(sanfewDownstairs, this));
            return;
        }

        if (GameObjects.closest("Staircase") != null) {
            GameObjects.closest("Staircase").interact("Climb-up");
            Sleep.sleepUntil(() -> sanfewUpstairs.contains(Players.getLocal()), 5000);
            NPC sanfew = NPCs.closest("Sanfew");
            if (sanfew != null) {
                if (!Dialogues.inDialogue()) {
                    sanfew.interact("Talk-to");
                    Sleep.sleepUntil(() -> Dialogues.inDialogue(), Calculations.random(3000, 5000));
                }

                if (Dialogues.inDialogue()) {
                    while (Dialogues.inDialogue()) {
                        if (Dialogues.canContinue()) {
                            Dialogues.spaceToContinue();
                        }
                    }
                    spokeToSanfewAfterEnchantment = true;
                }
            }
        }
    }

    private void completeQuest(WatAIO instance) {
        if (!stoneCircle.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(stoneCircle, this));
            return;
        }

        NPC kaqemeex = NPCs.closest("Kaqemeex");
        if (kaqemeex != null) {
            kaqemeex.interact("Talk-to");
            DialogueUtils.continueWhilePossible();
            questCompleted = Quests.isFinished(PaidQuest.DRUIDIC_RITUAL);
        }
    }

    @Override
    public String getName() {
        return "Druidic Ritual";
    }

    @Override
    public boolean canPerformTask() {
        return GenericUtils.isMember() && (!Quests.isFinished(PaidQuest.DRUIDIC_RITUAL) || !Quests.isStarted(PaidQuest.DRUIDIC_RITUAL));
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
        // Not used in this quest, but required by interface.
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