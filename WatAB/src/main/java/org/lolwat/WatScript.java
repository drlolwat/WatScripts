package org.lolwat;

import lombok.Getter;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.script.listener.SpawnListener;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.ScriptManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.misc.utils.WatUtils;
import org.lolwat.tasks.misc.BreakingTask;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.Instant;
import java.util.HashMap;

@ScriptManifest(name = "WatAB Alpha",
        description = "All in one P2P account building script for OSRS",
        author = "lolwat",
        version = 0.12,
        category = Category.MISC,
        image = "https://api.botbuddy.net/WatScripts.png")
public class WatScript extends AbstractScript implements ExperienceListener, ChatListener, MouseListener, SpawnListener {
    @Getter
    private static WatScript instance;
    private static BufferedImage image;

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
            instance = this;
        }

        ScriptManager.start(profile);

        try {
            String assetsPath = System.getProperty("user.dir") + "/WatAB/assets/WatAB_Paint_withLogo_2rows.png";
            File imageFile = new File(assetsPath);
            if (imageFile.exists()) {
                image = ImageIO.read(imageFile);
            }
        } catch (Exception ignored) {

        }

        getRandomManager().disableSolver(RandomEvent.DISMISS);
    }

    @Override
    public int onLoop() {
        return ScriptManager.run();
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
        if(TaskManager.getInstance() == null
                || ConfigManager.getInstance() == null
                || image == null) {

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

        g.drawImage(image, 0, 304, null);

        Font font = new Font("Tahoma", Font.BOLD, 10);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setFont(font);
        g2d.setColor(Color.BLACK);

        // main
        g2d.drawString(String.valueOf(Quests.getQuestPoints()), 335, 361); // qp
        g2d.drawString(String.valueOf(Skills.getTotalLevel()), 235, 361); // total level
        g2d.drawString((TaskManager.getInstance().getCurrentTask() != null && TaskManager.getInstance().getCurrentTask() instanceof BreakingTask) ? "Break" : String.valueOf(Combat.getCombatLevel()), 143, 361); // combat level
        g2d.drawString(Players.getLocal().getName().isEmpty() ? "lolwat" : Players.getLocal().getName(), 35, 361);

        g2d.drawString(taskTime, 34, 382);
        g2d.drawString(TaskManager.getInstance().getCurrentTask() != null ? TaskManager.getInstance().getCurrentTask().getName() : "Task selection", 233, 382);
        g2d.drawString(TaskManager.getInstance().getCurrentTask() != null ? TaskManager.getInstance().getCurrentTask().getLocation() : "Unknown", 422, 382);

        // column 1
        drawSkill(g2d, Skill.ATTACK, String.valueOf(Skills.getRealLevel(Skill.ATTACK)), 34, 403);
        drawSkill(g2d, Skill.STRENGTH, String.valueOf(Skills.getRealLevel(Skill.STRENGTH)), 34, 424);
        drawSkill(g2d, Skill.DEFENCE, String.valueOf(Skills.getRealLevel(Skill.DEFENCE)), 34, 445);
        drawSkill(g2d, Skill.PRAYER, String.valueOf(Skills.getRealLevel(Skill.PRAYER)), 34, 466);

        // column 2
        drawSkill(g2d, Skill.HITPOINTS, String.valueOf(Skills.getRealLevel(Skill.HITPOINTS)), 119, 403);
        drawSkill(g2d, Skill.RANGED, String.valueOf(Skills.getRealLevel(Skill.RANGED)), 119, 424);
        drawSkill(g2d, Skill.MAGIC, String.valueOf(Skills.getRealLevel(Skill.MAGIC)), 119, 445);
        drawSkill(g2d, Skill.SLAYER, String.valueOf(Skills.getRealLevel(Skill.SLAYER)), 119, 466);

        // column 3
        drawSkill(g2d, Skill.AGILITY, String.valueOf(Skills.getRealLevel(Skill.AGILITY)), 204, 403);
        drawSkill(g2d, Skill.HERBLORE, String.valueOf(Skills.getRealLevel(Skill.HERBLORE)), 204, 424);
        drawSkill(g2d, Skill.THIEVING, String.valueOf(Skills.getRealLevel(Skill.THIEVING)), 204, 445);
        drawSkill(g2d, Skill.HUNTER, String.valueOf(Skills.getRealLevel(Skill.HUNTER)), 204, 466);

        // column 4
        drawSkill(g2d, Skill.MINING, String.valueOf(Skills.getRealLevel(Skill.MINING)), 288, 403);
        drawSkill(g2d, Skill.SMITHING, String.valueOf(Skills.getRealLevel(Skill.SMITHING)), 288, 424);
        drawSkill(g2d, Skill.FIREMAKING, String.valueOf(Skills.getRealLevel(Skill.FIREMAKING)), 288, 445);
        drawSkill(g2d, Skill.WOODCUTTING, String.valueOf(Skills.getRealLevel(Skill.WOODCUTTING)), 288, 466);

        // column 5
        drawSkill(g2d, Skill.FISHING, String.valueOf(Skills.getRealLevel(Skill.FISHING)), 374, 403);
        drawSkill(g2d, Skill.COOKING, String.valueOf(Skills.getRealLevel(Skill.COOKING)), 374, 424);
        drawSkill(g2d, Skill.FLETCHING, String.valueOf(Skills.getRealLevel(Skill.FLETCHING)), 374, 445);
        drawSkill(g2d, Skill.RUNECRAFTING, String.valueOf(Skills.getRealLevel(Skill.RUNECRAFTING)), 374, 466);

        // column 6
        drawSkill(g2d, Skill.CRAFTING, String.valueOf(Skills.getRealLevel(Skill.CRAFTING)), 456, 403);
        drawSkill(g2d, Skill.CONSTRUCTION, String.valueOf(Skills.getRealLevel(Skill.CONSTRUCTION)), 456, 424);
        drawSkill(g2d, Skill.FARMING, String.valueOf(Skills.getRealLevel(Skill.FARMING)), 456, 445);

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
                if (Players.all(x -> !x.equals(Players.getLocal())).size() == 1) {
                    ConfigManager.getInstance().setWaitingForResponse(true);
                    new Thread(() -> {
                        String response = WatUtils.getRealResponse(m.getUsername(), m.getMessage(), TaskManager.getInstance().getCurrentTask().getName());
                        if (!response.isEmpty()) {
                            Keyboard.type(response, true);
                        }
                        ConfigManager.getInstance().setWaitingForResponse(false);
                    }).start();
                }

            }

            TaskManager.getInstance().getCurrentTask().onMessage(m);
        }
    }

    @Override
    public void onGroundItemSpawn(GroundItem object) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onGroundItemSpawn(object);
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
