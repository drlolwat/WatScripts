package org.lolwat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.dreambot.api.Client;
import org.dreambot.api.input.Keyboard;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.CameraMode;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.walking.pathfinding.impl.web.WebFinder;
import org.dreambot.api.methods.walking.web.node.impl.teleports.MagicTeleport;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.randoms.RandomEvent;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.event.impl.ExperienceEvent;
import org.dreambot.api.script.listener.AnimationListener;
import org.dreambot.api.script.listener.ChatListener;
import org.dreambot.api.script.listener.ExperienceListener;
import org.dreambot.api.script.listener.SpawnListener;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.Menu;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.TeleportManager;
import org.lolwat.misc.mouse.HumanMouse;
import org.lolwat.misc.paint.CustomPaint;
import org.lolwat.misc.paint.Paint;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.misc.BondingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.MulingTask;
import org.lolwat.tasks.shamans.ShamanCombatTask;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@ScriptManifest(name = "WatShamans", description = "Shaman killer", author = "lolwat", version = 1.0, category = Category.MISC)
public class WatScript extends AbstractScript implements ExperienceListener, ChatListener, AnimationListener, SpawnListener {
    private static WatScript instance;
    public static WatScript getInstance() {
        return instance;
    }

    List<String> logoutMessages = Arrays.asList(
            "You've been playing for a while, consider taking a break from your screen.",
            "You will be logged out in approximately 30 minutes. Make sure you move to a safe area or log out now.",
            "You will be logged out in approximately 10 minutes. Make sure you move to a safe area or log out now.",
            "You will be logged out in approximately 5 minutes. Make sure you move to a safe area or log out now.");

