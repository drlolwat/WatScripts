package org.lolwat.types.gear;

import lombok.Getter;

@Getter
public class GearItem {
    private final String name;
    private final String searchFor;
    private final int quantity;

    public GearItem(String name, String searchFor, int quantity) {
        this.name = name;
        this.searchFor = searchFor;
        this.quantity = quantity;
    }

    public GearItem(String name, int quantity) {
        this.name = name;
        this.searchFor = name;
        this.quantity = quantity;
    }
}
