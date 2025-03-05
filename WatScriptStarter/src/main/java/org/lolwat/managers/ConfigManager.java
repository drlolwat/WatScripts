package org.lolwat.managers;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {
    @Getter
    @Setter
    private static ConfigManager instance;
    @Getter
    @Setter
    private boolean hasLoaded;
    @Getter
    @Setter
    private boolean firstStart;
    @Setter
    private boolean muleConnectionFailed;

    // svensson alch stuff
    @Getter
    @Setter
    private Map<String, Integer> tradeLimits = new HashMap<>();
    @Getter
    @Setter
    private HashMap<String, Integer> alchables; // name, buy price
    @Getter
    @Setter
    private HashMap<String, Long> purchasedWhen; // name, timestamp
    @Getter
    @Setter
    @Nullable
    private String currentTarget;
    @Getter
    @Setter
    private int currentTargetAmount;
    @Getter
    @Setter
    private HashMap<String, Integer> itemsAlched;
    @Getter
    @Setter
    private int failedAttempts = 0;

    private final HashMap<Object, Object> config;
    private final HashMap<String, Integer> itemThresholds;

    public ConfigManager() {
        config = new HashMap<>();
        hasLoaded = false;
        HashMap<String, Integer> levelUps = new HashMap<>();
        firstStart = true;
        muleConnectionFailed = false;
        itemThresholds = new HashMap<>();
        purchasedWhen = new HashMap<>();
        alchables = new HashMap<>();

        fetchItemLimits();
        //fetchLatestPrices();
    }

    private JsonObject getDefaultProfile() {
        JsonObject defaultProfile = new JsonObject();
        defaultProfile.addProperty("mule_at_gp", 100000000);
        defaultProfile.addProperty("keep_gp", 150000);
        defaultProfile.addProperty("price_modifier", 100);
        defaultProfile.addProperty("max_item_buy_qty", 3000);
        return defaultProfile;
    }

    public int itemCost(String name) {
        return alchables.get(name);
    }

    public boolean hasItemExpired(String name) {
        if(purchasedWhen.containsKey(name)) {
            int diff = 18000;
            double timestamp = purchasedWhen.get(name);
            double now = Instant.now().getEpochSecond();

            if((now - timestamp) >= diff) {
                purchasedWhen.remove(name);
                return true;
            }

            return false;
        }

        return true;
    }

    public void addItemExpiry(String name) {
        purchasedWhen.put(name, Instant.now().getEpochSecond());
    }

    public boolean allowedToBuy(String name) {
        return !purchasedWhen.containsKey(name);
    }

    public String getNewAlchTarget() {
        Logger.log("getting new HA target");
        fetchLatestPrices();

        String bestTarget = "";

        if (alchables == null || alchables.isEmpty()) {
            return bestTarget;
        }

        int bestValue = 0;
        for (Map.Entry<String, Integer> entry : alchables.entrySet()) {
            if (entry.getValue() > bestValue && allowedToBuy(entry.getKey())) {
                if (failedAttempts > 0 && entry.getKey().equals(currentTarget)) {
                    continue;
                }

                bestValue = entry.getValue();
                bestTarget = entry.getKey();
            }
        }

        Logger.log("selected target for HA: " + bestTarget + " for " + bestValue + " profit");

        currentTarget = bestTarget;
        int tradeLimit = getConfigInt("max_item_buy_qty");

        setCurrentTargetAmount(Math.max(
                tradeLimit,
                tradeLimits.get(bestTarget)));

        return bestTarget;
    }

    public String getItemName(int itemId) {
        Item i = new Item(itemId, 1);
        return !i.getName().isEmpty() ? i.getName() : "";
    }

    public void checkItemExpiries() {
        for(Map.Entry<String, Long> map : purchasedWhen.entrySet()) {
            if(hasItemExpired(map.getKey())) {
                Logger.log(map.getKey() + " has a high limit or was re added to the pool");
            }
        }
    }

    private void fetchLatestPrices() {
        StringBuilder result = new StringBuilder();

        try {
            alchables = new HashMap<>();

            URL url = new URL("https://prices.runescape.wiki/api/v1/osrs/latest");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "SmallAlcherTracker/0.1");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(result.toString(), JsonObject.class);
            JsonObject data = jsonObject.getAsJsonObject("data");

            int natPrice = LivePrices.getHigh("Nature rune");
            if (natPrice <= 0) {
                Logger.error("problem");
                ScriptManager.getScriptManager().stop();
                return;
            }

            for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
                String key = entry.getKey();

                Item i = new Item(Integer.parseInt(key), 1);

                if (i.getName() == null || i.getHighAlchValue() <= 0) {
                    continue;
                }

                int alchValue = i.getHighAlchValue();

                if(alchValue <= 0) {
                   Logger.error("problem3");
                   continue;
                }

                JsonObject itemData = entry.getValue().getAsJsonObject();
                if (!itemData.has("high") || itemData.get("high").isJsonNull()) {
                    continue;
                }

                int highValue = itemData.get("high").getAsInt();

                if (alchValue > highValue) {
                    int profitPer = alchValue - natPrice - highValue - getConfigInt("price_modifier");
                    if (profitPer > highValue && !alchables.containsKey(i.getName())) {
                        Logger.log(Color.CYAN, i.getName() + ": buy for max of " + highValue + " + nat(" + natPrice + ") + modifier " + getConfigInt("price_modifier") + " alch for " + alchValue);
                        Logger.log(Color.CYAN, "adding " + i.getName() + " to alchables with " + profitPer + " ppi");

                        if(tradeLimits.containsKey(i.getName())) {
                            Logger.log("ATTN: " + i.getName() + " has a limit of " + tradeLimits.get(i.getName()));
                            alchables.put(i.getName(), highValue);
                        } else {
                            Logger.error("skipping suspicious item: " + i.getName());
                        }
                    }
                }
            }

            rd.close();
        } catch (Exception e) {
            Logger.error(e);
        }

        checkItemExpiries();
    }

    public int getTradeLimit(String name) {
        return tradeLimits.get(name);
    }

    private void fetchItemLimits() {
        StringBuilder response = new StringBuilder();
        tradeLimits = new HashMap<>();

        try {
            URL url = new URL("https://prices.runescape.wiki/api/v1/osrs/mapping");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "SmallAlcherTracker/0.1");

            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();

            Gson gson = new Gson();
            JsonArray arr = gson.fromJson(response.toString(), JsonArray.class);

            for (JsonElement e : arr) {
                JsonObject obj = e.getAsJsonObject();
                if (!obj.has("id") || obj.get("id").isJsonNull()
                        || !obj.has("limit") || obj.get("limit").isJsonNull()) {
                    Logger.error("skipped item " + obj);
                    continue;
                }

                int itemId = obj.get("id").getAsInt();
                int limit = obj.get("limit").getAsInt();
                String itemName = getItemName(itemId);
                Logger.log("added " + itemName + " (" + itemId + ") with limit of " + limit);
                tradeLimits.put(itemName, limit);
            }

        } catch (Exception e) {
            Logger.error("2: " + e);
        }
    }

    public int getItemThreshold(String item) {
        return itemThresholds.getOrDefault(item, 0);
    }

    public void loadFromProfile(String p) {
        String filePath = System.getProperty("user.dir") + "/WatAlcher/" + p + ".json";

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

    public boolean hasMuleConnectionFailed() {
        return muleConnectionFailed;
    }

}
