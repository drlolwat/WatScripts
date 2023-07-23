package org.lolwat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.dreambot.api.Client;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.CameraMode;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.quest.book.Quest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widget;
import org.dreambot.api.methods.widget.Widgets;
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
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.misc.utils.DialogueUtils;
import org.lolwat.tasks.types.misc.*;
import org.lolwat.misc.mouse.BezierMouse;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.WatTask;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.misc.utils.SkillUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
    private HashMap<Skill, Integer> skillTargets;
    private HashMap<String, Integer> levelUps;
    public double CHECKED_HOURS_AT = 0;
    public int HOURS_PLAYED = 0;
    public int NET_WORTH = 0;
    public double NET_WORTH_GENERATED = 0;
    public boolean MULE_DEAD = false;
    public List<String> SINGULAR_ITEMS = Arrays.asList("Hammer", "Amulet mould", "Bracelet mould", "Ring mould", "Necklace mould");
    public int TASKS_UNTIL_BREAK = 0;
    private static Area tutorialIsland = new Area(
            new Tile(3056, 3134, 0),
            new Tile(3055, 3053, 0),
            new Tile(3146, 3052, 0),
            new Tile(3159, 3072, 0),
            new Tile(3157, 3125, 0),
            new Tile(3126, 3142, 0));

    // TODO CONFIGURATION CLASS
    public boolean QUESTS_ENABLED = true; //
    public boolean TRADE_UNLOCKED = false; // false;
    public boolean ENABLE_BREAKS = true; //
    public List<String> EMERGENCY_SELL = Arrays.asList("Iron ore",
            "Coal",
            "Logs",
            "Oak logs",
            "Trout",
            "Salmon",
            "Lobster",
            "Gold ring",
            "Gold amulet (u)",
            "Sapphire ring",
            "Emerald ring",
            "Emerald amulet (u)",
            "Ruby amulet (u)",
            "Diamond amulet (u)",
            "Yew logs",
            "Tin ore",
            "Copper ore",
            "Emerald necklace",
            "Ruby necklace",
            "Diamond necklace",
            "Clay",
            "Soft clay",
            "Big bones");
    public static boolean STOP_ON_TRADEUNLOCK = true;
    public int MULE_SAFETY_NET;
    public int MULE_TRIGGER;

    private static boolean IGNORE_CHECK_TRADE;

    @Override
    public void onStart(String... params) {
        doStart(params[0]);
    }
    @Override
    public void onStart() {
        doStart("default");
    }

    private JsonObject getDefaultProfile() {
        JsonObject defaultProfile = new JsonObject();
        defaultProfile.addProperty("attack", 10);
        defaultProfile.addProperty("defence", 10);
        defaultProfile.addProperty("strength", 10);
        defaultProfile.addProperty("ranged", 10);
        defaultProfile.addProperty("prayer", 1);
        defaultProfile.addProperty("magic", 1);
        defaultProfile.addProperty("cooking", 10);
        defaultProfile.addProperty("woodcutting", 40);
        defaultProfile.addProperty("fishing", 40);
        defaultProfile.addProperty("firemaking", 10);
        defaultProfile.addProperty("crafting", 10);
        defaultProfile.addProperty("smithing", 10);
        defaultProfile.addProperty("mining", 50);

        defaultProfile.addProperty("quests_enabled", true);
        defaultProfile.addProperty("breaks_enabled", true);
        defaultProfile.addProperty("ignore_trade_restriction", false);
        defaultProfile.addProperty("mule_trigger", 125000);
        defaultProfile.addProperty("mule_safety_net", 75000);
        defaultProfile.addProperty("logout_after_unrestricted", true);

        return defaultProfile;
    }

    private void loadFromProfile(String p) {
        String filePath = System.getProperty("user.dir") + "/WatAIO/" + p + ".json";

        try {
            // Create a Gson object
            Gson gson = new Gson();

            File file = new File(filePath);
            if (!file.exists()) {
                // Create the directories if they don't exist
                file.getParentFile().mkdirs();

                JsonObject defaultProfile = getDefaultProfile();
                FileWriter fileWriter = new FileWriter(file);
                gson.toJson(defaultProfile, fileWriter);
                fileWriter.close();
            }

            // Read the JSON content from the file and parse it into a JsonObject
            JsonObject jsonObject = gson.fromJson(new FileReader(filePath), JsonObject.class);

            // Now you can access the values in the JsonObject
            int attack = jsonObject.get("attack").getAsInt();
            int defense = jsonObject.get("defence").getAsInt();
            int strength = jsonObject.get("strength").getAsInt();
            int ranged = jsonObject.get("ranged").getAsInt();
            int prayer = jsonObject.get("prayer").getAsInt();
            int magic = 0;//jsonObject.get("magic").getAsInt();
            int cooking = jsonObject.get("cooking").getAsInt();
            int woodcutting = jsonObject.get("woodcutting").getAsInt();
            int fishing = jsonObject.get("fishing").getAsInt();
            int firemaking = jsonObject.get("firemaking").getAsInt();
            int crafting = jsonObject.get("crafting").getAsInt();
            int smithing = jsonObject.get("smithing").getAsInt();
            int mining = jsonObject.get("mining").getAsInt();

            boolean questsEnabled = jsonObject.get("quests_enabled").getAsBoolean();
            boolean breaksEnabled = jsonObject.get("breaks_enabled").getAsBoolean();
            boolean ignoreTradeRest = jsonObject.get("ignore_trade_restriction").getAsBoolean();
            boolean logoutAfterUnrestricted = jsonObject.get("logout_after_unrestricted").getAsBoolean();
            int muleTrigger = jsonObject.get("mule_trigger").getAsInt();
            int muleSafety = jsonObject.get("mule_safety_net").getAsInt();

            skillTargets = new HashMap<Skill, Integer>(){
                {
                    put(Skill.ATTACK, attack);
                    put(Skill.STRENGTH, strength);
                    put(Skill.DEFENCE, defense);
                    put(Skill.RANGED, ranged);
                    put(Skill.PRAYER, prayer);
                    put(Skill.MAGIC, magic);
                    put(Skill.COOKING, cooking);
                    put(Skill.WOODCUTTING, woodcutting);
                    put(Skill.FISHING, fishing);
                    put(Skill.FIREMAKING, firemaking);
                    put(Skill.CRAFTING, crafting);
                    put(Skill.SMITHING, smithing);
                    put(Skill.MINING, mining);
                }};

            QUESTS_ENABLED = questsEnabled;
            ENABLE_BREAKS = breaksEnabled;
            IGNORE_CHECK_TRADE = ignoreTradeRest;
            MULE_TRIGGER = muleTrigger;
            MULE_SAFETY_NET = muleSafety;
            STOP_ON_TRADEUNLOCK = logoutAfterUnrestricted;

        } catch (IOException | JsonSyntaxException e) {
            e.printStackTrace();
            Logger.error("Encountered an error during setup.");
        }
    }

    private void doStart(String profile) {
        loadFromProfile(profile);
        // Enable our custom mouse
        Client.getInstance().setMouseMovementAlgorithm(new BezierMouse());
        Walking.setMinimapTargetSize(15);
        Camera.setCameraMode(CameraMode.KEYBOARD_ONLY);

        Logger.log("WatAIO is starting, creating WatTask instances");

        timer = new Timer();

        getRandomManager().disableSolver(RandomEvent.DISMISS);

        levelUps = new HashMap<>();

        TaskManager.setupAllTasks(this);
        allTasks = TaskManager.getAllTasks();
        allQuests = TaskManager.getQuests();
        Logger.log("Set up " + allTasks.size() + " total tasks and " + allQuests.size() + " total quests");
        NET_WORTH = 0;
        TASKS_UNTIL_BREAK = Calculations.random(8, 12);
    }

    private void checkTradeStatus() {
        if(IGNORE_CHECK_TRADE) {
            TRADE_UNLOCKED = true;
            return;
        }

        if(TRADE_UNLOCKED) {
            return;
        }

        if(CHECKED_HOURS_AT == 0) {
            if (!Tabs.isOpen(Tab.QUEST)) {
                Tabs.open(Tab.QUEST);
                Sleep.sleep(100, 200);
            }

            List<String> dialogue = Collections.singletonList("Yes and don't ask me again");

            Widget w = Widgets.getWidget(712);
            if (w == null || !w.isVisible()) {
                Widget x = Widgets.getWidget(629);
                if (x != null && x.isVisible() && x.getChild(3).interact()) {
                    Logger.log("Opened character area");
                    Sleep.sleep(1200, 2200);
                }
            }

            w = Widgets.getWidget(712);

            if (w == null || !w.isVisible()) {
                Logger.error("Problem getting trade unlock status");
                return;
            }

            WidgetChild c = w.getChild(2).getChild(100);
            if (c != null && c.isVisible()) {
                if (c.getText().contains("Click")) {
                    if (c.interact()) {
                        if (Dialogues.inDialogue()) {
                            DialogueUtils.solve(dialogue);
                        }
                        Sleep.sleep(800, 1500);
                    }
                }

                Widget newW = Widgets.getWidget(712);

                if (newW == null) {
                    Logger.error("really odd error");
                    return;
                }

                WidgetChild newC = newW.getChild(2).getChild(100);

                if (newC == null) {
                    Logger.error("really odd error 2");
                    return;
                }

                Sleep.sleep(400, 900);

                int minutesPlayed = 0;

                //double check
                if (Dialogues.inDialogue()) {
                    DialogueUtils.solve(dialogue);
                }
                Sleep.sleep(800, 1500);

                String[] splitArr = newC.getText().split(">", 2)[1].split("<", 2)[0].split(",");
                HashMap<String, Integer> timeWordsKey = new HashMap<>();
                timeWordsKey.put("day", 24 * 60);
                timeWordsKey.put("hour", 60);
                timeWordsKey.put("minute", 1);
                for (String time : splitArr) {
                    for (String timeWord : timeWordsKey.keySet()) {
                        if (time.contains(timeWord)) {
                            time = time.split(" " + timeWord, 2)[0];
                            if (time.charAt(0) == ' ') {
                                time = time.substring(1);
                            }
                            minutesPlayed += Integer.parseInt(time) * timeWordsKey.get(timeWord);
                        }
                    }
                }

                HOURS_PLAYED = minutesPlayed / 60;
            }

            CHECKED_HOURS_AT = Instant.now().getEpochSecond();
            Sleep.sleep(400, 700);
        } else {
            // time passed since checking
            double secondsSince = Instant.now().getEpochSecond() - CHECKED_HOURS_AT;
            int hoursSince = (int) (secondsSince / 60) / 60;

            if(hoursSince >= 1) {
                HOURS_PLAYED += hoursSince;
                CHECKED_HOURS_AT = Instant.now().getEpochSecond();
            }
        }

        Logger.log("I have played for " + HOURS_PLAYED + " hour(s)");
        if (HOURS_PLAYED >= 22 && Quests.getQuestPoints() >= 10 && Skills.getTotalLevel() >= 100) {
            TRADE_UNLOCKED = true;
        }

        if (!Tabs.isOpen(Tab.INVENTORY)) {
            Tabs.open(Tab.INVENTORY);
            Sleep.sleep(100, 200);
        }
    }

    private boolean evaluateBreak() {
        if(ENABLE_BREAKS && TASKS_UNTIL_BREAK < 0) { // -1 == finished last task in q
            Logger.log("Going on break");
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(28800, 43200);
            currentTask = new BreakingTask((Instant.now().getEpochSecond() + skillRunTime));
            return true;
        }
        return false;
    }

    private boolean evaluateGoals() {
        boolean goalsMet = true;
        for(Map.Entry<Skill, Integer> tgt : skillTargets.entrySet()) {
            if(Skills.getRealLevel(tgt.getKey()) < tgt.getValue()) {
                goalsMet = false;
            }
        }

        if(goalsMet) {
            Logger.log("WAIO: Reached target ttl and qp"); // TODO make my own, add to BB
            currentTask = new LogoutTask(true, true, null);
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(1200, 6750); // in seconds
            Logger.log("We are going to the bank to log out");
            Sleep.sleep(1000, 3000);
            return true;
        }

        return false;
    }

    private void evaluate(Skill sk) {
        if(!Client.isLoggedIn())
            return;

        if (currentTask != null && currentTask instanceof MulingTask) {
            return;
        }

        if (PlayerSettings.getConfig(281) != 1000) {
            currentTask = new TutorialTask();
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(1200, 6750); // in seconds
            Logger.log("We are performing Tutorial Island");
            return;
        }

        Logger.log("Checking current trade status");
        checkTradeStatus();

        Logger.log("Evaluating goals for stop conditions");

        if(evaluateGoals()) {
            return;
        }

        if(TRADE_UNLOCKED && STOP_ON_TRADEUNLOCK) {
            Logger.log("WAIO: Trade unrestricted, stopping");
            currentTask = new LogoutTask(true, true,null);
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(1200, 6750); // in seconds
            Logger.log("We are going to the bank to log out");
            return;
        }

        Logger.log("Evaluating for breaks");
        if (evaluateBreak()) {
            return;
        } else {
            if(ENABLE_BREAKS) {
                Logger.log("Going on break after " + TASKS_UNTIL_BREAK + " more completed task(s)");
            }
        }

        Logger.log("Assessing tasks to see what is available for us to perform");
        List<WatTask> removal = new ArrayList<>();

        if (QUESTS_ENABLED && sk == null) {
            if ((Calculations.random(8) == 1 && Skills.getTotalLevel() >= 100)) {
                if (allQuests != null && allQuests.size() > 0) {
                    for (java.util.Map.Entry<Quest, WatTask> t : allQuests.entrySet()) {
                        if (Quests.isFinished(t.getKey())) {
                            removal.add(t.getValue());
                            continue;
                        }

                        if (t.getValue().canPerformTask()) {
                            currentTask = t.getValue();
                            skillSelectedAt = Instant.now().getEpochSecond();
                            skillRunTime = Calculations.random(1200, 3750); // in seconds
                            Logger.log("We have picked: " + t.getValue().getName());
                            return;
                        }
                    }

                    for (WatTask task : removal) {
                        allQuests.remove(task.completesQuest());
                    }
                }
            }
        }

        if (allTasks != null && allTasks.size() > 0) {
            if (skillTargets.size() > 0) {
                long now = Instant.now().getEpochSecond();
                List<Skill> skills = new ArrayList<>(skillTargets.keySet());

                if(sk == null) {
                    Collections.shuffle(skills);
                    for (Skill s : skills) {
                        if (Skills.getRealLevel(s) < skillTargets.get(s)) {
                            skillSelected = s;
                            Logger.log(s.toString());
                            break;
                        } else {
                            skillSelected = null;
                        }
                    }

                    if(skillSelected != null) {
                        skillSelectedAt = now;
                        skillRunTime = Calculations.random(1200, 3750 + (60 * Skills.getRealLevel(skillSelected))); // in seconds

                        if (skillRunTime < 1800) {
                            skillRunTime = 1800;
                        }
                    }
                } else {
                    skillSelected = sk;
                }

                if(skillSelected == null) {
                    Logger.error("Problem finding a skill to select...");
                    return;
                }

                Logger.log("Finding task..");

                Collections.shuffle(allTasks);
                for (WatTask task : allTasks) {
                    if (task.trainsSkill() == skillSelected && task.canPerformTask()) {
                        if(ENABLE_BREAKS) {
                            TASKS_UNTIL_BREAK--;
                        }
                        currentTask = task;
                        Logger.log("I have selected " + currentTask.getName() + " for " + (skillRunTime / 60) + " minutes");
                        return;
                    }
                }
            } else {
                Logger.error("No skill targets are available");
                fatalError = true;
            }
        } else {
            Logger.error("No tasks are available for the skills we have");
            fatalError = true;
        }
    }

    @Override
    public int onLoop() {
        if (fatalError) {
            return 1;
        }

        if (!Client.isLoggedIn()) {
            if(currentTask == null || !(currentTask instanceof BreakingTask)) {
                if(!getRandomManager().getSolver(RandomEvent.LOGIN.toString()).isEnabled()) {
                    Logger.log("Login manager was not enabled, enabling it");
                    enableLoginManager();
                    return 3000;
                }
            }
        }

        if (firstStart) {
            Sleep.sleep(5000);
            firstStart = false;
        }

        if (currentTask != null) {
            if (!(currentTask instanceof HopperTask) && Tabs.isOpen(Tab.LOGOUT)) {
                Tabs.open(Tab.INVENTORY);
            }

            if (skillSelectedAt > 0 && (Instant.now().getEpochSecond() - skillSelectedAt) >= skillRunTime) {
                Logger.log("Picking a new task due to expiry");
                evaluate(null);
                return 1000;
            }

            if (currentTask.completesQuest() == null) {
                if (currentTask.trainsSkill() != null) {
                    if (skillSelected != null && Skills.getRealLevel(skillSelected) > currentTask.avoidAfterLevel()) {
                        Logger.log("We are now avoiding this task " + currentTask.getName() + " due to level, picking new task..");
                        evaluate(skillSelected);
                        return 1000;
                    }

                    if (skillSelected != null && Skills.getRealLevel(skillSelected) >= skillTargets.get(skillSelected)) {
                        Logger.log("We are now avoiding this task " + currentTask.getName() + " due to (target) level, picking new task..");
                        evaluate(null);
                        return 1000;
                    }
                }
            } else {
                if (Quests.isFinished(currentTask.completesQuest())) {
                    Logger.log("We are now avoiding this quest, it's completed, picking new task..");
                    evaluate(null);
                    return 1000;
                }
            }
        } else {
            Logger.log("Task was null, finding a new one...");
            evaluate(null);
            return 2500;
        }

        if(!Walking.isRunEnabled() && Walking.getRunEnergy() >= Calculations.random(75, 100)) {
            Walking.toggleRun();
            Sleep.sleep(50, 120);
        }

        // double check here
        if (currentTask != null) {
            if(!Client.isLoggedIn() && currentTask.requiresLogin()) {
                Logger.log("Waiting for login...");
                return 1000;
            }

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
        String[][] additionalInfo = {{"Runtime: " + Timer.formatTime(timer.elapsed()), "Current task: " + (currentTask != null ? currentTask.getName() : "Thinking")}, {"Net worth: " + NumUtils.simplifyNumber(NET_WORTH), "Time left: " + taskTime}};
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
        boolean tenOrThirty = Calculations.random(1, 3) == 1;
        if(m.toString().contains("You will be logged out in approximately " + (tenOrThirty ? "10" : "30") + " minutes. Make sure you move to a safe area or log out now.")) {
            if(currentTask != null) {
                currentTask = new LogoutTask(false, false, currentTask);
            }
        }
    }

    public void disableLoginManager() {
        getRandomManager().disableSolver(RandomEvent.LOGIN);
    }

    public void enableLoginManager() {
        getRandomManager().enableSolver(RandomEvent.LOGIN);
    }
}
