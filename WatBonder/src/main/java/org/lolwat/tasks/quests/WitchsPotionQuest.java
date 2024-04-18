package org.lolwat.tasks.quests;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.QuestTask;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WitchsPotionQuest implements QuestTask {
    private final Area startLocation = new Area(2965, 3208, 2970, 3203);
    List<String> startDialogue = Arrays.asList("I am in search of a quest.", "Yes.");
    private final Area ratLocation = new Area(2953, 3205, 2960, 3202);

    @Override
    public Quest completes() {
        return FreeQuest.WITCHS_POTION;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }

            case 1:
            case 0: {
                if(Players.getLocal().isInCombat()) {
                    return;
                }

                if (!Inventory.contains(x -> x != null && !x.isNoted()
                        && (x.getName().equals("Eye of newt") || x.getName().equals("Onion")))) {
                    TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {{
                        put("Onion", 1);
                        put("Eye of newt", 1);
                    }}, null, 1, wrapper));
                    return;
                }

                if(!Inventory.contains("Burnt meat")) {
                    if(!Inventory.contains(x -> x != null
                            && (x.getName().equals("Raw beef") || x.getName().equals("Cooked meat")))) {

                        TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {{
                            put("Raw beef", 1);
                        }}, null, 1, wrapper));
                        return;
                    }

                    Area cookingArea = new Area(2963, 3215, 2969, 3210);
                    GameObject range = GameObjects.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Range"));

                    if(!cookingArea.contains(Players.getLocal()) && range == null) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(cookingArea, wrapper));
                        return;
                    }

                    if(range != null) {
                        if(!range.isOnScreen()) {
                            Camera.rotateToEntity(range);
                            Sleep.sleepUntil(range::isOnScreen, 5000);
                            return;
                        }

                        if(Inventory.contains(x -> x != null && x.getName().equals("Raw beef") && !x.isNoted())) {
                            if (Inventory.use("Raw beef") && Mouse.click(range.getClickablePoint())) {
                                Sleep.sleepUntil(() -> Inventory.contains("Burnt meat"), 5000);
                            }
                        } else if(Inventory.contains(x -> x != null && x.getName().equals("Cooked meat") && !x.isNoted())) {
                            if (Inventory.use("Cooked meat") && Mouse.click(range.getClickablePoint())) {
                                Sleep.sleepUntil(() -> Inventory.contains("Burnt meat"), 5000);
                            }
                        }
                    }

                    return;
                }

                if(Quests.isStarted(completes()) && !Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Rat's tail"))) {
                    NPC rat = NPCs.closest(x -> x != null && x.exists() && x.getName().equals("Rat") && x.canReach());
                    if(!ratLocation.contains(Players.getLocal()) && rat == null) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(ratLocation, wrapper));
                        return;
                    }

                    GroundItem i = GroundItems.closest("Rat's tail");
                    if(i == null || !i.exists()) {
                        if (rat != null) {
                            if (!rat.interact("Attack")) {
                                Logger.log("Failed to interact with rat");
                                return;
                            }

                            Sleep.sleepUntil(() -> !rat.exists() && Players.getLocal().isInCombat(), 5000);
                        }
                    } else {
                        if(!i.interact("Take")) {
                            Logger.log("Failed to grab tail");
                            return;
                        }

                        Sleep.sleepUntil(() -> Inventory.contains("Rat's tail"), 5000);
                    }

                    return;
                }

                NPC hetty = NPCs.closest(x -> x != null && x.exists() && x.getName().equals("Hetty") && x.canReach());
                if(!startLocation.contains(Players.getLocal()) && hetty == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(startDialogue);
                } else {
                    if(hetty != null) {
                        if(!hetty.interact("Talk-to")) {
                            Logger.log("Failed to interact with Hetty");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case 2: {
                GameObject cauldron = GameObjects.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Cauldron"));
                if(!startLocation.contains(Players.getLocal()) && cauldron == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                } else {
                    if (cauldron != null) {
                        if (!cauldron.interact("Drink from")) {
                            Logger.log("Failed to interact with cauldron");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(completes());
    }

    @Override
    public int getState() {
        return PlayerSettings.getConfig(67);
    }

    @Override
    public List<String> inventoryTolerated() {
        return Arrays.asList("Burnt meat", "Eye of newt", "Onion", "Raw beef");
    }
}