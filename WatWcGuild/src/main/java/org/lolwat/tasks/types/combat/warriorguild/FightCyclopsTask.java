package org.lolwat.tasks.types.combat.warriorguild;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.item.GroundItems;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.GroundItem;
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


public class FightCyclopsTask implements WatTask {
    private Skill trainingSkill;
    private boolean needsEat;
    private final HashMap<String, Integer> food;
    private String lookingForDefender;
    private final Area nonDragonFightingArea = new Area(
            new Tile(2847, 3543, 2),
            new Tile(2852, 3534, 2),
            new Tile(2858, 3534, 2),
            new Tile(2859, 3538, 2),
            new Tile(2874, 3538, 2),
            new Tile(2874, 3554, 2),
            new Tile(2838, 3555, 2),
            new Tile(2838, 3544, 2));

    private final Area dragonFightingArea = new Area(
            new Tile(2911, 9965, 0),
            new Tile(2905, 9965, 0),
            new Tile(2905, 9957, 0),
            new Tile(2940, 9957, 0),
            new Tile(2940, 9973, 0),
            new Tile(2916, 9973, 0));

    private final Area lobbyArea = new Area(2842, 3541, 2845, 3538, 2);

    private boolean needsCheck = true;
    private final boolean basement;
    private String latestDefender;
    private final HashMap<String, Integer> inventoryReq = new HashMap<>();

    public FightCyclopsTask(Skill skillType, HashMap<String, Integer> foodReq, HashMap<String, Integer> inv, String latest) {
        trainingSkill = skillType;

        if(foodReq != null && !foodReq.isEmpty()) {
            needsEat = true;
            food = foodReq;
        } else {
            needsEat = false;
            food = new HashMap<>();
        }

        basement = latest.contains("Rune");
        latestDefender = latest;
    }

    @Override
    public String getName() {
        return "Obtaining " + lookingForDefender;
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute(WatAIO instance) {
        Area fightingArea;
        HashMap<String, Integer> requiredItems = inventoryReq;
        if(basement || latestDefender.contains("Rune"))
            fightingArea = dragonFightingArea;
        else
            fightingArea = nonDragonFightingArea;

        int defLevel = Skills.getRealLevel(Skill.DEFENCE);

        if(defLevel >= 60)
            lookingForDefender = "Dragon defender";
        else if(defLevel >= 40)
            lookingForDefender = "Rune defender";
        else if (defLevel >= 30)
            lookingForDefender = "Adamant defender";
        else if (defLevel >= 20)
            lookingForDefender = "Mithril defender";
        else if(defLevel >= 10)
            lookingForDefender = "Black defender";
        else if(defLevel >= 5)
            lookingForDefender = "Steel defender";
        else if(defLevel > 1)
            lookingForDefender = "Iron defender";
        else
            lookingForDefender = "Bronze defender";

        List<String> toRemove = new ArrayList<>();

        for (java.util.Map.Entry<String, Integer> item : requiredItems.entrySet()) {
            if (!Equipment.contains(item.getKey())) {
                if (Inventory.contains(item.getKey())) {
                    if(Inventory.get(item.getKey()).isNoted()) {
                        continue;
                    }

                    if (Inventory.get(item.getKey()).hasAction("Eat")) {
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

        if(!Inventory.contains("Warrior guild token")) {
            Logger.log("We need tokens so we'll fight for 'em.");
            instance.currentTask = new FightArmorSetTask(trainsSkill(), new HashMap<String, Integer>() {
                {
                    put("Lobster", 14);
                }
            }, latestDefender);
            return;
        }

        if (!requiredItems.isEmpty()) {
            Logger.log("We need to grab the rest of our melee equipment..");
            for (String s : requiredItems.keySet()) {
                Logger.log("- " + s);
            }

            instance.currentTask = new BankingTask(food, null, 1, this);
            return;
        }

        if(!food.isEmpty()) {
            for (Map.Entry<String, Integer> f : food.entrySet()) {
                if (!Inventory.contains(f.getKey())) {
                    instance.currentTask = new BankingTask(food, null, 1, this);
                    return;
                }
            }
        }

        if(Bank.isOpen()) {
            if(!Inventory.contains("Warrior guild token")) {
                Bank.withdrawAll("Warrior guild token");
                Sleep.sleepUntil(() -> Inventory.contains("Warrior guild token"), 10000);
            }
        }

        if (needsEat && Combat.getHealthPercent() <= 50) {
            Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
            if (i != null && i.interact()) {
                Sleep.sleep(60, 120);
            }
        }

        if (!fightingArea.contains(Players.getLocal())) {
            instance.currentTask = new TraversalTask(fightingArea, this);
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

        if(Inventory.isFull()) {
            instance.currentTask = new BankingTask(food, null, 1, this);
            return;
        }

        if(needsCheck)
            needsCheck = false;

        for(GroundItem i : GroundItems.all()) {
            if(i.getName().contains("defender")) {
                if(!i.interact("Take")) {
                    Logger.error("Error picking up defender...");
                } else {
                    latestDefender = i.getName();
                    Sleep.sleepUntil(() -> Inventory.contains(latestDefender), Calculations.random(1500, 5000));

                    if(Inventory.contains(latestDefender)) {
                        if(!lobbyArea.contains(Players.getLocal())) {
                            instance.currentTask = new TraversalTask(lobbyArea, this);
                            return;
                        }
                    }
                }
            }
        }

        if(Inventory.contains(latestDefender) && !Equipment.contains(latestDefender)) {
            if(!Inventory.interact(latestDefender, "Wield")) {
                Logger.error("Error wielding new defender...");
                return;
            }

            Sleep.sleepUntil(() -> Equipment.contains(latestDefender), Calculations.random(1500, 5000));
        }

        if(latestDefender.equalsIgnoreCase(lookingForDefender)) {
            Logger.log("We got the defender we were looking for!");
            instance.currentTask = null;
            return;
        }

        if (!Players.getLocal().isInCombat()) {
            NPC closestFriend = NPCs.closest(x -> x != null && x.exists() && x.getName().equalsIgnoreCase("cyclops") && !x.isInCombat() && !x.isHealthBarVisible() && fightingArea.contains(x));
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
        return 100;
    }

    @Override
    public Quest completesQuest() {
        return null;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        HashMap<String, Integer> gear = MeleeUtils.getRequiredItems(true);
        if(!latestDefender.isEmpty()) {
            String toRemove = "";
            for(Map.Entry<String, Integer> e : gear.entrySet()) {
                if(e.getKey().contains("kiteshield")) {
                    toRemove = e.getKey();
                }
            }

            if(!toRemove.isEmpty()) {
                gear.remove(toRemove);
                gear.put(latestDefender, 1);
            }
        }

        return gear;
    }
}
