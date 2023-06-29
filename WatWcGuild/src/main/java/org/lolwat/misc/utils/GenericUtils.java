package org.lolwat.misc.utils;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

public class GenericUtils {
    public static void equipItem(String item) {
        Item i = Inventory.get(item);
        if(i != null) {
            if(i.hasAction("Wear") && i.interact("Wear")) {
                Logger.log("EQUIPMENTCHECKER: EQUIPPED WEARABLE");
                Sleep.sleep(100, 300);
            }
            else if(i.hasAction("Wield") && i.interact("Wield")) {
                Logger.log("EQUIPMENTCHECKER: EQUIPPED WEAPON/AMMO");
                Sleep.sleep(100, 300);
            }
        } else {
            Logger.error("item to equip was null");
        }
    }
}
