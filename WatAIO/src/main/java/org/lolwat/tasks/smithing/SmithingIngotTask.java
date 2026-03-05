package org.lolwat.tasks.smithing;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Map;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.types.smithing.IngotType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.smithing.SmithingUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class SmithingIngotTask implements WatTask {
    private final List<Tile> furnaceLocations = Arrays.asList(new Tile(3107, 3499), new Tile(3276, 3186));
    private final Tile selectedLocation;
    private final IngotType smithingType;
    private final int minLevel;
    private final int avoidAtLevel;
    private final HashMap<String, Integer> toSell;
    private long cooldown;
    private final int totalLoads;

    public SmithingIngotTask(IngotType type, int smithingLevel, int pAvoidAtLevel, HashMap<String, Integer> sellList) {
        minLevel = smithingLevel;
        selectedLocation = furnaceLocations.get(new Random().nextInt(furnaceLocations.size()));
        smithingType = type;
        avoidAtLevel = pAvoidAtLevel;
        toSell = sellList;
        cooldown = 0;
        totalLoads = Calculations.random(10, 15);
    }

    @Override
    public void execute() {
        for(java.util.Map.Entry<String, Integer> m : SmithingUtils.getMaterialsForBar(smithingType, false, 1).entrySet()) {
            // do we have enough to create at least 1 bar of this type?
            if(!Inventory.contains(m.getKey()) || Inventory.get(m.getKey()).getAmount() < m.getValue()) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(
                        SmithingUtils.getMaterialsForBar(smithingType, true, 1),
                        toSell, totalLoads, this));

                return;
            }
        }

        if(!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
            Sleep.sleep(200, 300);
        }

        if(!Map.isTileOnScreen(selectedLocation)) {
            if(Map.isTileOnMap(selectedLocation) && Map.exactDistance(selectedLocation) <= 6) {
                Camera.rotateToTile(selectedLocation);
                return;
            }

            TaskManager.getInstance().setCurrentTask(new TraversalTask(selectedLocation.getArea(3),this));
            return;
        }

        if(GameObjects.closest("Furnace") != null && !Players.getLocal().isAnimating()) {
            if(GameObjects.closest("Furnace").interact()) {
                Sleep.sleepUntil(() -> Widgets.getWidget(270) != null && Widgets.getWidget(270).isVisible(), 10000);

                if(Widgets.getWidget(270) != null && Widgets.getWidget(270).isVisible()) {
                    Widgets.getWidget(270).getChild(SmithingUtils.getIngotWidgetId(smithingType)).interact();
                    Sleep.sleepUntil(() -> cooldown > 5 || Dialogues.canContinue() || !Inventory.contains(n -> n.getName().contains("ore")), 30000);
                }
            }
        }
    }


    @Override
    public String getName() {
        return "Smelting " + smithingType.toString().toLowerCase() + " bars";
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.SMITHING) >= minLevel && Skills.getRealLevel(Skill.SMITHING) <= avoidAtLevel;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {
        cooldown = 0;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.SMITHING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAtLevel;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return GenericUtils.getSkillingGear();
    }
}
