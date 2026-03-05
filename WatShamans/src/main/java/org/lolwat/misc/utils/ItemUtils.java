package org.lolwat.misc.utils;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.bank.BankMode;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Sleep;

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
        if (Bank.getWithdrawMode().equals(mode)) {
            return;
        }

        Bank.setWithdrawMode(mode);
        Sleep.sleep(100, 200);
    }

    public static boolean inventoryContains(String item, int itemQty, boolean allowNoted) {
        int qty = itemQty > 0 ? itemQty : -itemQty;
        return Inventory.count(x -> x != null && (allowNoted || !x.isNoted()) && x.getName().contains(item)) >= qty;
    }

    public static int inventoryCount(String item, boolean allowNoted) {
        return Inventory.count(x -> x != null && x.getName().contains(item) && (allowNoted || !x.isNoted()));
    }

    public static boolean bankContains(String item, int itemQty) {
        int qty = itemQty > 0 ? itemQty : -itemQty;
        return Bank.contains(x -> x != null && x.getName().contains(item) && x.getAmount() >= qty && !x.getName().contains("(1)"));
    }

    public static boolean equipmentContains(String item, int itemQty) {
        int qty = itemQty > 0 ? itemQty : -itemQty;
        return Equipment.contains(x -> x != null && x.getName().contains(item) && x.getAmount() >= qty);
    }
}
