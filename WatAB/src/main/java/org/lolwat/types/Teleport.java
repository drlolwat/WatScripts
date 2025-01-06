package org.lolwat.types;

import lombok.Getter;
import org.dreambot.api.methods.map.Tile;

@Getter
public class Teleport {
    private final String name;
    private final Tile destination;
    private final String searchFor;
    private final String fullyCharged;
    private final String option;

    public Teleport(String name, Tile destination, String searchFor, String fullyCharged, String option) {
        this.name = name;
        this.destination = destination;
        this.searchFor = searchFor;
        this.fullyCharged = fullyCharged;
        this.option = option;
    }
}
