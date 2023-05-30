package org.lolwat;

import org.dreambot.api.Client;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.lolwat.Mouse.BezierMouse;
import org.lolwat.Tasks.Mining.GuildCoal;
import org.lolwat.Tasks.Mining.GuildIron;
import org.lolwat.Tasks.Mining.VarrockEastIron;
import org.lolwat.Tasks.WatTask;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ScriptManifest(name = "WatAIO", description = "It is what it is, but all in one", author = "lolwat", version = 0.1, category = Category.MISC, image = "")
public class WatAIO extends AbstractScript implements ExperienceListener {
    private Timer timer;
    private List<WatTask> allTasks;
    private boolean firstStart = true;
    public WatTask currentTask;
    public boolean fatalError = false;
    public static int MULE_SAFETY_NET = 25000;
    public static int MULE_TRIGGER = 125000;

    @Override
    public void onStart() {
        // Enable our custom mouse
        Client.getInstance().setMouseMovementAlgorithm(new BezierMouse());
        Walking.setMinimapTargetSize(15);

        Logger.log("WatMiner is starting, creating WatTask instances");

        allTasks = new ArrayList<>();
        Logger.log("Added " + allTasks.size() + " WatTasks");

        timer = new Timer();

        getRandomManager().disableSolver(RandomEvent.DISMISS);
    }

    private void evaluate() {
        Logger.log("Assessing tasks to see what is available for us to perform");

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

        if(currentTask == null) {
            evaluate();
            return 1;
        }

        if(WorldHopper.isWorldHopperOpen()) {
            WorldHopper.closeWorldHopper();
        }


        currentTask.execute(this);
        // We have to check below, because sometimes we rid ourselves of the task before the loop will complete.
        return currentTask != null ? (currentTask.loopTime() > 0 ? currentTask.loopTime() : 500) : 500;
    }

    @Override
    public void onGained(ExperienceEvent ev) {
        if(currentTask != null) {
            currentTask.onExpGained(ev.getSkill(), ev.getChange(), this);
        }
    }

    @Override
    public void onPaint(Graphics g) {
        if(currentTask != null) {
            // ChatGPT wrote this
            // Enable anti-aliasing for smoother text
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            // Set the font metrics for sizing the chatbox
            FontMetrics fm = g2d.getFontMetrics();

            // Determine the height of each section
            int sectionHeight = 58;

            // Draw the background
            g.setColor(new Color(42, 42, 42, 220));
            int boxWidth = Math.max(400, fm.stringWidth("WatAIO"));
            int boxHeight = 2 * sectionHeight;
            g.fillRect(0, 0, boxWidth, boxHeight);

            // Draw the border
            g.setColor(new Color(107, 107, 107));
            g.drawRect(0, 0, boxWidth, boxHeight);

            // Draw the title
            g.setColor(new Color(220, 220, 220));
            g.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g.drawString("WatAIO", 15, 30);
            g.drawLine(15, 42, boxWidth - 15, 42);

            // Draw the mining/ore stats
            g.setColor(new Color(220, 220, 220));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g.drawString("Current task: " + currentTask.getName(), 15, 70);
            g.drawString("Runtime: " + Timer.formatTime(timer.elapsed()), 15, 100);
            // Draw a horizontal line at the bottom of the box
            g.setColor(new Color(107, 107, 107));
        }
    }

    //TODO make utils for numbers
    public static String simplifyNumber(double number) {
        if (number >= 1000000) {
            return String.format("%.2fM", number / 1000000);
        } else if (number >= 1000) {
            return String.format("%.2fK", number / 1000);
        } else {
            return String.format("%.2f", number);
        }
    }
}
