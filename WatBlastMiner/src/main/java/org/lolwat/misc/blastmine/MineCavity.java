package org.lolwat.misc.blastmine;

import org.dreambot.api.methods.map.Tile;

public class MineCavity {
    private final Tile adjacentTile;
    private final Tile objTile;
    private final String action;

    public MineCavity(Tile adjacentTile, Tile objTile, String action) {
        this.adjacentTile = adjacentTile;
        this.objTile = objTile;
        this.action = action;
    }

    public MineCavity(String action) {
        this(null, null, action);
    }

    public Tile getAdjacentTile() {
        return adjacentTile;
    }

    public Tile getObjTile() {
        return objTile;
    }

    public String getAction() {
        return action;
    }
}
