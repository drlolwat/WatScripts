package org.lolwat.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class ConfigManager {
    @Getter
    @Setter
    private static ConfigManager instance;
    private final HashMap<Object, Object> config;
    @Getter
    @Setter
    private int netWorth;
    @Getter
    @Setter
    private double netWorthGeneratedAt;
    @Getter
    @Setter
    private boolean firstStart;
    @Getter
    @Setter
    private boolean waitingForResponse;
    @Getter
    @Setter
    private HashMap<String, Integer> levelUps;
    @Setter
    private boolean muleConnectionFailed;
    private final HashMap<String, Integer> itemThresholds;

    public ConfigManager() {
        config = new HashMap<>();
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
        defaultProfile.addProperty("mule_ip", "127.0.0.1");
        defaultProfile.addProperty("use_menu_manip", false);
        defaultProfile.addProperty("min_task_time", 10);
        defaultProfile.addProperty("max_task_time", 30);
        defaultProfile.addProperty("keep_min_gold", 100000);
        defaultProfile.addProperty("min_sleep_time", 500);
        defaultProfile.addProperty("max_sleep_time", 1500);

        JsonObject items = new JsonObject();
        items.addProperty("Trousers", 1);
        defaultProfile.add("item_thresholds", items);
        return defaultProfile;
    }

    public int getItemThreshold(String item) {
        return itemThresholds.getOrDefault(item, 0);
    }

    public void loadFromProfile(String p) {
        String filePath = System.getProperty("user.dir") + "/WatAB/" + p + ".json";

        try {
            Gson gson = new Gson();

            File file = new File(filePath);
            if (!file.exists()) {
                if(!file.getParentFile().mkdirs()) {
                    Logger.error("problem creating config directory");
                }

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
}
