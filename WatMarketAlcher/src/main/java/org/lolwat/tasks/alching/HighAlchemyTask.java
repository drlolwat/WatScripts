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
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.widgets.Menu;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.misc.BankingTask;
import org.lolwat.tasks.misc.MulingTask;

import java.util.HashMap;


public class HighAlchemyTask implements WatTask {
    private String item;
    @Override
    public String getName() {
        return "Market Level Alchemy";
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public void execute() {
        if(!Bank.isCached()) {
            Logger.warn("first run, caching bank data");
            ItemUtils.bank(this);
            return;
        }

        item = ConfigManager.getInstance().getCurrentTarget();
        if(item == null || item.isEmpty()) {
            ConfigManager.getInstance().getNewAlchTarget();
            return;
        }

        if (!WatUtils.canAffordCast(Normal.HIGH_LEVEL_ALCHEMY)) {
            Logger.log("We need to grab runes...");
            TaskManager.getInstance().setCurrentTask(new BankingTask(new HashMap<String, Integer>() {
                {
                    put("Nature rune", ConfigManager.getInstance().getConfigInt("buy_nature_qty"));
                }
            }, this));
            return;
        }

        int coinsOnHand = Bank.count("Coins") + Inventory.count("Coins");
        int totalToMule = coinsOnHand - ConfigManager.getInstance().getConfigInt("keep_gp");
        int inventoryCoins = Inventory.count("Coins");

        if (coinsOnHand >= ConfigManager.getInstance().getConfigInt("mule_at_gp")) {
            if (inventoryCoins != totalToMule) {
                if (!Bank.isOpen()) {
                    ItemUtils.bank(this);
                    return;
                }

                if (inventoryCoins < totalToMule) {
                    int amountToWithdraw = totalToMule - inventoryCoins;
                    if (!Bank.withdraw("Coins", amountToWithdraw)) {
                        Logger.error("Error withdrawing coins before muling");
                        return;
                    }
                } else {
                    int amountToDeposit = inventoryCoins - totalToMule;
                    if (!Bank.deposit("Coins", amountToDeposit)) {
                        Logger.error("Error depositing coins before muling");
                        return;
                    }
                }
            }

            Logger.warn("Handing off " + NumUtils.simplifyNumber(totalToMule) + " to the mule");
            Sleep.sleepUntil(() -> Inventory.count("Coins") == totalToMule, 5000);
            TaskManager.getInstance().setCurrentTask(new MulingTask("Muling Gold", Worlds.getCurrentWorld(), new AlchingBankingTask(this)));
            return;
        }

        if (!Inventory.contains(x -> x != null && x.getName().equals(item))) {
            if(Bank.contains(x -> x != null && x.getName().equals(item))) {
                Logger.log("We need to grab HA target (" + item + ")");
                TaskManager.getInstance().setCurrentTask(new AlchingBankingTask(this));
            } else {
                Logger.log("onTask: fetching new target");
                ConfigManager.getInstance().getNewAlchTarget();
                TaskManager.getInstance().setCurrentTask(new BuyAlchItemTask(this));
            }
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

        Item i = Inventory.get(item);
        if(i == null)
            return;

        int profitChange = (
                i.getHighAlchValue() -
                ConfigManager.getInstance().getAlchables().get(item) -
                ConfigManager.getInstance().getNaturePrice()) -
                ConfigManager.getInstance().getConfigInt("price_modifier");

        if(!Menu.isMenuManipulationActive()) {
            int slot = ConfigManager.getInstance().getConfigInt("item_inventory_slot");

            if (Inventory.getItemInSlot(slot) == null || (Inventory.getItemInSlot(slot) != null &&
                    !Inventory.getItemInSlot(slot).getName().equals(ConfigManager.getInstance().getCurrentTarget()))) {

                if (!Tabs.isOpen(Tab.INVENTORY)) {
                    Tabs.open(Tab.INVENTORY);
                    Sleep.sleepUntil(() -> Tabs.isOpen(Tab.INVENTORY), 5000);
                }

                if (!Inventory.drag(item, slot)) {
                    Logger.error("error dragging HA target to slot");
                    return;
                }

                Sleep.sleepUntil(() -> Inventory.getItemInSlot(slot) != null, 5000);
            }
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
            if(!Magic.deselect()) {
                Logger.error("error deselecting");
            }

            return;
        }

        ConfigManager.getInstance().setCurrentTargetAmount(
                ConfigManager.getInstance().getCurrentTargetAmount() - 1
        );

        ConfigManager.getInstance().setTotalAlchs(
                ConfigManager.getInstance().getTotalAlchs() + 1
        );

        ConfigManager.getInstance().setTotalProfit(
                ConfigManager.getInstance().getTotalProfit() + profitChange
        );

        Sleep.sleepTicks(4);
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
                put("Nature rune", ConfigManager.getInstance().getConfigInt("buy_nature_qty"));
            }
        };
    }
}
