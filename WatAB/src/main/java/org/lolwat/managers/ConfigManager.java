package org.lolwat.managers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.utilities.Logger;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;

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

    private volatile long lastReloadTime = 0;
    private static final long RELOAD_DEBOUNCE_MS = 1000;

    private final HashMap<String, Integer> itemThresholds;
    private final HashMap<Skill, Integer> skillTargets;

    public ConfigManager() {
        config = new HashMap<>();
        levelUps = new HashMap<>();
        firstStart = true;
        waitingForResponse = false;
        itemThresholds = new HashMap<>();
        skillTargets = new HashMap<>();
    }

    private JsonObject getDefaultProfile() {
        JsonObject defaultProfile = new JsonObject();

        JsonObject skillTargets = new JsonObject();
        skillTargets.addProperty("attack", 99);
        skillTargets.addProperty("strength", 99);
        skillTargets.addProperty("defence", 99);
        skillTargets.addProperty("prayer", 1);
        skillTargets.addProperty("ranged", 1);
        skillTargets.addProperty("magic", 1);
        skillTargets.addProperty("slayer", 1);
        skillTargets.addProperty("agility", 1);
        skillTargets.addProperty("herblore", 1);
        skillTargets.addProperty("thieving", 1);
        skillTargets.addProperty("hunter", 1);
        skillTargets.addProperty("mining", 1);
        skillTargets.addProperty("smithing", 1);
        skillTargets.addProperty("firemaking", 1);
        skillTargets.addProperty("woodcutting", 1);
        skillTargets.addProperty("fishing", 1);
        skillTargets.addProperty("cooking", 1);
        skillTargets.addProperty("fletching", 1);
        skillTargets.addProperty("runecrafting", 1);
        skillTargets.addProperty("crafting", 1);
        skillTargets.addProperty("construction", 1);
        skillTargets.addProperty("farming", 1);
        defaultProfile.add("skill_targets", skillTargets);

        defaultProfile.addProperty("quests_enabled", false);
        defaultProfile.addProperty("breaks_enabled", false);
        defaultProfile.addProperty("mule_trigger", 125000);
        defaultProfile.addProperty("mule_safety_net", 75000);
        defaultProfile.addProperty("disable_mule", false);
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

    public void watchConfigFile(String profileName) {
        Path configDir = Paths.get(System.getProperty("user.dir"), "WatAB");
        Path configFile = configDir.resolve(profileName + ".json");

        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            configDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            new Thread(() -> {
                while (true) {
                    try {
                        WatchKey key = watchService.take();
                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changed = (Path) event.context();
                            if (changed.endsWith(configFile.getFileName())) {
                                long now = System.currentTimeMillis();
                                if (now - lastReloadTime > RELOAD_DEBOUNCE_MS) {
                                    lastReloadTime = now;
                                    Logger.log("Config file changed, reloading...");
                                    loadFromProfile(profileName, false);
                                }
                            }
                        }
                        key.reset();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }).start();
        } catch (IOException e) {
            Logger.error("Failed to watch config file: " + e.getMessage());
        }
    }

    public int getItemThreshold(String item) {
        return itemThresholds.getOrDefault(item, 0);
    }

    public void loadFromProfile(String p, boolean firstStart) {
        String filePath = System.getProperty("scripts.path") + "/WatScripts/AccountBuilder/" + p + ".json";
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
                } else if (key.equals("skill_targets")) {
                    JsonObject skillTargets = jsonObject.getAsJsonObject(key);
                    for (String skillKey : skillTargets.keySet()) {
                        Skill skill = Skill.valueOf(skillKey.toUpperCase());
                        this.skillTargets.put(skill, skillTargets.get(skillKey).getAsInt());
                    }
                } else {
                    config.put(key, jsonObject.get(key));
                }
            }

            this.skillTargets.put(Skill.HITPOINTS, 100);

            if(firstStart) {
                watchConfigFile(p);
            }
        } catch (IOException | JsonSyntaxException ignored) {
            Logger.error("Encountered an error during setup");
        }
    }

    public void cacheAssets() {
        List<String> urls = Collections.singletonList(
                "https://api.watscripts.com/assets/WatAB_Paint_withLogo_2rows.png"
        );

        String assetsDirPath = System.getProperty("user.dir") + "/WatAB/assets";
        File assetsDir = new File(assetsDirPath);
        if (!assetsDir.exists() && !assetsDir.mkdirs()) {
            Logger.error("Could not create assets directory");
            return;
        }

        for (String urlString : urls) {
            try (InputStream in = new URL(urlString).openStream()) {
                String fileName = urlString.substring(urlString.lastIndexOf('/') + 1);
                File outFile = new File(assetsDir, fileName);
                Files.copy(in, outFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logger.error("Failed to download asset: " + urlString);
            }
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

    public int getSkillTarget(Skill sk) {
        return skillTargets.getOrDefault(sk, 1);
    }
}
