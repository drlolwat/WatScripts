package org.lolwat.tasks.types.combat;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.WatAIO;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.combat.melee.MeleeUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MeleeCombatTask implements WatTask {
    private Skill trainingSkill;
    private int minLevel;
    private int maxLevel;
    private Area zone;
    private String name;
    private boolean needsEat;
    private HashMap<String, Integer> food;

    public MeleeCombatTask(Skill skillType, int minimumLevel, int maximumLevel, Area killingArea, String monsterName, HashMap<String, Integer> foodToTake) {
        trainingSkill = skillType;
        minLevel = minimumLevel;
        zone = killingArea;
        name = monsterName;
        maxLevel = maximumLevel;

        if(foodToTake != null && foodToTake.size() > 0) {
            needsEat = true;
            food = foodToTake;
        } else {
            needsEat = false;
            food = new HashMap<>();
        }
    }

    @Override
    public String getName() {
        return "Training " + trainingSkill.getName();
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(trainingSkill) >= minLevel && Skills.getRealLevel(trainingSkill) <= maxLevel;
    }

    @Override
    public void execute(WatAIO instance) {
        List<String> toRemove = new ArrayList<>();
        HashMap<String, Integer> requiredItems = MeleeUtils.getRequiredItems();

        for (java.util.Map.Entry<String, Integer> item : requiredItems.entrySet()) {
            if (!Equipment.contains(item.getKey())) {
                if (Inventory.contains(item.getKey())) {
                    if(Inventory.get(item.getKey()).isNoted()) {
                        continue;
                    }

                    if (Inventory.get(item.getKey()).hasAction("Eat") || Inventory.interact(item.getKey())) {
                        toRemove.add(item.getKey());
                    }
                }
            } else {
                toRemove.add(item.getKey());
            }
        }

        for (String s : toRemove) {
            requiredItems.remove(s);
        }

        if (requiredItems.size() > 0) {
            Logger.log("We need to grab the rest of our melee equipment..");
            for (String s : requiredItems.keySet()) {
                Logger.log("- " + s);
            }

            instance.currentTask = new BankingTask(requiredItems, food, null, 1,this);
            return;
        }

        if(food.size() > 0) {
            for (Map.Entry<String, Integer> f : food.entrySet()) {
                if (!Inventory.contains(f.getKey())) {
                    instance.currentTask = new BankingTask(requiredItems, food, null, 1, this);
                    return;
                }
            }
        }

        if (needsEat && Combat.getHealthPercent() <= 50) {
            Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
            if (i != null && i.interact()) {
                Sleep.sleep(60, 120);
            }
        }

        if (!zone.contains(Players.getLocal()) && !Players.getLocal().isInCombat()) {
            instance.currentTask = new TraversalTask(zone, this);
            return;
        }

        if (trainingSkill == Skill.ATTACK && Combat.getCombatStyle() != CombatStyle.ATTACK) {
            Tabs.open(Tab.COMBAT);
            Combat.setCombatStyle(CombatStyle.ATTACK);
        } else if (trainingSkill == Skill.DEFENCE && Combat.getCombatStyle() != CombatStyle.DEFENCE) {
            Tabs.open(Tab.COMBAT);
            Combat.setCombatStyle(CombatStyle.DEFENCE);
        } else if (trainingSkill == Skill.STRENGTH && Combat.getCombatStyle() != CombatStyle.STRENGTH) {
            Tabs.open(Tab.COMBAT);
            Combat.setCombatStyle(CombatStyle.STRENGTH);
        }

        if(!Combat.isAutoRetaliateOn()) {
            if(!Tabs.isOpen(Tab.COMBAT)) {
                Tabs.open(Tab.COMBAT);
                Sleep.sleep(120, 240);
            }
            Combat.toggleAutoRetaliate(true);
            Sleep.sleep(60, 120);
        }

        Sleep.sleep(300, 500);

        if (!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
            Sleep.sleep(300, 500);
        }

        if(Dialogues.canContinue()) {
            Dialogues.continueDialogue();
        }

        if (!Players.getLocal().isInCombat()) {
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
    public int loopTime() {
        return 650;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatAIO instance) {

    }

    @Override
    public Skill trainsSkill() {
        return trainingSkill;
    }

    @Override
    public Integer avoidAfterLevel() {
        return maxLevel;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return MeleeUtils.getRequiredItems();
    }
}
