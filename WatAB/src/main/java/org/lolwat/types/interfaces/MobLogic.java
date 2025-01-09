package org.lolwat.types.interfaces;

import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.types.gear.GearItem;
import org.lolwat.types.mobs.Mob;

import java.util.HashMap;

public interface MobLogic {
    void execute(Mob mob, Skill skill);

    default HashMap<String, Integer> inventoryLoadout() {
        return null;
    }

    default HashMap<EquipmentSlot, GearItem> gearLoadout() {
        return null;
    }
}
