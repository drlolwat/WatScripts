package org.lolwat.tasks.combat;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.types.combat.CombatType;
import org.lolwat.misc.utils.CombatUtils;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.banking.WithdrawSingleItemTask;
import org.lolwat.tasks.combat.gearing.CombatGearTask;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.types.mobs.Mob;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CombatTask implements WatTask {
    private final Skill skill;
    private Mob target;
    private CombatType type;

    public CombatTask(Skill skill) {
        this.skill = skill;

        if (skill == Skill.ATTACK || skill == Skill.DEFENCE || skill == Skill.STRENGTH)
            type = CombatType.MELEE;
        else if (skill == Skill.RANGED)
            type = CombatType.RANGED;
        else if (skill == Skill.MAGIC)
            type = CombatType.MAGIC;
    }

    @Override
    public void execute() {
        if(target == null) {
            target = CombatUtils.getBestMob(skill);
            Logger.log("combat selected " + target.getName() + " for skill " + skill.getName());
            return;
        }

        if(!Players.getLocal().isInCombat()) {
            Mob bestMob = CombatUtils.getBestMob(skill);
            if (target == null || !target.equals(bestMob)) {
                target = bestMob;
                Logger.log("Switched to better mob: " + target.getName() + " for skill " + skill.getName());
                return;
            }
        }

        // check for required items. we will do this custom
        List<EquipmentSlot> slotsMissing = new ArrayList<>();
        List<EquipmentSlot> slotsRequired = new ArrayList<>(target.getMobLogic().slotsRequired());

        if(type.equals(CombatType.RANGED)) {
            slotsRequired.add(EquipmentSlot.ARROWS);
            slotsRequired.remove(EquipmentSlot.SHIELD);
        }

        for(EquipmentSlot s : slotsRequired) {
            boolean acceptable = true;
            if(!Equipment.isSlotEmpty(s)) {
                Item i = Equipment.getItemInSlot(s);
                if(i != null) {
                    if(s.equals(EquipmentSlot.WEAPON)) {
                        if (!ItemManager.getInstance().isValidWeapon(i.getName(), type)) {
                            Logger.log(i.getName() + " is not a valid weapon for this task");
                            acceptable = false;
                        }
                    } else {
                        if (!ItemManager.getInstance().isValidWearable(i.getName(), s, type)) {
                            Logger.log(i.getName() + " is not a valid item for this task");
                            acceptable = false;
                        }
                    }
                }
            } else {
                acceptable = false;
                for(Item i : Inventory.all()) {
                    if(i == null || i.isNoted()) continue;
                    if(s.equals(EquipmentSlot.WEAPON)) {
                        if (ItemManager.getInstance().isValidWeapon(i.getName(), type)) {
                            Logger.log("we have a valid weapon in inventory");
                            if(!WatUtils.equipItem(i.getName(), null)) {
                                Logger.error("error equipping weapon " + i.getName());
                                return;
                            }

                            Sleep.sleepUntil(() -> Equipment.contains(i.getName()), 5000);

                            if(!Equipment.contains(i.getName())) {
                                Logger.error("error equipping weapon " + i.getName());
                                return;
                            }

                            acceptable = true;
                            break;
                        }
                    } else {
                        if (ItemManager.getInstance().isValidWearable(i.getName(), s, type)) {
                            Logger.log("we have a valid item in inventory");
                            if(!WatUtils.equipItem(i.getName(), null)) {
                                Logger.error("error equipping wearable " + i.getName());
                                return;
                            }

                            Sleep.sleepUntil(() -> Equipment.contains(i.getName()), 5000);

                            if(!Equipment.contains(i.getName())) {
                                Logger.error("error equipping wearable " + i.getName());
                                return;
                            }

                            acceptable = true;
                            break;
                        }
                    }
                }
            }

            if(!acceptable) {
                Logger.log("we need to get equipment for slot " + s.name());
                slotsMissing.add(s);
            }
        }

        if(!slotsMissing.isEmpty()) {
            Logger.log("we need to get equipment for " + slotsMissing.size() + " slots");
            TaskManager.getInstance().setCurrentTask(new CombatGearTask(this, type, slotsMissing));
            return;
        }

        if(!target.getMobLogic().inventoryLoadout().isEmpty()) {
            for(Map.Entry<WatItem, Integer> map : target.getMobLogic().inventoryLoadout().entrySet()) {
                if(!Inventory.contains(x -> x != null && x.getName().equalsIgnoreCase(map.getKey().getName()) && !x.isNoted())) {
                    Logger.log("we need to get " + map.getKey().getName() + " x" + map.getValue());
                    TaskManager.getInstance().setCurrentTask(new WithdrawSingleItemTask(map.getKey().getName(), map.getValue(), this));
                    return;
                }
            }
        }

        //go to best location for mob
        if(!target.getBestLocation().contains(Players.getLocal())) {
            Logger.log("running to best location for " + target.getName());
            TaskManager.getInstance().setCurrentTask(new WalkingTask(target.getBestLocation(), this));
            return;
        }

        //handle food, etc
        target.getMobLogic().runPriority(target, skill);

        //run the targets logic
        target.getMobLogic().execute(target, skill);
    }


    @Override
    public boolean canPerformTask() {
        return avoidAfterLevel() > Skills.getRealLevel(skill);
    }

    @Override
    public Skill trainsSkill() {
        return skill;
    }

    @Override
    public Integer avoidAfterLevel() {
        return ConfigManager.getInstance().getSkillTarget(skill);
    }

    @Override
    public boolean requiresMembers() {
        if(target == null) return true;
        return target.isMembersOnly();
    }
}
