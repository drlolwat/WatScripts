package org.lolwat.Tasks.Manager;

import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.lolwat.Enums.IngotType;
import org.lolwat.Tasks.Types.Mining.MiningTask;
import org.lolwat.Tasks.Types.Smithing.SmithingIngotTask;
import org.lolwat.Tasks.WatTask;
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

    private static List<WatTask> createSmithingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new SmithingIngotTask(IngotType.BRONZE, 1, 40, new HashMap<String, Integer>() { { put("Bronze bar", -200); }}));
        tasks.add(new SmithingIngotTask(IngotType.IRON, 25, 99, new HashMap<String, Integer>() { { put("Iron bar", -200); }}));

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
        tasks.add(new MiningTask(70, 99, new Tile(3033, 9738), "Coal rocks",
                new HashMap<String, Integer>() {{ put("Coal", -1000); }}, instance));

        return tasks;
    }

    public static List<WatTask> getAllTasks() { return allTasks; }
    public static List<WatTask> getTasksBySkill(Skill skill) {
        if(tasksBySkill.containsKey(skill)) {
            return tasksBySkill.get(skill);
        }
        else {
            return new ArrayList<WatTask>();
        }
    }
}
