package org.lolwat.misc.utils.firemaking;

import org.lolwat.misc.types.mixed.TreeType;
import org.lolwat.misc.utils.woodcutting.WoodcuttingUtils;

import java.util.HashMap;

public class FiremakingUtils {
    public static HashMap<String, Integer> getMaterialsForFiremaking(TreeType type, boolean fullInventory, int inventoryLoads) {
        HashMap<String, Integer> ret = new HashMap<>();
        ret.put("Tinderbox", 1);
        ret.put(WoodcuttingUtils.getLogName(type), fullInventory ? 27 * inventoryLoads : 1);
        return ret;
    }
}
