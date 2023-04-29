package org.lolwat;

import org.dreambot.api.Client;
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
import org.lolwat.Tasks.Dynamic.DynamicHopperTask;
import org.lolwat.Tasks.Mining.FaladorGuild;
import org.lolwat.Tasks.Mining.VarrockEastIron;
import org.lolwat.Tasks.WatTask;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@ScriptManifest(name = "WatMiner", description = "It is what it is", author = "lolwat", version = 2.0, category = Category.MINING, image = "")
public class WatMiner extends AbstractScript implements ExperienceListener {
    public HashMap<Integer, WatTask> tasks;
    public WatTask currentTask;
    public boolean fatalError = false;
    public Integer rocksMined = 0;
    public Integer expGained = 0;
    private Timer timer;
    private boolean firstStart = true;
    public final int MULE_TRIGGER = 100000;
    public final int MULE_SAFETY_NET = 25000;

    @Override
    public void onStart() {
        // Enable our custom mouse
        Client.getInstance().setMouseMovementAlgorithm(new BezierMouse());
        Walking.setMinimapTargetSize(15);

        Logger.log("WatMiner is starting, creating WatTask instances");

        // Self-explanatory, eventually we'll probably want to dynamically load everything in the Tasks/Building Tasks/Mining etc
        tasks = new HashMap<>();

        // The higher the integer, the more important the task. So if we
        // want to add Blast mining, then it would be 101 so they pick it first.
        // (if they have the available stats/quests, otherwise VarrockEastIron would go first)
        //tasks.Put(101, new BlastMiner());
        tasks.put(101, new FaladorGuild());
        tasks.put(100, new VarrockEastIron());

        Logger.log("Added " + tasks.size() + " WatTasks");

        timer = new Timer();

        getRandomManager().disableSolver(RandomEvent.DISMISS);
    }

    private void evaluate() {
        Logger.log("Assessing tasks to see what is available for us to perform");
        tasks.entrySet().stream()
                .sorted(Map.Entry.<Integer, WatTask>comparingByKey().reversed())
                .forEach(entry -> {
                    if(currentTask == null) {
                        WatTask task = entry.getValue();
                        if (task.canPerformTask()) {
                            currentTask = task; // Begins the task if it can be done. Maybe wait for login to do this logic
                            Logger.log("Picked a task: " + currentTask.getName());
                        } else {
                            // Else we can probably add in logic here to check if we have tasks to meet the requirements
                            // and perform those if possible (though ideally any acc we pass to this script can perform
                            // most if not all of the tasks already via another builder script or something)
                        }
                    }
                });

        if(currentTask == null) {
            Logger.error("Unable to pick a task: does not meet any requirements");
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

        if(currentTask == null) {
            evaluate();
            return 1;
        }

        if(WorldHopper.isWorldHopperOpen()) {
            WorldHopper.closeWorldHopper();
        }

        currentTask.execute(this);
        // We have to check below, because sometimes we rid ourselves of the task before the loop will complete.
        return currentTask != null ? (currentTask.loopTime() > 0 ? currentTask.loopTime() : 5) : 5;
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
            int boxWidth = Math.max(400, fm.stringWidth("WatMiner V2: " + currentTask.getName()));
            int boxHeight = 2 * sectionHeight;
            g.fillRect(0, 0, boxWidth, boxHeight);

            // Draw the border
            g.setColor(new Color(107, 107, 107));
            g.drawRect(0, 0, boxWidth, boxHeight);

            // Draw the title
            g.setColor(new Color(220, 220, 220));
            g.setFont(new Font("Segoe UI", Font.BOLD, 20));
            g.drawString("WatMiner V2: " + currentTask.getName(), 15, 30);
            g.drawLine(15, 42, boxWidth - 15, 42);

            // Draw the mining/ore stats
            g.setColor(new Color(220, 220, 220));
            g.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            g.drawString("Ores mined: " + rocksMined, 15, 70);
            g.drawString("Mining rate: " + timer.getHourlyRate(rocksMined) + "/h", boxWidth / 2, 70);
            g.drawString("Mining level: " + Skills.getRealLevel(Skill.MINING), 15, 85);
            g.drawString("Exp gained: " + expGained, boxWidth / 2, 85);

            // Draw the script statistics information
            g.drawString("Runtime: " + Timer.formatTime(timer.elapsed()), 15, 100);

            // Draw a horizontal line at the bottom of the box
            g.setColor(new Color(107, 107, 107));
        }
    }
}
