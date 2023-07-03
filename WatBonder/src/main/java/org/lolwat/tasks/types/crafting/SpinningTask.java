package org.lolwat.tasks.types.crafting;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.misc.utils.crafting.CraftingUtils;
import org.lolwat.misc.types.crafting.CraftingType;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;
import org.lolwat.tasks.WatTask;
import org.lolwat.WatAIO;

import java.util.*;

public class SpinningTask implements WatTask {
    private final List<Tile> spinnerLocations = Collections.singletonList(new Tile(3209, 3213, 1));
    private final HashMap<Skill, Integer> levelRequirements = new HashMap<>();
    private final Tile selectedLocation;
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
        inventoryLoads = Calculations.random(5, totalInventories);
    }

    @Override
    public void execute(WatAIO instance) {
        String name = CraftingUtils.getCraftingItemName(spinningType);
        HashMap<String, Integer> requiredItems = new HashMap<String, Integer>() { { put(name, 28); }};

        if(!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
        }

        for(String it : requiredItems.keySet()) {
            if(!Inventory.contains(it)) {
                instance.currentTask = new BankingTask(null, requiredItems, toSell, inventoryLoads, this);
                return;
            }
        }

        if(!Map.isTileOnMap(selectedLocation) || !Players.getLocal().canReach(selectedLocation)) {
            if(Map.isTileOnMap(selectedLocation) && !Map.isTileOnScreen(selectedLocation)) {
                Camera.rotateToTile(selectedLocation);
                return;
            }

            instance.currentTask = new TraversalTask(selectedLocation, true, this);
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
        return Skills.getRealLevel(Skill.CRAFTING) >= minLevel && Skills.getRealLevel(Skill.CRAFTING) < avoidAtLevel;
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
    public Quest completesQuest() {
        return null;
    }
}
