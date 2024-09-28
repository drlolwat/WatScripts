package org.lolwat.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.dreambot.api.utilities.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ConfigManager {
    private static ConfigManager instance;
    private final HashMap<Object, Object> config;
    private boolean hasLoaded;
    private int netWorth;
    private double netWorthGeneratedAt;
    private boolean firstStart;
    private HashMap<String, Integer> levelUps;
    private boolean muleConnectionFailed;
    private final HashMap<String, Integer> itemThresholds;

    public ConfigManager() {
        config = new HashMap<>();
        hasLoaded = false;
        levelUps = new HashMap<>();
        firstStart = true;
        muleConnectionFailed = false;
        itemThresholds = new HashMap<>();
    }

    private JsonObject getDefaultProfile() {
        JsonObject defaultProfile = new JsonObject();
        defaultProfile.addProperty("mule_at_gp", 100000000);
        defaultProfile.addProperty("keep_gp", 150000);
        defaultProfile.addProperty("mule_ip", "127.0.0.1");

        JsonObject items = new JsonObject();
        items.addProperty("Chaos rune", -100);
        items.addProperty("Death rune", -100);
        items.addProperty("Coal", -100);
        items.addProperty("Iron ore", -100);
        items.addProperty("Runite ore", -30);
        items.addProperty("Grimy kwuarm", -50);
        items.addProperty("Grimy cadantine", -50);
        items.addProperty("Grimy dwarf weed", -50);
        items.addProperty("Grimy lantadyme", -50);
        items.addProperty("Ranarr seed", -15);
        items.addProperty("Snapdragon seed", -15);
        items.addProperty("Yew seed", -15);
        items.addProperty("Magic seed", -15);
        items.addProperty("Palm tree seed", -15);
        items.addProperty("Dragonfruit tree seed", -2);
        items.addProperty("Celastrus seed", -15);
        items.addProperty("Redwood tree seed", -15);
        items.addProperty("Dragon warhammer", -1);
        defaultProfile.add("item_thresholds", items);
        return defaultProfile;
    }

    public int getItemThreshold(String item) {
        return itemThresholds.getOrDefault(item, 0);
    }

    public void loadFromProfile(String p) {
        String filePath = System.getProperty("user.dir") + "/WatShamans/" + p + ".json";

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

    public boolean hasMuleConnectionFailed() {
        return muleConnectionFailed;
    }

    public void setMuleConnectionFailed(boolean muleConnectionFailed) {
        this.muleConnectionFailed = muleConnectionFailed;
    }
}
