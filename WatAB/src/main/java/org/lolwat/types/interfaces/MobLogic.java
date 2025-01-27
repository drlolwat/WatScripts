package org.lolwat.types.interfaces;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.types.gear.GearItem;
import org.lolwat.types.mobs.Mob;

import java.util.HashMap;
import java.util.List;

public interface MobLogic {
    void execute(Mob mob, Skill skill);

    default List<GearItem> inventoryLoadout() {
        return null;
    }

    default HashMap<EquipmentSlot, GearItem> gearLoadout() {
        return null;
    }

    default void runPriority() {
        Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
        if(i != null) {
            if(!i.interact("Eat")) {
                Logger.log("Issue eating food during combat task [default interface method]");
            }
        }
    }
}
