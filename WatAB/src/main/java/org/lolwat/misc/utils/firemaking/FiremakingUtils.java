package org.lolwat.misc.utils.firemaking;

import org.lolwat.managers.ItemManager;
import org.lolwat.misc.types.mixed.TreeType;
import org.lolwat.misc.utils.woodcutting.WoodcuttingUtils;
import org.lolwat.types.gear.WatItem;

import java.util.HashMap;

public class FiremakingUtils {
    public static HashMap<WatItem, Integer> getMaterialsForFiremaking(TreeType type, boolean fullInventory, int inventoryLoads) {
        HashMap<WatItem, Integer> ret = new HashMap<>();
        ret.put(ItemManager.getInstance().getItem("Tinderbox"), 1);
        ret.put(ItemManager.getInstance().getItem(WoodcuttingUtils.getLogName(type)), fullInventory ? 27 * inventoryLoads : 1);
        return ret;
    }
}
