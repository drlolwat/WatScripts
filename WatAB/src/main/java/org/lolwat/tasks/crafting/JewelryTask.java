package org.lolwat.tasks.crafting;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.types.crafting.CraftingType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.misc.utils.crafting.CraftingUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;
import org.lolwat.types.gear.GearItem;
import org.lolwat.types.interfaces.WatTask;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class JewelryTask implements WatTask {
    private final Area selectedLocation;
    private final CraftingType craftingType;
    private final int minLevel;
    private final int avoidAtLevel;
    private final HashMap<String, Integer> toSell;
    private final int totalLoads;

    public JewelryTask(CraftingType type, int craftingLevel, int pAvoidAtLevel, HashMap<String, Integer> sellList) {
        minLevel = craftingLevel;

        List<Area> furnaceLocations = Arrays.asList(
                new Area(3105, 3501, 3110, 3496), // edge
                new Area(3272, 3188, 3276, 3184) // al-kharid
        );

        selectedLocation = furnaceLocations.get(new Random().nextInt(furnaceLocations.size()));
        craftingType = type;
        avoidAtLevel = pAvoidAtLevel;
        toSell = sellList;
        totalLoads = Calculations.random(20, 28);
    }

    @Override
    public void execute() {
        for(GearItem i : CraftingUtils.getMaterialsForJewelry(craftingType, false, 1)) {
            if(ItemUtils.inventoryContains(i.getName(), i.getQuantity(), false)) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(null,
                        toSell, totalLoads, this));

                return;
            }
        }

        if (!selectedLocation.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(selectedLocation, this));
            return;
        }

        GameObject furnace = GameObjects.closest("Furnace");
        if (furnace != null && !Players.getLocal().isAnimating()) {
            if (furnace.interact()) {
                Sleep.sleepUntil(() -> Widgets.getWidget(CraftingUtils.getJewelryParentId(craftingType)) != null, 5000);
                Widget w = Widgets.getWidget(CraftingUtils.getJewelryParentId(craftingType));
                if(w != null) {
                    WidgetChild c = w.getChild(CraftingUtils.getJewelryChildId(craftingType));
                    if (c.interact()) {
                        Sleep.sleepUntil(() -> !Inventory.contains("Gold bar") || Dialogues.canContinue(), 45000);
                    }
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
    public Skill trainsSkill() {
        return Skill.CRAFTING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return avoidAtLevel;
    }

    @Override
    public HashMap<EquipmentSlot, GearItem> loadout() {
        return GenericUtils.getSkillingGear();
    }

    @Override
    public List<GearItem> inventory() {
        return CraftingUtils.getMaterialsForJewelry(craftingType, true, 1);
    }

}
