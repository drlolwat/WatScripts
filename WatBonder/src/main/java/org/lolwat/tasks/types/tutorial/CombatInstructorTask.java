package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.HashMap;

public class CombatInstructorTask implements WatTask {

    boolean traversed = false;

    @Override
    public String getName() {
        return "Tutorial: Combat Instructor";
    }

    @Override
    public boolean canPerformTask() {
        return false;
    }

    @Override
    public void execute(WatAIO instance) {
        if(Players.getLocal().isInCombat()) {
            return;
        }

        if(GameObjects.closest("Ladder") != null) {
            if(GameObjects.closest("Ladder").hasAction("Climb-down")) {
                TaskManager.getInstance().setCurrentTask(new TutorialBankTask());
                return;
            }
        }

        Area a = new Tile(3104, 9506).getArea(3);
        if(!traversed) {
            traversed = true;
            TaskManager.getInstance().setCurrentTask(new TraversalTask(a, this));
            return;
        }

        if (TutorialUtils.needsOpenTab()) {
            TutorialUtils.handleTab();
        }

        if(HintArrow.exists()) {
            if(HintArrow.getType().equals(HintArrowType.NPC)) {
                if(NPCs.closest("Combat Instructor").getTile().equals(HintArrow.getTile())) {
                    NPC n = NPCs.closest(x -> x.getName().contains("Combat Instructor"));
                    if(!Players.getLocal().canReach(n.getTile())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(n.getTile().getArea(2), this));
                        return;
                    }

                    DialogueUtils.talkTo("Combat Instructor");
                } else {
                    NPC n = NPCs.closest(x -> !x.isInCombat() && x.getName().contains("rat"));
                    if(n != null) {
                        if(n.getName().equals("Giant rat")) {
                            if(Inventory.contains("Shortbow") && Inventory.interact("Shortbow")) {
                                Sleep.sleep(30, 90);
                            }

                            if(Inventory.contains("Bronze arrow") && Inventory.interact("Bronze arrow")) {
                                Sleep.sleep(30, 90);
                            }

                            if(Equipment.getItemInSlot(EquipmentSlot.WEAPON).getName().equals("Bronze sword")) {
                                if(!Players.getLocal().canReach(n.getTile())) {
                                    TaskManager.getInstance().setCurrentTask(new TraversalTask(n.getTile().getArea(5), this));
                                    return;
                                }

                                if(!Players.getLocal().isInCombat() && !n.isInCombat() && n.interact()) {
                                    Sleep.sleepUntil(() -> !n.exists() && !Players.getLocal().isInCombat(), 15000);
                                }
                            } else {
                                if(!Players.getLocal().isInCombat() && !n.isInCombat() && n.interact()) {
                                    Sleep.sleepUntil(() -> !n.exists(), 15000);
                                }
                            }
                        }
                    }
                }
            } else {
                GameObject o = GameObjects.getTopObjectOnTile(HintArrow.getTile());

                if(o != null) {
                    if(o.getName().equals("Ladder")) {
                        if (Map.isTileOnScreen(o.getTile())) {
                            if(o.interact("Climb-up")) {
                                Sleep.sleepUntil(() -> !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 15000);
                                TaskManager.getInstance().setCurrentTask(new TraversalTask(new Tile(3121, 3122).getArea(3), new TutorialBankTask()));
                                return;
                            }
                        } else {
                            TaskManager.getInstance().setCurrentTask(new TraversalTask(o.getTile().getArea(2), this));
                            return;
                        }
                    }
                }

                if(Equipment.contains("Wooden shield") && Equipment.contains("Bronze sword")) {
                    TaskManager.getInstance().setCurrentTask(new TraversalTask(new Tile(3104, 9518).getArea(3), this));
                }
            }
        } else {
            //step 1 stats + equip dagger
            if(Equipment.isSlotEmpty(EquipmentSlot.WEAPON)) {
                if (Tabs.isOpen(Tab.EQUIPMENT)) {
                    Widget w = Widgets.getWidget(387);
                    if(w != null && w.isVisible()) {
                        WidgetChild c = w.getChild(1);
                        if (c != null && c.isVisible() && c.interact()) {
                            Sleep.sleep(100, 150);
                            if (Inventory.contains("Bronze dagger") && Inventory.interact("Bronze dagger", "Equip")) {
                                Sleep.sleep(50, 120);
                            }
                        }
                    }
                } else {
                    if (!Tabs.isDisabled(Tab.EQUIPMENT)) {
                        Tabs.open(Tab.EQUIPMENT);
                    }
                }
            }
            else {
                if(!Tabs.isOpen(Tab.INVENTORY)) {
                    Tabs.open(Tab.INVENTORY);
                    Sleep.sleep(20, 40);
                }

                if(Inventory.contains("Bronze sword") && Inventory.interact("Bronze sword", "Wield")) {
                    Sleep.sleep(30, 90);
                }

                if(Inventory.contains("Wooden shield") && Inventory.interact("Wooden shield", "Wield")) {
                    Sleep.sleep(30, 90);
                }
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
        return Skill.HITPOINTS;
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
