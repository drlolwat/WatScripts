package org.lolwat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.dreambot.api.Client;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.combat.Combat;
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
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.misc.config.WatConfig;
import org.lolwat.misc.utils.WebUtils;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.types.mining.MiningTask;
import org.lolwat.tasks.types.misc.*;
import org.lolwat.misc.mouse.BezierMouse;
import org.lolwat.managers.TaskManager;
import org.lolwat.tasks.WatTask;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.types.tutorial.*;
import org.lolwat.tasks.types.woodcutting.WoodcuttingTask;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.List;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

@ScriptManifest(name = "Watvertiser", description = "It is what it is, but all in one", author = "lolwat", version = 0.9, category = Category.MISC)
public class WatAIO extends AbstractScript implements ExperienceListener, ChatListener, MouseListener {
    private Timer timer;
    private List<WatTask> allTasks;
    private HashMap<Quest, WatTask> allQuests;
    private boolean firstStart = true;
    private Skill skillSelected;
    private long skillSelectedAt;
    private int skillRunTime;

    public WatTask currentTask;
    public boolean fatalError = false;
    private boolean waitingForResponse = false;
    private HashMap<Skill, Integer> skillTargets;
    private HashMap<String, Integer> levelUps;
    public double CHECKED_HOURS_AT = 0;
    public int NET_WORTH = 0;
    public double NET_WORTH_GENERATED = 0;
    public boolean MULE_DEAD = false;
    public List<String> SINGULAR_ITEMS = Arrays.asList("Hammer", "Amulet mould", "Bracelet mould", "Ring mould", "Necklace mould");
    public int TASKS_UNTIL_BREAK = 0;
    public static boolean NEED_MM;
    // TODO CONFIGURATION CLASS
    public static String CAPE_TYPE;
    public static boolean USE_SKIRT;
    public static int MOUSE_DIFF = 1;
    public boolean PROFILE_LOADED = false;
    public static boolean QUESTS_ENABLED = true; //
    public static boolean TRADE_UNLOCKED = false; // false;
    public boolean ENABLE_BREAKS = true; //
    public List<String> EMERGENCY_SELL = Arrays.asList("Adamant arrow",
            "Adamant axe",
            "Adamant full helm",
            "Adamant kiteshield",
            "Adamant pickaxe",
            "Adamant platebody",
            "Adamant platelegs",
            "Adamant plateskirt",
            "Adamant scimitar",
            "Air rune",
            "Amulet of strength",
            "Black ore",
            "Black arrow",
            "Black full helm",
            "Black kiteshield",
            "Black pickaxe",
            "Black platebody",
            "Black platelegs",
            "Black plateskirt",
            "Black scimitar",
            "Blue wizard robe",
            "Big bones",
            "Body rune",
            "Bronze bar",
            "Chaos rune",
            "Clay",
            "Coal",
            "Copper ore",
            "Cowhide",
            "Death rune",
            "Diamond",
            "Diamond amulet (u)",
            "Diamond necklace",
            "Earth rune",
            "Emerald",
            "Emerald amulet (u)",
            "Emerald necklace",
            "Emerald ring",
            "Fire rune",
            "Gold amulet (u)",
            "Gold bar",
            "Gold necklace",
            "Gold ring",
            "Green d'hide chaps",
            "Green d'hide vambraces",
            "Herring",
            "Iron ore",
            "Iron arrow",
            "Iron full helm",
            "Iron kiteshield",
            "Iron pickaxe",
            "Iron platebody",
            "Iron platelegs",
            "Iron plateskirt",
            "Steel scimitar",
            "Leather",
            "Lobster",
            "Logs",
            "Maple shortbow",
            "Mind rune",
            "Mithril arrow",
            "Mithril axe",
            "Mithril bar",
            "Mithril full helm",
            "Mithril kiteshield",
            "Mithril pickaxe",
            "Mithril platebody",
            "Mithril platelegs",
            "Mithril plateskirt",
            "Mithril scimitar",
            "Nature rune",
            "Oak logs",
            "Oak shortbow",
            "Pike",
            "Raw herring",
            "Raw lobster",
            "Raw pike",
            "Raw salmon",
            "Raw sardine",
            "Raw shrimps",
            "Raw trout",
            "Raw tuna",
            "Ruby",
            "Ruby amulet (u)",
            "Ruby necklace",
            "Rune arrow",
            "Rune axe",
            "Rune chainbody",
            "Rune full helm",
            "Rune kiteshield",
            "Rune pickaxe",
            "Rune platelegs",
            "Rune plateskirt",
            "Rune scimitar",
            "Salmon",
            "Sapphire",
            "Sapphire ring",
            "Sardine",
            "Shrimps",
            "Soft clay",
            "Staff of air",
            "Staff of fire",
            "Steel arrow",
            "Steel bar",
            "Steel full helm",
            "Steel kiteshield",
            "Steel pickaxe",
            "Steel platebody",
            "Steel platelegs",
            "Steel plateskirt",
            "Steel scimitar",
            "Tin ore",
            "Trout",
            "Tuna",
            "Uncut diamond",
            "Uncut emerald",
            "Uncut ruby",
            "Uncut sapphire",
            "Water rune",
            "Willow logs",
            "Willow shortbow",
            "Yew logs",
            "Zamarok monk bottom");

