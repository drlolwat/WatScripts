package org.lolwat.tasks.combat;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.combat.ranged.RangedUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RangedCombatTask implements WatTask {
    private final int minLevel;
    private final int maxLevel;
    private final Area zone;
    private final String name;
    private final HashMap<String, Integer> food;

    public RangedCombatTask(int minimumLevel, int maximumLevel, Area killingArea, String monsterName, HashMap<String, Integer> foodToTake) {
        minLevel = minimumLevel;
        zone = killingArea;
        name = monsterName;
        maxLevel = maximumLevel;

        if(foodToTake != null && !foodToTake.isEmpty()) {
            food = foodToTake;
        } else {
            food = new HashMap<>();
        }
    }

    @Override
    public String getName() {
        return "Training ranged";
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.RANGED) >= minLevel && Skills.getRealLevel(Skill.RANGED) <= maxLevel;
    }

    @Override
    public void execute() {
        for(String n : clothesRequired().keySet()) {
            if(!Equipment.contains(n)) {
                if(!Inventory.contains(n) || Inventory.get(n).isNoted()) {
                    Logger.error("RangedCombatTask was missing " + n + " (" + Inventory.count(n) + "/" + clothesRequired().get(n) + ")");
                    TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, this));
                    return;
                } else {
                    if(!GenericUtils.equipItem(n, null)) {
                        Logger.error("Error equipping item during RangedCombatTask");
                    }
                }
            }
        }

        for(String n : inventoryRequired().keySet()) {
            if(!Inventory.contains(n) || Inventory.get(n).isNoted()) {
                Logger.error("RangedCombatTask was missing " + n + " (" + Inventory.count(n) + "/" + inventoryRequired().get(n) + ")");
                TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryRequired(), null, 1, this));
                return;
            }
        }

        if (!food.isEmpty()) {
            if(Inventory.isEmpty() || !Inventory.contains(x -> x != null && x.hasAction("Eat"))) {
                Logger.error("RangedCombatTask is missing food");
                TaskManager.getInstance().setCurrentTask(new BankingTask(inventoryRequired(), null, Calculations.random(1, 5), this));
                return;
            }

            if(Combat.getHealthPercent() <= 50) {
                Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
                if (i != null && i.interact()) {
                    Sleep.sleep(60, 120);
                }
            }
        }

        if (!zone.contains(Players.getLocal()) && !Players.getLocal().isInCombat()) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(zone, this));
            return;
        }

        if (!Combat.getCombatStyle().equals(CombatStyle.RANGED_RAPID)) {
            if (!Tabs.isOpen(Tab.COMBAT)) {
                Tabs.open(Tab.COMBAT);
                Sleep.sleep(120, 140);
            }

            Combat.setCombatStyle(CombatStyle.RANGED_RAPID);
        }

        if(!Combat.isAutoRetaliateOn()) {
            if(!Tabs.isOpen(Tab.COMBAT)) {
                Tabs.open(Tab.COMBAT);
                Sleep.sleep(120, 240);
            }
            Combat.toggleAutoRetaliate(true);
            Sleep.sleep(60, 120);
        }


        if (!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
            Sleep.sleep(300, 500);
        }

        if(Dialogues.canContinue()) {
            Dialogues.continueDialogue();
        }

        GenericUtils.handleSpecial();

        if (!Players.getLocal().isInCombat()) {
            if(GenericUtils.tooManyPlayers(10, 3)) {
                TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
                return;
            }

            NPC closestFriend = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase(name) && !x.isInCombat() && !x.isHealthBarVisible() && zone.contains(x));
            if (closestFriend != null && !closestFriend.isInCombat() && !closestFriend.isHealthBarVisible() && closestFriend.interact("Attack")) {
                GenericUtils.moveMouseInOrOut();
            }
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.RANGED;
    }

    @Override
    public Integer avoidAfterLevel() {
        return maxLevel;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        HashMap<String, Integer> requiredItems = RangedUtils.getRequiredItems();
        requiredItems.put(RangedUtils.bestArrow(), -1000);
        return requiredItems;
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return food;
    }

    @Override
    public List<String> inventoryTolerated() {
        List<String> ret = new ArrayList<String>() { { add(RangedUtils.bestArrow()); } };
        for (Map.Entry<String, Integer> f : clothesRequired().entrySet()) {
            ret.add(f.getKey());
        }
        return ret;
    }
}
