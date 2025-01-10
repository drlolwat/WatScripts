package org.lolwat.types.gear;

import lombok.Getter;
import lombok.Setter;
import org.lolwat.misc.utils.NumUtils;

@Getter
public class GearItem {
    private final String name;
    private final String searchFor;
    private final int quantity;
    @Setter
    private int price;

    public GearItem(String name, String searchFor, int quantity) {
        this.name = name;
        this.searchFor = searchFor;
        this.quantity = quantity;
        this.price = NumUtils.getItemPrice(name);
    }

    public GearItem(String name, int quantity) {
        this.name = name;
        this.searchFor = name;
        this.quantity = quantity;
        this.price = NumUtils.getItemPrice(name);
    }
}
