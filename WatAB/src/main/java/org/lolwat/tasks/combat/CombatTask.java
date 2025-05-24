package org.lolwat.tasks.combat;

import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.types.combat.CombatType;
import org.lolwat.misc.utils.CombatUtils;
import org.lolwat.tasks.combat.gearing.CombatGearTask;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.types.mobs.Mob;

import java.util.ArrayList;
import java.util.List;

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
                    if(!ItemManager.getInstance().isValidWeapon(i.getName(), type)) {
                        Logger.log(i.getName() + " is not a valid weapon for this task");
                        acceptable = false;
                    }
                }
            } else {
                acceptable = false;
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
