package org.lolwat.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.dreambot.api.Client;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.quest.Quests;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.AccountManager;
import org.dreambot.api.utilities.Logger;
import org.lolwat.WatAIO;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {
    private static ConfigManager instance;
    private final HashMap<Object, Object> config;
    private boolean hasLoaded;
    private int netWorth;
    private double netWorthGeneratedAt;
    private boolean firstStart;
    private boolean waitingForResponse;
    private HashMap<String, Integer> levelUps;
    private boolean muleConnectionFailed;
    private HashMap<String, Integer> itemThresholds;

    public ConfigManager() {
        config = new HashMap<>();
        hasLoaded = false;
        levelUps = new HashMap<>();
        firstStart = true;
        waitingForResponse = false;
        muleConnectionFailed = false;
        itemThresholds = new HashMap<>();
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
        defaultProfile.addProperty("agility", 1);
        defaultProfile.addProperty("herblore", 1);
        defaultProfile.addProperty("runecrafting", 1);

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
        defaultProfile.addProperty("pickup_bones", false);
        defaultProfile.addProperty("rest_after_tut", 0);
        defaultProfile.addProperty("logout_after_ttl", 0);

        JsonObject items = new JsonObject();
        items.addProperty("Raw shrimp", 150);
        items.addProperty("Lobster", 100);
        items.addProperty("Iron full helm", 1);
        items.addProperty("Iron platebody", 1);
        items.addProperty("Iron platelegs", 1);
        items.addProperty("Iron kiteshield", 1);
        items.addProperty("Iron scimitar", 1);

        defaultProfile.add("item_thresholds", items);
        return defaultProfile;
    }

    public int getItemThreshold(String item) {
        return itemThresholds.getOrDefault(item, -1);
    }

    public void printConfigContents() {
        for (Map.Entry<Object, Object> entry : config.entrySet()) {
            Logger.log("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }
    }

    public void loadFromProfile(String p) {
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

            for (String key : jsonObject.keySet()) {
                if (key.equals("item_thresholds")) {
                    JsonObject items = jsonObject.getAsJsonObject(key);
                    for (String itemKey : items.keySet()) {
                        itemThresholds.put(itemKey, items.get(itemKey).getAsInt());
                    }
                } else {
                    config.put(key, jsonObject.get(key));
                }
            }

            config.put("hitpoints", 100);
            hasLoaded = true;

        } catch (IOException | JsonSyntaxException ignored) {
            Logger.error("Encountered an error during setup");
        }
    }

    public void getWsProfile(int breaking) {
        try {
            String urlString = "https://api.botbuddy.net/ws_profile.php?fu=" + Client.getForumUser().getUsername() + "&_hash=" + AccountManager.getAccountHash();

            if(getConfigBoolean("ignore_trade_restriction")) {
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

                for (String key : jsonObject.keySet()) {
                    config.put("profile_" + key, jsonObject.get(key));
                }

                WatAIO.getInstance().enableLoginManager();
                Logger.log(Color.green, (breaking > 0) ? "Updated account hivetime due to break" :"Loaded unique account profile from BotBuddy Hive");

            } else {
                Logger.error("HTTP request failed with response code: " + responseCode);
            }
            connection.disconnect();
        } catch (IOException ignored) {
            ScriptManager.getScriptManager().stop();
        }
    }
    public String getConfigString(String key) {
        String value = config.get(key).toString();
        return value.replace("\"", "");
    }

    public boolean getConfigBoolean(String key) {
        String value = config.get(key).toString().replace("\"", "");
        return Objects.equals(value, "true") || Objects.equals(value, "1");
    }

    public int getConfigInt(String key) {
        return Integer.parseInt(config.get(key).toString());
    }
    public double getConfigDouble(String key) {
        return Double.parseDouble(config.get(key).toString());
    }

    public int getSkillTarget(Skill sk) {
        String key = sk.getName().toLowerCase();
        if (config.containsKey(key)) {
            return Integer.parseInt(config.get(key).toString());
        } else {
            return 0;
        }
    }

    public boolean isTradeUnlocked() {
        return Combat.getCombatLevel() >= 50 || getConfigBoolean("ignore_trade_restriction") ||
                (Instant.now().getEpochSecond() - getConfigDouble("profile_appeared_at") >= 75600 && Quests.getQuestPoints() >= 10);
    }

    public int getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(int netWorth) {
        this.netWorth = netWorth;
    }

    public double getNetWorthGeneratedAt() {
        return netWorthGeneratedAt;
    }

    public void setNetWorthGeneratedAt(double netWorthGeneratedAt) {
        this.netWorthGeneratedAt = netWorthGeneratedAt;
    }

    public static ConfigManager getInstance() {
        return instance;
    }

    public static void setInstance(ConfigManager inst) {
        instance = inst;
    }

    public boolean hasLoadedProfile() {
        return hasLoaded;
    }

    public void setHasLoadedProfile(boolean hasLoaded) {
        this.hasLoaded = hasLoaded;
    }

    public boolean isFirstStart() {
        return firstStart;
    }

    public void setFirstStart(boolean firstStart) {
        this.firstStart = firstStart;
    }

    public boolean isWaitingForResponse() {
        return waitingForResponse;
    }

    public void setWaitingForResponse(boolean waitingForResponse) {
        this.waitingForResponse = waitingForResponse;
    }

    public HashMap<String, Integer> getLevelUps() {
        return levelUps;
    }

    public void setLevelUps(HashMap<String, Integer> levelUps) {
        this.levelUps = levelUps;
    }

    public boolean hasMuleConnectionFailed() {
        return muleConnectionFailed;
    }

    public void setMuleConnectionFailed(boolean muleConnectionFailed) {
        this.muleConnectionFailed = muleConnectionFailed;
    }
}
