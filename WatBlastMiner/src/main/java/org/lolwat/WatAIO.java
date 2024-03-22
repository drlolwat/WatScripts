package org.lolwat;

import org.dreambot.api.Client;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.CameraMode;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.managers.ConfigManager;
import org.lolwat.misc.config.WatConfig;
import org.lolwat.misc.utils.WebUtils;
import org.lolwat.tasks.types.mining.MiningTask;
import org.lolwat.tasks.types.misc.*;
import org.lolwat.misc.mouse.BezierMouse;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.types.woodcutting.WoodcuttingTask;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@ScriptManifest(name = "WatAIO", description = "It is what it is, but all in one", author = "lolwat", version = 0.9, category = Category.MISC)
public class WatAIO extends AbstractScript implements ExperienceListener, ChatListener, MouseListener {
    // DOING NEW V1 HANDLING
    private TaskManager taskManager;
    private ConfigManager configManager;

    // TODO
    // TODO /////// --------------------- OLD SHIT WE ARE GONNA REFACTOR/GET RID OF
    private boolean firstStart = true;
    private boolean waitingForResponse = false;
    private HashMap<String, Integer> levelUps;
    public boolean muleConnectionFailed = false;
    private static BufferedImage image;
    private Map<Skill, Rectangle> invisibleButtons;

    @Override
    public void onStart(String... params) {
        if(params.length > 0) {
            doStart(params[0]);
        }
        else {
            doStart("default");
        }
    }

    @Override
    public void onStart() {
        doStart("default");
    }

    private void doStart(String profile) {
        taskManager = new TaskManager(this);
        configManager = new ConfigManager(this);


        int height = 10;
        int width = 10;
        invisibleButtons = new HashMap<>();
        invisibleButtons.put(Skill.ATTACK, new Rectangle(158, 22, width, height));
        invisibleButtons.put(Skill.STRENGTH, new Rectangle(158, 40, width, height));
        invisibleButtons.put(Skill.DEFENCE, new Rectangle(158, 56, width, height));
        invisibleButtons.put(Skill.RANGED, new Rectangle(158, 76, width, height));
        invisibleButtons.put(Skill.PRAYER, new Rectangle(158, 96, width, height));
        invisibleButtons.put(Skill.MAGIC, new Rectangle(158, 114, width, height));
        invisibleButtons.put(Skill.CRAFTING, new Rectangle(245, 23, width, height));
        invisibleButtons.put(Skill.MINING, new Rectangle(246, 40, width, height));
        invisibleButtons.put(Skill.SMITHING, new Rectangle(246, 58, width, height));
        invisibleButtons.put(Skill.FISHING, new Rectangle(246, 78, width, height));
        invisibleButtons.put(Skill.COOKING, new Rectangle(247, 96, width, height));
        invisibleButtons.put(Skill.FIREMAKING, new Rectangle(246, 113, width, height));
        invisibleButtons.put(Skill.WOODCUTTING, new Rectangle(247, 132, width, height));

        configManager.loadFromProfile(profile);
        Walking.setMinimapTargetSize(15);
        Camera.setCameraMode(CameraMode.KEYBOARD_ONLY);

        BezierMouse m = new BezierMouse();
        Mouse.setMouseAlgorithm(m);

        try {
            image = ImageIO.read(new URL("https://api.botbuddy.net/waio2.png")); //300x143
        } catch (Exception ignored) {

        }

        Logger.log(Color.green, "WatAIO is starting...");
        getRandomManager().disableSolver(RandomEvent.DISMISS);
        levelUps = new HashMap<>();

        ConfigManager.getInstance().setNetWorth(0);
        ConfigManager.getInstance().setNetWorthGeneratedAt(0);
    }

