package org.lolwat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import org.dreambot.api.Client;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.input.CameraMode;
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
import org.dreambot.api.wrappers.items.GroundItem;
import org.dreambot.api.wrappers.widgets.message.Message;
import org.lolwat.managers.ConfigManager;
import org.lolwat.managers.TaskManager;
import org.lolwat.managers.TeleportManager;
import org.lolwat.misc.mouse.SmartMouseMultiDir;
import org.lolwat.misc.paint.CustomPaint;
import org.lolwat.misc.paint.Paint;
import org.lolwat.misc.utils.GenericUtils;
import org.lolwat.tasks.misc.BondingTask;
import org.lolwat.tasks.misc.HopperTask;
import org.lolwat.tasks.misc.MulingTask;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

@ScriptManifest(name = "WatMarketAlcher",
        description = "Reads the OSRS market and performs High Level Alchemy at the Grand Exchange",
        author = "lolwat",
        version = 1.03,
        category = Category.MAGIC,
        image = "https://api.botbuddy.net/WatScripts.png")

public class WatScript extends AbstractScript implements ExperienceListener, ChatListener, AnimationListener, SpawnListener {
    public static final Instant startTime = Instant.now();

    private static final HashMap<String, String> webhookUrls = new HashMap<String, String>() {{
        put("lolwat", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
        put("user1", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
        put("user2", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
        put("user3", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
        put("user4", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
        put("user6", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");
    }};

    @Getter
    private static WatScript instance;
    @Override
    public void onStart(String... params) {
        if (params.length > 0) {
            doStart(params[0]);
        } else {
            doStart("default");
        }
    }

    private CustomPaint paint;
    private final RenderingHints aa = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

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

        if (instance == null) {
            Logger.log(Color.green, "WatScript starting: assigning instance");
            instance = this;
        }

        if (ConfigManager.getInstance() == null) {
            Logger.log("Constructing ConfigManager singleton.");
            ConfigManager.setInstance(new ConfigManager());
            ConfigManager.getInstance().loadFromProfile(profile);
        }

        if (TeleportManager.getInstance() == null) {
            Logger.log("Constructing TeleportManager singleton.");
            TeleportManager.setInstance(new TeleportManager());
        }

        Walking.setMinimapTargetSize(15);
        Camera.setCameraMode(CameraMode.MOUSE_ONLY);
        Mouse.setMouseAlgorithm(new SmartMouseMultiDir());

        if (TaskManager.getInstance() == null) {
            Logger.log("Constructing TaskManager singleton.");
            TaskManager.setInstance(new TaskManager());
        }

        getRandomManager().disableSolver(RandomEvent.DISMISS);

        WebFinder.getWebFinder().disableEquipmentTeleports();
        WebFinder.getWebFinder().disableEquippingTeleports();
        WebFinder.getWebFinder().disableInventoryTeleports();
        WebFinder.getWebFinder().disableTeleport(MagicTeleport.LUMBRIDGE_HOME_TELEPORT);

        paint = new CustomPaint(new Paint(),
                CustomPaint.PaintLocations.TOP_LEFT_PLAY_SCREEN,
                new Color[]{Color.WHITE}, // Text color
                "Verdana",
                new Color[]{new Color(98, 86, 12)}, // Background color
                new Color[]{Color.BLACK}, // Border color
                1, false, 5, 3, 0);
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
                ConfigManager.getInstance().setCurrentTarget(null);
                TaskManager.getInstance().setCurrentTask(null);
                Logger.log("Enabling login manager");
                enableLoginManager();
                return 3000;
            }
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

        if(!GenericUtils.isMember()) {
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
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onExpGained(ev.getSkill(), ev.getChange(), this);
        }
    }

    @Override
    public void onMessage(Message m) {
        if (m.getMessage().equals("Oh dear, you are dead!")) {
            sendWebhook(AccountManager.getAccountNickname() + " has alched themselves", true);
            Logger.log("DEATH DETECTED: STOPPING SCRIPT");
            ScriptManager.getScriptManager().stop();
        }
    }

    @Override
    public void onNpcAnimation(NPC npc, int animation, int animationDelay) {
        if (TaskManager.getInstance().getCurrentTask() != null) {
            TaskManager.getInstance().getCurrentTask().onNpcAnimation(npc, animation, animationDelay);
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

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHints(aa);
        paint.paint(g2d);
    }

    public void disableLoginManager() {
        getRandomManager().disableSolver(RandomEvent.LOGIN);
    }

    public void enableLoginManager() {
        getRandomManager().enableSolver(RandomEvent.LOGIN);
    }

    public String getElapsedTime() {
        Duration duration = Duration.between(startTime, Instant.now());
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%dh %dm %ds", hours, minutes, seconds);
    }
}