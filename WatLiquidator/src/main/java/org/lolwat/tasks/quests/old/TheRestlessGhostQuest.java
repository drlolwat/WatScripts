package org.lolwat.tasks.quests.old;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TheRestlessGhostQuest implements WatTask {
    private final Area lumbridgeChurch = new Area(3240, 3211, 3247, 3204);
    private final Area lumbridgeSwamp = new Area(3144, 3177, 3151, 3173);
    private final Area graveyard = new Area(3247, 3195, 3251, 3190);
    private final Area swampRocks = new Area(3236, 3148, 3229, 3142);
    private final Area wizardTower = new Area(3111, 9569, 3121, 9564);


    private final List<String> fatherAereckDialogue = Arrays.asList("I'm looking for a quest!", "Ok, let me help then.", "Yes.");
    private final List<String> fatherUrhneyDialogue = Arrays.asList("Father Aereck sent me to talk to you.", "He's got a ghost haunting his graveyard.");
    private final List<String> ghost0Dialogue = Arrays.asList("Yep, now tell me what the problem is.");

    private boolean questStarted = false;
    private boolean amuletAcquired = false;
    private boolean talkedToGhost = false;
    private boolean skullAcquired = false;
    private boolean questCompleted = false;

    @Override
    public void execute(WatAIO instance) {
        if (!questStarted) {
            startQuest(instance);
        } else if (!amuletAcquired) {
            acquireAmulet(instance);
        } else if (!talkedToGhost) {
            talkToRestlessGhost(instance);
        } else if (!skullAcquired) {
            retrieveSkull(instance);
        } else {
            completeQuest(instance);
        }
    }

    private void startQuest(WatAIO instance) {
        if (Inventory.isFull()) {
            Bank.open();
            Sleep.sleepUntil(() -> Bank.isOpen(), Calculations.random(1000, 2000));
            Bank.depositAllItems();
            Sleep.sleepUntil(() -> Inventory.isEmpty(), Calculations.random(1000, 2000));
            return;
        }

        if (!lumbridgeChurch.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(lumbridgeChurch, this));
            return;
        }

        NPC fatherAereck = NPCs.closest("Father Aereck");
        if (fatherAereck != null) {
            interactWithNPC(fatherAereck, fatherAereckDialogue);
            questStarted = Quests.isStarted(FreeQuest.THE_RESTLESS_GHOST);
        }
    }

    private void acquireAmulet(WatAIO instance) {
        if (!lumbridgeSwamp.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(lumbridgeSwamp, this));
            return;
        }

        NPC fatherUrhney = NPCs.closest("Father Urhney");
        if (fatherUrhney != null) {
            interactWithNPC(fatherUrhney, fatherUrhneyDialogue);
            amuletAcquired = Inventory.contains("Ghostspeak amulet");
        }
    }

    // Go to graveyard and interact with coffin then speak to ghost
    private void talkToRestlessGhost(WatAIO instance) {
        if (!graveyard.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(graveyard, this));
            return;
        }

        if (Inventory.contains("Ghostspeak amulet")) {
            Inventory.interact("Ghostspeak amulet", "Wear");
        }

        GameObject coffin = GameObjects.closest("Coffin");
        NPC ghost = NPCs.closest("Restless ghost");
        if (coffin != null) {
            // search or open coffin if not in dialogue and ghost is null
            if (!Dialogues.inDialogue() && ghost == null) {
                coffin.interact();
                Sleep.sleep(1000, 1500);
            } else if (ghost != null) {
                ghost.interact("Talk-to");
                Sleep.sleep(1000, 1500);
                if (Dialogues.inDialogue()) {
                    while (Dialogues.inDialogue()) {
                        if (Dialogues.canContinue()) {
                            Dialogues.spaceToContinue();
                        } else {
                            DialogueUtils.solve(ghost0Dialogue);
                            Sleep.sleepUntil(() -> !Dialogues.inDialogue(), Calculations.random(1000, 1500));
                            talkedToGhost = true;
                        }
                    }
                }
            }
        }
    }


    private void retrieveSkull(WatAIO instance) {
        if (!wizardTower.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(wizardTower, this));
            return;
        }

        if (!Inventory.contains("Ghost's skull")) {
            GameObject altar = GameObjects.closest("Altar");
            if (altar != null) {
                altar.interact("Search");
            }
        } else {
            skullAcquired = true;
        }
    }

    private void completeQuest(WatAIO instance) {
        if (!graveyard.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(graveyard, this));
            return;
        }

        GameObject coffin = GameObjects.closest("Coffin");
        if (coffin != null) {
            if (!coffin.hasAction("Close")) {
                coffin.interact("Open");
                Sleep.sleep(1000, 3000);
            }
            coffin.interact("Search");
            DialogueUtils.continueWhilePossible();
            Sleep.sleepUntil(() -> !Dialogues.inDialogue(), Calculations.random(1000, 1500));
            questCompleted = Quests.isFinished(FreeQuest.THE_RESTLESS_GHOST);
        }
    }

    private void interactWithNPC(NPC npc, List<String> dialogueOptions) {
        if (!Dialogues.inDialogue()) {
            npc.interact("Talk-to");
            Sleep.sleepUntil(() -> Dialogues.inDialogue(), Calculations.random(1000, 2000));
        }

        if (Dialogues.inDialogue()) {
            DialogueUtils.continueWhilePossible();
            DialogueUtils.solve(dialogueOptions);
            DialogueUtils.continueWhilePossible();
        }
    }

    @Override
    public String getName() {
        return "The Restless Ghost";
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(FreeQuest.THE_RESTLESS_GHOST) || Quests.isStarted(FreeQuest.THE_RESTLESS_GHOST);
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