    public static boolean STOP_ON_TRADEUNLOCK = true;
    public int MULE_SAFETY_NET;
    public int MULE_TRIGGER;
    private static boolean IGNORE_CHECK_TRADE;
    public static boolean RUNNING_TUT = false;
    private static BufferedImage image;
    private Map<Skill, Rectangle> invisibleButtons;
    private static int QUEST_MIN_TTL = 150;
    private static int BOND_MIN_TTL = 500;
    private static boolean ENABLE_GPT = false;
    public static boolean WEAR_CAPES = true;
    private static boolean FASTER_QUESTS = false;
    private static boolean ADVERTISE_MODE = true;

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

    private JsonObject getDefaultProfile() {
        JsonObject defaultProfile = new JsonObject();
        defaultProfile.addProperty("attack", 99);
        defaultProfile.addProperty("defence", 99);
        defaultProfile.addProperty("strength", 99);
        defaultProfile.addProperty("ranged", 99);
        defaultProfile.addProperty("prayer", 1);
        defaultProfile.addProperty("magic", 99);
        defaultProfile.addProperty("cooking", 99);
        defaultProfile.addProperty("woodcutting", 99);
        defaultProfile.addProperty("fishing", 99);
        defaultProfile.addProperty("firemaking", 1);
        defaultProfile.addProperty("crafting", 10);
        defaultProfile.addProperty("smithing", 10);
        defaultProfile.addProperty("mining", 99);

        defaultProfile.addProperty("quests_enabled", true);
        defaultProfile.addProperty("breaks_enabled", true);
        defaultProfile.addProperty("ignore_trade_restriction", false);
        defaultProfile.addProperty("mule_trigger", 125000);
        defaultProfile.addProperty("mule_safety_net", 75000);
        defaultProfile.addProperty("logout_after_unrestricted", false);
        defaultProfile.addProperty("disable_mule", false);
        defaultProfile.addProperty("quest_min_ttl", 175);
        defaultProfile.addProperty("bond_min_ttl", 0);
        defaultProfile.addProperty("use_profile_cape", false);
        defaultProfile.addProperty("faster_quests", false);

        return defaultProfile;
    }

