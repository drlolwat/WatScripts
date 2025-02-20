package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.utilities.Logger;
import org.lolwat.types.gear.WatItem;

import java.util.List;

@Getter
public class ItemManager {
    @Getter @Setter
    private static ItemManager instance;
    @Getter
    List<WatItem> items;

    public ItemManager() {
        instance = this;
    }

    public WatItem getItem(String name) {
        for(WatItem item : items) {
            if(item.getName().equalsIgnoreCase(name)) {
                return item;
            } else {
                Logger.log("ItemManager: adding new GearItem " + name + " on the fly");
                items.add(new WatItem(name, name));
                return getItem(name);
            }
        }

        return null;
    }
}
