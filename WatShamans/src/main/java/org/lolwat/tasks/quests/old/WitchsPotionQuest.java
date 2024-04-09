package org.lolwat.tasks.quests.old;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.Shop;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.items.Item;
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

public class WitchsPotionQuest implements WatTask {

    private final Area startLocation = new Area(2965, 3208, 2970, 3203);
    private final Area rangeLocation = new Area(2968, 3211, 2969, 3209);
    private final Area ratLocation = new Area(2953, 3205, 2960, 3202);
    private final Area portSarimMagicShop = new Area(3011, 3261, 3016, 3256);
    List<String> startDialogue = Arrays.asList("I am in search of a quest.", "Yes.");
    List<String> bettyDialogue = Arrays.asList("Can I see your wares?");

    private HashMap<String, Integer> needed = new HashMap<String, Integer>() {{
        put("Raw beef", 1);
        put("Onion", 1);
    }};

    boolean geItemsAcquired = false;
    boolean goldAcquired = false;
    boolean newtAcquired = false;
    boolean beefCooked = false;
    boolean ratTailAcquired = false;
    boolean questStarted = false;

    public WitchsPotionQuest() {
    }

    @Override
    public void execute(WatAIO instance) {
        if (!geItemsAcquired) {
            acquireGEItems(instance);
        } else if (!goldAcquired) {
            acquireGold();
        } else if (!newtAcquired) {
            acquireEyeOfNewt(instance);
        } else if (!beefCooked) {
            cookBeef(instance);
        } else if (!questStarted) {
            startQuest(instance);
        } else if (!ratTailAcquired) {
            acquireRatsTail(instance);
        } else {
            completeQuest(instance);
        }
    }

    private void acquireGEItems(WatAIO instance) {
        if (!Inventory.containsAll(needed.keySet())) {
            TaskManager.getInstance().setCurrentTask(new BankingTask(needed, new HashMap<>(), 1, this));
            return;
        }

        if (Inventory.contains("Burnt meat") && Inventory.contains("Onion")) {
            geItemsAcquired = true;
        }

        geItemsAcquired = Inventory.containsAll(needed.keySet());
    }

    private void acquireGold() {
        if (Inventory.count("Coins") < 3) {
            Bank.open();
            if (Bank.isOpen() && Bank.contains("Coins")) {
                Bank.withdraw("Coins", 3);
                Bank.close();
                if (Inventory.contains("Coins")) {
                    goldAcquired = true;
                }
            }
        }
    }