    private void loadFromProfile(String p) {
        String filePath = System.getProperty("user.dir") + "/WatAIO/" + p + ".json";

        try {
            Gson gson = new Gson();

            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();

                JsonObject defaultProfile = getDefaultProfile();
                FileWriter fileWriter = new FileWriter(file);
                gson.toJson(defaultProfile, fileWriter);
                fileWriter.close();
            }

            JsonObject jsonObject = gson.fromJson(new FileReader(filePath), JsonObject.class);
            JsonObject defaultProfile = getDefaultProfile();

            for (String key : defaultProfile.keySet()) {
                if (!jsonObject.has(key)) {
                    jsonObject.add(key, defaultProfile.get(key));
                }
            }

            FileWriter fileWriter = new FileWriter(file);
            gson.toJson(jsonObject, fileWriter);
            fileWriter.close();

            int attack = jsonObject.get("attack").getAsInt();
            int defense = jsonObject.get("defence").getAsInt();
            int strength = jsonObject.get("strength").getAsInt();
            int ranged = jsonObject.get("ranged").getAsInt();
            int prayer = jsonObject.get("prayer").getAsInt();
            int magic = jsonObject.get("magic").getAsInt();
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
            boolean muleDisabled = jsonObject.get("disable_mule").getAsBoolean();
            int questMinTtl = jsonObject.get("quest_min_ttl").getAsInt();
            int bondMinTtl = jsonObject.get("bond_min_ttl").getAsInt();
            boolean useProfileCape = jsonObject.get("use_profile_cape").getAsBoolean();
            boolean fasterQuests = jsonObject.get("faster_quests").getAsBoolean();

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
                    put(Skill.HITPOINTS, 100);
                }};

            QUESTS_ENABLED = questsEnabled;
            ENABLE_BREAKS = breaksEnabled;
            IGNORE_CHECK_TRADE = ignoreTradeRest;
            MULE_TRIGGER = muleTrigger;
            MULE_SAFETY_NET = muleSafety;
            STOP_ON_TRADEUNLOCK = logoutAfterUnrestricted;
            MULE_DEAD = muleDisabled;
            NEED_MM = false;
            QUEST_MIN_TTL = questMinTtl;
            BOND_MIN_TTL = bondMinTtl;
            WEAR_CAPES = useProfileCape;
            FASTER_QUESTS = fasterQuests;

        } catch (IOException | JsonSyntaxException ignored) {
            Logger.error("Encountered an error during setup");
        }
    }

    private void getWsProfile(int breaking) {
        try {
            String urlString = "https://api.botbuddy.net/ws_profile.php?fu=" + Client.getForumUser().getUsername() + "&_hash=" + AccountManager.getAccountHash();

            if(IGNORE_CHECK_TRADE) {
                urlString += "&_unl";
            }

            if(breaking > 0) {
                urlString += "&breakTime=" + breaking;
            }

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                StringBuilder response = new StringBuilder();

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                connection.disconnect();

                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(response.toString(), JsonObject.class);

                double appearedAt = jsonObject.get("appeared_at").getAsDouble();
                String capeType = jsonObject.get("cape_type").getAsString();
                boolean useSkirt = Objects.equals(jsonObject.get("use_plateskirt").getAsString(), "1");
                int mouseDiff = jsonObject.get("mouse_diff").getAsInt();

                TRADE_UNLOCKED = IGNORE_CHECK_TRADE ||
                        (Instant.now().getEpochSecond() - appearedAt >= 75600) && Quests.getQuestPoints() >= 10;

                CAPE_TYPE = capeType;
                USE_SKIRT = useSkirt;
                MOUSE_DIFF = mouseDiff;
                PROFILE_LOADED = true;
                CHECKED_HOURS_AT = Instant.now().getEpochSecond();

                Logger.log(Color.green, (breaking > 0) ? "Updated account hivetime due to break" :"Loaded unique account profile from BotBuddy Hive");

            } else {
                Logger.error("HTTP request failed with response code: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException ignored) {
            ScriptManager.getScriptManager().stop();
        }
    }

    private void doStart(String profile) {
        WebUtils.postWebhook("WatAIO Started", Client.getForumUser().getUsername() + " has started the script with a profile named " + profile);

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

        loadFromProfile(profile);
        Walking.setMinimapTargetSize(15);
        Camera.setCameraMode(CameraMode.KEYBOARD_ONLY);

        BezierMouse m = new BezierMouse();
        Mouse.setMouseAlgorithm(m);

        try {
            image = ImageIO.read(new URL("https://api.botbuddy.net/waio2.png")); //300x143
        } catch (Exception ignored) {

        }

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

    private boolean evaluateBreak() {
        if(ENABLE_BREAKS && TASKS_UNTIL_BREAK < 0) { // -1 == finished last task in q
            Logger.log("Going on break");
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(28800, 43200);
            TASKS_UNTIL_BREAK = Calculations.random(8, 12);
            currentTask = new BreakingTask((Instant.now().getEpochSecond() + skillRunTime));
            getWsProfile(skillRunTime);
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
            Logger.log("WAIO: Reached target ttl and qp");
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
        if(!PROFILE_LOADED || (CHECKED_HOURS_AT == 0 || (Instant.now().getEpochSecond() - CHECKED_HOURS_AT) >= 3600)) {
            disableLoginManager();
            getWsProfile(0);
            enableLoginManager();
        }

        if(!Client.isLoggedIn()) {
            Logger.log("Awaiting login...");
            enableLoginManager();
            return;
        }

        if (PlayerSettings.getConfig(281) != 1000) {
            currentTask = new SelectUsernameTask();
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(1200, 6750); // in seconds
            RUNNING_TUT = true;
            Logger.log("We are performing Tutorial Island");
            return;
        }

        if(ADVERTISE_MODE) {
            currentTask = new AdvertiseTask();
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(1200, 6750);
            return;
        }

        if (!GenericUtils.isMember() && (BOND_MIN_TTL > 0 && Skills.getTotalLevel() >= BOND_MIN_TTL)) {
            currentTask = new BondingTask(null);
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(1200, 6750); // in seconds
            Logger.log("We are making our account a member");
            return;
        }

        if(GenericUtils.isMember() && !Worlds.getCurrent().isMembers()) {
            currentTask = new HopperTask(0, (currentTask != null) ? currentTask : null);
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(1200, 6750); // in seconds
            Logger.log("We are hopping into a P2P world");
            return;
        }

        Logger.log("Evaluating goals for stop conditions");
        if(evaluateGoals()) {
            return;
        }

        if(TRADE_UNLOCKED && STOP_ON_TRADEUNLOCK) {
            currentTask = new LogoutTask(true, true,null);
            skillSelectedAt = Instant.now().getEpochSecond();
            skillRunTime = Calculations.random(1200, 6750); // in seconds
            Logger.log("We are going to the bank to log out");
            return;
        }

        if(ENABLE_BREAKS) {
            Logger.log("Evaluating for breaks");
            if (evaluateBreak()) {
                return;
            } else {
                Logger.log("Going on break after " + TASKS_UNTIL_BREAK + " more completed task(s)");
            }
        }

        Logger.log("Assessing tasks to see what is available for us to perform");
        List<WatTask> removal = new ArrayList<>();

        if (QUESTS_ENABLED && sk == null) {
            if ((Calculations.random(FASTER_QUESTS ? 4 : 8) == 1 && Skills.getTotalLevel() >= QUEST_MIN_TTL)) {
                if (allQuests != null && !allQuests.isEmpty()) {
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

        List<WatTask> taskPool;
        List<Skill> skills;
        List<Skill> mmSkills = new ArrayList<>();

        if(NEED_MM) {
            if(TRADE_UNLOCKED) {
                mmSkills.add(Skill.WOODCUTTING);
                mmSkills.add(Skill.MINING);
                mmSkills.add(Skill.FISHING);
                taskPool = TaskManager.getUnrestrictedTasks();
            }
            else {
                mmSkills.add(Skill.WOODCUTTING);
                taskPool = TaskManager.getRestrictedTasks();
            }
        } else {
            taskPool = allTasks;
        }

        if (taskPool != null && !taskPool.isEmpty()) {
            if (!skillTargets.isEmpty()) {
                long now = Instant.now().getEpochSecond();

                if(!NEED_MM) {
                    skills = new ArrayList<>(skillTargets.keySet());
                } else {
                    skills = mmSkills;
                    NEED_MM = false;
                }

                skillSelected = null;

                if(sk == null) {
                    Collections.shuffle(skills);
                    for (Skill s : skills) {
                        if (Skills.getRealLevel(s) < skillTargets.get(s)) {
                            skillSelected = s;
                            break;
                        } else {
                            if(NEED_MM) {
                                for(WatTask t : taskPool) {
                                    if(t.trainsSkill().equals(s)) {
                                        skillSelected = s;
                                        break;
                                    }
                                }
                            }
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

                Collections.shuffle(taskPool);
                for (WatTask task : taskPool) {
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
            if (currentTask == null || !(currentTask instanceof BreakingTask)) {
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
        if(WatConfig.getToolFailures() >= 3 && currentTask != null) {
            if(!(currentTask instanceof GrandExchangeTask) &&
                    !(currentTask instanceof TraversalTask) &&
                    !(currentTask instanceof BankingTask) &&
                    !(currentTask instanceof MiningTask) &&
                    !(currentTask instanceof WoodcuttingTask)) {

                Logger.log(currentTask.getName() + ": Resetting tool failures, no longer on task");
                WatConfig.resetToolFailures();
            }
        }

        //Logger.log("tool failures: " + WatConfig.getToolFailures());
        //if(WatConfig.getToolFailures() > 3) {
        //    Logger.log("falling back on tool..");
        //}

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

            //if(!isBlockedTask()) {
            //    GenericUtils.moveMouse();
            //}

            // We have to triple check below, because sometimes we rid ourselves of the task before the loop will complete.
            return currentTask != null ? (currentTask.loopTime() > 0 ? currentTask.loopTime() : 500) : 500;
        }

        return 1000;
    }

    private boolean isBlockedTask() {
        return currentTask == null ||
                currentTask instanceof BrotherBraceTask ||
                currentTask instanceof CombatInstructorTask ||
                currentTask instanceof CookingInstructorTask ||
                currentTask instanceof GuideTask ||
                currentTask instanceof MagicInstructorTask ||
                currentTask instanceof MiningInstructorTask ||
                currentTask instanceof QuestGuideTask ||
                currentTask instanceof SelectAppearanceTask ||
                currentTask instanceof SelectUsernameTask ||
                currentTask instanceof SurvivalInstructorTask ||
                currentTask instanceof TutorialBankTask ||
                currentTask instanceof TraversalTask ||
                currentTask instanceof BankingTask ||
                currentTask instanceof MulingTask ||
                currentTask instanceof BreakingTask;
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
        g2d.drawString(NumUtils.simplifyNumber(NET_WORTH), 135, 40);
        g2d.drawString(taskTime, 77, 40);

        if(totalLevelsGained > 0) {
            g2d.setColor(new Color(0, 200, 0));
            g2d.drawString("+" + totalLevelsGained, 277, 40);
            g2d.setColor(Color.WHITE);
        }

        // row 1
        g2d.drawString((currentTask != null && currentTask instanceof BreakingTask) ? "Break" : String.valueOf(Combat.getCombatLevel()), 38, 59);
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
            if (skillSelected != null && skillSelected.equals(sk))
                g2d.setColor(Color.CYAN);

            if (skillTargets != null && skillTargets.containsKey(sk) && Skills.getRealLevel(sk) >= skillTargets.get(sk))
                g2d.setColor(Color.GREEN);

            g2d.drawString(msg, x, y);

            if (skillSelected != null && skillSelected.equals(sk) || (skillTargets.containsKey(sk) && Skills.getRealLevel(sk) >= skillTargets.get(sk)))
                g2d.setColor(Color.WHITE);

            drawLevelUp(g2d, sk, x + 15, y);
        } catch (Exception ignored) { }
    }

    @Override
    public void onMessage(Message m) {
        if (currentTask != null) {
            if(!waitingForResponse && !m.getUsername().isEmpty() && !m.getUsername().equals(Players.getLocal().getName())) {
                if(ENABLE_GPT) {
                    if (Players.all(x -> !x.equals(Players.getLocal())).size() == 1) {
                        waitingForResponse = true;
                        new Thread(() -> {
                            String response = WebUtils.getRealResponse(m.getUsername(), m.getMessage(), currentTask.getName());
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
                currentTask = new LogoutTask(false, false, currentTask);
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

    private void runSkillFunction(Skill sk) {
        if(currentTask != null && skillTargets.containsKey(sk) && !currentTask.getName().contains("Tutorial")) {
            Logger.log("Overriding skill selection with " + sk.getName());
            currentTask = null;
            evaluate(sk);
        }
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
