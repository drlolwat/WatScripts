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
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebFinder;
import org.dreambot.api.methods.walking.web.node.impl.teleports.MagicTeleport;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.QuestManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.TeleportManager;
import org.lolwat.managers.types.WatConfig;
import org.lolwat.misc.mouse.HumanMouse;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.misc.utils.WebUtils;
import org.lolwat.tasks.mining.MiningTask;
import org.lolwat.tasks.misc.*;
import org.lolwat.tasks.woodcutting.WoodcuttingTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;

@ScriptManifest(name = "WatAIO", description = "All in one Account Building script for Old School RuneScape", author = "lolwat", version = 1.1, category = Category.MISC)
public class WatAIO extends AbstractScript implements ExperienceListener, ChatListener, MouseListener {
    private static BufferedImage image;
    private static WatAIO instance;
    public static WatAIO getInstance() {
        return instance;
    }

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
        if(instance == null) {
            Logger.log(Color.green, "WatAIO starting: assigning instance");
            instance = this;
        }

        if (ConfigManager.getInstance() == null) {
            Logger.log("Constructing ConfigManager singleton.");
            ConfigManager.setInstance(new ConfigManager());
            ConfigManager.getInstance().setLevelUps(new HashMap<>());
            ConfigManager.getInstance().setNetWorth(0);
            ConfigManager.getInstance().setNetWorthGeneratedAt(0);
            ConfigManager.getInstance().loadFromProfile(profile);
        }

        if(TeleportManager.getInstance() == null) {
            Logger.log("Constructing TeleportManager singleton.");
            TeleportManager.setInstance(new TeleportManager());
        }

        if(QuestManager.getInstance() == null) {
            Logger.log("Constructing QuestManager singleton.");
            QuestManager.setInstance(new QuestManager());
        }

        Walking.setMinimapTargetSize(15);
        Camera.setCameraMode(CameraMode.MOUSE_ONLY);
        HumanMouse m = new HumanMouse();
        Mouse.setMouseAlgorithm(m);

        if (TaskManager.getInstance() == null) {
            Logger.log("Constructing TaskManager singleton.");
            TaskManager.setInstance(new TaskManager());
        }

        try {
            image = ImageIO.read(new URL("https://api.botbuddy.net/paint.png")); //300x143
        } catch (Exception ignored) {

        }

        getRandomManager().disableSolver(RandomEvent.DISMISS);
        if(!Menu.isMenuManipulationActive()) {
            Logger.log("Enabling menu manipulation and noclick walk");
            Menu.toggleMenuManipulation(true);
            Walking.toggleNoClickWalk(true);
        }

