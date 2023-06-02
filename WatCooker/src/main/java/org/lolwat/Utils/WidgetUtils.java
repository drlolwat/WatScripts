package org.lolwat.Utils;

import org.lolwat.Enums.IngotType;

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
}
