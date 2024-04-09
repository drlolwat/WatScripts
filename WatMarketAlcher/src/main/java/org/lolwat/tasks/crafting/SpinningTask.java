package org.lolwat.tasks.crafting;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.crafting.CraftingUtils;
import org.lolwat.misc.types.crafting.CraftingType;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;
import org.lolwat.managers.types.WatTask;
import org.lolwat.WatAIO;

import java.util.*;

public class SpinningTask implements WatTask {
    private final List<Area> spinnerLocations = Collections.singletonList(new Area(
            new Tile(3208, 3212, 1), // lumbridge
            new Tile(3212, 3212, 1),
            new Tile(3212, 3217, 1),
            new Tile(3208, 3217, 1)));
    private final HashMap<Skill, Integer> levelRequirements = new HashMap<>();
    private final Area selectedLocation;
    private final CraftingType spinningType;
    private final int minLevel;
    private final int avoidAtLevel;
    private final HashMap<String, Integer> toSell;
    private final int inventoryLoads;

    public SpinningTask(CraftingType type, int craftingLevel, int pAvoidAtLevel, int totalInventories, HashMap<String, Integer> sellList) {
        minLevel = craftingLevel;
        selectedLocation = spinnerLocations.get(new Random().nextInt(spinnerLocations.size()));
        spinningType = type;
        avoidAtLevel = pAvoidAtLevel;
        toSell = sellList;
        inventoryLoads = Calculations.random(3, totalInventories); // to level 5.
    }

    @Override
    public void execute() {
        String name = CraftingUtils.getCraftingItemName(spinningType);
        HashMap<String, Integer> requiredItems = new HashMap<String, Integer>() { { put(name, 28); }};

        if(!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
        }

        for(String it : requiredItems.keySet()) {
            if(!Inventory.contains(it)) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(requiredItems, toSell, inventoryLoads, this));
                return;
            }
        }

        if(!selectedLocation.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(selectedLocation, this));
            return;
        }

        if(GameObjects.closest("Spinning wheel") != null && !Players.getLocal().isAnimating()) {
            if(GameObjects.closest("Spinning wheel").interact()) {
                Sleep.sleepUntil(() -> Widgets.getWidget(270) != null && Widgets.getWidget(270).isVisible(), 10000);

                if(Widgets.getWidget(270) != null && Widgets.getWidget(270).isVisible()) {
                    Widgets.getWidget(270).getChild(CraftingUtils.getSpinnerWidgetId(spinningType)).interact();
                    Sleep.sleepUntil(() -> Dialogues.canContinue() || !Inventory.contains(n -> n.getName().contains(CraftingUtils.getCraftingItemName(spinningType))), 30000);
                }
            }
        }
    }
    @Override
    public String getName() {
        return "Crafting with " + spinningType.toString().toLowerCase();
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.CRAFTING) >= minLevel && Skills.getRealLevel(Skill.CRAFTING) <= avoidAtLevel;
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
        return Skill.CRAFTING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAtLevel;
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
