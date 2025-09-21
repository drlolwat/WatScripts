package org.lolwat.types.mobs.logic;

import org.lolwat.managers.ItemManager;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.MobLogic;

import java.util.HashMap;

public class DefaultLogic implements MobLogic {
    @Override
    public HashMap<WatItem, Integer> inventoryLoadout() {
        return new HashMap<WatItem, Integer>() {
            {
                put(ItemManager.getInstance().getItem("Tuna"), 16);
            }
        };
    }
}
