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
                new Tile(3285, 3360, 0)), "Copper rocks", false, "Mine", true, false)
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
                new Tile(3285, 3360, 0)), "Tin rocks", false, "Mine", true, false)
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
                new Tile(3285, 3360, 0)), "Iron rocks", false, "Mine", true, false)
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
                        new Tile(2977, 3229, 0)), "Tin rocks", false, "Mine", true, false)
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
                        new Tile(2977, 3229, 0)), "Copper rocks", false, "Mine", true, false)
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
                        new Tile(2977, 3229, 0)), "Iron rocks", false, "Mine", true, false)
        );

        zones.add(new WatZone(
                "Seers Trees W", Skill.WOODCUTTING, 1,
                new Area(
                        new Tile(2671, 3476, 0),
                        new Tile(2671, 3464, 0),
                        new Tile(2680, 3458, 0),
                        new Tile(2687, 3464, 0),
                        new Tile(2687, 3469, 0),
                        new Tile(2692, 3470, 0),
                        new Tile(2698, 3473, 0),
                        new Tile(2698, 3486, 0),
                        new Tile(2680, 3488, 0)), "Tree", false, "Chop down", true, false)
        );

        zones.add(new WatZone(
                "Seers Trees S", Skill.WOODCUTTING, 1,
                new Area(
                        new Tile(2720, 3443, 0),
                        new Tile(2720, 3432, 0),
                        new Tile(2711, 3438, 0),
                        new Tile(2708, 3448, 0),
                        new Tile(2718, 3454, 0),
                        new Tile(2731, 3449, 0),
                        new Tile(2728, 3434, 0),
                        new Tile(2720, 3432, 0)), "Tree", false, "Chop down", true, false)
        );

        zones.add(new WatZone(
                "Seers OakS 1", Skill.WOODCUTTING, 15,
                new Area(
                        new Tile(2727, 3478, 0),
                        new Tile(2726, 3473, 0),
                        new Tile(2719, 3473, 0),
                        new Tile(2717, 3483, 0),
                        new Tile(2725, 3484, 0),
                        new Tile(2733, 3483, 0),
                        new Tile(2738, 3480, 0),
                        new Tile(2735, 3476, 0)), "Oak tree", false, "Chop down", true, false)
        );

        zones.add(new WatZone(
                "Seers OakS 2", Skill.WOODCUTTING, 15,
                new Area(
                        new Tile(2722, 3445, 0),
                        new Tile(2716, 3449, 0),
                        new Tile(2716, 3455, 0),
                        new Tile(2728, 3455, 0),
                        new Tile(2731, 3445, 0)), "Oak tree", false, "Chop down", true, false)
        );

        zones.add(new WatZone(
                "Seers MapleN 1", Skill.WOODCUTTING, 45,
                new Area(
                        new Tile(2726, 3503, 0),
                        new Tile(2726, 3498, 0),
                        new Tile(2733, 3498, 0),
                        new Tile(2734, 3498, 0),
                        new Tile(2734, 3503, 0),
                        new Tile(2723, 3506, 0),
                        new Tile(2719, 3499, 0),
                        new Tile(2724, 3499, 0),
                        new Tile(2726, 3499, 0)), "Maple tree", false, "Chop down", true, false)
        );

        zones.add(new WatZone("LumbridgeC", Skill.THIEVING, 1,
                new Area(
                        new Tile(3226, 3219, 0),
                        new Tile(3226, 3210, 0),
                        new Tile(3220, 3204, 0),
                        new Tile(3216, 3204, 0),
                        new Tile(3217, 3211, 0),
                        new Tile(3217, 3227, 0),
                        new Tile(3226, 3229, 0)), "Man", true, "Pickpocket", false, true));
    }

    public WatZone getBestZone(Skill s) {
        WatZone bestLocation = null;
        int highestLevel = -1;
        double closestDistance = Double.MAX_VALUE;

        for (WatZone z : zones) {
            if (z.getSkill().equals(s) && Skills.getRealLevel(s) >= z.getMinLevel()) {
                int level = z.getMinLevel();
                double distance = z.getSearchArea().distance(Players.getLocal().getTile());

                if (level > highestLevel || (level == highestLevel && distance < closestDistance)) {
                    highestLevel = level;
                    closestDistance = distance;
                    bestLocation = z;
                }
            }
        }

        return bestLocation;
    }
}
