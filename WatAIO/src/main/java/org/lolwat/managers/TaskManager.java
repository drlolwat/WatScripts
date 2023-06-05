package org.lolwat.managers;

import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.tasks.types.combat.MeleeCombatTask;
import org.lolwat.tasks.types.cooking.CookingFishTask;
import org.lolwat.tasks.types.crafting.JewelryTask;
import org.lolwat.tasks.types.crafting.SpinningTask;
import org.lolwat.tasks.types.firemaking.FiremakingTask;
import org.lolwat.tasks.types.prayer.BuryBonesTask;
import org.lolwat.misc.types.crafting.CraftingType;
import org.lolwat.misc.types.mixed.FishType;
import org.lolwat.misc.types.prayer.BoneType;
import org.lolwat.misc.types.smithing.IngotType;
import org.lolwat.misc.types.mixed.TreeType;
import org.lolwat.tasks.types.fishing.FishingTask;
import org.lolwat.tasks.types.mining.MiningTask;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.smithing.SmithingItemTask;
import org.lolwat.tasks.types.woodcutting.WoodcuttingTask;
import org.lolwat.WatAIO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private static List<WatTask> allTasks;
    private static HashMap<Skill, List<WatTask>> tasksBySkill;
    private static WatAIO instance;

    public static void setupAllTasks(WatAIO core) {
        allTasks = new ArrayList<>();
        tasksBySkill = new HashMap<>();
        instance = core;

        allTasks.addAll(createMiningTasks());
        allTasks.addAll(createSmithingTasks());
        allTasks.addAll(createWoodcuttingTasks());
        allTasks.addAll(createFishingTasks());
        allTasks.addAll(createCraftingTasks());
        allTasks.addAll(createFiremakingTasks());
        allTasks.addAll(createPrayerTasks());
        allTasks.addAll(createCookingTasks());
        allTasks.addAll(createMeleeTasks());

        for(WatTask task : allTasks) {
            if(tasksBySkill.containsKey(task.trainsSkill())) {
                if(!tasksBySkill.get(task.trainsSkill()).contains(task)) {
                    tasksBySkill.get(task.trainsSkill()).add(task);
                }
            } else {
                tasksBySkill.put(task.trainsSkill(), new ArrayList<WatTask>() { { add(task); }});
            }
        }
    }

    private static List<WatTask> createMeleeTasks() {
        List<WatTask> tasks = new ArrayList<>();

        //dynamically make the same tasks for all 3 melee skills
        List<Skill> s = Arrays.asList(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE);
        for (Skill sk : s) {
            // lumbridge chickens north
            tasks.add(new MeleeCombatTask(sk, 1, 20, new Area(
                    new Tile(3170, 3289, 0),
                    new Tile(3183, 3289, 0),
                    new Tile(3185, 3291, 0),
                    new Tile(3185, 3295, 0),
                    new Tile(3186, 3296, 0),
                    new Tile(3186, 3298, 0),
                    new Tile(3185, 3299, 0),
                    new Tile(3185, 3301, 0),
                    new Tile(3183, 3302, 0),
                    new Tile(3182, 3302, 0),
                    new Tile(3182, 3303, 0),
                    new Tile(3181, 3303, 0),
                    new Tile(3179, 3303, 0),
                    new Tile(3179, 3307, 0),
                    new Tile(3174, 3307, 0),
                    new Tile(3172, 3302, 0),
                    new Tile(3170, 3300, 0),
                    new Tile(3169, 3293, 0)), "Chicken", new HashMap<String, Integer>() {
            }));

            // lumbridge cows north
            tasks.add(new MeleeCombatTask(sk, 10, 20, new Area(
                    new Tile(3193, 3300, 0),
                    new Tile(3193, 3286, 0),
                    new Tile(3197, 3282, 0),
                    new Tile(3201, 3283, 0),
                    new Tile(3205, 3283, 0),
                    new Tile(3207, 3284, 0),
                    new Tile(3211, 3284, 0),
                    new Tile(3212, 3285, 0),
                    new Tile(3212, 3289, 0),
                    new Tile(3213, 3290, 0),
                    new Tile(3213, 3292, 0),
                    new Tile(3211, 3295, 0),
                    new Tile(3210, 3297, 0),
                    new Tile(3210, 3301, 0),
                    new Tile(3209, 3302, 0),
                    new Tile(3194, 3302, 0)), "Cow", new HashMap<String, Integer>() {
                {
                    put("Salmon", 12);
                }
            }));

            // al kharid warriors east
            tasks.add(new MeleeCombatTask(sk, 20, 40, new Area(3299, 3177, 3303, 3167), "Al Kharid warrior", new HashMap<String, Integer>() {
                {
                    put("Salmon", 28);
                }
            }));

            // al kharid warrior west
            tasks.add(new MeleeCombatTask(sk, 20, 40, new Area(3282, 3177, 3286, 3167), "Al Kharid warrior", new HashMap<String, Integer>() {
                {
                    put("Salmon", 28);
                }
            }));

            // varrock guards, palace upstairs north
            tasks.add(new MeleeCombatTask(sk, 30, 99, new Area(
                    new Tile(3207, 3490, 1),
                    new Tile(3200, 3490, 1),
                    new Tile(3200, 3499, 1),
                    new Tile(3202, 3501, 1),
                    new Tile(3205, 3501, 1),
                    new Tile(3207, 3499, 1)), "Guard", new HashMap<String, Integer>() {
                {
                    put("Salmon", 20);
                }
            }));

            // varrock guards, palace courtyard
            tasks.add(new MeleeCombatTask(sk, 30, 99, new Area(
                    new Tile(3202, 3468, 0),
                    new Tile(3202, 3461, 0),
                    new Tile(3204, 3459, 0),
                    new Tile(3222, 3459, 0),
                    new Tile(3224, 3461, 0),
                    new Tile(3224, 3469, 0),
                    new Tile(3217, 3471, 0),
                    new Tile(3207, 3471, 0)), "Guard", new HashMap<String, Integer>() {{
                put("Salmon", 20);
            }}));

            // hill giants (plateau)
            tasks.add(new MeleeCombatTask(sk, 40, 99, new Area(
                    new Tile(3367, 3157, 0),
                    new Tile(3367, 3142, 0),
                    new Tile(3381, 3142, 0),
                    new Tile(3388, 3150, 0),
                    new Tile(3390, 3150, 0),
                    new Tile(3384, 3158, 0),
                    new Tile(3375, 3160, 0)), "Hill giant", new HashMap<String, Integer>() {{
                put("Salmon", 28);
            }}));
        }

        return tasks;
    }

    private static List<WatTask> createCookingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new CookingFishTask(FishType.SHRIMPS, 1, 5, 15, new HashMap<String, Integer>() {{
            put("Shrimp", -100);
        }}));

        tasks.add(new CookingFishTask(FishType.HERRING, 5, 15, 15, new HashMap<String, Integer>() {{
            put("Shrimp", -1);
            put("Raw shrimps", -1);
            put("Herring", -200);
        }}));

        tasks.add(new CookingFishTask(FishType.TROUT, 15, 50, 15, new HashMap<String, Integer>() {
            {
                put("Shrimp", -1);
                put("Raw shrimps", -1);
                put("Herring", -1);
                put("Raw herring", -1);
                put("Trout", -200);
            }
        }));

        tasks.add(new CookingFishTask(FishType.PIKE, 20, 50, 15, new HashMap<String, Integer>() {
            {
                put("Shrimp", -1);
                put("Raw shrimps", -1);
                put("Herring", -1);
                put("Raw herring", -1);
                put("Pike", -200);
            }
        }));

        tasks.add(new CookingFishTask(FishType.SALMON, 25, 55, 15, new HashMap<String, Integer>() {
            {
                put("Shrimp", -1);
                put("Raw shrimps", -1);
                put("Herring", -1);
                put("Raw herring", -1);
                put("Salmon", -200);
            }
        }));

        tasks.add(new CookingFishTask(FishType.TUNA, 35, 70, 15, new HashMap<String, Integer>() {
            {
                put("Shrimp", -1);
                put("Raw shrimps", -1);
                put("Herring", -1);
                put("Raw herring", -1);
                put("Tuna", -200);
            }
        }));

        tasks.add(new CookingFishTask(FishType.LOBSTER, 45, 99, 15, new HashMap<String, Integer>() {
            {
                put("Shrimp", -1);
                put("Raw shrimps", -1);
                put("Herring", -1);
                put("Raw herring", -1);
                put("Lobster", -200);
            }
        }));

        return tasks;
    }

    private static List<WatTask> createPrayerTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new BuryBonesTask(BoneType.BIGBONES, 20));

        return tasks;
    }

    private static List<WatTask> createFiremakingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new FiremakingTask(TreeType.TREE, 1, 15, 20, new HashMap<>()));
        tasks.add(new FiremakingTask(TreeType.OAK, 15, 31, 20, new HashMap<>()));
        tasks.add(new FiremakingTask(TreeType.WILLOW, 31, 99, 20, new HashMap<>()));

        return tasks;
    }

    private static List<WatTask> createCraftingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        // wool, only uses lumbridge castle at the moment
        tasks.add(new SpinningTask(CraftingType.WOOL, 1, 5, 20, new HashMap<String, Integer>() { { put("Ball of wool", -200); }}));

        // jewelry
        tasks.add(new JewelryTask(CraftingType.RING, 5, 15, new HashMap<String, Integer>() { { put("Ball of wool", -1); put("Wool", -1); put("Gold ring", -200); }}));
        tasks.add(new JewelryTask(CraftingType.AMULET, 15, 99, new HashMap<String, Integer>() { { put("Gold amulet (u)", -200); put("Gold ring", -1); put("Wool", -1); put("Ball of wool", -1); }}));

        return tasks;
    }

    private static List<WatTask> createFishingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        // lumbridge
        tasks.add(new FishingTask(FishType.SHRIMPS, 1, 10, new Tile(3243, 3157), new HashMap<String, Integer>() {{ put("Raw shrimps", -250); put("Raw anchovies", -250); }}));
        tasks.add(new FishingTask(FishType.HERRING, 10, 30, new Tile(3243, 3157), new HashMap<String, Integer>() {{put("Raw herring", -250); put("Raw sardine", -250); put("Raw shrimps", -1); put("Raw anchovies", -1);}}));

        // barbarian village
        tasks.add(new FishingTask(FishType.PIKE, 30, 99, new Tile(3108, 3433), new HashMap<String, Integer>() {{ put("Raw pike", -250); }}));
        tasks.add(new FishingTask(FishType.SALMON, 35, 99, new Tile(3108, 3433), new HashMap<String, Integer>() {{ put("Raw salmon", -250); put("Raw trout", -250); }}));

        return tasks;
    }

    private static List<WatTask> createWoodcuttingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        // regular logs
        tasks.add(new WoodcuttingTask(TreeType.TREE, new Tile(3275, 3443), 1, 21, new HashMap<String, Integer>() { { put("Logs", -500); }}, false)); //varrock east
        tasks.add(new WoodcuttingTask(TreeType.TREE, new Tile(3160, 3455), 1, 21, new HashMap<String, Integer>() { { put("Logs", -500); }}, false)); //grand exchange south wall
        tasks.add(new WoodcuttingTask(TreeType.TREE, new Tile(3194, 3248), 1, 21, new HashMap<String, Integer>() { { put("Logs", -500); }}, false)); //lumbridge

        // oak logs
        tasks.add(new WoodcuttingTask(TreeType.OAK, new Tile(3277, 3429), 21, 50, new HashMap<String, Integer>() { { put("Oak logs", -500); put("Logs", -1); }}, false)); //varrock east
        tasks.add(new WoodcuttingTask(TreeType.OAK, new Tile(3165, 3422), 21, 50, new HashMap<String, Integer>() { { put("Oak logs", -500); put("Logs", -1); }}, false)); //varrock west
        tasks.add(new WoodcuttingTask(TreeType.OAK, new Tile(3203, 3243), 21, 50, new HashMap<String, Integer>() { { put("Oak logs", -500); put("Logs", -1); }}, false)); //lumbridge

        // willow logs
        tasks.add(new WoodcuttingTask(TreeType.WILLOW, new Tile(3176, 3276), 50, 99, new HashMap<String, Integer>(){ { put("Willow logs", -2000); put("Oak logs", -1); put("Logs", -1); }}, false)); //lumbridge

        // yew logs
        tasks.add(new WoodcuttingTask(TreeType.YEW, new Tile(3088, 3475), 60, 99, new HashMap<String, Integer>() { { put("Yew logs", -500); }}, false)); //edge
        tasks.add(new WoodcuttingTask(TreeType.YEW, new Tile(3206, 3502), 60, 99, new HashMap<String, Integer>() { { put("Yew logs", -500); }}, false)); //g.e
        tasks.add(new WoodcuttingTask(TreeType.YEW, new Tile(3035, 3318), 60, 99, new HashMap<String, Integer>() { { put("Yew logs", -500); }}, false)); //fally

        return tasks;
    }

    private static List<WatTask> createSmithingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        /*
        tasks.add(new SmithingIngotTask(IngotType.BRONZE, 1, 15, new HashMap<String, Integer>() {
            {
                put("Bronze bar", -200);
            }
        }));
        tasks.add(new SmithingIngotTask(IngotType.IRON, 15, 20, new HashMap<String, Integer>() {
            {
                put("Iron bar", -200);
            }
        }));
        tasks.add(new SmithingIngotTask(IngotType.SILVER, 20, 99, new HashMap<String, Integer>() {
            {
                put("Silver bar", -200);
            }
        })); // profitable*/

        // actual items, followed wiki for most efficient leveling
        tasks.add(new SmithingItemTask(IngotType.BRONZE, 1, 5, new Area(3185, 3427, 3190, 3420), 10, new HashMap<String, Integer>() {
            {
                put("Bronze axe", -100);
            }
        }));

        tasks.add(new SmithingItemTask(IngotType.BRONZE, 5, 9, new Area(3185, 3427, 3190, 3420), 10, new HashMap<String, Integer>() {
            {
                put("Bronze axe", -1);
                put("Bronze scimitar", -100);
            }
        }));

        tasks.add(new SmithingItemTask(IngotType.BRONZE, 9, 18, new Area(3185, 3427, 3190, 3420), 10, new HashMap<String, Integer>() {
            {
                put("Bronze axe", -1);
                put("Bronze scimitar", -1);
                put("Bronze warhammer", -30);
            }
        }));

        tasks.add(new SmithingItemTask(IngotType.BRONZE, 18, 33, new Area(3185, 3427, 3190, 3420), 10, new HashMap<String, Integer>() {
            {
                put("Bronze axe", -1);
                put("Bronze scimitar", -1);
                put("Bronze warhammer", -1);
                put("Bronze platebody", -30);
            }
        }));

        tasks.add(new SmithingItemTask(IngotType.IRON, 33, 48, new Area(3185, 3427, 3190, 3420), 10, new HashMap<String, Integer>() {
            {
                put("Bronze axe", -1);
                put("Bronze scimitar", -1);
                put("Bronze warhammer", -1);
                put("Bronze platebody", -2);
                put("Iron platebody", -30);
            }
        }));

        tasks.add(new SmithingItemTask(IngotType.STEEL, 48, 68, new Area(3185, 3427, 3190, 3420), 10, new HashMap<String, Integer>() {
            {
                put("Bronze axe", -1);
                put("Bronze scimitar", -1);
                put("Bronze warhammer", -1);
                put("Bronze platebody", -2);
                put("Iron platebody", -2);
                put("Steel platebody", -30);
            }
        }));

        tasks.add(new SmithingItemTask(IngotType.MITHRIL, 68, 88, new Area(3185, 3427, 3190, 3420), 10, new HashMap<String, Integer>() {
            {
                put("Bronze axe", -1);
                put("Bronze scimitar", -1);
                put("Bronze warhammer", -1);
                put("Bronze platebody", -2);
                put("Iron platebody", -2);
                put("Steel platebody", -2);
                put("Mithril platebody", -30);
            }
        }));

        tasks.add(new SmithingItemTask(IngotType.ADAMANTITE, 88, 98, new Area(3185, 3427, 3190, 3420), 10, new HashMap<String, Integer>() {
            {
                put("Bronze axe", -1);
                put("Bronze scimitar", -1);
                put("Bronze warhammer", -1);
                put("Bronze platebody", -2);
                put("Iron platebody", -2);
                put("Steel platebody", -2);
                put("Mithril platebody", -2);
                put("Adamant platebody", -30);
            }
        }));

        tasks.add(new SmithingItemTask(IngotType.RUNITE, 88, 98, new Area(3185, 3427, 3190, 3420), 10, new HashMap<String, Integer>() {
            {
                put("Bronze axe", -1);
                put("Bronze scimitar", -1);
                put("Bronze warhammer", -1);
                put("Bronze platebody", -2);
                put("Iron platebody", -2);
                put("Steel platebody", -2);
                put("Mithril platebody", -2);
                put("Adamant platebody", -2);
                put("Rune platelegs", -30);
            }
        }));

        return tasks;
    }

    private static List<WatTask> createMiningTasks() {
        List<WatTask> tasks = new ArrayList<>();

        // Varrock East Copper
        tasks.add(new MiningTask(1, 20, new Tile(3289, 3362),
                        Arrays.asList(
                                Arrays.asList(new Tile(3290, 3362), new Tile(3289, 3363)),
                                Arrays.asList(new Tile(3285, 3361), new Tile(3287, 3361))
                        ),
                        new HashMap<String, Integer>() {{ put("Copper ore", -1000); }}, instance));

        // Varrock East Tin
        tasks.add(new MiningTask(1, 20, new Tile(3282, 3363),
                Arrays.asList(
                        Arrays.asList(new Tile(3281, 3363), new Tile(3282, 3363)),
                        Arrays.asList(new Tile(3288, 3366), new Tile(3289, 3368))
                        ),
                new HashMap<String, Integer>() {{ put("Tin ore", -1000); }}, instance));


        // Varrock East Iron
        tasks.add(new MiningTask(15, 60, new Tile(3286, 3368),
                Arrays.asList(
                    Arrays.asList(new Tile(3286, 3369), new Tile(3285, 3368)),
                    Arrays.asList(new Tile(3288, 3370), new Tile(3285, 3369))
                ),
                new HashMap<String, Integer>() {{ put("Iron ore", -1000); }}, instance));

        // Mining Guild Iron
        tasks.add(new MiningTask(60, 70, new Tile(3033, 9738),
                Arrays.asList(
                        Arrays.asList(new Tile(3033, 9737), new Tile(3034, 9738)),
                        Arrays.asList(new Tile(3032, 9737), new Tile(3032, 9739))
                ),
                new HashMap<String, Integer>() {{ put("Iron ore", -1000); }}, instance));

        // Mining Guild Coal
        tasks.add(new MiningTask(70, 100, new Tile(3033, 9738), "Coal rocks",
                new HashMap<String, Integer>() {{ put("Coal", -1000); }}, instance));

        return tasks;
    }

    public static List<WatTask> getAllTasks() { return allTasks; }
    public static List<WatTask> getTasksBySkill(Skill skill) {
        if(tasksBySkill.containsKey(skill)) {
            return tasksBySkill.get(skill);
        }
        else {
            return new ArrayList<>();
        }
    }
}
