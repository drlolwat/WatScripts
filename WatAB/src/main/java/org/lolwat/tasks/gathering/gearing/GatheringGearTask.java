package org.lolwat.tasks.gathering.gearing;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.exchange.BuyMultipleItemsTask;
import org.lolwat.tasks.exchange.BuySingleItemTask;
import org.lolwat.types.WatZone;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.gear.WatTool;
import org.lolwat.types.interfaces.WatTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GatheringGearTask implements WatTask {
    private final Skill skill;
    private final WatZone zone;
    private final WatTask parent;

    public GatheringGearTask(Skill sk, WatZone zone, WatTask parent) {
        this.skill = sk;
        this.zone = zone;
        this.parent = parent;
    }

    @Override
    public void execute() {
        if(!Bank.isOpen()) {
            WatUtils.bank(this);
            return;
        }

        if(zone.isGatheringItemRequired()) {
            WatTool bestTool = ItemManager.getInstance().getBestTool(skill);

            if(Inventory.isFull()) {
                if(!Bank.depositAllExcept(bestTool.getName())) {
                    Logger.error("problem depositing all items when inv is full during gathering gearing 1");
                    return;
                }
            }

            // get rid of other tools
            Item toolSlot = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
            if(toolSlot != null) {
                if(ItemManager.getInstance().isValidTool(toolSlot.getName(), skill)) {
                    if(!toolSlot.getName().equalsIgnoreCase(bestTool.getName())) {
                        if(!Bank.depositAllEquipment()) {
                            Logger.error("problem depositing gear tool during gearing");
                            return;
                        }
                    }
                }
            }

            for(Item i : Inventory.all()) {
                if(i == null) continue;

                if(ItemManager.getInstance().isValidTool(i.getName(), skill)) {
                    if(!i.getName().equalsIgnoreCase(bestTool.getName())) {
                        if(!Bank.depositAll(i)) {
                            Logger.error("problem depositing individual tool during gearing");
                            return;
                        }
                    }
                }
            }

            if (Inventory.count(x -> x != null && x.getName().contains(bestTool.getName()) && x.isNoted()) > 0) {
                if (!Bank.depositAll(bestTool.getName())) {
                    Logger.log("failed to deposit item: " + bestTool.getName());
                    return;
                }

                Sleep.sleepUntil(() -> Inventory.count(x -> x != null && x.getName().contains(bestTool.getName()) && x.isNoted()) == 0, 5000);
            }

            if (Inventory.contains(x -> x != null && x.getName().contains(bestTool.getName()) && !x.isNoted())) {
                Logger.log("we have the tool we need: " + bestTool.getName());
                TaskManager.getInstance().setCurrentTask(parent);
                return;
            }

            if (Bank.contains(x -> x != null && x.getName().contains(bestTool.getName()))) {
                if (!Bank.withdraw(bestTool.getName(), 1)) {
                    Logger.error("problem withdrawing tool: " + bestTool.getName());
                    return;
                }

                Sleep.sleepUntil(() -> Inventory.contains(x -> x != null && x.getName().contains(bestTool.getName())), 5000);

                if (!Bank.depositAllExcept(bestTool.getName())) {
                    Logger.error("problem depositing all items except needed tool");
                    return;
                }
            } else {
                TaskManager.getInstance().setCurrentTask(new BuySingleItemTask(bestTool.getName(), 1, bestTool.getPrice(), this));
                return;
            }
        }

        if(Inventory.isFull()) {
            if(!Bank.depositAllItems()) {
                Logger.error("problem depositing all items when inv is full during gathering gearing 2");
                return;
            }
        }

        HashMap<WatItem, Integer> toBuy = new HashMap<>();
        if(!zone.getExtraGear().isEmpty()) {
            for(Map.Entry<WatItem, Integer> item : zone.getExtraGear().entrySet()) {
                if(!WatUtils.gearContains(item.getKey(), item.getValue())) {
                    if(!WatUtils.inventoryContains(item.getKey(), item.getValue(), false)) {
                        if (!WatUtils.bankContains(item.getKey(), item.getValue())) {
                            Logger.log("we will need to buy " + item.getKey().getName() + " x" + item.getValue() + " for the gathering task");
                            toBuy.put(item.getKey(), item.getValue());
                        } else {
                            if(!WatUtils.bankWithdraw(item.getKey(), item.getValue())) {
                                Logger.error("problem withdrawing gear item during gearing for gathering");
                            }
                        }
                    }

                    if(toBuy.isEmpty()) {
                        Item i = Inventory.get(x -> x != null && x.getName().contains(item.getKey().getName()) && x.getAmount() >= item.getValue());
                        if(i != null) {
                            if(!WatUtils.equipItem(i.getName())) {
                                Logger.error("problem equipping the req'd gathering gear");
                                return;
                            }
                        } else {
                            Logger.error("somehow the gathering item was not in the inventory");
                            return;
                        }
                    }
                }
            }
        }

        List<WatItem> toKeep = new ArrayList<>();
        if(!zone.getExtraInventory().isEmpty()) {
            toKeep.addAll(zone.getExtraInventory().keySet());
        }

        if(!zone.getExtraGear().isEmpty()) {
            toKeep.addAll(zone.getExtraGear().keySet());
        }

        for(Item i : Inventory.all()) {
            if(i == null) continue;
            if(!toKeep.isEmpty()) {
                for(WatItem wi : toKeep) {
                    if(!wi.getName().contains(i.getName())) {
                       if(!Bank.depositAll(i)) {
                            Logger.error("problem depositing all extra gather inventory: " + i.getName());
                            return;
                       }
                    }
                }
            }
        }

        if(!zone.getExtraInventory().isEmpty()) {
            for(Map.Entry<WatItem, Integer> item : zone.getExtraInventory().entrySet()) {
                if(!WatUtils.inventoryContains(item.getKey(), item.getValue(), false)) {
                    if(!WatUtils.bankContains(item.getKey(), item.getValue())) {
                        Logger.log("we need to buy " + item.getKey().getName() + " x" + item.getValue());
                        toBuy.put(item.getKey(), item.getValue());
                    } else {
                        if(!WatUtils.bankWithdraw(item.getKey(), item.getValue())) {
                            Logger.error("problem withdrawing inv item during gearing for gathering");
                            return;
                        }
                    }
                }
            }
        }

        if(toBuy.isEmpty()) {
            Logger.log("all things obtained, lets go gathering");
            TaskManager.getInstance().setCurrentTask(parent);
        } else {
            Logger.log("we need to buy some things");
            TaskManager.getInstance().setCurrentTask(new BuyMultipleItemsTask(toBuy, this));
        }
    }

    @Override
    public boolean canPerformTask() {
        return true;
    }

    @Override
    public Skill trainsSkill() {
        return skill;
    }

    @Override
    public Integer avoidAfterLevel() {
        return ConfigManager.getInstance().getSkillTarget(skill);
    }
}