    private static final HashMap<String, String> webhookUrls = new HashMap<String, String>() {{
        put("lolwat", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
        put("user1", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
        put("user2", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
    }};

    private final CustomPaint paint = new CustomPaint(new Paint(),
            CustomPaint.PaintLocations.TOP_LEFT_PLAY_SCREEN,
            new Color[]{Color.WHITE}, // Text color
            "Verdana",
            new Color[]{new Color(20, 54, 16)}, // Background color
            new Color[]{Color.BLACK}, // Border color
            1, false, 5, 3, 0);

    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    public static final Instant startTime = Instant.now();

    private int shamansKilled = 0;
    private int dwhCollected = 0;
    private int deaths = 0;
    private int startRangedXp = 0;
    private int rangedLevelsGained = 0;
    private int goldAlched = 0;
    private int itemWorthPicked = 0;

    private long GPT_LAST_CALL = 0;
    private boolean GPT_WAITING_FOR_REPLY;

    @Override
    public void onStart(String... params) {
        if (params.length > 0) {
            doStart(params[0]);
        } else {
            doStart("default");
        }
    }

    private static String getScriptName() {
        return "WatScript1";
    }

    @Override
    public void onStart() {
        doStart("default");
    }

    private void doStart(String profile) {
        if(Client.getForumUser() == null) {
            Logger.error("lolwat");
            ScriptManager.getScriptManager().stop();
            return;
        }

        boolean auth = false;
        for(String u : webhookUrls.keySet()) {
            if(Client.getForumUser().getUsername().toLowerCase().equals(u)) {
                auth = true;
                break;
            }
        }

        if(!auth) {
            Logger.error("lulwut");
            ScriptManager.getScriptManager().stop();
            return;
        }

        if(Client.getForumUser().getUsername().equals("lolwat") || Client.getForumUser().getUsername().equals("user1")) {
            GenericUtils.setHopperTime(Calculations.random(21, 35));
        }

        if (instance == null) {
            Logger.log(Color.green, "WatShamans starting: assigning instance");
            instance = this;
        }

        if (ConfigManager.getInstance() == null) {
            Logger.log("Constructing ConfigManager singleton.");
            ConfigManager.setInstance(new ConfigManager());
            ConfigManager.getInstance().setNetWorth(0);
            ConfigManager.getInstance().setNetWorthGeneratedAt(0);
            ConfigManager.getInstance().loadFromProfile(profile);
        }

        if (TeleportManager.getInstance() == null) {
            Logger.log("Constructing TeleportManager singleton.");
            TeleportManager.setInstance(new TeleportManager());
        }

        Walking.setMinimapTargetSize(15);
        Camera.setCameraMode(CameraMode.MOUSE_ONLY);
        HumanMouse m = new HumanMouse();
        Mouse.setMouseAlgorithm(m);

        if (TaskManager.getInstance() == null) {
            Logger.log("Constructing TaskManager singleton.");
            TaskManager.setInstance(new TaskManager());
        }

        getRandomManager().disableSolver(RandomEvent.DISMISS);
        if (!Menu.isMenuManipulationActive()) {
            Logger.log("Enabling menu manipulation and noclick walk");
            Menu.toggleMenuManipulation(true);
            Walking.toggleNoClickWalk(true);
        }

        WebFinder.getWebFinder().disableEquipmentTeleports();
        WebFinder.getWebFinder().disableEquippingTeleports();
        WebFinder.getWebFinder().disableInventoryTeleports();
        WebFinder.getWebFinder().disableTeleport(MagicTeleport.LUMBRIDGE_HOME_TELEPORT);
    }

    public void sendWebhook(String message, boolean error) {
        String webhookUrl = "https://api.botbuddy.net/ws_discord.php";
        try {
            int responseCode = getResponseCode(message, webhookUrl, error);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Logger.log("webhook failed: " + responseCode);
            }
        } catch (Exception e) {
            Logger.log("webhook: " + e.getMessage());
        }
    }

    private static int getResponseCode(String message, String webhookUrl, boolean error) throws IOException {
        if(Client.getForumUser() == null)
            return 500;

        URL url = new URL(webhookUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JsonObject embed = new JsonObject();
        embed.addProperty("title", "Notification");
        embed.addProperty("description", message);
        embed.addProperty("color", error ? 16711680 : 3447003);

        JsonObject payload = new JsonObject();
        payload.add("embeds", new Gson().toJsonTree(new JsonObject[]{embed}));
        payload.addProperty("webhook_url", webhookUrls.get(Client.getForumUser().getUsername().toLowerCase()));

        String jsonPayload = new Gson().toJson(payload);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return connection.getResponseCode();
    }

    @Override
    public int onLoop() {
        if (!Client.isLoggedIn()) {
            if (TaskManager.getInstance().getCurrentTask() == null) {
                TaskManager.getInstance().setCurrentTask(null);
                Logger.log("Enabling login manager");
                enableLoginManager();
                return 3000;
            }
        }

        if(startRangedXp == 0) {
            startRangedXp = Skills.getExperience(Skill.RANGED);
        }

        if (ConfigManager.getInstance().isFirstStart()) {
            ConfigManager.getInstance().setFirstStart(false);
        }

        if(!(TaskManager.getInstance().getCurrentTask() instanceof HopperTask)
                && !(TaskManager.getInstance().getCurrentTask() instanceof BondingTask)
                && !(TaskManager.getInstance().getCurrentTask() instanceof MulingTask)) {
            if (GenericUtils.isMember() && !Worlds.getCurrent().isMembers()) {
                TaskManager.getInstance().setCurrentTask(new HopperTask(0,
                        (TaskManager.getInstance().getCurrentTask() != null) ?
                                TaskManager.getInstance().getCurrentTask() : null), 0);

                Logger.log("We are hopping into a P2P world");
                return 300;
            }
        }

        if(!GenericUtils.isMember() && TaskManager.getInstance().getCurrentTask() instanceof ShamanCombatTask) {
            Logger.log("need to bond");
            TaskManager.getInstance().setCurrentTask(new BondingTask(
                    (TaskManager.getInstance().getCurrentTask() != null) ?
                            TaskManager.getInstance().getCurrentTask() : null
            ));
            return 300;
        }

        if (TaskManager.getInstance().getCurrentTask() != null) {
            // things to do if the task is active
            if (!(TaskManager.getInstance().getCurrentTask() instanceof HopperTask) && Tabs.isOpen(Tab.LOGOUT)) {
                Tabs.open(Tab.INVENTORY);
            }
        } else {
            Logger.log("Task was null, finding a new one...");
            TaskManager.getInstance().getNewTask();
            return 1500;
        }

        if (!Walking.isRunEnabled() && Walking.getRunEnergy() >= Calculations.random(75, 100)) {
            Walking.toggleRun();
            Sleep.sleep(50, 120);
        }

        if (TaskManager.getInstance().getCurrentTask() != null) {
            if (!Client.isLoggedIn() && TaskManager.getInstance().getCurrentTask().requiresLogin()) {
                enableLoginManager();
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
    public void onLevelChange(ExperienceEvent event) {
        if (event.getSkill() == Skill.RANGED) {
            rangedLevelsGained++;
        }
    }

    @Override
    public void onGained(ExperienceEvent ev) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onExpGained(ev.getSkill(), ev.getChange(), this);
        }
    }

    @Override
    public void onMessage(Message m) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            if (m.getMessage().startsWith("Darts")) {
                int[] data = GenericUtils.parseBlowpipeData(m.getMessage());
                TaskManager.getInstance().getCurrentTask().data().put("darts", data[0]);
                TaskManager.getInstance().getCurrentTask().data().put("scales", data[1]);
                Logger.log("Darts: " + data[0] + ", Scales: " + data[1] + " sent to task");
            }

            TaskManager.getInstance().getCurrentTask().onMessage(m);
        }

        if (m.getMessage().equals("Oh dear, you are dead!")) {
            handleDeath();
        }

        if (logoutMessages.contains(m.getMessage()) && TaskManager.getInstance().getCurrentTask() instanceof ShamanCombatTask) {
            TaskManager.getInstance().setCurrentTask
                    (
                            new HopperTask(0, TaskManager.getInstance().getCurrentTask() != null
                                    ? TaskManager.getInstance().getCurrentTask()
                                    : null
                            )
                    );
        }

        if(Client.getForumUser() == null)
            return;

        boolean enableGpt = (Client.getForumUser().getUsername().equals("user1") || Client.getForumUser().getUsername().equals("lolwat"));
        if (enableGpt && TaskManager.getInstance().getCurrentTask() instanceof ShamanCombatTask && !m.getUsername().isEmpty() && !m.getUsername().equals(Players.getLocal().getName())) {
            if (Players.all(x -> !x.equals(Players.getLocal())).size() == 1 && !GPT_WAITING_FOR_REPLY) {
                setWaitingForReply(true);
                new Thread(() -> {
                    String response = getRealResponse(m.getUsername(), m.getMessage(), TaskManager.getInstance().getCurrentTask().getName());
                    if (!response.isEmpty()) {
                        Keyboard.type(response, true);
                    }
                    setWaitingForReply(false);
                }).start();
            }
        }
    }

    public String getRealResponse(String nm, String msg, String task) {
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - GPT_LAST_CALL < 5 * 60 * 1000) {
                return "";
            }
            GPT_LAST_CALL = currentTime;

            String urlParameters = "nm=" + nm + "&msg=" + msg + "&task=";
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            String GPT_URL = "https://api.botbuddy.net/wat.php";
            HttpURLConnection con = (HttpURLConnection) new URL(GPT_URL).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(postData.length));
            con.setUseCaches(false);
            con.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            }

