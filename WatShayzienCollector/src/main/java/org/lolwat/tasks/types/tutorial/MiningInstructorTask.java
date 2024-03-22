package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.widget.helpers.Smithing;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MiningInstructorTask implements WatTask {
    @Override
    public String getName() {
        return "Tutorial: Mining Instructor";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        List<String> rocks = new ArrayList<String>() {
            {
                add("Tin rocks");
                add("Copper rocks");
            }
        };

        if(Inventory.contains("Bronze dagger")) {
            TaskManager.getInstance().setCurrentTask(new CombatInstructorTask());
            return;
        }

        if(GameObjects.closest("Ladder") != null) {
            if(GameObjects.closest("Ladder").hasAction("Climb-down")) {
                GameObjects.closest("Ladder").interact("Climb-down");
                return;
            }
        }

        if(HintArrow.exists() && HintArrow.getTile() != null) {
            if(HintArrow.getType().equals(HintArrowType.NPC)) {
                if(!Map.isTileOnScreen(HintArrow.getTile())) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(HintArrow.getTile().getArea(3), this));
                    return;
                }

                DialogueUtils.talkTo("Mining Instructor");
            } else {
                GameObject obj = GameObjects.getTopObjectOnTile(HintArrow.getTile());

                if(rocks.contains(obj.getName())) {
                    List<GameObject> tins = GameObjects.all("Tin rocks");
                    List<GameObject> coppers = GameObjects.all("Copper rocks");
                    Collections.shuffle(tins);
                    Collections.shuffle(coppers);

                    if (!Inventory.contains("Tin ore")) {
                        if (tins.get(0).interact("Mine")) {
                            Sleep.sleepUntil(() -> Inventory.contains("Tin ore"), 15000);
                        }
                    } else if (!Inventory.contains("Copper ore")) {
                        if (coppers.get(0).interact("Mine")) {
                            Sleep.sleepUntil(() -> Inventory.contains("Copper ore"), 15000);
                        }
                    }
                } else {
                    if(obj.getName().equals("Furnace") && !Inventory.contains("Bronze bar")) {
                        obj.interact();
                        Sleep.sleepUntil(() -> Inventory.contains("Bronze bar") && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 25000);
                        Sleep.sleep(60, 120);
                    }
                    else if(obj.getName().equals("Anvil")) {
                        obj.interact();
                        Sleep.sleepUntil(() -> Widgets.getWidget(312) != null && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 15000);
                        Sleep.sleep(60, 120);
                    }
                }
            }
        } else {
            if(Inventory.contains("Bronze bar")) {
                WidgetChild wc = Widgets.getWidget(312).getChild(9);

                if (wc != null && wc.isVisible()) {
                    if (wc.interact()) {
                        Sleep.sleepUntil(() -> Inventory.contains("Bronze dagger"), 5000);
                        Sleep.sleep(60, 120);
                    }
                } else {
                    GameObject obj = GameObjects.closest("Anvil");
                    if (obj != null && obj.interact()) {
                        Sleep.sleep(50, 120);
                    }
                }
            } else {
                TaskManager.getInstance().setCurrentTask(new TraversalTask(new Tile(3081,  9506, 0).getArea(6), this));
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return Calculations.random(350, 450);
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
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<>();
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<>();
    }
}
