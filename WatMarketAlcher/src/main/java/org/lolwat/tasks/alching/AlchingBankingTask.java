package org.lolwat.tasks.alching;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.types.WatTask;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.misc.MulingTask;

import java.util.HashMap;

public class AlchingBankingTask implements WatTask {
    private final WatTask post;

    public AlchingBankingTask(WatTask post) {
        this.post = post;
    }

    @Override
    public String getName() {
        return "Banking (Alching)";
    }

    @Override
    public void execute() {
        if (post == null) {
            Logger.error("HAB: post task was null for some reason, going to HA");
            TaskManager.getInstance().setCurrentTask(new HighAlchemyTask());
            return;
        }

        if (!Bank.isOpen()) {
            ItemUtils.bank(this);
            return;
        }

        if(Bank.contains("Nature rune")) {
            if(!Bank.withdraw("Nature rune")) {
                Logger.error("error withdrawing excess runes");
                return;
            }
        }

        int coinsOnHand = Bank.count("Coins") + Inventory.count("Coins");
        if(coinsOnHand >= ConfigManager.getInstance().getConfigInt("mule_at_gp")) {
            int totalToMule = ConfigManager.getInstance().getConfigInt("mule_at_gp")
                    - ConfigManager.getInstance().getConfigInt("keep_gp");
            int inventoryCoins = Inventory.count("Coins");

            if(totalToMule > 0) {
                if (inventoryCoins != totalToMule) {
                    int difference = (inventoryCoins - totalToMule);
                    if(inventoryCoins > totalToMule) {
                        if(!Bank.deposit("Coins", difference)) {
                            Logger.error("error depositing coins before muling");
                            return;
                        }
                    } else {
                        if(!Bank.withdraw("Coins", difference)) {
                            Logger.error("error withdrawing coins before muling");
                        }
                    }
                }

                Logger.warn("handing off " + NumUtils.simplifyNumber(totalToMule) + " to the mule");
                Sleep.sleepUntil(() -> Inventory.count("Coins") == ConfigManager.getInstance().getConfigInt("mule_at_gp"), 5000);
                TaskManager.getInstance().setCurrentTask(new MulingTask("Muling Gold", Worlds.getCurrentWorld(), this));
                return;
            }
        }

        String target = ConfigManager.getInstance().getCurrentTarget();
        int targetsOnHand = Bank.count(x -> x != null && x.getName().equals(target))
                + Inventory.count(x -> x != null && x.getName().equals(target));

        if (targetsOnHand > 0) {
            if (target != null) {
                if (Bank.contains(target)) {
                    ItemUtils.setBankMode(BankMode.NOTE);
                    if (!Bank.withdrawAll(target)) {
                        Logger.error("problem withdrawing HA target");
                        return;
                    }
                    ItemUtils.setBankMode(BankMode.ITEM);
                }

                Sleep.sleepUntil(() -> !Bank.contains(target) && Inventory.contains(target), 5000);

                if (!Inventory.contains(target)) {
                    Logger.error("somehow didnt have the item");
                    return;
                }

                TaskManager.getInstance().setCurrentTask(post);
            } else {
                Logger.error("target was null, getting a new one");
                ConfigManager.getInstance().getNewAlchTarget();
            }
        } else {
            if (ConfigManager.getInstance().allowedToBuy(target)) {
                int priceBuff = ConfigManager.getInstance().getConfigInt("price_modifier");
                int toBuy = ConfigManager.getInstance().getCurrentTargetAmount();
                int itemPrice = ConfigManager.getInstance().itemCost(target) + priceBuff;
                int canAfford = 0;

                int cost = itemPrice + priceBuff;
                if (cost > 0) {
                    canAfford = coinsOnHand / cost;
                }

                int finalBuyAmount = Math.min(canAfford, toBuy);
                if (finalBuyAmount > 0) {
                    Logger.log("We can buy up to " + finalBuyAmount + " of: " + target);
                    if (Bank.contains("Coins") && !Bank.withdrawAll("Coins")) {
                        Logger.error("problem withdrawing coins");
                        return;
                    }

                    TaskManager.getInstance().setCurrentTask(new BuyAlchItemTask(post));
                } else {
                    if (ConfigManager.getInstance().getFailedAttempts() > 5) {
                        Logger.log("We cant afford to buy any alchs (5 attempts), reverse muling the minimum keep_gp");
                        ConfigManager.getInstance().setFailedAttempts(0);
                        TaskManager.getInstance().setCurrentTask(new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                            {
                                put("Coins", ConfigManager.getInstance().getConfigInt("keep_gp"));
                            }
                        }, this));
                    } else {
                        ConfigManager.getInstance().setFailedAttempts(
                                ConfigManager.getInstance().getFailedAttempts() + 1
                        );

                        Logger.log("we cant afford any alchs for this item, finding a new one");
                        ConfigManager.getInstance().getNewAlchTarget();
                    }
                }
            } else {
                ConfigManager.getInstance().getNewAlchTarget();
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public boolean requiresLogin() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return Skill.HITPOINTS;
    }

    @Override
    public Integer avoidAfterLevel() {
        return 101;
    }
}
