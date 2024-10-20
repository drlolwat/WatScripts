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

public class ConfigManager {
    private static ConfigManager instance;
    private final HashMap<Object, Object> config;
    private double netWorthGeneratedAt;
    private boolean firstStart;
    private boolean muleConnectionFailed;
    private final HashMap<String, Integer> itemThresholds;

    public ConfigManager() {
        config = new HashMap<>();
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
        defaultProfile.add("item_thresholds", items);
        return defaultProfile;
    }

    public int getItemThreshold(String item) {
        return itemThresholds.getOrDefault(item, 0);
    }

    public void loadFromProfile(String p) {
        String filePath = System.getProperty("user.dir") + "/Preparer/" + p + ".json";

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
        } catch (IOException | JsonSyntaxException ignored) {
            Logger.error("Encountered an error during setup");
        }
    }

    public String getConfigString(String key) {
        String value = config.get(key).toString();
        return value.replace("\"", "");
    }

    public int getConfigInt(String key) {
        return Integer.parseInt(config.get(key).toString());
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
