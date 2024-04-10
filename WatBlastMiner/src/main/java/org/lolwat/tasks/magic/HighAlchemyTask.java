package org.lolwat.tasks.magic;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.WatAIO;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.misc.utils.combat.magic.MagicUtils;
import org.lolwat.tasks.misc.BankingTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class HighAlchemyTask implements WatTask {
    private final int minLevel;
    private final int maxLevel;
    private final String item;
    boolean alched = false;

    List<String> items = new ArrayList<String>() {
        {
            add("Gold necklace");
        }
    };

    public HighAlchemyTask(int minimumLevel, int maximumLevel) {
        minLevel = minimumLevel;
        maxLevel = maximumLevel;
        Collections.shuffle(items);
        item = items.get(0);
    }

    @Override
    public String getName() {
        return "High level alchemy";
    }

    @Override
    public boolean canPerformTask() {
        return Skills.getRealLevel(Skill.MAGIC) >= minLevel && Skills.getRealLevel(Skill.MAGIC) <= maxLevel;
    }

    @Override
    public void execute() {
        int casts = Calculations.random(75, 150);
        if (Skills.getRealLevel(Skill.MAGIC) >= 75) {
            casts *= 2;
        }

        HashMap<String, Integer> requiredItems = new HashMap<>();
        requiredItems.put(item, -casts);
        requiredItems.putAll(MagicUtils.getRunesRequired(Normal.HIGH_LEVEL_ALCHEMY, casts));

        if (!MagicUtils.canAffordCast(Normal.HIGH_LEVEL_ALCHEMY)) {
            Logger.log("We need to grab runes...");
            TaskManager.getInstance().setCurrentTask(new BankingTask(requiredItems, null, 1, this));
            return;
        }

        if (!Inventory.contains(item)) {
            Logger.log("We need to grab HA target (" + item + ")");
            TaskManager.getInstance().setCurrentTask(new BankingTask(requiredItems, null, 1, this));
            return;
        }

        for(String s : clothesRequired().keySet()) {
            if(!Equipment.contains(s)) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(requiredItems, null, 1, this));
                return;
            }
        }

        if(Bank.isOpen()) {
            Bank.close();
            Sleep.sleep(100, 200);
        }

        if (Dialogues.canContinue()) {
            Dialogues.continueDialogue();
        }

        if(Inventory.getItemInSlot(15) == null) {
            if (!Tabs.isOpen(Tab.INVENTORY)) {
                Tabs.open(Tab.INVENTORY);
                Sleep.sleep(120, 240);
            }

            Inventory.drag(item, 15);
        }

        if (!Tabs.isOpen(Tab.MAGIC)) {
            Tabs.open(Tab.MAGIC);
            Sleep.sleep(120, 240);
        }

        if (!Magic.castSpell(Normal.HIGH_LEVEL_ALCHEMY)) {
            Logger.log("error casting HA");
            return;
        }

        Sleep.sleep(100, 200);

        if (!Tabs.isOpen(Tab.INVENTORY)) {
            Logger.log("inventory was not open..?");
            if(Magic.isSpellSelected()) {
                Magic.deselect();
                Sleep.sleep(100, 200);
            }
            return;
        }

        if (Inventory.interact(item)) {
            alched = false;
            Sleep.sleep(750, 1500);
            Sleep.sleepUntil(() -> alched && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), Calculations.random(750, 1000));
        } else {
            Logger.log("issue interacting with " + item);
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
        Sleep.sleep(360, 720);
        alched = true;
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
        return new HashMap<String, Integer>() {
            {
                putAll(GenericUtils.getSkillingGear());
                put("Staff of fire", 1);
            }
        };
    }

    @Override
    public List<String> inventoryTolerated() {
        return new ArrayList<String>() { {
            add("Gold necklace");
            add("Staff of fire");
        } };
    }
}
