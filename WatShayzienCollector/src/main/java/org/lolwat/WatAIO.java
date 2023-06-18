package org.lolwat;

import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.CameraMode;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.tasks.types.misc.MulingTask;
import org.lolwat.misc.mouse.BezierMouse;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.types.misc.HopperTask;
import org.lolwat.tasks.WatTask;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.misc.utils.SkillUtils;
import org.lolwat.tasks.types.misc.TutorialTask;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

@ScriptManifest(name = "WatAIO", description = "It is what it is, but all in one", author = "lolwat", version = 0.1, category = Category.MISC)
public class WatAIO extends AbstractScript implements ExperienceListener, ChatListener {
    private Timer timer;
    private List<WatTask> allTasks;
    private HashMap<Quest, WatTask> allQuests;
    private boolean firstStart = true;
    private Skill skillSelected;
    private long skillSelectedAt;
    private int skillRunTime;
    public WatTask currentTask;
    public boolean fatalError = false;
    public static HashMap<Skill, Integer> skillTargets;
    public static HashMap<String, Integer> levelUps;
    public Integer netWorth = 0;

    public static int MULE_SAFETY_NET = 75000;
    public static int MULE_TRIGGER = 125000;
    public boolean MULE_DEAD = false;

    public static boolean QUESTS_ENABLED = false;
    public static int QP_TRADEUNLOCKED = 10;

    private Area tutorialIsland = new Area(
            new Tile(3056, 3134, 0),
            new Tile(3055, 3053, 0),
            new Tile(3146, 3052, 0),
            new Tile(3159, 3072, 0),
            new Tile(3157, 3125, 0),
            new Tile(3126, 3142, 0));

    @Override
    public void onStart() {
        // Enable our custom mouse
        Client.getInstance().setMouseMovementAlgorithm(new BezierMouse());
        Walking.setMinimapTargetSize(15);
        Camera.setCameraMode(CameraMode.KEYBOARD_ONLY);

        Logger.log("WatAIO is starting, creating WatTask instances");

        timer = new Timer();

        getRandomManager().disableSolver(RandomEvent.DISMISS);

        skillTargets = new HashMap<Skill, Integer>(){
            {
                put(Skill.ATTACK, 99);
                put(Skill.STRENGTH, 99);
                put(Skill.DEFENCE, 99);
                put(Skill.RANGED, 99);
                //put(Skill.PRAYER, 43);
                //put(Skill.MAGIC, 99);
                //put(Skill.RUNECRAFTING, 99);
                put(Skill.COOKING, 99);
                put(Skill.WOODCUTTING, 99);
                put(Skill.FISHING, 99);
                put(Skill.FIREMAKING, 50);
                put(Skill.CRAFTING, 99);
                put(Skill.SMITHING, 50);
                put(Skill.MINING, 99);
            }};

        levelUps = new HashMap<>();

        TaskManager.setupAllTasks(this);
        allTasks = TaskManager.getAllTasks();
        allQuests = TaskManager.getQuests();
        Logger.log("Set up " + allTasks.size() + " total tasks and " + allQuests.size() + " total quests");
        netWorth = 0;
    }

    public void removeTaskAndReset() {
        if(currentTask != null) {
            allTasks.remove(currentTask);
            currentTask = null;
        }
        evaluate();
    }

