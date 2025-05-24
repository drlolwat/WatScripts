package org.lolwat.tasks.combat.gearing;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.managers.ItemManager;
import org.lolwat.misc.types.combat.CombatType;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;

import java.util.List;

public class CombatGearTask implements WatTask {
    private final WatTask parent;
    private final List<EquipmentSlot> slots;
    private final CombatType type;

    public CombatGearTask(WatTask parent, CombatType type, List<EquipmentSlot> slots) {
        this.parent = parent;
        this.slots = slots;
        this.type = type;
    }

    @Override
    public void execute() {
        if(!Bank.isOpen()) {
            WatUtils.bank(this);
            return;
        }

        for(EquipmentSlot s : slots) {
            WatItem bestItem;
            if(s.equals(EquipmentSlot.WEAPON)) {
                bestItem = ItemManager.getInstance().getBestWeapon(type);
            } else {
                bestItem = ItemManager.getInstance().getBestWearable(s, type);
            }

            if(bestItem == null) {
                Logger.log("no best item for " + s.name());
                continue;
            }

            Logger.log("best item for " + s.name() + " is " + bestItem.getName());

            if(Equipment.contains(bestItem.getName())) {
                Logger.log("already wearing " + bestItem.getName());
                continue;
            }

            if(Inventory.contains(bestItem.getName())) {
                Logger.log("inventory contains " + bestItem.getName() + " already");
                if(!WatUtils.equipItem(bestItem.getName(), null)) {
                    Logger.error("error equipping " + bestItem.getName());
                    return;
                }

                Sleep.sleepUntil(() -> Equipment.contains(bestItem.getName()), 5000);
                if(!Equipment.contains(bestItem.getName())) {
                    Logger.error("error equipping " + bestItem.getName());
                    return;
                }
            }

            if(Bank.contains(bestItem.getName())) {
                Logger.log("bank contains " + bestItem.getName() + ", will equip");
                if(!Bank.withdraw(bestItem.getName())) {
                    Logger.error("problem withdrawing " + bestItem.getName());
                    return;
                }

                Sleep.sleepUntil(() -> Inventory.contains(bestItem.getName()), 5000);

                if(!Inventory.contains(bestItem.getName())) {
                    Logger.error("problem withdrawing " + bestItem.getName());
                    return;
                }

                if(!WatUtils.equipItem(bestItem.getName(), null)) {
                    return;
                }

                Sleep.sleepUntil(() -> Equipment.contains(bestItem.getName()), 5000);

                if(!Equipment.contains(bestItem.getName())) {
                    Logger.error("error equipping " + bestItem.getName());
                    return;
                }

            } else {
                Logger.log("looks like we need to buy " + bestItem.getName());
            }
        }
    }

    @Override
    public boolean canPerformTask() {
        return parent.canPerformTask();
    }

    @Override
    public Skill trainsSkill() {
        return parent.trainsSkill();
    }

    @Override
    public Integer avoidAfterLevel() {
        return parent.avoidAfterLevel();
    }
}
