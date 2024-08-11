package org.lolwat.tasks.crafting;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.types.crafting.CraftingType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.crafting.CraftingUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class JewelryTask implements WatTask {
    private final List<Area> furnaceLocations = Arrays.asList(new Area(3105, 3501, 3110, 3496), // edge
            new Area(3272, 3188, 3276, 3184) // al-kharid
    );
    private final Area selectedLocation;
    private final CraftingType craftingType;
    private final int minLevel;
    private final int avoidAtLevel;
    private final HashMap<String, Integer> toSell;
    private final int totalLoads;

    public JewelryTask(CraftingType type, int craftingLevel, int pAvoidAtLevel, HashMap<String, Integer> sellList) {
        minLevel = craftingLevel;
        selectedLocation = furnaceLocations.get(new Random().nextInt(furnaceLocations.size()));
        craftingType = type;
        avoidAtLevel = pAvoidAtLevel;
        toSell = sellList;
        totalLoads = Calculations.random(20, 28);
    }

    @Override
    public void execute() {
        for(java.util.Map.Entry<String, Integer> m : CraftingUtils.getMaterialsForJewelry(craftingType, false, 1).entrySet()) {
            if(!Inventory.contains(m.getKey()) || Inventory.get(m.getKey()).getAmount() < m.getValue() || Inventory.get(m.getKey()).isNoted()) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(null,
                        toSell, totalLoads, this));

                return;
            }
        }

        if (!selectedLocation.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(selectedLocation, this));
            return;
        }

        if (GameObjects.closest("Furnace") != null && !Players.getLocal().isAnimating()) {
            if (GameObjects.closest("Furnace").interact()) {
                Sleep.sleepUntil(() -> Widgets.getWidget(CraftingUtils.getJewelryParentId(craftingType)) != null && Widgets.getWidget(CraftingUtils.getJewelryParentId(craftingType)).isVisible(), 10000);
                if(Widgets.getWidget(CraftingUtils.getJewelryParentId(craftingType)).getChild(CraftingUtils.getJewelryChildId(craftingType)).interact()) {
                    Sleep.sleep(500, 900);
                    GenericUtils.moveMouseInOrOut();
                    Sleep.sleepUntil(() -> !Inventory.contains("Gold bar") || Dialogues.canContinue(), 45000);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "Making jewelry";
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
        return CraftingUtils.getMaterialsForJewelry(craftingType, true, 1);
    }

}
