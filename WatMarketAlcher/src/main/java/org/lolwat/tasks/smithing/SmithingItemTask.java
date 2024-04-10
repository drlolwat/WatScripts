package org.lolwat.tasks.smithing;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.widget.helpers.Smithing;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.types.smithing.IngotType;
import org.lolwat.misc.types.smithing.SmithingType;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.StringUtils;
import org.lolwat.misc.utils.smithing.SmithingUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.HashMap;

public class SmithingItemTask implements WatTask {
    private final IngotType ingotType;
    private final int minimumLevel;
    private final int maximumLevel;
    private final Area selectedArea;
    private final int maximumInventories;
    private final HashMap<String, Integer> byproducts;

    public SmithingItemTask(IngotType type, int minLevel, int maxLevel, Area area, int maxInventories, HashMap<String, Integer> toSell) {
        ingotType = type;
        minimumLevel = minLevel;
        maximumLevel = maxLevel;
        selectedArea = area;
        maximumInventories = Calculations.random(3, maxInventories);
        byproducts = toSell;
    }

    @Override
    public String getName() {
        return "Hammerin' " + ingotType.toString().toLowerCase() + " bars";
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.SMITHING) >= minimumLevel && Skills.getRealLevel(Skill.SMITHING) <= maximumLevel;
    }

    @Override
    public void execute() {
        SmithingType itemType = SmithingUtils.getBestSmithingChoice(ingotType);
        // check to see if we have enough bars
        for(java.util.Map.Entry<String, Integer> m : SmithingUtils.materialsForSmithing(itemType, ingotType,false, 1).entrySet()) {
            if(!Inventory.contains(m.getKey()) || Inventory.count(m.getKey()) < m.getValue()) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(
                        SmithingUtils.materialsForSmithing(itemType, ingotType, true, 1),
                        byproducts, maximumInventories, this));

                return;
            }
        }

        if(!selectedArea.contains(Players.getLocal())) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(selectedArea, this));
            return;
        }

        if(!Smithing.isOpen()) {
            GameObject anvil = GameObjects.closest(x -> x != null && x.getName().equalsIgnoreCase("anvil"));
            if(anvil != null && anvil.interact("Smith")) {
                Sleep.sleepUntil(Smithing::isOpen, 3500);
            }
        }

        if(!Smithing.isOpen()) {
            return;
        }

        String barType = StringUtils.capitalize(ingotType.toString().toLowerCase());
        String toMake = barType + " " + itemType.toString().toLowerCase();

        if(Smithing.canMake(toMake) && Smithing.makeAll(toMake)) {
            Sleep.sleepUntil(() -> !Inventory.contains(barType + " bar") || Dialogues.canContinue(), 35000);
        }
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
        return Skill.SMITHING;
    }

    @Override
    public Integer avoidAfterLevel() {
        return maximumLevel;
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
