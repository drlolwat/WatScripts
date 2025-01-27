package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.utilities.Logger;
import org.lolwat.types.gear.GearItem;

import java.util.List;

@Getter
public class ItemManager {
    @Getter @Setter
    private static ItemManager instance;
    @Getter
    List<GearItem> gearItems;

    public ItemManager() {
        instance = this;
    }

    public GearItem getGearItem(String name) {
        for(GearItem item : gearItems) {
            if(item.getName().equalsIgnoreCase(name)) {
                return item;
            } else {
                Logger.log("ItemManager: adding new GearItem " + name + " on the fly");
                gearItems.add(new GearItem(name, name));
                return getGearItem(name);
            }
        }

        return null;
    }
}
