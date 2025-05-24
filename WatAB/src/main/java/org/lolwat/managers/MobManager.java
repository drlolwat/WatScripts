package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.types.interfaces.MobLogic;
import org.lolwat.types.mobs.Mob;
import org.lolwat.types.mobs.logic.DefaultLogic;
import org.lolwat.types.mobs.logic.EdgevilleGiantsLogic;
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
        instance = this;
    }

    public void createMobs() {
        addBasicMob("Chicken",
                new HashMap<String, Area>() {
                    {
                        put("Falador Chickens Outside", new Area(3026, 3289, 3036, 3284));
                        put("Falador Chickens Inside", new Area(3014, 3298, 3019, 3282));
                        put("Lumbridge East Chickens", new Area(
                                new Tile(3233, 3295, 0),
                                new Tile(3225, 3295, 0),
                                new Tile(3225, 3300, 0),
                                new Tile(3226, 3301, 0),
                                new Tile(3235, 3301, 0),
                                new Tile(3237, 3299, 0),
                                new Tile(3236, 3295, 0),
                                new Tile(3237, 3292, 0),
                                new Tile(3236, 3289, 0),
                                new Tile(3235, 3287, 0),
                                new Tile(3231, 3287, 0),
                                new Tile(3231, 3294, 0),
                                new Tile(3231, 3295, 0)));
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

        addBasicMob("Cow",
                new HashMap<String, Area>() {
                    {
                        put("Lumbridge North Cows", new Area(
                                new Tile(3202, 3291, 0),
                                new Tile(3202, 3283, 0),
                                new Tile(3195, 3284, 0),
                                new Tile(3193, 3286, 0),
                                new Tile(3193, 3300, 0),
                                new Tile(3193, 3301, 0),
                                new Tile(3195, 3303, 0),
                                new Tile(3200, 3303, 0),
                                new Tile(3201, 3302, 0),
                                new Tile(3210, 3302, 0),
                                new Tile(3210, 3295, 0),
                                new Tile(3213, 3293, 0),
                                new Tile(3213, 3290, 0),
                                new Tile(3211, 3287, 0),
                                new Tile(3211, 3284, 0),
                                new Tile(3206, 3284, 0),
                                new Tile(3202, 3283, 0)));

                        put("Falador Cows", new Area(
                                new Tile(3043, 3306, 0),
                                new Tile(3043, 3311, 0),
                                new Tile(3041, 3313, 0),
                                new Tile(3030, 3313, 0),
                                new Tile(3026, 3307, 0),
                                new Tile(3021, 3307, 0),
                                new Tile(3021, 3297, 0),
                                new Tile(3031, 3298, 0),
                                new Tile(3038, 3298, 0)));

                        put("Crafting Guild Cows", new Area(
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

        addBasicMob("Dwarf",
                new HashMap<String, Area>() {
                    {
                        put("Mine 1", new Area(3039, 9828, 3053, 9812));
                        put("Mine 2", new Area(
                                new Tile(3016, 9812, 0),
                                new Tile(3021, 9804, 0),
                                new Tile(3022, 9798, 0),
                                new Tile(3026, 9799, 0),
                                new Tile(3027, 9802, 0),
                                new Tile(3027, 9807, 0),
                                new Tile(3029, 9811, 0),
                                new Tile(3031, 9812, 0),
                                new Tile(3030, 9818, 0),
                                new Tile(3033, 9825, 0),
                                new Tile(3032, 9836, 0),
                                new Tile(3017, 9836, 0)));
                    }
                },

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 15);
                        put(Skill.DEFENCE, 15);
                        put(Skill.STRENGTH, 15);
                        put(Skill.RANGED, 20);
                        put(Skill.MAGIC, 20);
                    }
                });

        addBasicMob("Guard",
                new HashMap<String, Area>() {
                    {
                        put("Varrock 1", new Area(
                                new Tile(3206, 3476, 2),
                                new Tile(3201, 3476, 2),
                                new Tile(3201, 3487, 2),
                                new Tile(3207, 3487, 2),
                                new Tile(3208, 3478, 2),
                                new Tile(3210, 3476, 2),
                                new Tile(3218, 3476, 2),
                                new Tile(3218, 3467, 2),
                                new Tile(3209, 3467, 2)));
                }},

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 30);
                        put(Skill.DEFENCE, 30);
                        put(Skill.STRENGTH, 30);
                        put(Skill.RANGED, 40);
                        put(Skill.MAGIC, 40);
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
                    }},

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 30);
                        put(Skill.DEFENCE, 30);
                        put(Skill.STRENGTH, 30);
                        put(Skill.RANGED, 30);
                        put(Skill.MAGIC, 30);
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
                    }},

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 30);
                        put(Skill.DEFENCE, 30);
                        put(Skill.STRENGTH, 30);
                        put(Skill.RANGED, 30);
                        put(Skill.MAGIC, 30);
                    }
                });

        addLogicMob("Hill Giant",
                new HashMap<String, Area>() {
                    {
                        put("Edgeville", new Area(
                                new Tile(3110, 9836, 0),
                                new Tile(3110, 9824, 0),
                                new Tile(3102, 9824, 0),
                                new Tile(3097, 9829, 0),
                                new Tile(3095, 9830, 0),
                                new Tile(3095, 9835, 0),
                                new Tile(3098, 9838, 0),
                                new Tile(3101, 9839, 0),
                                new Tile(3106, 9840, 0),
                                new Tile(3108, 9844, 0),
                                new Tile(3109, 9849, 0),
                                new Tile(3113, 9851, 0),
                                new Tile(3122, 9850, 0),
                                new Tile(3125, 9847, 0),
                                new Tile(3125, 9844, 0),
                                new Tile(3122, 9838, 0),
                                new Tile(3121, 9832, 0),
                                new Tile(3110, 9826, 0)));
                    }},

                new HashMap<Skill, Integer>() {
                    {
                        put(Skill.ATTACK, 40);
                        put(Skill.DEFENCE, 40);
                        put(Skill.STRENGTH, 40);
                        put(Skill.RANGED, 60);
                        put(Skill.MAGIC, 60);
                    }
                }, new EdgevilleGiantsLogic());

        addLogicMob("Moss Giant",
                new HashMap<String, Area>() {
                    {
                        put("Edgeville", new Area(
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
                    }},

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
