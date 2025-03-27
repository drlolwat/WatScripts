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
import org.dreambot.api.utilities.Logger;
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
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;

@ScriptManifest(name = "WatAIO P2P BETA", description = "All in one account building script for OSRS", author = "lolwat", version = 2.00, category = Category.MISC)
public class WatAIO extends AbstractScript implements ExperienceListener, ChatListener, MouseListener {
    @Getter
    private static WatAIO instance;
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
            Logger.log(Color.green, "WatAIO starting: assigning instance");
            instance = this;
        }

        ScriptManager.start(profile);

        try {
            image = ImageIO.read(new URL("https://api.botbuddy.net/paint.png")); //300x143
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
        g2d.drawString(WatUtils.simplifyNumber(ConfigManager.getInstance().getNetWorth()), 135, 40);
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