    private void acquireEyeOfNewt(WatAIO instance) {
        if (Inventory.contains("Eye of newt")) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, this)); // Proceed to the next task
            newtAcquired = true;
        } else {
            if (!portSarimMagicShop.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(portSarimMagicShop, this));
                return;
            }

            if (!Dialogues.inDialogue()) {
                NPC betty = NPCs.closest("Betty");
                if (betty != null) {
                    if (betty.interact()) {

                        Sleep.sleepUntil(() -> Dialogues.inDialogue(), Calculations.random(1000, 1500));
                        DialogueUtils.continueWhilePossible();
                        DialogueUtils.solve(bettyDialogue);
                        if (Shop.purchase("Eye of newt", 1)) {
                            Sleep.sleepUntil(() -> Inventory.contains("Eye of newt"), Calculations.random(1000, 1500));
                            Shop.close();
                            newtAcquired = true;
                        }
                    }
                }
            } else {
                DialogueUtils.continueWhilePossible();
                DialogueUtils.solve(bettyDialogue);
                if (Shop.purchase("Eye of newt", 1)) {
                    Sleep.sleepUntil(() -> Inventory.contains("Eye of newt"), Calculations.random(1000, 1500));
                    Shop.close();
                    newtAcquired = true;
                }
            }
        }
    }

    private void cookBeef(WatAIO instance) {
        if (Inventory.contains("Burnt meat")) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, this)); // Proceed to the next task
            beefCooked = true;
        } else {
            if (!rangeLocation.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(rangeLocation, this));
                return;
            }

            if (GameObjects.closest("Range") != null && GameObjects.closest("Range").interact()) {
                Item beef = Inventory.get("Raw beef");
                if (beef != null && beef.interact("Use")) {
                    GameObject range = GameObjects.closest("Range");
                    if (range != null && range.interact("Use")) {
                        Sleep.sleepUntil(() -> Inventory.contains("Cooked meat"), Calculations.random(1000, 1500));
                        Item cookedBeef = Inventory.get("Cooked meat");
                        if (cookedBeef != null && cookedBeef.interact("Use")) {
                            if (range.interact("Use")) {
                                Sleep.sleepUntil(() -> Inventory.contains("Burnt meat"), Calculations.random(1000, 1500));
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isRatBeingAttacked = false;

    private void acquireRatsTail(WatAIO instance) {
        if (questStarted && !Inventory.contains("Rat's tail")) {
            if (!ratLocation.contains(Players.getLocal())) {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(ratLocation, this));
                return;
            }

            if (!Inventory.contains("Rat's tail")) {
                if (!isRatBeingAttacked) {
                    NPC rat = NPCs.closest("Rat");
                    if (rat != null && rat.interact("Attack")) {
                        isRatBeingAttacked = true;
                        Sleep.sleepUntil(() -> !Players.getLocal().isInCombat(), Calculations.random(1000, 1500));
                    }

                } else {
                    if (!Players.getLocal().isInCombat()) {
                        GroundItem groundRatTail = GroundItems.closest("Rat's tail");
                        if (groundRatTail != null && groundRatTail.interact("Take")) {
                            Sleep.sleepUntil(() -> Inventory.contains("Rat's tail"), Calculations.random(1000, 1500));
                            ratTailAcquired = true;
                            isRatBeingAttacked = false;
                        }
                    }
                }
            } else {
                ratTailAcquired = true;
            }
        }

        if (Inventory.contains("Rat's tail")) {
            ratTailAcquired = true;
        }
    }

    private void startQuest(WatAIO instance) {
        if (!startLocation.contains(Players.getLocal()) && !Quests.isStarted(FreeQuest.WITCHS_POTION)) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, this));
            return;
        }

        NPC hetty = NPCs.closest("Hetty");
        if (hetty != null) {
            if (!Dialogues.inDialogue()) {
                hetty.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), Calculations.random(3000, 5000));
            }

            if (Dialogues.inDialogue()) {
                while (Dialogues.inDialogue()) {
                    if (Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    } else {
                        DialogueUtils.solve(startDialogue);
                    }
                }

                questStarted = Quests.isStarted(FreeQuest.WITCHS_POTION);
            }
        }
    }

    private boolean hettyDialogueComplete = false;

    private void completeQuest(WatAIO instance) {
        if (!startLocation.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, this));
            return;
        }

        NPC hetty = NPCs.closest("Hetty");
        if (hetty != null && !hettyDialogueComplete && !Dialogues.inDialogue()) {
            hetty.interact("Talk-to");
            Sleep.sleepUntil(() -> Dialogues.inDialogue(), Calculations.random(3000, 5000));
        }

        if (Dialogues.inDialogue()) {
            while (Dialogues.inDialogue()) {
                DialogueUtils.continueWhilePossible();
            }
            hettyDialogueComplete = true;
        }

        if (hettyDialogueComplete && !Quests.isFinished(FreeQuest.WITCHS_POTION)) {
            GameObject cauldron = GameObjects.closest("Cauldron");
            if (cauldron != null && cauldron.interact("Drink From")) {
                cauldron.interact("Drink From");
                DialogueUtils.continueWhilePossible();
            }
        }
    }

    @Override
    public String getName() {
        return "Witch's Potion";
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(FreeQuest.WITCHS_POTION) || Quests.isStarted(FreeQuest.WITCHS_POTION);
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
        return new HashMap<>();
    }

}