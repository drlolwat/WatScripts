package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.types.interfaces.MobLogic;
import org.lolwat.types.mobs.Mob;
import org.lolwat.types.mobs.logic.CrabLogic;
import org.lolwat.types.mobs.logic.DefaultLogic;
import org.lolwat.types.mobs.logic.MossGiantsLogic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class MobManager {
    @Getter @Setter
    private static MobManager instance;
    @Getter
    private List<Mob> mobs;

    public MobManager() {
        mobs = new ArrayList<>();
    }

    public void createMobs() {
        addBasicMob("Cow",
                new HashMap<String, Area>() {
                    {
                        put("LumbridgeN", new Area(
                                new Tile(3203, 3292, 0),
                                new Tile(3202, 3283, 0),
                                new Tile(3200, 3283, 0),
                                new Tile(3199, 3282, 0),
                                new Tile(3196, 3282, 0),
                                new Tile(3195, 3283, 0),
                                new Tile(3195, 3284, 0),
                                new Tile(3193, 3286, 0),
                                new Tile(3193, 3300, 0),
                                new Tile(3193, 3301, 0),
                                new Tile(3195, 3303, 0),
                                new Tile(3200, 3303, 0),
                                new Tile(3201, 3302, 0),
                                new Tile(3205, 3302, 0),
                                new Tile(3206, 3303, 0),
                                new Tile(3210, 3303, 0),
                                new Tile(3211, 3302, 0),
                                new Tile(3211, 3297, 0),
                                new Tile(3212, 3295, 0),
                                new Tile(3214, 3293, 0),
                                new Tile(3214, 3290, 0),
                                new Tile(3213, 3288, 0),
                                new Tile(3213, 3284, 0),
                                new Tile(3202, 3283, 0)));

                        put("Falador", new Area(
                                new Tile(3043, 3306, 0),
                                new Tile(3043, 3311, 0),
                                new Tile(3041, 3313, 0),
                                new Tile(3030, 3313, 0),
                                new Tile(3026, 3307, 0),
                                new Tile(3021, 3307, 0),
                                new Tile(3021, 3297, 0),
                                new Tile(3031, 3298, 0),
                                new Tile(3038, 3298, 0)));

                        put("CraftingG", new Area(
                                new Tile(2915, 3291, 0),
                                new Tile(2916, 3287, 0),
                                new Tile(2925, 3278, 0),
                                new Tile(2928, 3280, 0),
                                new Tile(2928, 3284, 0),
                                new Tile(2926, 3291, 0)));
                    }
                },

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 1);
                        put(Skill.DEFENCE, 1);
                        put(Skill.STRENGTH, 1);
                        put(Skill.RANGED, 1);
                        put(Skill.MAGIC, 1);
                    }
                });

        addLogicMob("Rock crab",
                new HashMap<String, Area>() {
                    {
                        put("Fremmy", new Area(
                                new Tile(2707, 3721, 0),
                                new Tile(2705, 3730, 0),
                                new Tile(2703, 3730, 0),
                                new Tile(2702, 3732, 0),
                                new Tile(2700, 3732, 0),
                                new Tile(2695, 3727, 0),
                                new Tile(2692, 3726, 0),
                                new Tile(2692, 3718, 0),
                                new Tile(2694, 3713, 0),
                                new Tile(2705, 3713, 0),
                                new Tile(2715, 3713, 0),
                                new Tile(2720, 3714, 0),
                                new Tile(2720, 3729, 0),
                                new Tile(2720, 3732, 0),
                                new Tile(2718, 3734, 0),
                                new Tile(2715, 3733, 0),
                                new Tile(2714, 3734, 0),
                                new Tile(2712, 3733, 0),
                                new Tile(2708, 3729, 0),
                                new Tile(2705, 3730, 0)));
                    }
                },

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 10);
                        put(Skill.DEFENCE, 10);
                        put(Skill.STRENGTH, 10);
                        put(Skill.RANGED, 10);
                        put(Skill.MAGIC, 10);
                    }
                }, new CrabLogic());

        addBasicMob("Barbarian",
                new HashMap<String, Area>() {
                    {
                        put("BarbVillage", new Area(
                                new Tile(3080, 3421, 0),
                                new Tile(3087, 3421, 0),
                                new Tile(3087, 3431, 0),
                                new Tile(3082, 3434, 0),
                                new Tile(3078, 3427, 0),
                                new Tile(3073, 3426, 0),
                                new Tile(3073, 3415, 0),
                                new Tile(3077, 3409, 0),
                                new Tile(3081, 3406, 0),
                                new Tile(3087, 3406, 0),
                                new Tile(3090, 3408, 0),
                                new Tile(3089, 3413, 0),
                                new Tile(3087, 3416, 0),
                                new Tile(3090, 3419, 0),
                                new Tile(3087, 3421, 0)));
                    }
                },

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 20);
                        put(Skill.DEFENCE, 20);
                        put(Skill.STRENGTH, 20);
                        put(Skill.RANGED, 30);
                        put(Skill.MAGIC, 30);
                    }
                });

        addBasicMob("Minotaur",
                new HashMap<String, Area>() {
                    {
                        put("Stronghold", new Area(
                                new Tile(1884, 5188, 0),
                                new Tile(1887, 5185, 0),
                                new Tile(1890, 5185, 0),
                                new Tile(1893, 5188, 0),
                                new Tile(1904, 5188, 0),
                                new Tile(1907, 5191, 0),
                                new Tile(1906, 5194, 0),
                                new Tile(1903, 5196, 0),
                                new Tile(1902, 5199, 0),
                                new Tile(1897, 5199, 0),
                                new Tile(1895, 5200, 0),
                                new Tile(1892, 5200, 0),
                                new Tile(1884, 5194, 0)));
                    }
                },

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 60);
                        put(Skill.DEFENCE, 60);
                        put(Skill.STRENGTH, 60);
                        put(Skill.RANGED, 60);
                        put(Skill.MAGIC, 60);
                    }
                });

        addBasicMob("Giant spider",
                new HashMap<String, Area>() {
                    {
                        put("Stronghold", new Area(
                                new Tile(2119, 5271, 0),
                                new Tile(2118, 5279, 0),
                                new Tile(2125, 5277, 0),
                                new Tile(2127, 5275, 0),
                                new Tile(2134, 5275, 0),
                                new Tile(2133, 5271, 0),
                                new Tile(2128, 5265, 0)));
                    }
                },

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 45);
                        put(Skill.DEFENCE, 45);
                        put(Skill.STRENGTH, 45);
                        put(Skill.RANGED, 45);
                        put(Skill.MAGIC, 45);
                    }
                });

        addLogicMob("Moss giant",
                new HashMap<String, Area>() {
                    {
                        put("VarrSewer", new Area(
                                new Tile(3159, 9902, 0),
                                new Tile(3159, 9898, 0),
                                new Tile(3156, 9901, 0),
                                new Tile(3155, 9902, 0),
                                new Tile(3153, 9903, 0),
                                new Tile(3153, 9907, 0),
                                new Tile(3155, 9909, 0),
                                new Tile(3158, 9908, 0),
                                new Tile(3159, 9907, 0),
                                new Tile(3160, 9906, 0),
                                new Tile(3165, 9906, 0),
                                new Tile(3165, 9904, 0),
                                new Tile(3164, 9902, 0),
                                new Tile(3162, 9901, 0),
                                new Tile(3159, 9898, 0)));
                    }
                },

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 50);
                        put(Skill.DEFENCE, 50);
                        put(Skill.STRENGTH, 50);
                        put(Skill.RANGED, 60);
                        put(Skill.MAGIC, 60);
                    }
                }, new MossGiantsLogic());
    }

    private void addBasicMob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements) {
        mobs.add(new Mob(name, locations, levelRequirements, null, true, new DefaultLogic()));
    }

    private void addLogicMob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements, MobLogic logic) {
        mobs.add(new Mob(name, locations, levelRequirements, null, true, logic));
    }

    private void addQuestedMob(String name, HashMap<String, Area> locations, HashMap<Skill, Integer> levelRequirements, List<Quest> questRequirements) {
        mobs.add(new Mob(name, locations, levelRequirements, questRequirements, true, new DefaultLogic()));
    }
}
