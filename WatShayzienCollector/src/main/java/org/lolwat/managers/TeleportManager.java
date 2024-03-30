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
        for(Teleport t : teleports) {
            if(t.getSearchFor().toLowerCase().contains(item.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    public String getChargedItemName(String item) {
        for(Teleport t : teleports) {
            if(t.getSearchFor().toLowerCase().contains(item.toLowerCase())) {
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

        teleports.add(new Teleport("Castle Wars Arena",
                new Tile(2439,3089,0),
                "Ring of dueling",
                "Ring of dueling(6)",
                "Castle Wars"));

        teleports.add(new Teleport("PVP Arena",
                new Tile(3313,3236,0),
                "Ring of dueling",
                "Ring of dueling(6)",
                "PVP"));

        teleports.add(new Teleport("Ferox Enclave",
                new Tile(3150,3633,0),
                "Ring of dueling",
                "Ring of dueling(6)",
                "Ferox Enclave"));

        teleports.add(new Teleport("Burthorpe",
                new Tile(2898,3552,0),
                "Games necklace",
                "Games necklace(8)",
                "Burthorpe"));

        teleports.add(new Teleport("Barbarian Outpost",
                new Tile(2520,3569,0),
                "Games necklace",
                "Games necklace(8)",
                "Barbarian"));

        teleports.add(new Teleport("Wintertotd",
                new Tile(1623, 3937, 0),
                "Games necklace",
                "Games necklace(8)",
                "Wintertotd"));

        teleports.add(new Teleport("Lumbridge",
                new Tile(3220, 3218, 0),
                "Lumbridge teleport",
                "Lumbridge teleport",
                "Lumbridge"));

        teleports.add(new Teleport("Varrock",
                new Tile(3212, 3425, 0),
                "Varrock teleport",
                "Varrock teleport",
                "Varrock"));

        teleports.add(new Teleport("Falador",
                new Tile(2966, 3378, 0),
                "Falador teleport",
                "Falador teleport",
                "Falador"));

        teleports.add(new Teleport("Camelot",
                new Tile(2756, 3478, 0),
                "Camelot teleport",
                "Camelot teleport",
                "Camelot"));

        teleports.add(new Teleport("Taverley",
                new Tile(2894, 3465, 0),
                "Taverley teleport",
                "Taverley teleport",
                "Taverley"));
    }
}
