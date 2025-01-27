package org.lolwat.tasks.combat;

import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.MobManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.ItemUtils;
import org.lolwat.tasks.misc.TraversalTask;
import org.lolwat.types.gear.GearItem;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.types.mobs.Mob;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CombatTask implements WatTask {
    private final Skill skill;
    private Mob target;

    public CombatTask(Skill skill) {
        this.skill = skill;
    }

    @Override
    public String getName() {
        if(target != null) {
            return target.getName();
        }

        return "Selecting target";
    }

    @Override
    public void execute() {
        if(target == null) {
            target = MobManager.getInstance().getBestMob(skill);
            Logger.log("combat selected " + target.getName());
            return;
        }

        List<GearItem> toObtain = new ArrayList<>();

        //check for loadout and inventory
        if(!loadout().isEmpty()) {
            for(HashMap.Entry<EquipmentSlot, GearItem> entry : loadout().entrySet()) {
                Logger.log("checking slot " + entry.getKey().name() + " for " + entry.getValue().getName() + " x" + entry.getValue().getQuantity());
                if(!ItemUtils.equipmentSlotContains(entry.getValue().getName(), entry.getValue().getQuantity())) {
                    Logger.log("missing gear: " + entry.getValue().getName() + " x" + entry.getValue().getQuantity());
                    toObtain.add(entry.getValue());
                }
            }
        }

        if(!inventory().isEmpty()) {
            for(GearItem entry : inventory()) {
                Logger.log("checking inventory for " + entry.getName() + " x" + entry.getQuantity());
                if(!ItemUtils.inventoryContains(entry.getName(), entry.getQuantity(), false)) {
                    Logger.log("missing item: " + entry.getName() + " x" + entry.getQuantity());
                    toObtain.add(entry);
                }
            }
        }

        //send to new banking task
        if(!toObtain.isEmpty()) {
            Logger.log("missing gear or inventory items, sending to bank TODO");
            return;
        }

        //go to best location for mob
        if(!target.getBestLocation().contains(Players.getLocal())) {
            Logger.log("running to best location for " + target.getName());
            TaskManager.getInstance().setCurrentTask(new TraversalTask(target.getBestLocation(), this));
            return;
        }

        //handle food, etc
        target.getMobLogic().runPriority();

        //run the targets logic
        target.getMobLogic().execute(target, skill);
    }

    @Override
    public HashMap<EquipmentSlot, GearItem> loadout() {
        HashMap<EquipmentSlot, GearItem> loadout = new HashMap<>(); //TODO combat gear based on skill + bank contents(gp+gear owned)

        if(target != null && target.getMobLogic().gearLoadout() != null) {
            loadout.putAll(target.getMobLogic().gearLoadout());
        }

        return loadout;
    }

    @Override
    public List<GearItem> inventory() {
        List<GearItem> inventory = new ArrayList<>();//TODO combat gear based on skill + bank contents(gp+gear owned)

        if(target != null && target.getMobLogic().inventoryLoadout() != null) {
            inventory.addAll(target.getMobLogic().inventoryLoadout());
        }

        return inventory;
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
