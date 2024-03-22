package org.lolwat.managers;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.types.combat.MagicCombatTask;
import org.lolwat.tasks.types.combat.MeleeCombatTask;
import org.lolwat.tasks.types.combat.RangedCombatTask;
import org.lolwat.tasks.types.cooking.CookingFishTask;
import org.lolwat.tasks.types.crafting.JewelryTask;
import org.lolwat.tasks.types.crafting.SpinningTask;
import org.lolwat.tasks.types.firemaking.FiremakingTask;
import org.lolwat.tasks.types.magic.HighAlchemyTask;
import org.lolwat.tasks.types.misc.*;
import org.lolwat.tasks.types.prayer.BuryBonesTask;
import org.lolwat.misc.types.crafting.CraftingType;
import org.lolwat.misc.types.mixed.FishType;
import org.lolwat.misc.types.prayer.BoneType;
import org.lolwat.misc.types.smithing.IngotType;
import org.lolwat.misc.types.mixed.TreeType;
import org.lolwat.tasks.types.fishing.FishingTask;
import org.lolwat.tasks.types.mining.MiningTask;
import org.lolwat.tasks.WatTask;
import org.lolwat.tasks.types.quests.*;
import org.lolwat.tasks.types.smithing.SmithingItemTask;
import org.lolwat.tasks.types.tutorial.SelectUsernameTask;
import org.lolwat.tasks.types.woodcutting.WoodcuttingTask;
import org.lolwat.WatAIO;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class TaskManager {
    private List<WatTask> tasks;
    private HashMap<Skill, List<WatTask>> tasksBySkill;
    private HashMap<Quest, WatTask> questTasks;
    private List<WatTask> restrictedMoneyMakingTasks; // TODO
    private List<WatTask> unrestrictedMoneyMakingTasks; // TODO

    // ---- RUNTIME VARIABLES ----
    private static TaskManager instance;
    private final WatAIO watAIO;
    private WatTask currentTask;
    private int tasksUntilBreak;
    private double taskSelectedAt;
    private int taskRunTime;
    private double checkedHoursAt;

    public TaskManager(WatAIO instance) {
        watAIO = instance;
        setupAllTasks();
        resetBreaks();
        setCheckedHoursAt(0);

        Logger.log(Color.green, "TaskManager: Set up " + tasks.size() + " total tasks and " + getQuests().size() + " total quests.");
        setInstance(this);
    }

    public static TaskManager getInstance() {
        return instance;
    }

    private static void setInstance(TaskManager value) {
        instance = value;
    }

    public void getNewTask() {
        getNewTask(false);
    }

    public void getNewTask(boolean noQuest) {
        preTaskSelection();
        boolean quest = !noQuest && Calculations.random(1, ConfigManager.getInstance().getConfigBoolean("faster_quests") ? 4 : 8) == 3;

        if(!quest) {
            for (WatTask task : tasks) {
                if (task.trainsSkill().equals(Skill.HITPOINTS))
                    continue;

                if (ConfigManager.getInstance().getSkillTarget(task.trainsSkill()) > Skills.getRealLevel(task.trainsSkill())) {
                    if (task.canPerformTask()) {
                        Logger.log("TaskManager: Selected task: " + task.getName());
                        setCurrentTask(task);
                        return;
                    }
                }
            }
        } else {
            for (Map.Entry<Quest, WatTask> questTask : questTasks.entrySet()) {
                if (questTask.getValue().canPerformTask() && Quests.isFinished(questTask.getValue().completesQuest())) {
                    Logger.log("TaskManager: Selected quest task: " + questTask.getValue().getName());
                    setCurrentTask(questTask.getValue());
                    return;
                }
            }

            Logger.log(Color.red, "TaskManager: no quest tasks available, selecting a regular task");
            getNewTask(true);
        }

        Logger.error("TaskManager: could not select a task");
    }

    public void getSpecificSkillTask(Skill sk) {
        preTaskSelection();
        if(sk.equals(Skill.HITPOINTS)) {
            List<WatTask> pool;
            if(ConfigManager.getInstance().isTradeUnlocked()) {
                pool = unrestrictedMoneyMakingTasks;
            } else {
                pool = restrictedMoneyMakingTasks;
            }

            Collections.shuffle(pool);
            for(WatTask t : pool) {
                if(t.canPerformTask()) {
                    Logger.log(Color.green, "TaskManager: Selected money making task: " + t.getName());
                    setCurrentTask(t);
                    return;
                }
            }
        }
        else {
            for(WatTask t : tasksBySkill.get(sk)) {
                if(ConfigManager.getInstance().getSkillTarget(t.trainsSkill()) > Skills.getRealLevel(t.trainsSkill()) &&
                        t.canPerformTask()) {

                    Logger.log(Color.green, "TaskManager: Selected task for skill: " + sk.getName() + " - " + t.getName());
                    setCurrentTask(t);
                    return;
                }
            }
        }

        Logger.error("TaskManager: could not select a specific task for skill: " + sk.getName());
    }

    public WatTask getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(WatTask task) {
        setCurrentTask(task, 0);
    }

    public void setCurrentTask(WatTask value, int runtime) {
        currentTask = value;
        taskSelectedAt = Instant.now().getEpochSecond();
        taskRunTime = runtime > 0 ? runtime : Calculations.random(1200, 6750);
    }

    public void shuffleQuestTasks() {
        List<Map.Entry<Quest, WatTask>> questTaskList = new ArrayList<>(questTasks.entrySet());
        Collections.shuffle(questTaskList);

        questTasks.clear();
        for (Map.Entry<Quest, WatTask> entry : questTaskList) {
            questTasks.put(entry.getKey(), entry.getValue());
        }
    }

    public void resetBreaks() {
        tasksUntilBreak = Calculations.random(8, 15);
    }

    private boolean evaluateBreak() {
        if(ConfigManager.getInstance().getConfigBoolean("breaks_enabled") && tasksUntilBreak < 0) {
            resetBreaks();
            setCurrentTask(new BreakingTask((Instant.now().getEpochSecond() + taskRunTime)), Calculations.random(28800, 43200));
            ConfigManager.getInstance().getWsProfile(taskRunTime);

            Logger.log("TaskManager: Going on break");
            return true;
        }
        return false;
    }

    private boolean evaluateGoals() {
        boolean goalsMet = true;
        for(Skill sk : Skill.values()) {
            if(Skills.getRealLevel(sk) < ConfigManager.getInstance().getSkillTarget(sk))
                goalsMet = false;
        }

        if(goalsMet) {
            Logger.log("We are going to the bank to log out");
            setCurrentTask(new LogoutTask(true, true, null), 0);
            Sleep.sleep(1000, 3000);
            return true;
        }

        return false;
    }

    private void preTaskSelection() {
        if(!ConfigManager.getInstance().hasLoadedProfile() || (getCheckedHoursAt() == 0 || (Instant.now().getEpochSecond() - getCheckedHoursAt()) >= 3600)) {
            watAIO.disableLoginManager();
            ConfigManager.getInstance().getWsProfile(0);
            ConfigManager.getInstance().setHasLoadedProfile(true);
            setCheckedHoursAt(Instant.now().getEpochSecond());
            watAIO.enableLoginManager();
        }

        if(!Client.isLoggedIn()) {
            Logger.log("Awaiting login...");
            watAIO.enableLoginManager();
            return;
        }

        if (PlayerSettings.getConfig(281) != 1000) {
            setCurrentTask(new SelectUsernameTask(), 0);
            Logger.log("We are performing Tutorial Island");
            return;
        }

        boolean devMode = false; // change at compile time
        if(devMode) {
            setCurrentTask(new AdvertiseTask(), 0);
            Logger.log("We are going to spam BotBuddy at the G.E");
            return;
        }

        if (!GenericUtils.isMember() && (ConfigManager.getInstance().getConfigInt("bond_min_ttl") > 0
                && Skills.getTotalLevel() >= ConfigManager.getInstance().getConfigInt("bond_min_ttl"))) {

            setCurrentTask(new BondingTask(null), 0);
            Logger.log("We are making our account a member");
            return;
        }

        if(GenericUtils.isMember() && !Worlds.getCurrent().isMembers()) {
            setCurrentTask(new HopperTask(0, (currentTask != null) ? currentTask : null), 0);
            Logger.log("We are hopping into a P2P world");
            return;
        }

        Logger.log("Evaluating goals for stop conditions");
        if(evaluateGoals()) {
            Logger.log("Goals have been met..");
            return;
        }

        if(!ConfigManager.getInstance().isTradeUnlocked()
                && ConfigManager.getInstance().getConfigBoolean("logout_after_unrestricted")) {

            setCurrentTask(new LogoutTask(true, true,null), 0);
            Logger.log("We are going to the bank to log out");
            return;
        }

        if(ConfigManager.getInstance().getConfigBoolean("breaks_enabled")) {
            Logger.log("Evaluating for breaks");
            if (!evaluateBreak()) {
                Logger.log("Going on break after " + tasksUntilBreak + " more completed task(s)");
            }
        }

        Collections.shuffle(tasks);
        shuffleQuestTasks();
    }

    private void setupAllTasks() {
        tasks = new ArrayList<>();
        tasksBySkill = new HashMap<>();
        questTasks = new HashMap<>();
        restrictedMoneyMakingTasks = new ArrayList<>();
        unrestrictedMoneyMakingTasks = new ArrayList<>();

        tasks.addAll(createMiningTasks());
        tasks.addAll(createSmithingTasks());
        tasks.addAll(createWoodcuttingTasks());
        tasks.addAll(createFishingTasks());
        tasks.addAll(createCraftingTasks());
        tasks.addAll(createFiremakingTasks());//
        tasks.addAll(createPrayerTasks());
        tasks.addAll(createCookingTasks());
        tasks.addAll(createMeleeTasks());
        tasks.addAll(createRangedTasks());
        tasks.addAll(createMagicTasks());

        for(WatTask task : tasks) {
            if(tasksBySkill.containsKey(task.trainsSkill())) {
                if(!tasksBySkill.get(task.trainsSkill()).contains(task)) {
                    tasksBySkill.get(task.trainsSkill()).add(task);
                }
            } else {
                tasksBySkill.put(task.trainsSkill(), new ArrayList<WatTask>() { { add(task); }});
            }
        }

        questTasks.putAll(createQuestTasks());

        restrictedMoneyMakingTasks.addAll(createRestrictedMMTasks());
        unrestrictedMoneyMakingTasks.addAll(createUnrestrictedMMTasks());
    }

    private List<WatTask> createUnrestrictedMMTasks() {
        List<WatTask> tasks = new ArrayList<>();
        List<Skill> mmSkills = new ArrayList<Skill>() { { add(Skill.WOODCUTTING); add(Skill.FISHING); add(Skill.MINING); }};

        for(Map.Entry<Skill, List<WatTask>> e : tasksBySkill.entrySet()) {
            if(mmSkills.contains(e.getKey())) {
                tasks.addAll(e.getValue());
            }
        }

        return tasks;
    }

    private List<WatTask> createRestrictedMMTasks() {
        List<WatTask> tasks = new ArrayList<>();
        tasks.add(new WoodcuttingTask(TreeType.TREE, new Tile(3275, 3443), 1, 99, new HashMap<String, Integer>() { { put("Logs", -Calculations.random(120, 250)); }}, false)); //varrock east
        tasks.add(new WoodcuttingTask(TreeType.TREE, new Tile(3160, 3455), 1, 99, new HashMap<String, Integer>() { { put("Logs", -Calculations.random(120, 250)); }}, false)); //grand exchange south wall
        tasks.add(new ScavengingTask());

        return tasks;
    }

    private  HashMap<Quest, WatTask> createQuestTasks() {
        HashMap<Quest, WatTask> tasks = new HashMap<>();

        tasks.put(FreeQuest.IMP_CATCHER, new ImpCatcherQuest());
        tasks.put(FreeQuest.SHEEP_SHEARER, new SheepShearerQuest());
        tasks.put(FreeQuest.COOKS_ASSISTANT, new CooksAssistantQuest());
        tasks.put(FreeQuest.DORICS_QUEST, new DoricsQuest());
        tasks.put(FreeQuest.GOBLIN_DIPLOMACY, new GoblinDiplomacyQuest());
        tasks.put(FreeQuest.ROMEO_AND_JULIET, new RomeoJulietQuest());
        tasks.put(FreeQuest.THE_RESTLESS_GHOST, new TheRestlessGhostQuest());

        return tasks;
    }

    private List<WatTask> createMagicTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new MagicCombatTask(1, 5, new Area(
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

        // lumbridge chickens east
        tasks.add(new MagicCombatTask(1, 10, new Area(
                new Tile(3231, 3295, 0),
                new Tile(3231, 3287, 0),
                new Tile(3236, 3287, 0),
                new Tile(3236, 3300, 0),
                new Tile(3226, 3301, 0),
                new Tile(3225, 3295, 0)), "Chicken", new HashMap<String, Integer>() { }));

        // lumbridge cows north
        tasks.add(new MagicCombatTask(5, 20, new Area(
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
                put("Trout", 12);
            }
        }));

        // lumbridge cows north 2
        tasks.add(new MagicCombatTask( 5, 20, new Area(
                new Tile(3154, 3344, 0),
                new Tile(3155, 3316, 0),
                new Tile(3161, 3316, 0),
                new Tile(3165, 3320, 0),
                new Tile(3172, 3317, 0),
                new Tile(3178, 3317, 0),
                new Tile(3181, 3315, 0),
                new Tile(3185, 3315, 0),
                new Tile(3190, 3311, 0),
                new Tile(3204, 3310, 0),
                new Tile(3199, 3334, 0)), "Cow", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }
        }));

        // lumbridge east cows
        tasks.add(new MagicCombatTask( 5, 20, new Area(
                new Tile(3252, 3277, 0),
                new Tile(3242, 3286, 0),
                new Tile(3243, 3297, 0),
                new Tile(3264, 3297, 0),
                new Tile(3265, 3256, 0),
                new Tile(3254, 3256, 0)), "Cow", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }
        }));

        // falador cows
        tasks.add(new MagicCombatTask( 5, 20, new Area(
                new Tile(3043, 3306, 0),
                new Tile(3043, 3311, 0),
                new Tile(3041, 3313, 0),
                new Tile(3030, 3313, 0),
                new Tile(3026, 3307, 0),
                new Tile(3021, 3307, 0),
                new Tile(3021, 3297, 0),
                new Tile(3031, 3298, 0),
                new Tile(3038, 3298, 0)), "Cow", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }
        }));

        tasks.add(new MagicCombatTask(20, 30, new Area(
                new Tile(3044, 3498, 0),
                new Tile(3044, 3483, 0),
                new Tile(3054, 3483, 0),
                new Tile(3054, 3498, 0)), "Monk", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }}));

        // giant frogs
        tasks.add(new MagicCombatTask(20, 50, new Area(
                new Tile(3190, 3179, 0),
                new Tile(3195, 3174, 0),
                new Tile(3204, 3173, 0),
                new Tile(3203, 3179, 0),
                new Tile(3201, 3182, 0),
                new Tile(3204, 3186, 0),
                new Tile(3208, 3189, 0),
                new Tile(3207, 3196, 0),
                new Tile(3199, 3195, 0),
                new Tile(3197, 3195, 0)), "Giant frog", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }
        }));

        tasks.add(new MagicCombatTask(50, 55, new Area(
                new Tile(3367, 3157, 0),
                new Tile(3367, 3142, 0),
                new Tile(3381, 3142, 0),
                new Tile(3388, 3150, 0),
                new Tile(3390, 3150, 0),
                new Tile(3384, 3158, 0),
                new Tile(3375, 3160, 0)), "Hill giant", new HashMap<String, Integer>() {{
            put("Trout", 20);
        }}));

        tasks.add(new HighAlchemyTask(55, 99));

        return tasks;
    }

    private List<WatTask> createRangedTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new RangedCombatTask(1, 10, new Area(
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

        // lumbridge chickens east
        tasks.add(new RangedCombatTask(1, 10, new Area(
                new Tile(3231, 3295, 0),
                new Tile(3231, 3287, 0),
                new Tile(3236, 3287, 0),
                new Tile(3236, 3300, 0),
                new Tile(3226, 3301, 0),
                new Tile(3225, 3295, 0)), "Chicken", new HashMap<String, Integer>() { }));

        // lumbridge cows north
        tasks.add(new RangedCombatTask(5, 20, new Area(
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
                put("Trout", 12);
            }
        }));

        // lumbridge cows north 2
        tasks.add(new RangedCombatTask( 5, 20, new Area(
                new Tile(3154, 3344, 0),
                new Tile(3155, 3316, 0),
                new Tile(3161, 3316, 0),
                new Tile(3165, 3320, 0),
                new Tile(3172, 3317, 0),
                new Tile(3178, 3317, 0),
                new Tile(3181, 3315, 0),
                new Tile(3185, 3315, 0),
                new Tile(3190, 3311, 0),
                new Tile(3204, 3310, 0),
                new Tile(3199, 3334, 0)), "Cow", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }
        }));

        // lumbridge east cows
        tasks.add(new RangedCombatTask( 5, 20, new Area(
                new Tile(3252, 3277, 0),
                new Tile(3242, 3286, 0),
                new Tile(3243, 3297, 0),
                new Tile(3264, 3297, 0),
                new Tile(3265, 3256, 0),
                new Tile(3254, 3256, 0)), "Cow", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }
        }));

        // falador cows
        tasks.add(new RangedCombatTask( 5, 20, new Area(
                new Tile(3043, 3306, 0),
                new Tile(3043, 3311, 0),
                new Tile(3041, 3313, 0),
                new Tile(3030, 3313, 0),
                new Tile(3026, 3307, 0),
                new Tile(3021, 3307, 0),
                new Tile(3021, 3297, 0),
                new Tile(3031, 3298, 0),
                new Tile(3038, 3298, 0)), "Cow", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }
        }));

        tasks.add(new RangedCombatTask(20, 30, new Area(
                new Tile(3044, 3498, 0),
                new Tile(3044, 3483, 0),
                new Tile(3054, 3483, 0),
                new Tile(3054, 3498, 0)), "Monk", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }}));

        // giant frogs
        tasks.add(new RangedCombatTask(20, 50, new Area(
                new Tile(3190, 3179, 0),
                new Tile(3195, 3174, 0),
                new Tile(3204, 3173, 0),
                new Tile(3203, 3179, 0),
                new Tile(3201, 3182, 0),
                new Tile(3204, 3186, 0),
                new Tile(3208, 3189, 0),
                new Tile(3207, 3196, 0),
                new Tile(3199, 3195, 0),
                new Tile(3197, 3195, 0)), "Giant frog", new HashMap<String, Integer>() {
            {
                put("Trout", 12);
            }
        }));

        tasks.add(new RangedCombatTask(50, 99, new Area(
                new Tile(3367, 3157, 0),
                new Tile(3367, 3142, 0),
                new Tile(3381, 3142, 0),
                new Tile(3388, 3150, 0),
                new Tile(3390, 3150, 0),
                new Tile(3384, 3158, 0),
                new Tile(3375, 3160, 0)), "Hill giant", new HashMap<String, Integer>() {{
            put("Trout", 20);
        }}));

        tasks.add(new RangedCombatTask(60, 99, new Area(
                new Tile(3165, 9882, 0),
                new Tile(3162, 9879, 0),
                new Tile(3162, 9877, 0),
                new Tile(3163, 9876, 0),
                new Tile(3164, 9876, 0),
                new Tile(3165, 9876, 0),
                new Tile(3170, 9881, 0),
                new Tile(3173, 9881, 0),
                new Tile(3173, 9886, 0),
                new Tile(3165, 9886, 0)), "Moss giant", new HashMap<String, Integer>() {{
            put("Lobster", 20);
            put("Knife", 1);
        }}));

        tasks.add(new RangedCombatTask(60, 99, new Area(
                new Tile(3154, 9908, 0),
                new Tile(3154, 9902, 0),
                new Tile(3156, 9902, 0),
                new Tile(3158, 9898, 0),
                new Tile(3159, 9898, 0),
                new Tile(3162, 9901, 0),
                new Tile(3163, 9901, 0),
                new Tile(3166, 9904, 0),
                new Tile(3165, 9906, 0),
                new Tile(3159, 9907, 0),
                new Tile(3158, 9909, 0),
                new Tile(3154, 9909, 0),
                new Tile(3156, 9900, 0),
                new Tile(3159, 9898, 0),
                new Tile(3156, 9900, 0)), "Moss giant", new HashMap<String, Integer>() {{
            put("Lobster", 20);
            put("Knife", 1);
        }}));

        return tasks;
    }

    private List<WatTask> createMeleeTasks() {
        List<WatTask> tasks = new ArrayList<>();
        List<Skill> s = Arrays.asList(Skill.ATTACK, Skill.STRENGTH, Skill.DEFENCE);
        for (Skill sk : s) {
            // lumbridge chickens north
            tasks.add(new MeleeCombatTask(sk, 1, 10, new Area(
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
            }, new ArrayList<>()));

            // lumbridge chickens east
            tasks.add(new MeleeCombatTask(sk, 1, 10, new Area(
                    new Tile(3231, 3295, 0),
                    new Tile(3231, 3287, 0),
                    new Tile(3236, 3287, 0),
                    new Tile(3236, 3300, 0),
                    new Tile(3226, 3301, 0),
                    new Tile(3225, 3295, 0)), "Chicken", new HashMap<String, Integer>() { }, new ArrayList<>()));

            // lumbridge cows north
            tasks.add(new MeleeCombatTask(sk, 5, 20, new Area(
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
                    put("Trout", 12);
                }
            }, new ArrayList<>()));

            // lumbridge cows north 2
            tasks.add(new MeleeCombatTask(sk, 5, 20, new Area(
                    new Tile(3154, 3344, 0),
                    new Tile(3155, 3316, 0),
                    new Tile(3161, 3316, 0),
                    new Tile(3165, 3320, 0),
                    new Tile(3172, 3317, 0),
                    new Tile(3178, 3317, 0),
                    new Tile(3181, 3315, 0),
                    new Tile(3185, 3315, 0),
                    new Tile(3190, 3311, 0),
                    new Tile(3204, 3310, 0),
                    new Tile(3199, 3334, 0)), "Cow", new HashMap<String, Integer>() {
                {
                    put("Trout", 12);
                }
            }, new ArrayList<>()));

            // lumbridge east cows
            tasks.add(new MeleeCombatTask(sk, 5, 20, new Area(
                    new Tile(3252, 3277, 0),
                    new Tile(3242, 3286, 0),
                    new Tile(3243, 3297, 0),
                    new Tile(3264, 3297, 0),
                    new Tile(3265, 3256, 0),
                    new Tile(3254, 3256, 0)), "Cow", new HashMap<String, Integer>() {
                {
                    put("Trout", 12);
                }
            }, new ArrayList<>()));

            // falador cows
            tasks.add(new MeleeCombatTask(sk, 5, 20, new Area(
                    new Tile(3043, 3306, 0),
                    new Tile(3043, 3311, 0),
                    new Tile(3041, 3313, 0),
                    new Tile(3030, 3313, 0),
                    new Tile(3026, 3307, 0),
                    new Tile(3021, 3307, 0),
                    new Tile(3021, 3297, 0),
                    new Tile(3031, 3298, 0),
                    new Tile(3038, 3298, 0)), "Cow", new HashMap<String, Integer>() {
                {
                    put("Trout", 12);
                }
            }, new ArrayList<>()));

            // al kharid warriors east
            tasks.add(new MeleeCombatTask(sk, 20, 40, new Area(3299, 3177, 3302, 3167), "Al Kharid warrior", new HashMap<String, Integer>() {
                {
                    put("Trout", 20);
                }
            }, new ArrayList<>()));

            // al kharid warrior west
            tasks.add(new MeleeCombatTask(sk, 20, 40, new Area(3282, 3177, 3285, 3167), "Al Kharid warrior", new HashMap<String, Integer>() {
                {
                    put("Trout", 20);
                }
            }, new ArrayList<>()));

            // varrock guards, palace upstairs north
            tasks.add(new MeleeCombatTask(sk, 35, 55, new Area(
                    new Tile(3207, 3490, 1),
                    new Tile(3200, 3490, 1),
                    new Tile(3200, 3499, 1),
                    new Tile(3202, 3501, 1),
                    new Tile(3205, 3501, 1),
                    new Tile(3207, 3499, 1)), "Guard", new HashMap<String, Integer>() {
                {
                    put("Trout", 20);
                }
            }, new ArrayList<>()));

            // varrock guards, palace courtyard
            tasks.add(new MeleeCombatTask(sk, 35, 55, new Area(
                    new Tile(3202, 3468, 0),
                    new Tile(3202, 3461, 0),
                    new Tile(3204, 3459, 0),
                    new Tile(3222, 3459, 0),
                    new Tile(3224, 3461, 0),
                    new Tile(3224, 3469, 0),
                    new Tile(3217, 3471, 0),
                    new Tile(3207, 3471, 0)), "Guard", new HashMap<String, Integer>() {{
                put("Trout", 20);
            }}, new ArrayList<>()));

            // hill giants (plateau)
            tasks.add(new MeleeCombatTask(sk, 40, 70, new Area(
                    new Tile(3367, 3157, 0),
                    new Tile(3367, 3142, 0),
                    new Tile(3381, 3142, 0),
                    new Tile(3388, 3150, 0),
                    new Tile(3390, 3150, 0),
                    new Tile(3384, 3158, 0),
                    new Tile(3375, 3160, 0)), "Hill giant", new HashMap<String, Integer>() {{
                put("Trout", 20);
            }}, new ArrayList<String>() { { add("Giant key"); }}));

            tasks.add(new MeleeCombatTask(sk, 50, 126, new Area(
                    new Tile(3165, 9882, 0),
                    new Tile(3162, 9879, 0),
                    new Tile(3162, 9877, 0),
                    new Tile(3163, 9876, 0),
                    new Tile(3164, 9876, 0),
                    new Tile(3165, 9876, 0),
                    new Tile(3170, 9881, 0),
                    new Tile(3173, 9881, 0),
                    new Tile(3173, 9886, 0),
                    new Tile(3165, 9886, 0)), "Moss giant", new HashMap<String, Integer>() {{
                put("Lobster", 20);
            }}, new ArrayList<String>() { { add("Mossy key"); }}));

            tasks.add(new MeleeCombatTask(sk,50, 126, new Area(
                    new Tile(3154, 9908, 0),
                    new Tile(3154, 9902, 0),
                    new Tile(3156, 9902, 0),
                    new Tile(3158, 9898, 0),
                    new Tile(3159, 9898, 0),
                    new Tile(3162, 9901, 0),
                    new Tile(3163, 9901, 0),
                    new Tile(3166, 9904, 0),
                    new Tile(3165, 9906, 0),
                    new Tile(3159, 9907, 0),
                    new Tile(3158, 9909, 0),
                    new Tile(3154, 9909, 0),
                    new Tile(3156, 9900, 0),
                    new Tile(3159, 9898, 0),
                    new Tile(3156, 9900, 0)), "Moss giant", new HashMap<String, Integer>() {{
                put("Lobster", 20);
            }}, new ArrayList<String>() { { add("Mossy key"); }}));
        }

        return tasks;
    }

    private List<WatTask> createCookingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new CookingFishTask(FishType.SHRIMPS, 1, 5, 15, new HashMap<String, Integer>() {{
            put("Shrimp", -100);
        }}));

        tasks.add(new CookingFishTask(FishType.HERRING, 5, 15, 15, new HashMap<String, Integer>() {{
            put("Shrimp", -1);
            put("Raw shrimps", -1);
            put("Herring", -Calculations.random(200, 400));
        }}));

        tasks.add(new CookingFishTask(FishType.TROUT, 15, 50, 15, new HashMap<String, Integer>() {
            {
                put("Shrimp", -1);
                put("Raw shrimps", -1);
                put("Herring", -1);
                put("Raw herring", -1);
                put("Trout", -Calculations.random(200, 400));
            }
        }));

        tasks.add(new CookingFishTask(FishType.SALMON, 25, 55, 15, new HashMap<String, Integer>() {
            {
                put("Shrimp", -1);
                put("Raw shrimps", -1);
                put("Herring", -1);
                put("Raw herring", -1);
                put("Salmon", -Calculations.random(200, 400));
            }
        }));

        tasks.add(new CookingFishTask(FishType.TUNA, 35, 70, 15, new HashMap<String, Integer>() {
            {
                put("Shrimp", -1);
                put("Raw shrimps", -1);
                put("Herring", -1);
                put("Raw herring", -1);
                put("Tuna", -Calculations.random(200, 400));
            }
        }));

        tasks.add(new CookingFishTask(FishType.LOBSTER, 45, 99, 15, new HashMap<String, Integer>() {
            {
                put("Shrimp", -1);
                put("Raw shrimps", -1);
                put("Herring", -1);
                put("Raw herring", -1);
                put("Lobster", -Calculations.random(200, 400));
            }
        }));

        return tasks;
    }

    private List<WatTask> createPrayerTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new BuryBonesTask(BoneType.BIGBONES, 99,20));

        return tasks;
    }

    private List<WatTask> createFiremakingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        tasks.add(new FiremakingTask(TreeType.TREE, 1, 15, 20, new HashMap<>()));
        tasks.add(new FiremakingTask(TreeType.OAK, 15, 31, 20, new HashMap<>()));
        tasks.add(new FiremakingTask(TreeType.WILLOW, 31, 99, 20, new HashMap<>()));

        return tasks;
    }

    private List<WatTask> createCraftingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        // wool, only uses lumbridge castle at the moment
        tasks.add(new SpinningTask(CraftingType.WOOL, 1, 5, 8, new HashMap<String, Integer>() { { put("Ball of wool", -Calculations.random(200, 400)); }}));
        // jewelry
        tasks.add(new JewelryTask(CraftingType.RING, 5, 15, new HashMap<String, Integer>() { { put("Ball of wool", -1); put("Wool", -1); put("Gold ring", -Calculations.random(200, 400)); }}));
        tasks.add(new JewelryTask(CraftingType.AMULET, 15, 20, new HashMap<String, Integer>() { { put("Gold amulet (u)", -Calculations.random(200, 400)); put("Gold ring", -1); put("Wool", -1); put("Ball of wool", -1); }}));
        tasks.add(new JewelryTask(CraftingType.SAPPHIRERING, 20, 27, new HashMap<String, Integer>() { { put("Sapphire ring", -Calculations.random(200, 400)); put("Gold amulet (u)", -1); put("Gold ring", -1); put("Wool", -1); put("Ball of wool", -1); }}));
        tasks.add(new JewelryTask(CraftingType.EMERALDRING, 27, 32, new HashMap<String, Integer>() { { put("Sapphire", -1); put("Emerald ring", -200); put("Sapphire ring", -1); put("Gold amulet (u)", -1); put("Gold ring", -1); put("Wool", -1); put("Ball of wool", -1); }}));
        tasks.add(new JewelryTask(CraftingType.EMERALDNECKLACE, 29, 40, new HashMap<String, Integer>() { { put("Emerald necklace", -Calculations.random(200, 400)); put("Emerald ring", -1); put("Sapphire ring", -1); }}));
        tasks.add(new JewelryTask(CraftingType.RUBYNECKLACE, 40, 56, new HashMap<String, Integer>() { { put("Emerald", -1); put("Ruby necklace", -Calculations.random(200, 400)); put("Emerald necklace", -1); put("Gold necklace", -1); }}));
        tasks.add(new JewelryTask(CraftingType.DIAMONDNECKLACE, 56, 99, new HashMap<String, Integer>() { { put("Sapphire", -1); put("Emerald", -1); put("Ruby", -1); put("Diamond necklace", -Calculations.random(200, 400)); put("Ruby necklace", -1); put("Emerald necklace", -1); }}));

        return tasks;
    }

    private List<WatTask> createFishingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        // lumbridge
        tasks.add(new FishingTask(FishType.SHRIMPS, 1, 10, new Tile(3243, 3157), new HashMap<String, Integer>() {{ put("Raw shrimps", -Calculations.random(200, 400)); put("Raw anchovies", -Calculations.random(200, 400)); }}));
        tasks.add(new FishingTask(FishType.HERRING, 10, 30, new Tile(3243, 3157), new HashMap<String, Integer>() {{put("Raw herring", -Calculations.random(200, 400)); put("Raw sardine", -Calculations.random(200, 400)); put("Raw shrimps", -1); put("Raw anchovies", -1);}}));
        // barbarian village
        tasks.add(new FishingTask(FishType.PIKE, 30, 99, new Tile(3108, 3433), new HashMap<String, Integer>() {{ put("Raw pike", -Calculations.random(200, 400)); }}));
        tasks.add(new FishingTask(FishType.SALMON, 35, 99, new Tile(3108, 3433), new HashMap<String, Integer>() {{ put("Raw salmon", -Calculations.random(200, 400)); put("Raw trout", -Calculations.random(200, 400)); }}));

        return tasks;
    }

    private List<WatTask> createWoodcuttingTasks() {
        List<WatTask> tasks = new ArrayList<>();

        // regular logs
        tasks.add(new WoodcuttingTask(TreeType.TREE, new Tile(3275, 3443), 1, 21, new HashMap<String, Integer>() { { put("Logs", -Calculations.random(200, 400)); }}, false)); //varrock east
        tasks.add(new WoodcuttingTask(TreeType.TREE, new Tile(3160, 3455), 1, 21, new HashMap<String, Integer>() { { put("Logs", -Calculations.random(200, 400)); }}, false)); //grand exchange south wall
        tasks.add(new WoodcuttingTask(TreeType.TREE, new Tile(3194, 3248), 1, 21, new HashMap<String, Integer>() { { put("Logs", -Calculations.random(200, 400)); }}, false)); //lumbridge

        // oak logs
        tasks.add(new WoodcuttingTask(TreeType.OAK, new Tile(3277, 3429), 21, 50, new HashMap<String, Integer>() { { put("Oak logs", -Calculations.random(200, 400)); put("Logs", -1); }}, false)); //varrock east
        tasks.add(new WoodcuttingTask(TreeType.OAK, new Tile(3165, 3422), 21, 50, new HashMap<String, Integer>() { { put("Oak logs", -Calculations.random(200, 400)); put("Logs", -1); }}, false)); //varrock west
        tasks.add(new WoodcuttingTask(TreeType.OAK, new Tile(3203, 3243), 21, 50, new HashMap<String, Integer>() { { put("Oak logs", -Calculations.random(200, 400)); put("Logs", -1); }}, false)); //lumbridge

        // willow logs
        tasks.add(new WoodcuttingTask(TreeType.WILLOW, new Tile(3176, 3276), 50, 99, new HashMap<String, Integer>(){ { put("Willow logs", -2000); put("Oak logs", -1); put("Logs", -1); }}, false)); //lumbridge

        // yew logs
        tasks.add(new WoodcuttingTask(TreeType.YEW, new Tile(3088, 3475), 60, 99, new HashMap<String, Integer>() { { put("Yew logs", -Calculations.random(200, 400)); }}, false)); //edge
        tasks.add(new WoodcuttingTask(TreeType.YEW, new Tile(3206, 3502), 60, 99, new HashMap<String, Integer>() { { put("Yew logs", -Calculations.random(200, 400)); }}, false)); //g.e

        return tasks;
    }

    private List<WatTask> createSmithingTasks() {
        List<WatTask> tasks = new ArrayList<>();

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

    private List<WatTask> createMiningTasks() {
        List<WatTask> tasks = new ArrayList<>();

        // Varrock East Copper
        tasks.add(new MiningTask(1, 20, new Tile(3289, 3362), "Copper rocks",
                        new HashMap<String, Integer>() {{ put("Copper ore", -Calculations.random(800, 1200)); }}));

        // Varrock East Tin
        tasks.add(new MiningTask(1, 20, new Tile(3282, 3363), "Tin rocks",
                new HashMap<String, Integer>() {{ put("Tin ore", -Calculations.random(800, 1200)); }}));

        // Varrock East Iron
        tasks.add(new MiningTask(15, 60, new Tile(3286, 3368), "Iron rocks",
                new HashMap<String, Integer>() {{ put("Iron ore", -Calculations.random(800, 1200)); }}));

        // Mining Guild Iron
        tasks.add(new MiningTask(60, 70, new Tile(3033, 9738), "Iron rocks",
                new HashMap<String, Integer>() {{ put("Iron ore", -Calculations.random(800, 1200)); }}));

        // Mining Guild Coal
        tasks.add(new MiningTask(70, 100, new Tile(3033, 9738), "Coal rocks",
                new HashMap<String, Integer>() {{ put("Coal", -Calculations.random(800, 1200)); }}));

        return tasks;
    }

    public HashMap<Quest, WatTask> getQuests() { return questTasks; }
    public List<WatTask> getTasks() { return tasks; }
    public double getTaskSelectedAt() { return taskSelectedAt; }
    public int getTaskRunTime() { return taskRunTime; }

    public double getCheckedHoursAt() {
        return checkedHoursAt;
    }

    public void setCheckedHoursAt(double checkedHoursAt) {
        this.checkedHoursAt = checkedHoursAt;
    }
}