            StringBuilder response = new StringBuilder();
            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
            } else {
                response.append("POST request did not work. Response Code: ").append(responseCode);
                sendWebhook("GPT failed: " + responseCode, true);
                return "";
            }

            String formattedMessage = String.format(
                    "**Account**: %s\n**Replying to**: %s\n**They sent**: %s\n**You sent**: %s",
                    AccountManager.getAccountNickname(),
                    nm,
                    msg,
                    response);

            sendWebhook(formattedMessage, false);
            return response.toString();
        } catch (Exception ex) {
            return "";
        }
    }

    @Override
    public void onNpcAnimation(NPC npc, int animation, int animationDelay) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onNpcAnimation(npc, animation, animationDelay);
        }
    }

    @Override
    public void onPlayerAnimation(Player player, int anim, int animDelay) {
        if(player.equals(Players.getLocal())) {
            if(anim == 836) {
                handleDeath();
            }
        }
    }

    @Override
    public void onNpcSpawn(NPC npc) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onNpcSpawn(npc);
        }
    }

    @Override
    public void onNpcDespawn(NPC npc) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onNpcDespawn(npc);
        }
    }

    @Override
    public void onGroundItemSpawn(GroundItem object) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onGroundItemSpawn(object);
        }
    }

    public void disableLoginManager() {
        getRandomManager().disableSolver(RandomEvent.LOGIN);
    }

    public void enableLoginManager() {
        getRandomManager().enableSolver(RandomEvent.LOGIN);
    }

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHints(aa);
        paint.paint(g2d);
    }

    public void handleDeath() {
        sendWebhook(AccountManager.getAccountNickname() + " has fallen and cannot get up", true);
        Logger.log("DEATH DETECTED: STOPPING SCRIPT");
        setDeaths(getDeaths() + 1);
        ScriptManager.getScriptManager().stop();
    }

    public String getElapsedTime() {
        Duration duration = Duration.between(startTime, Instant.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }

    public int getDwhCollected() {
        return dwhCollected;
    }

    public void setDwhCollected(int dwhCollected) {
        this.dwhCollected = dwhCollected;
    }

    public int getShamansKilled() {
        return shamansKilled;
    }

    public void setShamansKilled(int shamansKilled) {
        this.shamansKilled = shamansKilled;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public int getStartRangedXp() {
        return startRangedXp;
    }

    public int getRangedLevelsGained() {
        return rangedLevelsGained;
    }

    public int getGoldAlched() {
        return goldAlched;
    }

    public void setGoldAlched(int goldAlched) {
        this.goldAlched = goldAlched;
    }

    public int getItemWorthPicked() {
        return itemWorthPicked;
    }

    public void setItemWorthPicked(int itemWorthPicked) {
        this.itemWorthPicked = itemWorthPicked;
    }

    public void setWaitingForReply(boolean GPT_WAITING_FOR_REPLY) {
        this.GPT_WAITING_FOR_REPLY = GPT_WAITING_FOR_REPLY;
    }
}