    private void evaluate() {
        if(currentTask != null && currentTask instanceof MulingTask) {
            return;
        }

        if(tutorialIsland.contains(Players.getLocal())) {
            currentTask = new TutorialTask();
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(1200, 6750); // in seconds
            Logger.log("We have picked: " + currentTask.getName());
            return;
        }

        Logger.log("Assessing tasks to see what is available for us to perform");
        List<WatTask> removal = new ArrayList<>();

        if((Calculations.random(8) == 1 && Skills.getTotalLevel() >= 100) || skillTargets.size() == 0) {
            if(allQuests.size() > 0) {
                for(java.util.Map.Entry<Quest, WatTask> t : allQuests.entrySet()) {
                    if(Quests.isFinished(t.getKey())) {
                        removal.add(t.getValue());
                    }

                    if(t.getValue().canPerformTask()) {
                        currentTask = t.getValue();
                        skillSelectedAt = Instant.now().getEpochSecond();
                        skillRunTime = Calculations.random(1200, 6750); // in seconds
                        Logger.log("We have picked: " + t.getValue().getName());
                        return;
                    }
                }

                for(WatTask task : removal) {
                    allQuests.remove(task.completesQuest());
                }
            }
        }

        if(allTasks.size() > 0) {
            // let's remove skills we meet the goals of and that have no tasks
            List<Skill> toRemove = new ArrayList<>();
            for (Skill skill : skillTargets.keySet()) {
                if (Skills.getRealLevel(skill) >= skillTargets.get(skill) || TaskManager.getTasksBySkill(skill).size() == 0) {
                    toRemove.add(skill);

                    //TODO check instanceof for prayer burying task and sell the leftover bones
                }
            }

            for (Skill rem : toRemove) {
                skillTargets.remove(rem);
            }

            if(skillTargets.size() > 0) {
                long now = Instant.now().getEpochSecond();
                List<Skill> skills = new ArrayList<>(skillTargets.keySet());

                if (skillSelected == null || (now - skillSelectedAt) >= skillRunTime) {
                    skillSelected = skills.get(new Random().nextInt(skills.size()));
                    skillSelectedAt = now;
                    skillRunTime = Calculations.random(1200, 6750); // in seconds

                    if (skillRunTime < 1800) {
                        skillRunTime = 1800;
                    }
                }

                Collections.shuffle(allTasks);

                for (WatTask task : allTasks) {
                    if (task.trainsSkill().equals(skillSelected) && task.canPerformTask() &&
                            Skills.getRealLevel(skillSelected) <= task.avoidAfterLevel() && Skills.getRealLevel(skillSelected) < skillTargets.get(skillSelected)) {
                        currentTask = task;
                        Logger.log("I have selected " + currentTask.getName() + " for " + (skillRunTime / 60) + " minutes");
                        return;
                    }
                }
            } else {
                Logger.error("No tasks are available for the skills we have");
                fatalError = true;
            }
        }
        else {
            Logger.error("No tasks are available for the skills we have");
            fatalError = true;
        }
    }

    @Override
    public int onLoop() {
        if(fatalError) {
            return 1;
        }

        if(!Client.isLoggedIn()) {
            return 1;
        }

        if(firstStart) {
            Sleep.sleep(5000);
            firstStart = false;
        }

        if(currentTask == null || (skillSelectedAt > 0 && (Instant.now().getEpochSecond() - skillSelectedAt) >= skillRunTime)) {
            evaluate();
            return 1;
        }

        if(currentTask != null) {
            if(!(currentTask instanceof HopperTask) && WorldHopper.isWorldHopperOpen()) {
                WorldHopper.closeWorldHopper();
            }

            if(currentTask.completesQuest() == null) {
                if (skillSelected != null && Skills.getRealLevel(skillSelected) > currentTask.avoidAfterLevel()) {
                    Logger.log("We are now avoiding this task due to level, picking new task..");
                    evaluate();
                    return 1000;
                }

                if (skillSelected != null && Skills.getRealLevel(skillSelected) >= skillTargets.get(skillSelected)) {
                    Logger.log("We are now avoiding this task due to (target) level, picking new task..");
                    evaluate();
                    return 1000;
                }
            }
            else {
                if(Quests.isFinished(currentTask.completesQuest())) {
                    Logger.log("We are now avoiding this quest, it's completed or bugged, picking new task..");
                    evaluate();
                    return 1000;
                }
            }
        }

        // double check here
        if(currentTask != null) {
            currentTask.execute(this);
            // We have to triple check below, because sometimes we rid ourselves of the task before the loop will complete.
            return currentTask != null ? (currentTask.loopTime() > 0 ? currentTask.loopTime() : 500) : 500;
        }

        return 1000;
    }

