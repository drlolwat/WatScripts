package org.lolwat.tasks.types.tutorial;

import org.dreambot.api.input.Keyboard;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.hint.HintArrow;
import org.dreambot.api.methods.hint.HintArrowType;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.TutorialUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TutorialIslandTask implements WatTask {
    public TutorialIslandTask() {
    }

    @Override
    public String getName() {
        return "Tutorial Island V2";
    }

    @Override
    public boolean canPerformTask() {
        return TutorialUtils.isOnTutorial();
    }

    @Override
    public void execute(WatAIO instance) {
        int step = TutorialUtils.getTutorialStep();
        if(step < 1000) {
            switch(step) {
                default: {
                    Logger.log("Unhandled tutorial step: " + step);
                }

                case 1: {
                    if(Widgets.getWidget(TutorialUtils.NAME_WIDGET) != null && Widgets.getWidget(TutorialUtils.NAME_WIDGET).isVisible()) {
                        Logger.log("Tutorial: selecting username");
                        WidgetChild nameText = Widgets.getWidget(TutorialUtils.NAME_WIDGET).getChild(TutorialUtils.NAME_TEXT_CHILDID);
                        WidgetChild lookupButton = Widgets.getWidget(TutorialUtils.NAME_WIDGET).getChild(TutorialUtils.NAME_LOOKUP_CHILDID);

                        if(nameText != null && nameText.isVisible()) {
                            if(nameText.getText().equals("*")) {
                                nameText.interact();
                                Sleep.sleep(100, 200);
                                String name = GenericUtils.generateUsername();
                                Logger.log("Selected name:" + name);
                                Keyboard.type(name, false);
                                Sleep.sleep(100, 200);
                                if (lookupButton != null && lookupButton.isVisible() && lookupButton.hasAction("Look up name")) {
                                    lookupButton.interact("Look up name");
                                    Sleep.sleep(3000, 4000);
                                }
                            }
                            else {
                                if(nameText.interact()) {
                                    while(!nameText.getText().equals("*")) {
                                        Keyboard.typeKey(KeyEvent.VK_BACK_SPACE);
                                    }
                                }
                            }
                        }

                        WidgetChild confirmName = Widgets.getWidget(TutorialUtils.NAME_WIDGET).getChild(TutorialUtils.NAME_SETNAME_CHILDID);
                        if(confirmName != null && confirmName.isVisible() && confirmName.hasAction("Set name")) {
                            if(confirmName.interact("Set name")) {
                                Sleep.sleepUntil(() -> !confirmName.isVisible(), 10000);
                            }
                        }
                    } else {
                        final Widget par = Widgets.getWidget(TutorialUtils.APPEAR_PAR);
                        if (par != null && par.isVisible()) {
                            List<WidgetChild> widgets = Widgets.getAll(x -> x.hasAction("Select"));
                            List<WidgetChild> rightArrows = new ArrayList<>();
                            for (int i = 1; i < widgets.size(); i += 2) {
                                rightArrows.add(widgets.get(i));
                            }

                            Collections.shuffle(rightArrows);
                            List<WidgetChild> selectedWidgets = rightArrows.subList(0, Math.min(7, rightArrows.size()));

                            for (WidgetChild widget : selectedWidgets) {
                                int timesToClick = Calculations.random(1, 5);
                                for (int i = 0; i < timesToClick; i++) {
                                    if (widget != null && widget.isVisible() && widget.interact()) {
                                        Sleep.sleep(200, 400);
                                    }
                                }
                            }

                            WidgetChild acc = Widgets.getWidget(TutorialUtils.APPEAR_PAR).getChild(TutorialUtils.ACCEPT);
                            if(acc != null && acc.isVisible() && acc.hasAction("Confirm")) {
                                if(acc.interact()) {
                                    Sleep.sleepUntil(() -> step != 1, 6750);
                                }
                            }
                        }
                    }
                    break;
                }

                case 2: {


                    if (Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Gielinor Guide");
                    } else {
                        Dialogues.chooseOption("I am an experienced player.");
                    }

                    break;
                }

                case 230: // Quest Guide
                case 3: // Gielinor Guide
                case 50: // Survival Instructor
                case 30: { // Survival Instructor
                    if (TutorialUtils.needsOpenTab()) {
                        TutorialUtils.handleTab();
                    }

                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    break;
                }

                case 7: {
                    if(Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Gielinor Guide");
                    }

                    break;
                }

                case 10: {
                    Area area = new Area(
                            new Tile(3096, 3103, 0),
                            new Tile(3096, 3090, 0),
                            new Tile(3104, 3090, 0),
                            new Tile(3107, 3095, 0),
                            new Tile(3106, 3099, 0),
                            new Tile(3103, 3103, 0));

                    if (!area.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
                        return;
                    }

                    break;
                }

                case 60:
                case 20: {
                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    if (Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Survival Expert");
                    }

                    if(!Inventory.contains("Bronze axe")) {
                        if (Dialogues.getOptions() == null) {
                            DialogueUtils.talkTo("Survival Expert");
                        }
                    } else {
                        if(GameObjects.closest("Tree").interact("Chop down")) {
                            Sleep.sleepUntil(() -> Inventory.contains("Logs"), 15000);
                        }
                    }

                    break;
                }

                case 40: {
                    if(NPCs.closest("Fishing spot").interact("Net")) {
                        Sleep.sleepUntil(() -> Inventory.contains("Raw shrimps") && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 15000);
                    }

                    break;
                }

                case 70: {
                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    if(!Inventory.contains("Bronze axe")) {
                        if (Dialogues.getOptions() == null) {
                            DialogueUtils.talkTo("Survival Expert");
                        }
                    } else {
                        if(GameObjects.closest("Tree").interact("Chop down")) {
                            Sleep.sleepUntil(() -> Inventory.contains("Logs"), 15000);
                        }
                    }

                    break;
                }

                case 80: {
                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    Tile currentTile = Players.getLocal().getTile();
                    if(Inventory.contains("Logs")) {
                        if(!Inventory.use("Tinderbox")) {
                            Logger.error("Tutorial: Problem using tinderbox");
                        }
                        else {
                            if(!Inventory.use("Logs")) {
                                Logger.error("Tutorial: Problem using logs");
                            }

                            Sleep.sleepUntil(() -> Players.getLocal().getTile() != currentTile, 15000);
                        }
                    }
                    break;
                }

                case 90: {
                    GameObject fire = GameObjects.closest("Fire");
                    if(fire != null) {
                        if(!Inventory.use("Raw shrimps")) {
                            Logger.error("Tutorial: Problem using raw shrimps");
                        }
                        else {
                            if(fire.interact()) {
                                Sleep.sleepUntil(() -> !Inventory.contains("Raw shrimps"), 15000);
                            }
                        }
                    }
                    break;
                }

                case 120: {
                    Area area = new Area(3073, 3086, 3078, 3083);
                    if (!area.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
                        return;
                    }

                    break;
                }

                case 140: {
                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    if(Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Master Chef");
                    }

                    break;
                }

                case 150: {
                    if(!Inventory.use("Pot of flour")) {
                        Logger.error("Tutorial: Problem using pot of flour");
                    }
                    else {
                        if(!Inventory.use("Bucket of water")) {
                            Logger.error("Tutorial: Problem using bucket of water");
                        }
                        else {
                            Sleep.sleepUntil(() -> !Inventory.contains("Pot of flour"), 10000);
                        }
                    }
                    break;
                }

                case 160: {
                    GameObject range = GameObjects.closest("Range");
                    if (range != null && range.interact("Cook")) {
                        Sleep.sleepUntil(() -> !Inventory.contains("Bread dough"), 10000);
                    }

                    break;
                }

                case 170: {
                    Area area = new Area(3083, 3125, 3089, 3119);
                    if (!area.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
                        return;
                    }

                    break;
                }

                case 240:
                case 220: {
                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    if(Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Quest Guide");
                    }

                    break;
                }

                case 250: {
                    GameObject ladder = GameObjects.closest(x -> x.hasAction("Climb-down"));
                    if(ladder != null && ladder.interact("Climb-down")) {
                        Sleep.sleepUntil(() -> !ladder.exists(), 5000);
                    }

                    break;
                }

                case 260: {
                    NPC instructor = NPCs.closest("Mining Instructor");

                    Area area = new Tile(3081, 9506).getArea(6);
                    if((instructor == null || !instructor.exists()) || !area.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
                        return;
                    }

                    if(Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Mining Instructor");
                    }

                    break;
                }

                case 270: {
                    List<GameObject> tins = GameObjects.all("Tin rocks");
                    Collections.shuffle(tins);
                    if (!Inventory.contains("Tin ore")) {
                        if (tins.get(0).interact("Mine")) {
                            Sleep.sleepUntil(() -> Inventory.contains("Tin ore"), 15000);
                        }
                    }

                    break;
                }

                case 310: {
                    List<GameObject> coppers = GameObjects.all("Copper rocks");
                    Collections.shuffle(coppers);
                    if (!Inventory.contains("Copper ore")) {
                        if (coppers.get(0).interact("Mine")) {
                            Sleep.sleepUntil(() -> Inventory.contains("Copper ore"), 15000);
                        }
                    }
                    break;
                }

                case 320: {
                    GameObject furnace = GameObjects.closest("Furnace");
                    if(furnace != null && furnace.interact()) {
                        Sleep.sleepUntil(() -> Inventory.contains("Bronze bar") && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 25000);
                    }
                    break;
                }

                case 330: {
                    if(Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Mining Instructor");
                    }

                    break;
                }

                case 340: {
                    Widget w = Widgets.getWidget(312);
                    if(w == null || !w.isVisible()) {
                        GameObject obj = GameObjects.closest("Anvil");
                        if (obj != null && obj.interact()) {
                            Sleep.sleepUntil(() -> !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 15000);
                            Sleep.sleep(100, 200);
                        }
                    }

                    break;
                }

                case 350: {
                    Widget w = Widgets.getWidget(312);
                    if (w != null && w.isVisible()) {
                        WidgetChild wc = w.getChild(9);
                        if (wc.interact()) {
                            Sleep.sleepUntil(() -> Inventory.contains("Bronze dagger"), 5000);
                        }
                    }
                    break;
                }

                case 360: {
                    GameObject gate = GameObjects.closest("Gate");
                    if(gate != null && gate.interact("Open")) {
                        Sleep.sleepUntil(() -> !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 10000);
                    }

                    break;
                }

                case 370: {
                    NPC combatInstructor = NPCs.closest("Combat Instructor");
                    if(combatInstructor != null && Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Combat Instructor");
                        Sleep.sleepUntil(() -> Dialogues.getOptions() != null, 5000);
                    }
                    break;
                }

                case 390: {
                    if(TutorialUtils.needsOpenTab()) {
                        TutorialUtils.handleTab();
                    }

                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    break;
                }

                case 400: {
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
                    break;
                }

                case 410: {
                    if(Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Combat Instructor");
                    }

                    break;
                }

                case 420: {
                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    if(!Equipment.contains("Bronze sword")) {
                        if(!Inventory.interact("Bronze sword", "Wield")) {
                            Logger.error("Tutorial: Problem wielding bronze sword");
                        }
                    }

                    if(!Equipment.contains("Wooden shield")) {
                        if(!Inventory.interact("Wooden shield", "Wield")) {
                            Logger.error("Tutorial: Problem wielding wooden shield");
                        }
                    }

                    break;
                }

                case 430: {
                    if(TutorialUtils.needsOpenTab()) {
                        TutorialUtils.handleTab();
                    }

                    break;
                }

                case 440: {
                    GameObject ob = GameObjects.closest(9719);
                    if(ob != null && ob.exists()) {
                        if(ob.interact("Open")) {
                            Sleep.sleepUntil(() -> !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 5000);
                            Sleep.sleep(50, 100);
                        }
                    }
                    break;
                }

                case 450: {
                    if(Players.getLocal().isInCombat()) {
                        return;
                    }

                    NPC rat = NPCs.closest(x -> x != null && x.exists() && !x.isInCombat() && x.getName().contains("rat"));
                    if(rat != null) {
                        if(rat.interact()) {
                            Sleep.sleepUntil(() -> !rat.exists(), 20000);
                        } else {
                            Logger.error("Tutorial: Problem interacting with rat");
                        }
                    }
                    break;
                }

                case 470: {
                    if(Dialogues.getOptions() == null) {
                        NPC instructor = NPCs.closest("Combat Instructor");
                        if(instructor != null && instructor.exists()) {
                            if (instructor.canReach()) {
                                DialogueUtils.talkTo("Combat Instructor");
                            } else {
                                TaskManager.getInstance().setCurrentTask(new TraversalTask(instructor.getTile().getArea(2), this));
                                return;
                            }
                        }
                    }
                    break;
                }

                case 480: {
                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    if (!Tabs.isOpen(Tab.INVENTORY)) {
                        if(!Tabs.isDisabled(Tab.INVENTORY)) {
                            Tabs.open(Tab.INVENTORY);
                            Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), 5000);
                        }
                    }

                    if(!Equipment.contains("Shortbow")) {
                        if(!Inventory.interact("Shortbow", "Wield")) {
                            Logger.error("Tutorial: Problem wielding shortbow");
                        } else {
                            Sleep.sleepUntil(() -> Equipment.contains("Shortbow"), 5000);
                        }
                    }

                    if(!Equipment.contains("Bronze arrow")) {
                        if(!Inventory.interact("Bronze arrow", "Wield")) {
                            Logger.error("Tutorial: Problem wielding bronze arrow");
                        } else {
                            Sleep.sleepUntil(() -> Equipment.contains("Bronze arrow"), 5000);
                        }
                    }

                    if(!Players.getLocal().isInCombat()) {
                        NPC rat = NPCs.closest(x -> x != null && x.exists() && !x.isInCombat() && x.getName().contains("rat"));
                        if(rat != null) {
                            if(rat.interact()) {
                                Sleep.sleepUntil(() -> !rat.exists(), 20000);
                            } else {
                                Logger.error("Tutorial: Problem interacting with rat");
                            }
                        }
                    }

                    break;
                }

                case 500: {
                    GameObject ladder = GameObjects.closest(x -> x != null && x.exists() && x.hasAction("Climb-up"));
                    if(ladder != null) {
                        if(ladder.interact("Climb-up")) {
                            Sleep.sleepUntil(() -> !ladder.exists(), 7000);
                        } else {
                            Logger.error("Tutorial: Problem interacting with ladder");
                        }
                    }
                    break;
                }

                case 510: {
                    if(HintArrow.exists()) {
                        if (!HintArrow.getType().equals(HintArrowType.NPC)) {
                            GameObject obj = GameObjects.getTopObjectOnTile(HintArrow.getTile());
                            if (obj != null) {
                                if(obj.distance() > 5) {
                                    TaskManager.getInstance().setCurrentTask(new TraversalTask(obj.getTile().getArea(2), this));
                                    return;
                                }

                                if (obj.getName().equals("Bank booth")) {
                                    if (obj.interact("Use")) {
                                        Sleep.sleepUntil(Bank::isOpen, 1000);

                                        if (Bank.isOpen()) {
                                            Bank.close();
                                            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    break;
                }

                case 520: {
                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }
                    else {
                        if (HintArrow.exists()) {
                            if (!HintArrow.getType().equals(HintArrowType.NPC)) {
                                GameObject obj = GameObjects.getTopObjectOnTile(HintArrow.getTile());
                                if (obj.getName().equals("Poll booth")) {
                                    if (obj.interact("Use")) {
                                        Sleep.sleep(100, 200);
                                    }
                                }
                            }
                        }
                    }
                    break;
                }

                case 525: {
                    if (HintArrow.exists()) {
                        if (!HintArrow.getType().equals(HintArrowType.NPC)) {
                            GameObject obj = GameObjects.getTopObjectOnTile(HintArrow.getTile());

                            if (obj.getName().equals("Door")) {
                                if (obj.interact("Open")) {
                                    Sleep.sleepUntil(() -> !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 5000);
                                }
                            }
                        }
                    }
                    break;
                }

                case 532:
                case 530: {
                    if(TutorialUtils.needsOpenTab()) {
                        TutorialUtils.handleTab();
                    }

                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                        return;
                    }

                    if(Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Account Guide");
                    }

                    break;
                }

                case 531: {
                    if(TutorialUtils.needsOpenTab()) {
                        TutorialUtils.handleTab();
                    }

                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }

                    break;
                }

                case 540: {
                    Area a = new Tile(3124, 3107).getArea(3);
                    if (!a.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(a, this));
                        return;
                    }

                    break;
                }

                case 600:
                case 570:
                case 550: {
                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                        return;
                    }

                    if(Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Brother Brace");
                    }

                    break;
                }

                case 630:
                case 580:
                case 560: {
                    if(TutorialUtils.needsOpenTab()) {
                        TutorialUtils.handleTab();
                    }

                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                    }
                    break;
                }

                case 610: {
                    Area area = new Tile(3141, 3087).getArea(1);
                    if (!area.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(area, this));
                        return;
                    }

                    break;
                }

                case 670:
                case 640:
                case 620: {
                    List<String> answers = new ArrayList<String>() {
                        {
                            add("Yes.");
                            add("No, I'm not planning to do that.");
                        }
                    };

                    if(Dialogues.canContinue()) {
                        Dialogues.spaceToContinue();
                        return;
                    }

                    Widget w = Widgets.getWidget(219);
                    if(w != null && w.isVisible()) {
                        WidgetChild c = w.getChild(1);
                        if(c != null) {
                            WidgetChild c2 = c.getChild(3);
                            if(c2 != null && c2.isVisible() && c2.getText().contains("that")) {
                                c2.interact();
                                Sleep.sleepUntil(() -> !c2.isVisible(), 5000);
                            }
                        }
                    }

                    if(Dialogues.getOptions() == null) {
                        DialogueUtils.talkTo("Magic Instructor", answers);
                    } else {
                        Sleep.sleep(100, 200); // just in case
                    }

                    break;
                }

                case 650: {
                    Area location = new Area(3138, 3091, 3141, 3091);

                    if (!location.contains(Players.getLocal())) {
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(location, this));
                        return;
                    }

                    if (!Magic.castSpellOn(Normal.WIND_STRIKE, NPCs.closest(x -> !x.isInCombat() && x.getName().equals("Chicken")))) {
                        Logger.log("error castin' spell on the chucken");
                    }

                    break;
                }
            }
        } else {
            Logger.log("WAIO: tutorial completed");
            TaskManager.getInstance().getNewTask();
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public int loopTime() {
        return 400;
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