        WebFinder.getWebFinder().disableEquipmentTeleports();
        WebFinder.getWebFinder().disableEquippingTeleports();
        WebFinder.getWebFinder().disableInventoryTeleports();
        WebFinder.getWebFinder().disableTeleport(MagicTeleport.LUMBRIDGE_HOME_TELEPORT);
    }

    @Override
    public int onLoop() {
        if (!Client.isLoggedIn()) {
            if (TaskManager.getInstance().getCurrentTask() == null && !(TaskManager.getInstance().getCurrentTask() instanceof BreakingTask)) {
                TaskManager.getInstance().setCurrentTask(null);
                Logger.log("Enabling login manager");
                enableLoginManager();
                return 3000;
            }
        }

        if (ConfigManager.getInstance().isFirstStart()) {
            Sleep.sleep(5000);
            ConfigManager.getInstance().setFirstStart(false);
        }

        if (WatConfig.getToolFailures() >= 3 && TaskManager.getInstance().getCurrentTask() != null) {
            if (!(TaskManager.getInstance().getCurrentTask() instanceof GrandExchangeTask) &&
                    !(TaskManager.getInstance().getCurrentTask() instanceof TraversalTask) &&
                    !(TaskManager.getInstance().getCurrentTask() instanceof BankingTask) &&
                    !(TaskManager.getInstance().getCurrentTask() instanceof MiningTask) &&
                    !(TaskManager.getInstance().getCurrentTask() instanceof WoodcuttingTask)) {

                Logger.log(TaskManager.getInstance().getCurrentTask().getName() + ": Resetting tool failures, no longer on task");
                WatConfig.resetToolFailures();
            }
        }

        if (TaskManager.getInstance().getCurrentTask() != null) {
            if (!(TaskManager.getInstance().getCurrentTask() instanceof HopperTask) && Tabs.isOpen(Tab.LOGOUT)) {
                Tabs.open(Tab.INVENTORY);
            }

            if (TaskManager.getInstance().getTaskSelectedAt() > 0 &&
                    (Instant.now().getEpochSecond() - TaskManager.getInstance().getTaskSelectedAt()) >= TaskManager.getInstance().getTaskRunTime()) {

                Logger.log("Picking a new task due to expiry");
                TaskManager.getInstance().getNewTask();
                return 1000;
            }

            if (TaskManager.getInstance().getCurrentTask().questTask() == null) {
                if (TaskManager.getInstance().getCurrentTask().trainsSkill() != null) {
                    if (Skills.getRealLevel(TaskManager.getInstance().getCurrentTask().trainsSkill()) > TaskManager.getInstance().getCurrentTask().avoidAfterLevel()) {
                        Logger.log("We are now avoiding this task " + TaskManager.getInstance().getCurrentTask().getName() + " due to (task) level, picking new task..");
                        TaskManager.getInstance().getSpecificSkillTask(TaskManager.getInstance().getCurrentTask().trainsSkill());
                        return 1000;
                    }

                    if (Skills.getRealLevel(TaskManager.getInstance().getCurrentTask().trainsSkill()) >=
                            ConfigManager.getInstance().getSkillTarget(TaskManager.getInstance().getCurrentTask().trainsSkill())) {

                        if(!TaskManager.getInstance().getCurrentTask().data().containsKey("gp_to_generate")) {
                            Logger.log("We are now avoiding this task " + TaskManager.getInstance().getCurrentTask().getName() + " due to (target) level, picking new task..");
                            TaskManager.getInstance().getNewTask();
                            return 1000;
                        }
                    }
                }
            } else {
                if(Quests.isFinished(TaskManager.getInstance().getCurrentTask().questTask().completes())) {
                    Logger.log("We are now avoiding this quest, it's completed, picking new task..");
                    TaskManager.getInstance().getNewTask();
                    return 1000;
                }
            }
        } else {
            Logger.log("Task was null, finding a new one...");
            TaskManager.getInstance().getNewTask();
            return 2500;
        }

        if (!Walking.isRunEnabled() && Walking.getRunEnergy() >= Calculations.random(75, 100)) {
            Walking.toggleRun();
            Sleep.sleep(50, 120);
        }

        // double check here
        if (TaskManager.getInstance().getCurrentTask() != null) {
            if (!Client.isLoggedIn() && TaskManager.getInstance().getCurrentTask().requiresLogin()) {
                Logger.log("Waiting for login...");
                return 1000;
            }

            TaskManager.getInstance().getCurrentTask().execute();

            return TaskManager.getInstance().getCurrentTask() != null ? (TaskManager.getInstance().getCurrentTask().loopTime() > 0 ?
                    TaskManager.getInstance().getCurrentTask().loopTime() : 300) : 300;
        }

        return 100;
    }

    @Override
    public void onGained(ExperienceEvent ev) {
        if(TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onExpGained(ev.getSkill(), ev.getChange(), this);
        }
    }

    @Override
    public void onLevelUp(ExperienceEvent ev) {
        if(ConfigManager.getInstance().getLevelUps().containsKey(ev.getSkill().getName())) {
            ConfigManager.getInstance().getLevelUps().put(ev.getSkill().getName(),
                    ConfigManager.getInstance().getLevelUps().get(ev.getSkill().getName()) + 1);
        } else {
            ConfigManager.getInstance().getLevelUps().put(ev.getSkill().getName(), 1);
        }
    }

    @Override
    public void onPaint(Graphics g) {
        if(TaskManager.getInstance() == null || ConfigManager.getInstance() == null) {
            return;
        }

        // calculate task time
        String taskTime = "";
        if (TaskManager.getInstance().getCurrentTask() != null && TaskManager.getInstance().getTaskSelectedAt() > 0) {
            long currentTime = Instant.now().getEpochSecond();
            long elapsedTime = (long) (currentTime - TaskManager.getInstance().getTaskSelectedAt());
            long remainingTime = TaskManager.getInstance().getTaskRunTime() - elapsedTime;

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

        if(ConfigManager.getInstance().getLevelUps() == null)
            ConfigManager.getInstance().setLevelUps(new HashMap<>());

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

        // row 1
        g2d.drawString((TaskManager.getInstance().getCurrentTask() != null && TaskManager.getInstance().getCurrentTask() instanceof BreakingTask) ? "Break" : String.valueOf(Combat.getCombatLevel()), 38, 59);
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
        if(ConfigManager.getInstance().getLevelUps().containsKey(sk.getName())) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("+" + ConfigManager.getInstance().getLevelUps().get(sk.getName()), x, y);
            g2d.setColor(Color.WHITE);
        }
    }

    private void drawSkill(Graphics2D g2d, Skill sk, String msg, int x, int y) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            if (TaskManager.getInstance().getCurrentTask().trainsSkill().equals(sk))
                g2d.setColor(Color.CYAN);

            if (Skills.getRealLevel(sk) >= ConfigManager.getInstance().getSkillTarget(sk))
                g2d.setColor(Color.GREEN);

            g2d.drawString(msg, x, y);
            g2d.setColor(Color.WHITE);
            drawLevelUp(g2d, sk, x + 15, y);
        }
    }

    @Override
    public void onMessage(Message m) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            if (!ConfigManager.getInstance().isWaitingForResponse() && !m.getUsername().isEmpty() && !m.getUsername().equals(Players.getLocal().getName())) {
                boolean enableGpt = false; // change at compile time
                if (enableGpt) {
                    if (Players.all(x -> !x.equals(Players.getLocal())).size() == 1) {
                        ConfigManager.getInstance().setWaitingForResponse(true);
                        new Thread(() -> {
                            String response = WebUtils.getRealResponse(m.getUsername(), m.getMessage(), TaskManager.getInstance().getCurrentTask().getName());
                            if (!response.isEmpty()) {
                                Keyboard.type(response, true);
                            }
                            ConfigManager.getInstance().setWaitingForResponse(false);
                        }).start();
                    }
                }
            }

            TaskManager.getInstance().getCurrentTask().onMessage(m);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
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

    public void disableLoginManager() {
        getRandomManager().disableSolver(RandomEvent.LOGIN);
    }

    public void enableLoginManager() {
        getRandomManager().enableSolver(RandomEvent.LOGIN);
    }
}
