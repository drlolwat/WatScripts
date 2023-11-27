package org.lolwat.tasks.types.combat;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.combat.CombatStyle;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.magic.Spell;
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
import org.lolwat.misc.utils.combat.magic.MagicUtils;
import org.lolwat.misc.utils.combat.ranged.RangedUtils;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.misc.BankingTask;
import org.lolwat.tasks.types.misc.TraversalTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MagicCombatTask implements WatTask {
    private int minLevel;
    private int maxLevel;
    private Area zone;
    private String name;
    private boolean needsEat;
    private final HashMap<String, Integer> food;
    private Spell toCast;

    public MagicCombatTask(int minimumLevel, int maximumLevel, Area killingArea, String monsterName, HashMap<String, Integer> foodToTake) {
        minLevel = minimumLevel;
        zone = killingArea;
        name = monsterName;
        maxLevel = maximumLevel;

        toCast = MagicUtils.getBestSpellForLevel();

        if(foodToTake != null && !foodToTake.isEmpty()) {
            needsEat = true;
            food = foodToTake;
        } else {
            needsEat = false;
            food = new HashMap<>();
        }
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
    public void execute(WatAIO instance) {
        HashMap<String, Integer> requiredItems = new HashMap<>();
        requiredItems.putAll(food);

        if(!MagicUtils.canAffordCast(toCast)) {
            Logger.log("We need to grab runes...");
            for (String s : requiredItems.keySet()) {
                Logger.log("- " + s);
            }

            int casts = Calculations.random(75, 150);
            if(Skills.getRealLevel(Skill.MAGIC) >= 50) {
                casts *=2;
            }

            requiredItems.putAll(MagicUtils.getRunesRequired((Normal) toCast, casts));

            instance.currentTask = new BankingTask(null, requiredItems, null, 1,this);
            return;
        }

        if(!food.isEmpty()) {
            for (Map.Entry<String, Integer> f : food.entrySet()) {
                if (!Inventory.contains(f.getKey())) {
                    Logger.log("We need to grab food...");
                    instance.currentTask = new BankingTask(null, requiredItems, null, 2,this);
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

        if(!Magic.isAutocasting()) {
            if (!Tabs.isOpen(Tab.COMBAT)) {
                Tabs.open(Tab.COMBAT);
                Sleep.sleep(120, 240);
            }

            if (!Magic.setAutocastSpell(toCast)) {
                Logger.log("error setting autocast");
            }
        }

        if (!zone.contains(Players.getLocal()) && !Players.getLocal().isInCombat()) {
            instance.currentTask = new TraversalTask(zone, this);
            return;
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
        return Skill.MAGIC;
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
        return MagicUtils.getRequiredItems();
    }
}
