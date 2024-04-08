package org.lolwat.misc.utils.runecrafting;

import org.dreambot.api.methods.magic.cost.Rune;
import org.dreambot.api.methods.map.Area;

public class RunecraftingUtils {
    public static Area getRunecraftingAreaByRune(String rune) {
        switch(rune) {
            case "air":
            default: {
                return new Area(2982, 3296, 2990, 3288);
            }
            case "water": {
                return new Area(3179, 3168, 3186, 3161);
            }
            case "earth": {
                return new Area(3301, 3476, 3309, 3471);
            }
            case "fire": {
                return new Area(3307, 3257, 3315, 3249);
            }
            case "body": {
                return new Area(3049, 3448, 3056, 3441);
            }
            case "mind": {
                return new Area(2979, 3517, 2986, 3510);
            }
            case "chaos": {
                return new Area(3055, 3595, 3066, 3586);
            }
        }
    }
}
