package org.lolwat.misc.utils;

import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

import java.util.*;

public class GenericUtils {
    public static void equipItem(String item) {
        Item i = Inventory.get(item);
        if(i != null) {
            if(i.hasAction("Wear") && i.interact("Wear")) {
                Logger.log("EQUIPMENTCHECKER: EQUIPPED WEARABLE");
                Sleep.sleep(100, 200);
            }
            else if(i.hasAction("Wield") && i.interact("Wield")) {
                Logger.log("EQUIPMENTCHECKER: EQUIPPED WEAPON/AMMO");
                Sleep.sleep(100, 200);
            }
        } else {
            Logger.error("item to equip was null");
        }
    }

    public static <K, V> void shuffleHashMap(HashMap<K, V> hashMap) {
        // Convert HashMap entries to a List
        List<Map.Entry<K, V>> entryList = new ArrayList<>(hashMap.entrySet());

        // Shuffle the list
        Collections.shuffle(entryList);

        // Clear the original HashMap
        hashMap.clear();

        // Add the shuffled entries back to the original HashMap
        for (Map.Entry<K, V> entry : entryList) {
            hashMap.put(entry.getKey(), entry.getValue());
        }
    }

    public static void moveMouseInOrOut() {
        if(Calculations.random(4) == 1) {
            Mouse.moveOutsideScreen();
        } else {
            Mouse.move();
        }
    }
}
