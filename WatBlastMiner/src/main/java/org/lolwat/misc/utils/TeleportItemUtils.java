package org.lolwat.misc.utils;

import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.wrappers.items.Item;

public class TeleportItemUtils {
    public static final String RING_OF_DUELING = "Ring of dueling";
    public static final String LUMBRIDGE_TELEPORT = "Lumbridge teleport";
    public static final String FALADOR_TELEPORT = "Falador teleport";
    public static final String CAMELOT_TELEPORT = "Camelot teleport";

    public static String getTeleportForBank(BankLocation loc) {
        switch(loc) {
            case GNOME_STRONGHOLD:
            case CASTLE_WARS: {
                return RING_OF_DUELING;
            }

            case SEERS:
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
        switch(item) {
            case RING_OF_DUELING: {
                return "Ring of dueling(8)";
            }

            case LUMBRIDGE_TELEPORT: {
                return "Lumbridge teleport";
            }

            case FALADOR_TELEPORT: {
                return "Falador teleport";
            }

            case CAMELOT_TELEPORT: {
                return "Camelot teleport";
            }
        }

        return "";
    }
}
