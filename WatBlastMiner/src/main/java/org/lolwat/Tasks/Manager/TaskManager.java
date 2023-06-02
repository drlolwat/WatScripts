package org.lolwat.Tasks.Manager;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.Enums.FishType;
import org.lolwat.Enums.IngotType;
import org.lolwat.Enums.TreeType;
import org.lolwat.Tasks.Types.Fishing.FishingTask;
import org.lolwat.Tasks.Types.Mining.MiningTask;
import org.lolwat.Tasks.Types.Smithing.SmithingIngotTask;
import org.lolwat.Tasks.WatTask;
import org.lolwat.Tasks.Woodcutting.WoodcuttingTask;
import org.lolwat.WatAIO;
import sun.reflect.generics.tree.Tree;

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

    private static List<WatTask> createFishingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        // lumbridge
        tasks.add(new FishingTask(FishType.SHRIMP, 1, 10, new Tile(3243, 3157), new HashMap<String, Integer>() {{ put("Raw shrimps", -250); put("Raw anchovies", -250); }}));
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
        tasks.add(new WoodcuttingTask(TreeType.WILLOW, new Tile(3176, 3276), 50, 70, new HashMap<String, Integer>(){}, false)); //lumbridge

        return tasks;
    }

    private static List<WatTask> createSmithingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new SmithingIngotTask(IngotType.BRONZE, 1, 15, new HashMap<String, Integer>() { { put("Bronze bar", -200); }}));
        tasks.add(new SmithingIngotTask(IngotType.IRON, 30, 99, new HashMap<String, Integer>() { { put("Iron bar", -200); }}));
        //tasks.add(new SmithingIngotTask(IngotType.GOLD, 40, 99, new HashMap<String, Integer>(){} )); // very not profitable

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
