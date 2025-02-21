package org.lolwat.types.interfaces;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.mobs.Mob;

import java.util.HashMap;

public interface MobLogic {
    void execute(Mob mob, Skill skill);

    default HashMap<WatItem, Integer> inventoryLoadout() {
        return null;
    }

    default HashMap<WatItem, Integer> gearLoadout() {
        return null;
    }

    default void runPriority() {
        Item i = Inventory.get(x -> x != null && x.hasAction("Eat"));
        if(i != null && Combat.getHealthPercent() <= 50) {
            if(!i.interact("Eat")) {
                Logger.log("Issue eating food during combat task [default interface method]");
            }
        }
    }
}
