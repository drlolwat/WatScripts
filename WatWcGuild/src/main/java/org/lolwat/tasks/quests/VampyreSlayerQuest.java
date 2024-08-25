package org.lolwat.tasks.quests;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
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
import org.lolwat.misc.utils.combat.melee.MeleeUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.*;

public class VampyreSlayerQuest implements QuestTask {
    private final Area startLocation = new Area(3096, 3270, 3102, 3266);
    private final List<String> startDialogue = Collections.singletonList("Yes.");
    boolean checkedBank = false;

    @Override
    public Quest completes() {
        return FreeQuest.VAMPIRE_SLAYER;
    }

    @Override
    public void execute(WatTask wrapper) {
        switch(getState()) {
            case 0: {
                NPC fred = NPCs.closest("Morgan");
                if(!startLocation.contains(Players.getLocal()) && fred == null) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(startLocation, wrapper));
                    return;
                }

                if(Dialogues.inDialogue()) {
                    DialogueUtils.continueWhilePossible();
                    DialogueUtils.solve(startDialogue);
                } else {
                    if(fred != null) {
                        if(!fred.interact()) {
                            Logger.log("Failed to interact with Fred");
                        }

                        Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                    }
                }

                break;
            }

            case 2:
            case 1: {
                if(!Inventory.contains("Stake")) {
                    if(!checkedBank) {
                        checkedBank = true;
                        TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                            {
                                put("Stake", 1);
                            }
                        }, null, 1, wrapper, new HashMap<String, WatTask>() {
                            {
                                put("Stake", wrapper);
                            }
                        }));
                        return;
                    }

                    if (!Inventory.contains("Coins") && !Inventory.contains("Beer")) {
                        TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                            {
                                put("Coins", 2);
                            }
                        }, null, 1, wrapper));
                        return;
                    }

                    Area inn = new Area(3218, 3402, 3227, 3394);
                    if (!inn.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(inn, wrapper));
                        return;
                    }

                    if (Inventory.contains("Coins") && !Inventory.contains("Beer")) {
                        NPC bartender = NPCs.closest("Bartender");
                        if (!Dialogues.inDialogue()) {
                            if (bartender != null && bartender.exists() && bartender.canReach()) {
                                if (!bartender.interact()) {
                                    Logger.log("Failed to interact with Bartender");
                                }

                                Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                                return;
                            }
                        } else {
                            DialogueUtils.solve(Collections.singletonList("A glass of your finest ale please."));
                        }
                    }

                    NPC harlow = NPCs.closest("Dr Harlow");
                    if (!Dialogues.inDialogue()) {
                        if (harlow != null && harlow.exists() && harlow.canReach()) {
                            if (!harlow.interact()) {
                                Logger.log("Failed to interact with Dr Harlow");
                            }

                            Sleep.sleepUntil(Dialogues::inDialogue, 5000);
                            return;
                        }
                    } else {
                        DialogueUtils.solve(Arrays.asList("Morgan needs your help!"));
                    }
                } else {
                    if (Players.getLocal().isInCombat() || Players.getLocal().isHealthBarVisible()) {
                        return;
                    } else {
                        if (!Inventory.contains("Hammer") || !Inventory.contains("Garlic")) {
                            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                                {
                                    put("Hammer", 1);
                                    put("Garlic", 1);
                                    put("Stake", 1);
                                }
                            }, null, 1, wrapper));
                            return;
                        }

                        Area vampireLair = new Area(3075, 9778, 3080, 9768);
                        if (!vampireLair.contains(Players.getLocal())) {
                            TaskManager.getInstance().setCurrentTask(new TraversalTask(vampireLair, wrapper));
                            return;
                        }

                        GameObject obj = GameObjects.closest("Coffin");
                        if (obj != null && obj.exists() && obj.canReach()) {
                            if (!obj.interact("Open")) {
                                Logger.log("Failed to interact with Coffin");
                            }

                            Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                        }

                        NPC countDraynor = NPCs.closest("Count Draynor");
                        if (countDraynor != null && countDraynor.exists() && countDraynor.canReach()) {
                            if (!countDraynor.interact("Attack")) {
                                Logger.log("Failed to interact with Count Draynor");
                            }

                            Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                        }
                    }
                }

                break;
            }

            default: {
                Logger.log("Unhandled state: " + getState());
                break;
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return !Quests.isFinished(completes()) && Combat.getCombatLevel() >= 35;
    }

    @Override
    public int getState() {
        return PlayerSettings.getConfig(178);
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {{

        }};
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return MeleeUtils.getRequiredItems(false);
    }
}
