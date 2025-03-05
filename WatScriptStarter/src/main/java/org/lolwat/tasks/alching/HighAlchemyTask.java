package org.lolwat.tasks.alching;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.grandexchange.GrandExchange;
import org.dreambot.api.methods.magic.Magic;
import org.dreambot.api.methods.magic.Normal;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.misc.AlchingBankingTask;
import org.lolwat.tasks.misc.BankingTask;

import java.util.HashMap;


public class HighAlchemyTask implements WatTask {
    private String item;
    public HighAlchemyTask() {
        item = ConfigManager.getInstance().getCurrentTarget();

        if(item == null || item.isEmpty()) {
            item = ConfigManager.getInstance().getNewAlchTarget();
        }
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
        item = ConfigManager.getInstance().getCurrentTarget();
        if (!WatUtils.canAffordCast(Normal.HIGH_LEVEL_ALCHEMY)) {
            Logger.log("We need to grab runes...");
            TaskManager.getInstance().setCurrentTask(new BankingTask(null, this));
            return;
        }

        if (!Inventory.contains(x -> x != null && x.getName().equals(item))) {
            Logger.log("We need to grab HA target (" + item + ")");
            TaskManager.getInstance().setCurrentTask(new AlchingBankingTask(this));
            return;
        }

        for(String s : clothesRequired().keySet()) {
            if(!Equipment.contains(s)) {
                TaskManager.getInstance().setCurrentTask(new BankingTask(null, this));
                return;
            }
        }

        if(Bank.isOpen()) {
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 5000);
        }

        if(GrandExchange.isOpen()) {
            if(!GrandExchange.close()) {
                Logger.log("GE did not close");
                return;
            }

            Sleep.sleepUntil(() -> !GrandExchange.isOpen(), 5000);
        }

        while (Dialogues.canContinue()) {
            Dialogues.continueDialogue();
        }

        if(Inventory.getItemInSlot(15) == null || (Inventory.getItemInSlot(15) != null &&
                !Inventory.getItemInSlot(15).getName().equals(ConfigManager.getInstance().getCurrentTarget()))) {

            if (!Tabs.isOpen(Tab.INVENTORY)) {
                Tabs.open(Tab.INVENTORY);
                Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), 5000);
            }

            if(!Inventory.drag(item, 15)) {
                Logger.error("error dragging HA target to slot");
                return;
            }

            Sleep.sleepUntil(() -> Inventory.getItemInSlot(15) != null, 5000);
        }

        if (!Tabs.isOpen(Tab.MAGIC)) {
            if(!Tabs.open(Tab.MAGIC)) {
                if(Magic.isSpellSelected()) {
                    if(!Magic.deselect()) {
                        Logger.error("error deselecting cast");
                        return;
                    }

                    Sleep.sleepUntil(() -> !Magic.isSpellSelected(), 5000);
                }

                Logger.error("problem opening magic tab");
                return;
            }

            Sleep.sleepUntil(() -> Tabs.isOpen(Tab.MAGIC), 5000);
        }

        if (!Magic.castSpell(Normal.HIGH_LEVEL_ALCHEMY)) {
            Logger.log("error casting HA");
            return;
        }

        Sleep.sleepUntil(() -> Magic.isSpellSelected() && Tabs.isOpen(Tab.INVENTORY), 5000);

        if (!Tabs.isOpen(Tab.INVENTORY)) {
            if(Magic.isSpellSelected()) {
                if(!Magic.deselect()) {
                    Logger.error("error deselecting cast");
                    return;
                }

                Sleep.sleepUntil(() -> !Magic.isSpellSelected(), 5000);
            }
            return;
        }

        if(!Inventory.interact(item)) {
            Logger.error("problem interacting with HA target");
            return;
        }

        ConfigManager.getInstance().setCurrentTargetAmount(
                ConfigManager.getInstance().getCurrentTargetAmount() - 1
        );

        Sleep.sleepTicks(5);
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
                put(ConfigManager.getInstance().getCurrentTarget(), ConfigManager.getInstance().getCurrentTargetAmount());
                put("Nature rune", ConfigManager.getInstance().getCurrentTargetAmount());
            }
        };
    }
}
