package org.lolwat.tasks.alching;

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
import org.lolwat.WatScript;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.misc.BankingTask;

import java.util.HashMap;


public class HighAlchemyTask implements WatTask {
    private final String item;
    boolean alched = false;

    public HighAlchemyTask() {
        item = "Yew longbow";
    }

    @Override
    public String getName() {
        return "High level alchemy";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute() {
        int casts = Calculations.random(75, 150);
        if (Skills.getRealLevel(Skill.MAGIC) >= 75) {
            casts *= 2;
        }

        HashMap<String, Integer> requiredItems = new HashMap<>();
        requiredItems.put(item, -casts);
        requiredItems.putAll(WatUtils.getRunesRequired(Normal.HIGH_LEVEL_ALCHEMY, casts));

        if (!WatUtils.canAffordCast(Normal.HIGH_LEVEL_ALCHEMY)) {
            Logger.log("We need to grab runes...");
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, this, null));
            return;
        }

        if (!Inventory.contains(x -> x != null && x.isNoted() && x.getName().equals(item))) {
            Logger.log("We need to grab HA target (" + item + ")");
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, this, null));
            return;
        }

        for(String s : clothesRequired().keySet()) {
            if(!Equipment.contains(s)) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(null, null, 1, this, null));
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
            Sleep.sleepUntil(() -> Inventory.getItemInSlot(15) != null, 5000);
        }

        if (!Tabs.isOpen(Tab.MAGIC)) {
            Tabs.open(Tab.MAGIC);
            Sleep.sleep(120, 240);
        }

        if (!Magic.castSpell(Normal.HIGH_LEVEL_ALCHEMY)) {
            Logger.log("error casting HA");
            return;
        }

        Sleep.sleepUntil(() -> Magic.isSpellSelected() && Tabs.isOpen(Tab.INVENTORY), 5000);

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
            Sleep.sleepUntil(() -> alched && !Players.getLocal().isAnimating() && !Players.getLocal().isMoving(), 5000);
        } else {
            Logger.log("issue interacting with " + item);
        }
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public void onExpGained(Skill skill, int amount, WatScript instance) {
        alched = true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.MAGIC;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }

    @Override
    public HashMap<String, Integer> clothesRequired() {
        return new HashMap<String, Integer>() {
            {
                put("Staff of fire", 1);
            }
        };
    }

    @Override
    public HashMap<String, Integer> inventoryRequired() {
        return new HashMap<String, Integer>() {
            {
                put("Yew longbow", -2000);
                put("Nature rune", 2000);
            }
        };
    }
}
