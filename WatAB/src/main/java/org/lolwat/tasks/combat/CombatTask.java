package org.lolwat.tasks.combat;

import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.MobManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.misc.TraversalTask;
import org.lolwat.types.gear.GearItem;
import org.lolwat.types.interfaces.WatTask;
import org.lolwat.types.mobs.Mob;

import java.util.HashMap;

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

        //go to best location for mob
        if(!target.getBestLocation().contains(Players.getLocal())) {
            Logger.log("running to best location for " + target.getName());
            TaskManager.getInstance().setCurrentTask(new TraversalTask(target.getBestLocation(), this));
            return;
        }

        //handle food, etc

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
    public HashMap<String, Integer> inventory() {
        HashMap<String, Integer> inventory = new HashMap<>(); //TODO combat gear based on skill + bank contents(gp+gear owned)

        if(target != null && target.getMobLogic().inventoryLoadout() != null) {
            inventory.putAll(target.getMobLogic().inventoryLoadout());
        }

        return inventory;
    }

    @Override
    public boolean canPerformTask() {
        return false;
    }

    @Override
    public Skill trainsSkill() {
        return null;
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