    @Override
    public int onLoop() {
        if (!Client.isLoggedIn()) {
            if (taskManager.getCurrentTask() == null || !(taskManager.getCurrentTask() instanceof BreakingTask)) {
                Logger.log("Enabling login manager");
                enableLoginManager();
                return 3000;
            }
        }

        if (firstStart) {
            Sleep.sleep(5000);
            firstStart = false;
        }

        //TODO a method for this
        if(WatConfig.getToolFailures() >= 3 && taskManager.getCurrentTask() != null) {
            if(!(taskManager.getCurrentTask() instanceof GrandExchangeTask) &&
                    !(taskManager.getCurrentTask() instanceof TraversalTask) &&
                    !(taskManager.getCurrentTask() instanceof BankingTask) &&
                    !(taskManager.getCurrentTask() instanceof MiningTask) &&
                    !(taskManager.getCurrentTask() instanceof WoodcuttingTask)) {

                Logger.log(taskManager.getCurrentTask().getName() + ": Resetting tool failures, no longer on task");
                WatConfig.resetToolFailures();
            }
        }

        if (taskManager.getCurrentTask() != null) {
            if (!(taskManager.getCurrentTask() instanceof HopperTask) && Tabs.isOpen(Tab.LOGOUT)) {
                Tabs.open(Tab.INVENTORY);
            }

            if (taskManager.getTaskSelectedAt() > 0 &&
                    (Instant.now().getEpochSecond() - taskManager.getTaskSelectedAt()) >= taskManager.getTaskRunTime()) {

                Logger.log("Picking a new task due to expiry");
                taskManager.getNewTask();
                return 1000;
            }

            if (taskManager.getCurrentTask().completesQuest() == null) {
                if (taskManager.getCurrentTask().trainsSkill() != null) {
                    if (Skills.getRealLevel(taskManager.getCurrentTask().trainsSkill()) > taskManager.getCurrentTask().avoidAfterLevel()) {
                        Logger.log("We are now avoiding this task " + taskManager.getCurrentTask().getName() + " due to level, picking new task..");
                        taskManager.getSpecificSkillTask(taskManager.getCurrentTask().trainsSkill());
                        return 1000;
                    }

                    if (Skills.getRealLevel(taskManager.getCurrentTask().trainsSkill()) >=
                            configManager.getSkillTarget(taskManager.getCurrentTask().trainsSkill())) {

                        Logger.log("We are now avoiding this task " + taskManager.getCurrentTask().getName() + " due to (target) level, picking new task..");
                        taskManager.getNewTask();
                        return 1000;
                    }
                }
            } else {
                if (Quests.isFinished(taskManager.getCurrentTask().completesQuest())) {
                    Logger.log("We are now avoiding this quest, it's completed, picking new task..");
                    taskManager.getNewTask();
                    return 1000;
                }
            }
        } else {
            Logger.log("Task was null, finding a new one...");
            taskManager.getNewTask();
            return 2500;
        }

        if(!Walking.isRunEnabled() && Walking.getRunEnergy() >= Calculations.random(75, 100)) {
            Walking.toggleRun();
            Sleep.sleep(50, 120);
        }

        // double check here
        if (taskManager.getCurrentTask() != null) {
            if(!Client.isLoggedIn() && taskManager.getCurrentTask().requiresLogin()) {
                Logger.log("Waiting for login...");
                return 1000;
            }

            taskManager.getCurrentTask().execute(this);

            //if(!isBlockedTask()) {
            //    GenericUtils.moveMouse();
            //}

            // We have to triple check below, because sometimes we rid ourselves of the task before the loop will complete.
            return taskManager.getCurrentTask() != null ? (taskManager.getCurrentTask().loopTime() > 0 ?
                    taskManager.getCurrentTask().loopTime() : 500) : 500;
        }

        return 1000;
    }

