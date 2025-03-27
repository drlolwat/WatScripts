package org.lolwat.tasks.combat;

import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.utilities.Logger;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.misc.utils.CombatUtils;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.types.gear.WatItem;
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
    public void execute() {
        if(target == null) {
            target = CombatUtils.getBestMob(skill);
            Logger.log("combat selected " + target.getName() + " for skill " + skill.getName());
            return;
        }

        if(!WatUtils.hasInventory()) {
            Logger.log("need to bank for inventory for " + target.getName());
            return;
        }

        if(!WatUtils.hasRequiredLoadout()) {
            Logger.log("need to bank for loadout for " + target.getName());
            return;
        }

        //go to best location for mob
        if(!target.getBestLocation().contains(Players.getLocal())) {
            Logger.log("running to best location for " + target.getName());
            TaskManager.getInstance().setCurrentTask(new WalkingTask(target.getBestLocation(), this));
            return;
        }

        //handle food, etc
        target.getMobLogic().runPriority();

        //run the targets logic
        target.getMobLogic().execute(target, skill);
    }

    @Override
    public HashMap<WatItem, Integer> loadout() {
        HashMap<WatItem, Integer> loadout = new HashMap<>();

        if(target != null && target.getMobLogic().gearLoadout() != null) {
            loadout.putAll(target.getMobLogic().gearLoadout());
        } else {
            //TODO combat gear based on skill + bank contents(gp+gear owned)
        }

        return loadout;
    }

    @Override
    public HashMap<WatItem, Integer> inventory() {
        HashMap<WatItem, Integer> inventory = new HashMap<>();

        if(target != null && target.getMobLogic().inventoryLoadout() != null) {
            inventory.putAll(target.getMobLogic().inventoryLoadout());
        } else {
            //TODO combat gear based on skill + bank contents(gp+gear owned)
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
