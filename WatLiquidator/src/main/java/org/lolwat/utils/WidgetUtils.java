package org.lolwat.utils;

import org.lolwat.utils.types.CraftingType;
import org.lolwat.utils.types.IngotType;

public class WidgetUtils {
    public static int getIngotWidgetId(IngotType type) {
        switch(type) {
            default: return 14;
            case IRON: return 15;
            case SILVER: return 16;
            case STEEL: return 17;
            case GOLD: return 18;
            case MITHRIL: return 19;
            case ADAMANT: return 20;
            case RUNE: return 21;
        }
    }

    public static int getSpinnerWidgetId(CraftingType type) {
        switch(type) {
            default: return 0;
            case WOOL: return 14;
            case GOLDWOOL: return 15;
            case BOWSTRING: return 16;
            case ROPE: return 17;
            case CROSSBOWSTRING_S: return 18;
            case CROSSBOWSTRING_TR: return 19;
            case MAGICSTRING: return 20;
        }
    }
}