    @Override
    public void onGained(ExperienceEvent ev) {
        if(taskManager.getCurrentTask() != null) {
            taskManager.getCurrentTask().onExpGained(ev.getSkill(), ev.getChange(), this);
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
        if (taskManager.getCurrentTask() != null && taskManager.getTaskSelectedAt() > 0) {
            long currentTime = Instant.now().getEpochSecond();
            long elapsedTime = (long) (currentTime - taskManager.getTaskSelectedAt());
            long remainingTime = taskManager.getTaskRunTime() - elapsedTime;

            if (remainingTime > 0) {
                if (remainingTime >= 120) {
                    int minutes = (int) (remainingTime / 60);
                    taskTime = minutes + "m";
                } else {
                    taskTime = remainingTime + "s";
                }
            } else {
                taskTime = "0s";
            }
        }

        if(levelUps == null)
            levelUps = new HashMap<>();

        int totalLevelsGained = 0;
        for (int i : levelUps.values()) {
            totalLevelsGained += i;
        }

        g.drawImage(image, 10, 10, null);

        Font segoeUIBoldFont = new Font("Verdana", Font.BOLD, 10);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(segoeUIBoldFont);
        g2d.setColor(Color.WHITE);

        // main
        g2d.drawString(String.valueOf(Quests.getQuestPoints()), 193, 40);
        g2d.drawString(String.valueOf(Skills.getTotalLevel()), 252, 40);
        g2d.drawString(NumUtils.simplifyNumber(ConfigManager.getInstance().getNetWorth()), 135, 40);
        g2d.drawString(taskTime, 77, 40);

        if(totalLevelsGained > 0) {
            g2d.setColor(new Color(0, 200, 0));
            g2d.drawString("+" + totalLevelsGained, 277, 40);
            g2d.setColor(Color.WHITE);
        }

        // row 1
        g2d.drawString((taskManager.getCurrentTask() != null && taskManager.getCurrentTask() instanceof BreakingTask) ? "Break" : String.valueOf(Combat.getCombatLevel()), 38, 59);
        drawSkill(g2d, Skill.ATTACK, String.valueOf(Skills.getRealLevel(Skill.ATTACK)), 38, 77);
        drawSkill(g2d, Skill.STRENGTH, String.valueOf(Skills.getRealLevel(Skill.STRENGTH)), 38, 95);
        drawSkill(g2d, Skill.DEFENCE, String.valueOf(Skills.getRealLevel(Skill.DEFENCE)), 38, 113);
        drawSkill(g2d, Skill.PRAYER, String.valueOf(Skills.getRealLevel(Skill.PRAYER)), 38, 131);
        drawSkill(g2d, Skill.RANGED, String.valueOf(Skills.getRealLevel(Skill.RANGED)), 38, 149);
        drawSkill(g2d, Skill.MAGIC, String.valueOf(Skills.getRealLevel(Skill.MAGIC)), 38, 167);

        //row 2
        drawSkill(g2d, Skill.HITPOINTS, String.valueOf(Skills.getRealLevel(Skill.HITPOINTS)), 108, 59);
        drawSkill(g2d, Skill.WOODCUTTING, String.valueOf(Skills.getRealLevel(Skill.WOODCUTTING)), 108, 77);
        drawSkill(g2d, Skill.FISHING, String.valueOf(Skills.getRealLevel(Skill.FISHING)), 108, 95);
        drawSkill(g2d, Skill.MINING, String.valueOf(Skills.getRealLevel(Skill.MINING)), 108, 113);
        drawSkill(g2d, Skill.SMITHING, String.valueOf(Skills.getRealLevel(Skill.SMITHING)), 108, 131);
        drawSkill(g2d, Skill.RUNECRAFTING, String.valueOf(Skills.getRealLevel(Skill.RUNECRAFTING)), 108, 149);
        drawSkill(g2d, Skill.CRAFTING, String.valueOf(Skills.getRealLevel(Skill.CRAFTING)), 108, 167);

        //row 3
        drawSkill(g2d, Skill.COOKING, String.valueOf(Skills.getRealLevel(Skill.COOKING)), 180, 77);
        drawSkill(g2d, Skill.FIREMAKING, String.valueOf(Skills.getRealLevel(Skill.FIREMAKING)), 180, 59);

        g2d.setColor(new Color(0, 200, 0));
    }

    private void drawLevelUp(Graphics2D g2d, Skill sk, int x, int y) {
        if(levelUps.containsKey(sk.getName())) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("+" + levelUps.get(sk.getName()), x, y);
            g2d.setColor(Color.WHITE);
        }
    }

    private void drawSkill(Graphics2D g2d, Skill sk, String msg, int x, int y) {
        try {
            if(taskManager.getCurrentTask() != null) {
                if (taskManager.getCurrentTask().trainsSkill().equals(sk))
                    g2d.setColor(Color.CYAN);

                if (Skills.getRealLevel(sk) >= configManager.getSkillTarget(sk))
                    g2d.setColor(Color.GREEN);

                g2d.drawString(msg, x, y);
                g2d.setColor(Color.WHITE);
                drawLevelUp(g2d, sk, x + 15, y);
            }
        } catch (Exception ignored) { }
    }

    @Override
    public void onMessage(Message m) {
        if (taskManager.getCurrentTask() != null) {
            if(!waitingForResponse && !m.getUsername().isEmpty() && !m.getUsername().equals(Players.getLocal().getName())) {
                boolean enableGpt = false; // change at compile time
                if(enableGpt) {
                    if (Players.all(x -> !x.equals(Players.getLocal())).size() == 1) {
                        waitingForResponse = true;
                        new Thread(() -> {
                            String response = WebUtils.getRealResponse(m.getUsername(), m.getMessage(), taskManager.getCurrentTask().getName());
                            if(!response.isEmpty()) {
                                Keyboard.type(response, true);
                            }
                            waitingForResponse = false;
                        }).start();
                    }
                }
            }

            boolean tenOrThirty = Calculations.random(1, 3) == 1;
            if (m.toString().contains("approximately " + (tenOrThirty ? "10" : "30") + " minutes"))
                taskManager.setCurrentTask(new LogoutTask(false, false, taskManager.getCurrentTask()), 0);
        }
    }

    public void disableLoginManager() {
        getRandomManager().disableSolver(RandomEvent.LOGIN);
    }

    public void enableLoginManager() {
        getRandomManager().enableSolver(RandomEvent.LOGIN);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        /*
        for (Map.Entry<Skill, Rectangle> entry : invisibleButtons.entrySet()) {
            if (entry.getValue().contains(e.getX(), e.getY())) {
                runSkillFunction(entry.getKey());
                break;
            }
        }*/
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
