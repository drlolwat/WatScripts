package org.lolwat;

import org.dreambot.api.Client;
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
import org.lolwat.Tasks.Mining.MiningTask;
import org.lolwat.Tasks.Mining.VarrockEastIron;
import org.lolwat.Tasks.WatTask;
import org.lolwat.Utils.SkillUtils;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;

@ScriptManifest(name = "WatAIO", description = "It is what it is, but all in one", author = "lolwat", version = 0.1, category = Category.MISC, image = "")
public class WatAIO extends AbstractScript implements ExperienceListener {
    private Timer timer;
    private HashMap<Skill, WatTask> allTasks;
    private boolean firstStart = true;
    private Skill skillSelected;
    private long skillSelectedAt;
    private int skillRunTime;
    public WatTask currentTask;
    public boolean fatalError = false;
    public static int MULE_SAFETY_NET = 25000;
    public static int MULE_TRIGGER = 125000;
    public boolean MULE_DEAD = false;
    public static HashMap<Skill, Integer> skillTargets;

    @Override
    public void onStart() {
        // Enable our custom mouse
        Client.getInstance().setMouseMovementAlgorithm(new BezierMouse());
        Walking.setMinimapTargetSize(15);

        Logger.log("WatAIO is starting, creating WatTask instances");

        timer = new Timer();

        getRandomManager().disableSolver(RandomEvent.DISMISS);

        skillTargets = new HashMap<Skill, Integer>(){
            {
                //put(Skill.ATTACK, 99);
                //put(Skill.STRENGTH, 99);
                //put(Skill.DEFENCE, 99);
                //put(Skill.RANGED, 99);
                //put(Skill.PRAYER, 99);
                //put(Skill.MAGIC, 99);
                //put(Skill.RUNECRAFTING, 99);
                //put(Skill.HITPOINTS, 99);
                //put(Skill.COOKING, 99);
                //put(Skill.WOODCUTTING, 99);
                //put(Skill.FISHING, 99);
                //put(Skill.FIREMAKING, 99);
                //put(Skill.CRAFTING, 99);
                //put(Skill.SMITHING, 99);
                put(Skill.MINING, 99);
            }};

        //TODO TaskManager
        List<List<Tile>> varrockEastRockList = new ArrayList<List<Tile>>() {
            {
                // first entry is always the primary list
                add(new ArrayList<Tile>() { {
                    add(new Tile(3286, 3369));
                    add(new Tile(3285, 3368));
                }});

                // can add unlimited additional arrays of tiles for the bot to use
                // or one huge array, or we can pass just a rocks name like "Coal rocks" instead of arrays and it will
                // wander around and grab them all
                add(new ArrayList<Tile>() { {
                    add(new Tile(3288, 3370));
                    add(new Tile(3285, 3369));
                }});
            }
        };

        // like WatMiner. -1000 means check for 1000 and sell it all. 1000 would mean check for 1000 and sell only 1000
        HashMap<String, Integer> varrockEastToSell = new HashMap<String, Integer>() { {
            put("Iron ore", -1000);
        }};

        allTasks = new HashMap<>();
        allTasks.put(Skill.MINING, new MiningTask(15, new Tile(3286, 3368), varrockEastRockList, varrockEastToSell, this));
    }

    private void evaluate() {
        Logger.log("Assessing tasks to see what is available for us to perform");

        // lets remove skills we meet the goals of.
        List<Skill> toRemove = new ArrayList<>();
        for(Skill skill : skillTargets.keySet()) {
            if(Skills.getRealLevel(skill) >= skillTargets.get(skill))
                toRemove.add(skill);
        }

        for(Skill rem : toRemove) {
            skillTargets.remove(rem);
        }

        long now = Instant.now().getEpochSecond();
        List<Skill> skills = new ArrayList<>(skillTargets.keySet());

        if(skillSelected == null || (now - skillSelectedAt) >= skillRunTime) {
            skillSelected = skills.get(new Random().nextInt(skills.size()));
            skillSelectedAt = now;
            skillRunTime = 2700; // 45 mins for now.
        }

        for(Map.Entry<Skill, WatTask> map : allTasks.entrySet()) {
            if(map.getKey().equals(skillSelected) && map.getValue().canPerformTask()) {
                // maybe add a flag to avoid the task if it's below us.
                currentTask = map.getValue();
                return;
            }
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
        // Enable anti-aliasing for smoother text
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Set the font metrics for sizing the chatbox
        FontMetrics fm = g2d.getFontMetrics();

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
        int boxHeight = (rowsCount * rowHeight) - 38; // remove 28px for padding

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
        String[][] additionalInfo = {{"Runtime: " + Timer.formatTime(timer.elapsed()), "Current task: " + (currentTask != null ? currentTask.getName() : "Thinking")}, {"Placeholder3", "Placeholder4"}};
        int rowOffset = 16;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                g.drawString(additionalInfo[row][col], 170 + col * 150, rowOffset + row * 20);
            }
        }

        // Draw the skill stats
        int yOffset = 70;
        int count = 0;  // Track number of skills drawn to properly calculate column and row
        for (int i = 0; i < freeSkillsCount; i++) {
            Skill sk = Skill.forId(i);
            if(SkillUtils.isFreeToPlay(sk)) {
                int column = count % 7;
                int row = count / 7;

                int x = 15 + column * 65; // Adjust the x-offset for 7 columns
                int y = yOffset + row * 20;

                g.drawString(sk.getName().substring(0, 3) + ": " + Skills.getRealLevel(sk), x, y);
                count++;
            }
        }
    }
}
