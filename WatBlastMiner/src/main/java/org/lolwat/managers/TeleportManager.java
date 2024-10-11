package org.lolwat.managers;

import org.dreambot.api.methods.map.Tile;
import org.lolwat.managers.types.Teleport;

import java.util.ArrayList;
import java.util.List;

public class TeleportManager {
    private static TeleportManager instance;
    private List<Teleport> teleports;

    public TeleportManager() {
        setupTeleports();
    }

    public static void setInstance(TeleportManager inst) {
        instance = inst;
    }

    public static TeleportManager getInstance() {
        return instance;
    }

    public boolean isTeleportItem(String item) {
        for (Teleport t : teleports) {
            if (t.getSearchFor().toLowerCase().contains(item.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public String getChargedItemName(String item) {
        for (Teleport t : teleports) {
            if (t.getSearchFor().toLowerCase().contains(item.toLowerCase())) {
                return t.getFullyCharged();
            }
        }

        return null;
    }

    public Teleport getBestOption(Tile target) {
        Teleport closestTeleport = null;
        double minDistance = Double.MAX_VALUE;

        for (Teleport teleport : teleports) {
            double distance = teleport.getDestination().distance(target);
            if (distance < minDistance) {
                minDistance = distance;
                closestTeleport = teleport;
            }
        }

        return closestTeleport;
    }

    public void setupTeleports() {
        teleports = new ArrayList<>();
        teleports.add(new Teleport("Farming Guild",
                new Tile(1249, 3718, 0),
                "Skills necklace(",
                "Skills necklace(6)",
                "Farming Guild"));

        teleports.add(new Teleport("Grand Exchange",
                new Tile(3164, 3485, 0),
                "Ring of wealth (",
                "Ring of wealth (5)",
                "Grand Exchange"));
    }
}
