package org.lolwat.misc.utils;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.items.Item;

public class GenericUtils {
    public static boolean equipItem(String item, Item old) {
        Item i = Inventory.get(x -> x != null && x.getName().contains((item)));
        if (i != null) {
            if (i.hasAction("Wear") && i.interact("Wear")) {
                Logger.log("Equipment: Equipped wearable");
                Sleep.sleep(100, 200);

                if (Bank.isOpen() && old != null) {
                    Sleep.sleep(200, 400);
                    Bank.depositAll(old.getName());
                    Logger.log("Equipment: Deposited old item: " + old.getName());
                }

                return true;
            } else if (i.hasAction("Wield") && i.interact("Wield")) {
                Logger.log("Equipment: Equipped wieldable");
                Sleep.sleep(100, 200);

                if (Bank.isOpen() && old != null) {
                    Sleep.sleep(200, 400);
                    Bank.depositAll(old.getName());
                    Logger.log("Equipment: Deposited old item: " + old.getName());
                }

                return true;
            } else if (i.hasAction("Equip") && i.interact("Equip")) {
                Logger.log("Equipment: Equipped equippable");
                Sleep.sleep(100, 200);

                if (Bank.isOpen() && old != null) {
                    Sleep.sleep(200, 400);
                    Bank.depositAll(old.getName());
                    Logger.log("Equipment: Deposited old item: " + old.getName());
                }

                return true;
            }
        } else {
            Logger.error("item to equip was null");
        }

        return false;
    }

    public static boolean isMember() {
        return PlayerSettings.getConfig(1780) >= 2;
    }

    public static int getMemberDays() {
        return PlayerSettings.getConfig(1780);
    }
}
