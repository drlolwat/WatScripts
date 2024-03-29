package org.lolwat.managers.types;

import org.dreambot.api.methods.map.Tile;

import java.util.List;

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

    public String getName() {
        return name;
    }

    public Tile getDestination() {
        return destination;
    }

    public String getSearchFor() {
        return searchFor;
    }

    public String getFullyCharged() {
        return fullyCharged;
    }

    public String getOption() {
        return option;
    }
}
