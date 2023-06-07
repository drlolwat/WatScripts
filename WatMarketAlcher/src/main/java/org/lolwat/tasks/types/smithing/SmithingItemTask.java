package org.lolwat.tasks.types.smithing;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.widget.helpers.Smithing;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.misc.types.smithing.IngotType;
import org.lolwat.misc.types.smithing.SmithingType;
import org.lolwat.misc.utils.StringUtils;
import org.lolwat.misc.utils.crafting.CraftingUtils;
import org.lolwat.misc.utils.smithing.SmithingUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.HashMap;

public class SmithingItemTask implements WatTask {
    private IngotType ingotType;
    private int minimumLevel;
    private int maximumLevel;
    private Area selectedArea;
    private int maximumInventories;
    private HashMap<String, Integer> byproducts;

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
        return Skills.getRealLevel(Skill.SMITHING) >= minimumLevel;
    }

    @Override
    public void execute(WatAIO instance) {
        SmithingType itemType = SmithingUtils.getBestSmithingChoice(ingotType);
        // check to see if we have enough bars
        for(java.util.Map.Entry<String, Integer> m : SmithingUtils.materialsForSmithing(itemType, ingotType,false, 1).entrySet()) {
            if(!Inventory.contains(m.getKey()) || Inventory.count(m.getKey()) < m.getValue()) {
                instance.currentTask = new BankingTask("Grabbing bars",
                        SmithingUtils.materialsForSmithing(itemType, ingotType, true, 1),
                        true, this, true, byproducts, maximumInventories);

                return;
            }
        }

        if(!Inventory.contains("Hammer")) {
            instance.currentTask = new BankingTask("Grabbing hammer", new HashMap<String, Integer>() { { put("Hammer", 1); }}, false, this, true, new HashMap<>(), 1);
            return;
        }

        if(!selectedArea.contains(Players.getLocal())) {
            instance.currentTask = new TraversalTask(selectedArea, this);
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
}
