package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.map.Tile;
import org.lolwat.types.teleports.Teleport;

import java.util.ArrayList;
import java.util.List;

public class TeleportManager {
    @Setter
    @Getter
    private static TeleportManager instance;
    private List<Teleport> teleports;
    public TeleportManager() {
        setupTeleports();
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

        teleports.add(new Teleport("Castle Wars Arena",
                new Tile(2439,3089,0),
                "Ring of dueling",
                "Ring of dueling(8)",
                "Castle Wars"));

        teleports.add(new Teleport("PVP Arena",
                new Tile(3313,3236,0),
                "Ring of dueling",
                "Ring of dueling(8)",
                "Emir's Arena"));

        teleports.add(new Teleport("Ferox Enclave",
                new Tile(3150,3633,0),
                "Ring of dueling",
                "Ring of dueling(8)",
                "Ferox Enclave"));

        teleports.add(new Teleport("Woodcutting Guild",
                new Tile(1659, 3505, 0),
                "Skills necklace",
                "Skills necklace(6)",
                "Woodcutting Guild"));

        teleports.add(new Teleport("Cooking Guild",
                new Tile(3142, 3442, 0),
                "Skills necklace",
                "Skills necklace(6)",
                "Cooking Guild"));
    }
}
