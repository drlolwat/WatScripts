package org.lolwat.tasks.quests;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.QuestTask;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.misc.ErnestTheChickenSolver;
import org.lolwat.tasks.misc.GatheringTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ErnestTheChickenQuest implements QuestTask {
    boolean killedFish = false;

    @Override
    public Quest completes() {
        return FreeQuest.ERNEST_THE_CHICKEN;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }

            case 0: {
                NPC veronica = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Veronica"));
                Area veronicaArea = new Area(3110, 3330, 3112, 3328);

                if(!veronicaArea.contains(Players.getLocal()) && veronica == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(veronicaArea, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Collections.singletonList("Yes."));
                }
                else {
                    if (veronica != null) {
                        if (!veronica.interact("Talk-to")) {
                            Logger.log("Failed to interact with Veronica");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case 1: {
                NPC professor = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Professor Oddenstein"));
                Area professorArea = new Area(3108, 3368, 3111, 3362, 2);

                if(!professorArea.contains(Players.getLocal()) && professor == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(professorArea, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(Arrays.asList("I'm looking for a guy called Ernest.", "Change him back this instant!"));
                }
                else {
                    if (professor != null) {
                        if (!professor.interact("Talk-to")) {
                            Logger.log("Failed to interact with Professor Oddenstein");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }
                break;
            }

            case 2: {
                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Oil can"))) {
                    Logger.log("Need oil can, handing off to ErnestTheChickenSolver");
                    TaskManager.getInstance().setCurrentTask(new ErnestTheChickenSolver(wrapper));
                    return;
                }

                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Poisoned fish food"))) {
                    if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Pressure gauge"))) {
                        if (!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Fish food"))) {
                            Area get = new Area(3107, 3360, 3110, 3354, 1);
                            TaskManager.getInstance().setCurrentTask(new GatheringTask("Fish food", "Fish food", 1, get, true, wrapper));
                            return;
                        }

                        if (!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Poison"))) {
                            Area get = new Area(3097, 3366, 3100, 3364);
                            TaskManager.getInstance().setCurrentTask(new GatheringTask("Poison", "Poison", 1, get, true, wrapper));
                            return;
                        }

                        if (!Inventory.use("Poison")) {
                            Logger.log("Failed to use Poison on Fish food");
                            return;
                        }

                        if (!Inventory.use("Fish food")) {
                            Logger.log("Failed to use poison on fish food");
                            return;
                        }
                    }
                }

                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Spade"))) {
                    Area get = new Area(3120, 3360, 3126, 3354);
                    TaskManager.getInstance().setCurrentTask(new GatheringTask("Spade", "Spade", 1, get, true, wrapper));
                    return;
                }

                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Pressure gauge"))) {
                    Area fountain = new Area(new Tile(3090, 3332, 0),
                            new Tile(3085, 3332, 0),
                            new Tile(3085, 3337, 0),
                            new Tile(3091, 3339, 0));

                    if(!fountain.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(fountain, wrapper));
                        return;
                    }

                    GameObject fountainObject = GameObjects.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Fountain"));
                    if(fountainObject != null) {
                        if(!fountainObject.isOnScreen()) {
                            Camera.rotateToEntity(fountainObject);
                            Sleep.sleepUntil(fountainObject::isOnScreen, 5000);
                            return;
                        }

                        if(Inventory.use("Poisoned fish food")) {
                            if(!Mouse.move(fountainObject.getClickablePoint())) {
                                Logger.log("Failed to move to fountain");
                                return;
                            }

                            if(!fountainObject.interact("Use")) {
                                Logger.log("Failed to interact with Fountain");
                                return;
                            }

                            Sleep.sleepUntil(() -> !Players.getLocal().isAnimating() && !Inventory.contains(x -> x != null && x.getName().equals("Poisoned fish food")), 5000);
                            Sleep.sleep(4000, 6000);
                        }

                        if(!fountainObject.interact("Search")) {
                            Logger.log("Failed to interact with Fountain");
                            return;
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                        if(Dialogues.inDialogue()) {
                            DialogueUtils.continueWhilePossible();
                        }

                        Sleep.sleepUntil(() -> Inventory.contains(x -> x != null && x.getName().equals("Pressure gauge")), 5000);
                    }
                }

                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Key"))) {
                    Area compost = new Area(new Tile(3084, 3363, 0),
                            new Tile(3084, 3359, 0),
                            new Tile(3087, 3357, 0),
                            new Tile(3089, 3357, 0),
                            new Tile(3088, 3366, 0));

                    if(!compost.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(compost, wrapper));
                        return;
                    }

                    GameObject compostObject = GameObjects.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Compost heap"));
                    if(compostObject != null) {
                        if(!compostObject.isOnScreen()) {
                            Camera.rotateToEntity(compostObject);
                            Sleep.sleepUntil(compostObject::isOnScreen, 5000);
                            return;
                        }

                        if(!compostObject.interact("Search")) {
                            Logger.log("Failed to interact with Compost heap");
                            return;
                        }

                        Sleep.sleepUntil(() -> Inventory.contains(x -> x != null && x.getName().equals("Key")), 10000);
                    }
                }

                if(!Inventory.contains(x -> x != null && !x.isNoted() && x.getName().equals("Rubber tube"))) {
                    Area get = new Area(3108, 3368, 3111, 3366);
                    TaskManager.getInstance().setCurrentTask(new GatheringTask("Rubber tube", "Rubber tube", 1, get, true, wrapper));
                    return;
                }

                NPC professor = NPCs.closest(x -> x != null && x.exists() && x.canReach() && x.getName().equals("Professor Oddenstein"));
                Area professorArea = new Area(3108, 3368, 3111, 3362, 2);

                if(!professorArea.contains(Players.getLocal()) && professor == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(professorArea, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    DialogueUtils.continueWhilePossible();
                }
                else {
                    if (professor != null) {
                        if (!professor.interact("Talk-to")) {
                            Logger.log("Failed to interact with Professor Oddenstein");
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
        return PlayerSettings.getConfig(32);
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return QuestTask.super.inventoryRequired();
    }

    @Override
    public List<String> inventoryTolerated() {
        return QuestTask.super.inventoryTolerated();
    }
}
