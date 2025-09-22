package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.lolwat.types.gear.WatZone;

import java.util.ArrayList;
import java.util.List;

public class ZoneManager {
    @Setter
    @Getter
    private static ZoneManager instance;

    @Getter
    private List<WatZone> zones;

    public ZoneManager() {
        zones = new ArrayList<>();
        createZones();
    }

    public void createZones() {
        zones.add(new WatZone(
                "Varrock South Copper", Skill.MINING, 1, new Area(
                new Tile(3285, 3364, 0),
                new Tile(3285, 3360, 0),
                new Tile(3291, 3360, 0),
                new Tile(3292, 3362, 0),
                new Tile(3290, 3365, 0),
                new Tile(3290, 3371, 0),
                new Tile(3281, 3371, 0),
                new Tile(3281, 3366, 0),
                new Tile(3280, 3364, 0),
                new Tile(3280, 3360, 0),
                new Tile(3285, 3360, 0)), "Copper rocks", false, "Mine")
        );

        zones.add(new WatZone(
                "Varrock South Tin", Skill.MINING, 1, new Area(
                new Tile(3285, 3364, 0),
                new Tile(3285, 3360, 0),
                new Tile(3291, 3360, 0),
                new Tile(3292, 3362, 0),
                new Tile(3290, 3365, 0),
                new Tile(3290, 3371, 0),
                new Tile(3281, 3371, 0),
                new Tile(3281, 3366, 0),
                new Tile(3280, 3364, 0),
                new Tile(3280, 3360, 0),
                new Tile(3285, 3360, 0)), "Tin rocks", false, "Mine")
        );

        zones.add(new WatZone(
                "Varrock South Iron", Skill.MINING, 15, new Area(
                new Tile(3285, 3364, 0),
                new Tile(3285, 3360, 0),
                new Tile(3291, 3360, 0),
                new Tile(3292, 3362, 0),
                new Tile(3290, 3365, 0),
                new Tile(3290, 3371, 0),
                new Tile(3281, 3371, 0),
                new Tile(3281, 3366, 0),
                new Tile(3280, 3364, 0),
                new Tile(3280, 3360, 0),
                new Tile(3285, 3360, 0)), "Iron rocks", false, "Mine")
        );

        zones.add(new WatZone(
                "Rimmington Tin", Skill.MINING, 1,
                new Area(
                        new Tile(2977, 3240, 0),
                        new Tile(2977, 3230, 0),
                        new Tile(2967, 3237, 0),
                        new Tile(2967, 3243, 0),
                        new Tile(2973, 3248, 0),
                        new Tile(2980, 3249, 0),
                        new Tile(2987, 3244, 0),
                        new Tile(2990, 3240, 0),
                        new Tile(2988, 3233, 0),
                        new Tile(2981, 3229, 0),
                        new Tile(2977, 3229, 0)), "Tin rocks", false, "Mine")
        );

        zones.add(new WatZone(
                "Rimmington Copper", Skill.MINING, 1,
                new Area(
                        new Tile(2977, 3240, 0),
                        new Tile(2977, 3230, 0),
                        new Tile(2967, 3237, 0),
                        new Tile(2967, 3243, 0),
                        new Tile(2973, 3248, 0),
                        new Tile(2980, 3249, 0),
                        new Tile(2987, 3244, 0),
                        new Tile(2990, 3240, 0),
                        new Tile(2988, 3233, 0),
                        new Tile(2981, 3229, 0),
                        new Tile(2977, 3229, 0)), "Copper rocks", false, "Mine")
        );

        zones.add(new WatZone(
                "Rimmington Iron", Skill.MINING, 1,
                new Area(
                        new Tile(2977, 3240, 0),
                        new Tile(2977, 3230, 0),
                        new Tile(2967, 3237, 0),
                        new Tile(2967, 3243, 0),
                        new Tile(2973, 3248, 0),
                        new Tile(2980, 3249, 0),
                        new Tile(2987, 3244, 0),
                        new Tile(2990, 3240, 0),
                        new Tile(2988, 3233, 0),
                        new Tile(2981, 3229, 0),
                        new Tile(2977, 3229, 0)), "Iron rocks", false, "Mine")
        );
    }

    public WatZone getBestZone(Skill s) {
        WatZone bestLocation = null;
        double closestDistance = Double.MAX_VALUE;

        for(WatZone z : zones) {
            if(z.getSkill().equals(s) && Skills.getRealLevel(s) >= z.getMinLevel()) {
                double distance = z.getSearchArea().distance(Players.getLocal().getTile());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    bestLocation = z;
                }
            }
        }

        return bestLocation;
    }
}
