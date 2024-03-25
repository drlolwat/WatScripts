package org.lolwat.misc.utils;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.wrappers.items.Item;

public class TeleportItemUtils {
    public static String RING_OF_DUELING = "Ring of dueling";
    public static String LUMBRIDGE_TELEPORT = "Lumbridge teleport";
    public static String FALADOR_TELEPORT = "Falador teleport";
    public static String CAMELOT_TELEPORT = "Camelot teleport";

    public static String getTeleportForBank(BankLocation loc) {
        switch(loc) {
            case GNOME_STRONGHOLD:
            case CASTLE_WARS: {
                return RING_OF_DUELING;
            }

            case CATHERBY: {
                return CAMELOT_TELEPORT;
            }

            case DRAYNOR: {
                return LUMBRIDGE_TELEPORT;
            }

            case FALADOR_EAST:
            case FALADOR_WEST: {
                return FALADOR_TELEPORT;
            }
        }
        return "";
    }

    public static String getDialogueOption(String item, boolean equipped) {
        if (item.equals(RING_OF_DUELING)) {
            return equipped ? "Castle Wars" : "Castle Wars Arena.";
        }

        return "";
    }

    public static String getChargedItemName(String item) {
        if (item.equals(RING_OF_DUELING)) {
            return "Ring of dueling(8)";
        }

        return "";
    }
}
