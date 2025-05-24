package org.lolwat.types.mobs.logic;

import org.dreambot.api.methods.skills.Skill;
import org.lolwat.managers.ItemManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.MobLogic;

import java.util.HashMap;

public class MossGiantsLogic implements MobLogic {
    @Override
    public HashMap<WatItem, Integer> inventoryLoadout() {
        return new HashMap<WatItem, Integer>() {
            {
                if(TaskManager.getInstance().getCurrentTask().trainsSkill().equals(Skill.MAGIC)
                        || TaskManager.getInstance().getCurrentTask().trainsSkill().equals(Skill.RANGED)) {

                    put(ItemManager.getInstance().getItem("Knife"), 1);
                }

                put(ItemManager.getInstance().getItem("Lobster"), 20);
            }
        };
    }
}