    @Override
    public void onGained(ExperienceEvent ev) {
        if(currentTask != null) {
            currentTask.onExpGained(ev.getSkill(), ev.getChange(), this);
        }
    }

    @Override
    public void onLevelUp(ExperienceEvent ev) {
        if(levelUps.containsKey(ev.getSkill().getName())) {
            levelUps.put(ev.getSkill().getName(), levelUps.get(ev.getSkill().getName()) + 1);
        } else {
            levelUps.put(ev.getSkill().getName(), 1);
        }
    }

    @Override
    public void onPaint(Graphics g) {
        // calculate task time
        String taskTime = "";
        if (currentTask != null && skillSelectedAt > 0) {
            long currentTime = Instant.now().getEpochSecond();
            long elapsedTime = currentTime - skillSelectedAt;
            long remainingTime = skillRunTime - elapsedTime;

            if (remainingTime > 0) {
                if (remainingTime >= 120) {
                    int minutes = (int) (remainingTime / 60);
                    taskTime = minutes + " minutes";
                } else {
                    taskTime = remainingTime + " seconds";
                }
            } else {
                taskTime = "0s";
            }
        }

        // Enable antialiasing for smoother text
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Calculate the number of free-to-play skills
        int freeSkillsCount = 0;
        for (int i = 0; i < 23; i++) {
            Skill sk = Skill.forId(i);
            if(SkillUtils.isFreeToPlay(sk)) {
                freeSkillsCount++;
            }
        }

        int rowHeight = 30; // The height of each row, adjust this as needed
        int rowsCount = 3 + 2; // 3 rows of skills + 2 rows of additional text
        int boxHeight = (rowsCount * rowHeight) - 30; // remove 28px for padding

        // Draw the background
        g.setColor(new Color(42, 42, 42, 220));
        int boxWidth = 480;  // Set a fixed box width to fit 7 columns
        g.fillRect(0, 0, boxWidth, boxHeight);

        // Draw the border
        g.setColor(new Color(107, 107, 107));
        g.drawRect(0, 0, boxWidth, boxHeight);

        // Draw the title
        g.setColor(new Color(220, 220, 220));
        g.setFont(new Font("Segoe UI", Font.BOLD, 20));
        g.drawString("WatAIO", 15, 30);
        g.drawLine(15, 42, boxWidth - 15, 42);

        // Draw two rows of additional information
        g.setColor(new Color(220, 220, 220));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        String[][] additionalInfo = {{"Runtime: " + Timer.formatTime(timer.elapsed()), "Current task: " + (currentTask != null ? currentTask.getName() : "Thinking")}, {"Net worth: " + NumUtils.simplifyNumber(netWorth), "Time left: " + taskTime}};
        int rowOffset = 16;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                g.drawString(additionalInfo[row][col], 100 + col * 150, rowOffset + row * 20);
            }
        }

        // Draw the skill stats
        int yOffset = 70;
        int count = 0;  // Track number of skills drawn to properly calculate column and row
        for (int i = 0; i < freeSkillsCount; i++) {
            Skill sk = Skill.forId(i);
            if(SkillUtils.isFreeToPlay(sk)) {
                int column = count % 5;
                int row = count / 5;

                int x = 10 + column * 100; // Adjust the x-offset for 7 columns
                int y = yOffset + row * 20;

                g.drawString(sk.getName().substring(0, 3) + ": " + Skills.getRealLevel(sk) + (levelUps.containsKey(sk.getName()) ? "+" + levelUps.get(sk.getName()) : ""), x, y);
                count++;
            }
        }
    }

    @Override
    public void onMessage(Message m) {
        if(m.toString().equalsIgnoreCase("you will be logged out in approximately 10 minutes. make sure you move to a safe area or log out now.")) {
            // we should go somewhere and log out
        }
    }
}
