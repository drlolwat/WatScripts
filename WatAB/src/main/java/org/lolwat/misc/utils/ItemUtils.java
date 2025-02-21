package org.lolwat.misc.utils;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.misc.WalkingTask;
import org.lolwat.types.gear.WatItem;
import org.lolwat.types.interfaces.WatTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemUtils {
    public static void setBankMode(BankMode mode) {
        if(Bank.getWithdrawMode().equals(mode)) {
            return;
        }

        Bank.setWithdrawMode(mode);
        Sleep.sleep(100, 200);
    }

    public static int equipmentCount(String name) {
        return Equipment.count(x -> x != null && x.getName().contains(name));
    }

    public static int inventoryCount(String name) {
        return Inventory.count(x -> x != null && x.getName().contains(name));
    }

    public static boolean inventoryContains(int itemId, int itemQty, boolean allowNoted) {
        return Inventory.contains(x -> x != null && (allowNoted || !x.isNoted()) && x.getID() == itemId && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static boolean inventoryContains(String itemId, int itemQty, boolean allowNoted) {
        return Inventory.contains(x -> x != null && (allowNoted || !x.isNoted()) && x.getName().equals(itemId) && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static boolean equipmentContains(int itemId, int itemQty) {
        return Inventory.contains(x -> x != null && x.getID() == itemId && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static boolean equipmentSlotContains(String name, int itemQty) {
        return Inventory.contains(x -> x != null && x.getName().equals(name) && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static boolean bankContains(int itemId, int itemQty, boolean allowNoted) {
        return Bank.contains(x -> x != null && (allowNoted || !x.isNoted()) && x.getID() == itemId && x.getAmount() >= itemQty && !x.getName().contains("(1)"));
    }

    public static void bank(WatTask task) {
        List<String> allowedObjects = new ArrayList<String>() {
            {
                add("Bank booth");
                add("Open chest");
                add("Bank chest");
            }
        };

        NPC banker = NPCs.closest("Banker");
        if (banker == null || !banker.exists()) {
            Logger.log("Banking: Looking for bank chest");
            GameObject chest = GameObjects.closest(x -> x != null
                    && allowedObjects.contains(x.getName()) && x.distance() <= 15.00
                    && !x.hasAction("Shut") && !x.hasAction("Search") && !x.hasAction("Chop down")
                    && x.canReach());

            if (chest == null) {
                Logger.log("Banking: No banker or chest found (15r), walking to nearest bank");
                TaskManager.getInstance().setCurrentTask(new WalkingTask(BankLocation.getNearest(Players.getLocal().getTile(), false).getArea(3), task));
                return;
            } else {
                Logger.log("Banking: Opening bank via " + chest.getName() + " (id: " + chest.getID() + ", distance: " + chest.distance() + ")");
                if(!chest.isOnScreen()) {
                    if(!org.dreambot.api.methods.map.Map.isTileOnMap(chest.getTile()) || chest.distance() > 15.0) {
                        Logger.log("Banking: Walking closer to chest");
                        TaskManager.getInstance().setCurrentTask(new WalkingTask(chest.getTile().getArea(2), task));
                        return;
                    }

                    Camera.rotateToTile(chest.getTile());
                    Sleep.sleepUntil(chest::isOnScreen, Calculations.random(5000, 10000));
                }

                if (!chest.interact()) {
                    Logger.error("Banking: Failed to interact with chest");
                    return;
                }
            }
        } else {
            Logger.log("Banking: Opening bank via Banker (distance: " + banker.distance() + ")");
            if (!Bank.open()) {
                return;
            }
        }

        Logger.log("Banking: Waiting for bank to be open..");
        Sleep.sleepUntil(Bank::isOpen, 15000);
    }

    public static boolean hasInventory() {
        if(TaskManager.getInstance().getCurrentTask() != null) {
            for(Map.Entry<WatItem, Integer> map  : TaskManager.getInstance().getCurrentTask().inventory().entrySet()) {
                if(!ItemUtils.inventoryContains(map.getKey().getName(), map.getValue(), false)) {
                    Logger.log("[INVENTORY] " + TaskManager.getInstance().getCurrentTask().getName() + ": Missing " + map.getKey().getName() + " x" + map.getValue());
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean hasRequiredLoadout() {
        if(TaskManager.getInstance().getCurrentTask() != null) {
            for(Map.Entry<WatItem, Integer> map  : TaskManager.getInstance().getCurrentTask().loadout().entrySet()) {
                if(!ItemUtils.inventoryContains(map.getKey().getName(), map.getValue(), false)) {
                    Logger.log("[LOADOUT] " + TaskManager.getInstance().getCurrentTask().getName() + ": Missing " + map.getKey().getName() + " x" + map.getValue());
                    return false;
                }
            }
        }

        return true;
    }
}
