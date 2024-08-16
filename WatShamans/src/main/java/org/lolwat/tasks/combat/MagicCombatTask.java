package org.lolwat.tasks.combat;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
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
import org.lolwat.misc.utils.combat.magic.MagicUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.ArrayList;
import java.util.HashMap;


public class MagicCombatTask implements WatTask {
    private final int minLevel;
    private final int maxLevel;
    private final Area zone;
    private final String name;
    private final HashMap<String, Integer> food;
    private final Spell toCast;
    private int casts;

    public MagicCombatTask(int minimumLevel, int maximumLevel, Area killingArea, String monsterName, HashMap<String, Integer> foodToTake) {
        minLevel = minimumLevel;
        zone = killingArea;
        name = monsterName;
        maxLevel = maximumLevel;

        toCast = MagicUtils.getBestSpellForLevel();

        if(foodToTake != null && !foodToTake.isEmpty()) {
            food = foodToTake;
        } else {
            food = new HashMap<>();
        }

        casts = Calculations.random(75, 150);
    }

    @Override
    public String getName() {
        return "Training magic";
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.MAGIC) >= minLevel && Skills.getRealLevel(Skill.MAGIC) <= maxLevel;
    }

    @Override
    public void execute() {
        HashMap<String, Integer> requiredItems = new HashMap<>();

        if (Skills.getRealLevel(Skill.MAGIC) >= 50) {
            casts *= 2;
        }

        requiredItems.putAll(MagicUtils.getRunesRequired((Normal) toCast, casts));
        requiredItems.putAll(food);

        if (Equipment.isEmpty() || Equipment.all(x -> x.getName().toLowerCase().contains("staff")).isEmpty()) {
            Logger.log("We need to grab runes and staff...");
            TaskManager.getInstance().setCurrentTask(new BankingTask(requiredItems, null, 1, this));
            return;
        }

        if (!MagicUtils.canAffordCast(toCast)) {
            Logger.log("We need to grab runes...");
            TaskManager.getInstance().setCurrentTask(new BankingTask(requiredItems, null, 1, this));
            return;
        }

        if (!food.isEmpty()) {
            if (Inventory.isEmpty() || Inventory.all(x -> x.hasAction("Eat")).isEmpty()) {
                Logger.log("We need to grab food...");
                TaskManager.getInstance().setCurrentTask(new BankingTask(requiredItems, null, 2, this));
                return;
            }
        }

        if (!food.isEmpty() && Combat.getHealthPercent() <= 50) {
            Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
            if (i != null && i.interact()) {
                Sleep.sleep(60, 120);
            }
        }

        if (!Magic.isAutocasting()) {
            if (Bank.isOpen()) {
                Bank.close();
                Sleep.sleep(120, 240);
            }

            if (!Tabs.isOpen(Tab.COMBAT)) {
                Tabs.open(Tab.COMBAT);
                Sleep.sleep(120, 240);
            }

            if (!Magic.setAutocastSpell(toCast)) {
                Logger.log("error setting autocast");
            }
        }

        if (!zone.contains(Players.getLocal()) && !Players.getLocal().isInCombat()) {
            TaskManager.getInstance().setCurrentTask(new TraversalTask(zone, this));
            return;
        }

        if (!Combat.isAutoRetaliateOn()) {
            if (!Tabs.isOpen(Tab.COMBAT)) {
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

        if (Dialogues.canContinue()) {
            Dialogues.continueDialogue();
        }


        if (!Players.getLocal().isInCombat()) {
            if (GenericUtils.tooManyPlayers(10, 3)) {
                TaskManager.getInstance().setCurrentTask(new HopperTask(0, this));
                return;
            }

            GenericUtils.handlePickup(new ArrayList<>());


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
        return Skill.MAGIC;
    }
    @Override
    public Integer avoidAfterLevel() {
        return maxLevel;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return MagicUtils.getRequiredItems();
    }
}
