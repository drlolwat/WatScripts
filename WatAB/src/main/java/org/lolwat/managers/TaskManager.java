package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.agility.AgilityCourseTask;
import org.lolwat.tasks.agility.types.Obstacle;
import org.lolwat.tasks.combat.CombatTask;
import org.lolwat.tasks.misc.BondingTask;
import org.lolwat.tasks.misc.BreakingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.LogoutTask;
import org.lolwat.tasks.quests.wrapper.QuestWrapperTask;
import org.lolwat.tasks.tutorial.TutorialIslandTask;
import org.lolwat.types.interfaces.WatTask;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class TaskManager {
    @Getter
    private List<WatTask> tasks;
    private HashMap<Skill, List<WatTask>> tasksBySkill;
    private List<Skill> skillsAvailable;
    private List<WatTask> restrictedMoneyMakingTasks;
    private List<WatTask> unrestrictedMoneyMakingTasks;

    @Getter @Setter
    private static TaskManager instance;
    @Getter
    private WatTask currentTask;
    private int tasksUntilBreak;
    @Getter
    private double taskSelectedAt;
    @Getter
    private int taskRunTime;

    public TaskManager() {
        setupAllTasks();
        resetBreaks();
        skillsAvailable.addAll(Arrays.asList(Skill.values()));
    }

    public void getNewTask() {
        getNewTask(false);
    }

    public void getNewTask(boolean noQuest) {
        if (preTaskSelection()) {
            return;
        }

        Logger.log("TaskManager: Selecting a new task..");

        DialogueUtils.wipeOptions();

        boolean quest = !noQuest
                && Calculations.random(1, ConfigManager.getInstance().getConfigBoolean("faster_quests") ? 4 : 8) == 3
                && Skills.getTotalLevel() >= ConfigManager.getInstance().getConfigInt("quest_min_ttl");

        if(!ConfigManager.getInstance().getConfigBoolean("quests_enabled"))
            quest = false;

        if (!quest) {
            Logger.log("TaskManager: Selecting skill task");
            Collections.shuffle(skillsAvailable);
            for (Skill skill : skillsAvailable) {
                if(ConfigManager.getInstance().getSkillTarget(skill) <= 1) {
                    continue;
                }

                List<WatTask> skillTasks = tasksBySkill.get(skill);
                if(isCombatSkill(skill) && ConfigManager.getInstance().getSkillTarget(skill) > Skills.getRealLevel(skill)) {
                    Logger.log("TaskManager: Selecting combat for skill: " + skill);
                    setCurrentTask(new CombatTask(skill), 0);
                    return;
                } else {
                    if (skillTasks != null && !skillTasks.isEmpty() && ConfigManager.getInstance().getSkillTarget(skill) > Skills.getRealLevel(skill)) {
                        Collections.shuffle(skillTasks);
                        for (WatTask task : skillTasks) {
                            if (ConfigManager.getInstance().getSkillTarget(task.trainsSkill()) > Skills.getRealLevel(task.trainsSkill())) {
                                if (task.canPerformTask() && Skills.getRealLevel(task.trainsSkill()) < task.avoidAfterLevel()) {
                                    Logger.log("TaskManager: Selected skilling skill: " + task.trainsSkill());
                                    Logger.log("TaskManager: Selected task: " + task.getName());
                                    setCurrentTask(task, 0);
                                    return;
                                }
                            }
                        }
                    } else {
                        Logger.log("TaskManager: No tasks available for skill: " + skill.getName());
                    }
                }
            }
        } else {
            Logger.log("TaskManager: Selected questing");
            Quest incompleteQuest = QuestManager.getInstance().getIncompleteQuest();
            if (incompleteQuest != null) {
                setCurrentTask(new QuestWrapperTask(QuestManager.getInstance().getIncompleteQuest()), Calculations.random(3600, 7200));
                Logger.log("TaskManager: Selected quest: " + getCurrentTask().questTask().completes().toString());
            } else {
                tasksUntilBreak++; // so we don't decrement it if we don't get a quest
                Logger.log("TaskManager: All available quests completed, selecting a skill-based task..");
                getNewTask(true);
            }
        }
    }

    private boolean isCombatSkill(Skill s) {
        return s.equals(Skill.ATTACK)
                || s.equals(Skill.DEFENCE)
                || s.equals(Skill.STRENGTH)
                || s.equals(Skill.MAGIC)
                || s.equals(Skill.RANGED);
    }

    public void getSpecificSkillTask(Skill sk) {
        getSpecificSkillTask(sk, 0);
    }

    public void getSpecificSkillTask(Skill sk, int gpToGenerate) {
        if(preTaskSelection()) {
            return;
        }

        if(sk.equals(Skill.HITPOINTS)) {
            Collections.shuffle(unrestrictedMoneyMakingTasks);
            for(WatTask t : unrestrictedMoneyMakingTasks) {
                if(t.canPerformTask() && (t.requiresMembers() && WatUtils.isMember() || !t.requiresMembers() && !WatUtils.isMember())) {
                    if(gpToGenerate > 0) {
                        t.data().put("gp_to_generate", gpToGenerate);
                    }

                    if(getCurrentTask() != null) {
                        t.data().put("previous_task", getCurrentTask());
                    }

                    Logger.log(Color.green, "TaskManager: Selected money making task: " + t.getName());
                    setCurrentTask(t, 0);
                    return;
                }
            }
        }
        else {
            WatUtils.shuffleHashMap(tasksBySkill);
            for(WatTask t : tasksBySkill.get(sk)) {
                if(Skills.getRealLevel(t.trainsSkill()) > t.avoidAfterLevel())
                    continue;

                if(t.avoidAfterLevel() > Skills.getRealLevel(t.trainsSkill())
                        && ConfigManager.getInstance().getSkillTarget(t.trainsSkill()) > Skills.getRealLevel(t.trainsSkill())
                        && t.canPerformTask() && (t.requiresMembers() && WatUtils.isMember() || !t.requiresMembers() && !WatUtils.isMember())) {

                    Logger.log(Color.green, "TaskManager: Selected task for skill: " + sk.getName() + " - " + t.getName());
                    setCurrentTask(t, 0);
                    return;
                }
            }
        }

        Logger.error("TaskManager: could not select a specific task for skill: " + sk.getName());
        getNewTask();
    }

    public void setCurrentTask(WatTask task) {
        setCurrentTask(task, getTaskRunTime());
    }

    public void setCurrentTask(WatTask value, int runtime) {
        currentTask = value;

        if (runtime == 0)
            taskSelectedAt = Instant.now().getEpochSecond();

        taskRunTime = runtime > 0
                ? runtime
                : Calculations.random
                (
                        ConfigManager.getInstance().getConfigInt("min_task_time") * 60,
                        ConfigManager.getInstance().getConfigInt("max_task_time") * 60
                );
    }

    public void resetBreaks() {
        tasksUntilBreak = Calculations.random(12, 20);
    }

    private boolean evaluateBreak() {
        if(ConfigManager.getInstance().getConfigBoolean("breaks_enabled") && tasksUntilBreak < 0) {
            resetBreaks();
            setCurrentTask(new BreakingTask((Instant.now().getEpochSecond() + taskRunTime)), Calculations.random(28800, 43200));
            Logger.log("TaskManager: Going on break");
            return true;
        }
        return false;
    }

    private boolean evaluateGoals() {
        boolean goalsMet = true;
        for(Skill sk : Skill.values()) {
            if(sk.equals(Skill.HITPOINTS))
                continue;

            if(Skills.getRealLevel(sk) < ConfigManager.getInstance().getSkillTarget(sk)) {
                goalsMet = false;
            }
        }

        if(goalsMet) {
            Logger.log("We are going to the bank to log out (skill goals met)");
            setCurrentTask(new LogoutTask(true, true, null), 0);
            Sleep.sleep(1000, 3000);
            return true;
        }

        if(ConfigManager.getInstance().getConfigInt("logout_after_ttl") > 0) {
            if (Client.isLoggedIn() && Skills.getTotalLevel() >= ConfigManager.getInstance().getConfigInt("logout_after_ttl")) {
                Logger.log("We are going to the bank to log out (ttl goal met)");
                setCurrentTask(new LogoutTask(true, true, null), 0);
                Sleep.sleep(1000, 3000);
                return true;
            }
        }

        return false;
    }

    private boolean preTaskSelection() {
        if(!Client.isLoggedIn()) {
            Logger.log("Awaiting login...");
            return true;
        }

        if (PlayerSettings.getConfig(281) != 1000) {
            setCurrentTask(new TutorialIslandTask(), 0);
            Logger.log("We are performing Tutorial Island");
            return true;
        }

        if (!WatUtils.isMember() && (ConfigManager.getInstance().getConfigInt("bond_min_ttl") > 0
                && Skills.getTotalLevel() >= ConfigManager.getInstance().getConfigInt("bond_min_ttl"))) {

            setCurrentTask(new BondingTask(null), 0);
            Logger.log("We are making our account a member");
            return true;
        }

        if(WatUtils.isMember() && !Worlds.getCurrent().isMembers()) {
            setCurrentTask(new HopperTask(0, (currentTask != null) ? currentTask : null), 0);
            Logger.log("We are hopping into a P2P world");
            return true;
        }

        Logger.log("Evaluating goals for stop conditions");
        if(evaluateGoals()) {
            Logger.log("Goals have been met..");
            return true;
        }

        if(ConfigManager.getInstance().getConfigBoolean("breaks_enabled")) {
            Logger.log("Evaluating for breaks");
            if (evaluateBreak()) {
                return true;
            }
            else {
                Logger.log("Going on break after " + tasksUntilBreak + " more completed task(s)");
            }
        }

        tasksUntilBreak--;
        Collections.shuffle(tasks);
        return false;
    }

    private void setupAllTasks() {
        tasks = new ArrayList<>();
        tasksBySkill = new HashMap<>();
        restrictedMoneyMakingTasks = new ArrayList<>();
        unrestrictedMoneyMakingTasks = new ArrayList<>();
        skillsAvailable = new ArrayList<>();

        // add tasks here

        for(WatTask task : tasks) {
            if(tasksBySkill.containsKey(task.trainsSkill())) {
                if(!tasksBySkill.get(task.trainsSkill()).contains(task)) {
                    tasksBySkill.get(task.trainsSkill()).add(task);
                }
            } else {
                tasksBySkill.put(task.trainsSkill(), new ArrayList<WatTask>() { { add(task); }});
            }
        }
    }

    private List<WatTask> createAgilityTasks() {
        List<WatTask> tasks = new ArrayList<>();
        tasks.add(new AgilityCourseTask("Gnome", new Area(2471, 3436, 2477, 3438), new ArrayList<Obstacle>() { {
            add(new Obstacle("Log balance", "Walk-across"));
            add(new Obstacle("Obstacle net", "Climb-over"));
            add(new Obstacle("Tree branch", "Climb"));
            add(new Obstacle("Balancing rope", "Walk-on"));
            add(new Obstacle("Tree branch", "Climb-down"));
            add(new Obstacle("Obstacle net", "Climb-over"));
            add(new Obstacle("Obstacle pipe", "Squeeze-through"));
        }}, 1, 10, false, BankLocation.GNOME_STRONGHOLD));

        tasks.add(new AgilityCourseTask("Draynor", new Area(3103, 3281, 3105, 3274), new ArrayList<Obstacle>() { {
            add(new Obstacle("Rough wall", "Climb"));
            add(new Obstacle("Tightrope", "Cross"));
            add(new Obstacle("Tightrope", "Cross"));
            add(new Obstacle("Narrow wall", "Balance"));
            add(new Obstacle("Wall", "Jump-up"));
            add(new Obstacle("Gap", "Jump"));
            add(new Obstacle("Crate", "Climb-down"));
        }}, 10, 20, false, BankLocation.DRAYNOR));

        tasks.add(new AgilityCourseTask("AlKharid", new Area(3270, 3197, 3277, 3195), new ArrayList<Obstacle>() { {
            add(new Obstacle("Rough wall", "Climb"));
            add(new Obstacle("Tightrope", "Cross"));
            add(new Obstacle("Cable", "Swing-across"));
            add(new Obstacle("Zip line", "Teeth-grip", new Tile(3297, 3164, 3).getArea(3)));
            add(new Obstacle("Tropical tree", "Swing-across"));
            add(new Obstacle("Roof top beams", "Climb"));
            add(new Obstacle("Tightrope", "Cross"));
            add(new Obstacle("Gap", "Jump"));
        }}, 20, 30, false, BankLocation.AL_KHARID));

        tasks.add(new AgilityCourseTask("Varrock", new Area(3221, 3411, 3224, 3419), new ArrayList<Obstacle>() {
            {
                add(new Obstacle("Rough wall", "Climb"));
                add(new Obstacle("Clothes line", "Cross"));
                add(new Obstacle("Gap", "Leap"));
                add(new Obstacle("Wall", "Balance"));
                add(new Obstacle("Gap", "Leap"));
                add(new Obstacle("Gap", "Leap", new Tile(3201, 3397, 3).getArea(1)));
                add(new Obstacle("Gap", "Leap", new Tile(3228, 3402, 3).getArea(1)));
                add(new Obstacle("Ledge", "Hurdle"));
                add(new Obstacle("Edge", "Jump-off"));
            }
        }, 30, 40, false, BankLocation.VARROCK_EAST));

        tasks.add(new AgilityCourseTask("Falador", new Area(3031, 3340, 3036, 3336), new ArrayList<Obstacle>() {
            {
                add(new Obstacle("Rough wall", "Climb"));
                add(new Obstacle("Tightrope", "Cross"));
                add(new Obstacle("Hand holds", "Cross"));
                add(new Obstacle("Gap", "Jump"));
                add(new Obstacle("Gap", "Jump"));
                add(new Obstacle("Tightrope", "Cross"));
                add(new Obstacle("Tightrope", "Cross"));
                add(new Obstacle("Gap", "Jump"));
                add(new Obstacle("Ledge", "Jump"));
                add(new Obstacle("Ledge", "Jump"));
                add(new Obstacle("Ledge", "Jump"));
                add(new Obstacle("Ledge", "Jump"));
                add(new Obstacle("Edge", "Jump"));
            }
        }, 50, 60, false, BankLocation.FALADOR_EAST));

        tasks.add(new AgilityCourseTask("Seers", new Area(2728, 3489, 2730, 3487), new ArrayList<Obstacle>() {
            {
                add(new Obstacle("Wall", "Climb-up"));
                add(new Obstacle("Gap", "Jump"));
                add(new Obstacle("Tightrope", "Cross"));
                add(new Obstacle("Gap", "Jump"));
                add(new Obstacle("Gap", "Jump"));
                add(new Obstacle("Edge", "Jump"));
            }
        }, 60, 80, true, BankLocation.SEERS));
        return tasks;
    }
}
