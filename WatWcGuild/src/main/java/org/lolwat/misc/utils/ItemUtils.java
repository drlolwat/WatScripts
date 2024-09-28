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
import org.lolwat.managers.types.WatTask;
import org.lolwat.tasks.misc.TraversalTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemUtils {
    public static List<String> SINGULAR_ITEMS = Arrays.asList(
            "Hammer",
            "Amulet mould",
            "Bracelet mould",
            "Ring mould",
            "Necklace mould",
            "Camelot teleport",
            "Varrock teleport",
            "Games necklace",
            "Ring of dueling"
    );

    public static final List<BankLocation> allowedLocations = Arrays.asList(BankLocation.GRAND_EXCHANGE, BankLocation.LUMBRIDGE, BankLocation.VARROCK_WEST);

    public static BankLocation getClosestAllowedBank() {
        BankLocation closestBank = null;
        double closestDistance = Double.MAX_VALUE;

        for (BankLocation bankLocation : allowedLocations) {
            double distance = Players.getLocal().distance(bankLocation.getArea(1).getCenter());
            if (distance < closestDistance) {
                closestDistance = distance;
                closestBank = bankLocation;
            }
        }

        return closestBank;
    }

    public static void setBankMode(BankMode mode) {
        if(Bank.getWithdrawMode().equals(mode)) {
            return;
        }

        Bank.setWithdrawMode(mode);
        Sleep.sleep(100, 200);
    }

    public static boolean inventoryContains(int itemId, int itemQty, boolean allowNoted) {
        return Inventory.contains(x -> x != null && (allowNoted || !x.isNoted()) && x.getID() == itemId && x.getAmount() >= itemQty);
    }

    public static boolean equipmentContains(int itemId, int itemQty) {
        return Inventory.contains(x -> x != null && x.getID() == itemId && x.getAmount() >= itemQty);
    }

    public static boolean bankContains(int itemId, int itemQty, boolean allowNoted) {
        return Bank.contains(x -> x != null && (allowNoted || !x.isNoted()) && x.getID() == itemId && x.getAmount() >= itemQty);
    }

    public static boolean inventoryContains(String item, int itemQty, boolean allowNoted) {
        return Inventory.contains(x -> x != null && (allowNoted || !x.isNoted()) && x.getName().contains(item) && Inventory.count(x.getName()) >= itemQty);
    }

    public static int inventoryCount(String item, boolean allowNoted) {
        return Inventory.count(x -> x != null && x.getName().contains(item) && (allowNoted || !x.isNoted()));
    }

    public static boolean bankContains(String item, int itemQty) {
        return Bank.contains(x -> x != null && x.getName().contains(item) && x.getAmount() >= itemQty);
    }

    public static boolean equipmentContains(String item, int itemQty) {
        return Equipment.contains(x -> x != null && x.getName().contains(item) && x.getAmount() >= itemQty);
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
                TaskManager.getInstance().setCurrentTask(new TraversalTask(getClosestAllowedBank().getArea(3), task));
                return;
            } else {
                Logger.log("Banking: Opening bank via " + chest.getName() + " (id: " + chest.getID() + ", distance: " + chest.distance() + ")");
                if(!chest.isOnScreen()) {
                    if(!org.dreambot.api.methods.map.Map.isTileOnMap(chest.getTile()) || chest.distance() > 15.0) {
                        Logger.log("Banking: Walking closer to chest");
                        TaskManager.getInstance().setCurrentTask(new TraversalTask(chest.getTile().getArea(2), task));
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
}
