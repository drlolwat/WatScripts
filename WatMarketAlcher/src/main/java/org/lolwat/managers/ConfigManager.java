package org.lolwat.managers;

import com.google.gson.*;
import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.grandexchange.LivePrices;
import org.dreambot.api.methods.world.Worlds;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;
import org.lolwat.misc.utils.NumUtils;
import org.lolwat.tasks.alching.AlchingBankingTask;
import org.lolwat.tasks.alching.HighAlchemyTask;
import org.lolwat.tasks.misc.MulingTask;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.List;

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
    private HashMap<String, Integer> alchables;// name, buy price

    @Getter
    @Setter
    private HashMap<String, Integer> itemIds;

    @Getter
    @Setter
    private HashMap<String, Integer> buyLimits;

    @Getter
    @Setter
    private List<String> noBuy;

    @Getter
    @Setter
    private HashMap<String, Long> purchasedWhen; // name, timestamp

    @Getter
    @Setter
    private HashMap<String, Integer> purchasedAmount;

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
    @Getter
    private final HashMap<String, Integer> itemBlacklist;

    @Getter @Setter
    private int totalAlchs = 0;

    @Getter @Setter
    private int totalProfit = 0;

    @Getter @Setter
    private int naturePrice = 0;

    public ConfigManager() {
        config = new HashMap<>();
        hasLoaded = false;
        firstStart = true;
        muleConnectionFailed = false;
        itemBlacklist = new HashMap<>();
        purchasedWhen = new HashMap<>();
        alchables = new HashMap<>();
        buyLimits = new HashMap<>();
        noBuy = new ArrayList<>();
        naturePrice = LivePrices.get("Nature rune");
        itemIds = new HashMap<>();
        purchasedAmount = new HashMap<>();
    }

    private JsonObject getDefaultProfile() {
        JsonObject defaultProfile = new JsonObject();
        defaultProfile.addProperty("mule_at_gp", 3000000);
        defaultProfile.addProperty("keep_gp", 1000000);
        defaultProfile.addProperty("price_modifier", 0);
        defaultProfile.addProperty("min_profit", 100);
        defaultProfile.addProperty("buy_nature_qty", 2000);
        defaultProfile.addProperty("mule_ip", "127.0.0.1");
        defaultProfile.addProperty("mule_port", 8081);
        defaultProfile.addProperty("item_inventory_slot", 11);

        JsonObject items = new JsonObject();
        items.addProperty("Trousers", 1);
        defaultProfile.add("item_blacklist", items);
        return defaultProfile;
    }

    public int itemCost(String name) {
        return alchables.getOrDefault(name, -10000);
    }

    public void addItemExpiry(String name) {
        purchasedWhen.put(name, Instant.now().getEpochSecond());
    }

    public boolean allowedToBuy(String name) {
        return !purchasedWhen.containsKey(name) && !noBuy.contains(name);
    }

    public void removeCooldown(String name) {
        purchasedAmount.remove(name);
        purchasedWhen.remove(name);
        noBuy.remove(name);
    }

    public void getNewAlchTarget() {
        Logger.log("getting new HA target");
        checkItemExpiries();
        fetchLatestPrices();

        String bestTarget = null;

        if (alchables == null || alchables.isEmpty()) {
            purchasedWhen.clear();
            purchasedAmount.clear();
            noBuy.clear();
            getNewAlchTarget();
            return;
        }

        for(String item : alchables.keySet()) {
            if(Inventory.contains(item) || Bank.contains(item)) {
                currentTarget = item;
                currentTargetAmount = Inventory.count(item) + Bank.count(item);
                Logger.log("selecting already owned item: " + item + " (x" + currentTargetAmount + ")");
                TaskManager.getInstance().setFutureTask(new AlchingBankingTask(new HighAlchemyTask()));
                return;
            }
        }

        int totalCoins = Inventory.count("Coins");// + Bank.count("Coins");
        int toBuy = 0;

        Logger.log("selecting new item to alch");
        Logger.log("coins available " + totalCoins);
        Logger.log("(" + NumUtils.simplifyNumber(totalCoins) + ")");

        List<Map.Entry<String, Integer>> sortedAlchables = new ArrayList<>(alchables.entrySet());
        sortedAlchables.sort((entry1, entry2) -> {
            int profit1 = calculateProfit(entry1.getKey(), entry1.getValue());
            int profit2 = calculateProfit(entry2.getKey(), entry2.getValue());
            return Integer.compare(profit2, profit1);
        });

        for (Map.Entry<String, Integer> entry : sortedAlchables) {
            if (allowedToBuy(entry.getKey())) {
                bestTarget = entry.getKey();
                toBuy = totalCoins / (alchables.get(entry.getKey()) + getConfigInt("price_modifier")) - 1;
                break;
            }
        }

        if(toBuy <= 0) {
            Logger.log("reverse muling - no money available apparently");
            TaskManager.getInstance().setFutureTask(new MulingTask("Reverse muling", Worlds.getCurrentWorld(), new HashMap<String, Integer>() {
                {
                    put("Coins", ConfigManager.getInstance().getConfigInt("keep_gp"));
                }
            }, new HighAlchemyTask()));
            return;
        }

        int cost = (alchables.get(bestTarget) + getConfigInt("price_modifier")) * toBuy;

        Logger.log("going to buy " + toBuy + " " + bestTarget);
        Logger.log("this will cost " + cost);
        Logger.log("(" + NumUtils.simplifyNumber(cost) + ")");

        if(cost > totalCoins) {
            Logger.error("somehow the math didnt math");
            currentTargetAmount = -1000;
        }

        if (bestTarget != null) {
            int buyLimit = buyLimits.get(bestTarget);
            if(purchasedAmount.containsKey(bestTarget)) {
                buyLimit -= purchasedAmount.get(bestTarget);
            }

            currentTargetAmount = Math.min(buyLimit, toBuy);
        }

        if(currentTargetAmount > 0) {
            Logger.log("selected target for HA: " + bestTarget + " x" + currentTargetAmount);
            currentTarget = bestTarget;
        } else {
            addItemExpiry(bestTarget);
        }
    }

    private int calculateProfit(String name, int buyPrice) {
        int highAlchValue = new Item(itemIds.get(name), 1).getHighAlchValue();
        return highAlchValue - getNaturePrice() - buyPrice - getConfigInt("price_modifier");
    }

    public void checkItemExpiries() {
        Iterator<Map.Entry<String, Long>> iterator = purchasedWhen.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Long> entry = iterator.next();
            int diff = 14400;
            int noBuyDiff = 3600;//1800;
            long timestamp = entry.getValue();
            double now = Instant.now().getEpochSecond();

            if ((now - timestamp) >= diff) {
                noBuy.remove(entry.getKey());
                purchasedAmount.remove(entry.getKey());
                iterator.remove();
                Logger.log("timer expired for item: " + entry.getKey());
            }

            if(noBuy.contains(entry.getKey()) && (now - timestamp) >= noBuyDiff) {
                noBuy.remove(entry.getKey());
                purchasedAmount.remove(entry.getKey());
                iterator.remove();
                Logger.log("removed nobuy restrict for: " + entry.getKey());
            }
        }
    }

    public void fetchLatestPrices() {
        StringBuilder result = new StringBuilder();
        try {
            alchables = new HashMap<>();

            // Fetch item mappings
            URL url = new URL("https://gpt.lolwat.net/get_market.php?type=mapping&time=" + Instant.now().getEpochSecond());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "SmallAlcherTracker/0.1");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            Gson gson = new Gson();
            JsonArray jsonArray = gson.fromJson(result.toString(), JsonArray.class);

            // Fetch high prices
            result.setLength(0); // Clear the result buffer
            url = new URL("https://gpt.lolwat.net/get_market.php?type=5m&time=" + Instant.now().getEpochSecond());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "SmallAlcherTracker/0.1");
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = rd.readLine()) != null) {
                result.append(line);
            }

            JsonObject highPricesJson = gson.fromJson(result.toString(), JsonObject.class).getAsJsonObject("data");

            if (getNaturePrice() <= 0) {
                Logger.error("problem - nature rune price is <= 0");
                ScriptManager.getScriptManager().stop();
                return;
            }

            for (JsonElement element : jsonArray) {
                try {
                    JsonObject jsonObject = element.getAsJsonObject();
                    int id = jsonObject.get("id").getAsInt();
                    int alchValueApi = jsonObject.get("highalch").getAsInt();

                    int itemLimit = jsonObject.has("limit") ? jsonObject.get("limit").getAsInt() : 0;

                    if (alchValueApi == 0 || alchValueApi == 1)
                        continue;

                    boolean alchable = alchValueApi > 1;

                    if (id == 0 || !alchable)
                        continue;

                    Item i = new Item(id, 1);
                    int highAlchValue = i.getHighAlchValue();

                    JsonObject itemPriceData = highPricesJson.getAsJsonObject(String.valueOf(id));
                    int itemHighPrice = itemPriceData != null && itemPriceData.has("avgHighPrice") ? itemPriceData.get("avgHighPrice").getAsInt() : 0;
                    int itemLowPrice = itemPriceData != null && itemPriceData.has("avgLowPrice") ? itemPriceData.get("avgLowPrice").getAsInt() : 0;
                    int highVolume = itemPriceData != null && itemPriceData.has("highPriceVolume") ? itemPriceData.get("highPriceVolume").getAsInt() : 0;
                    int lowVolume = itemPriceData != null && itemPriceData.has("lowPriceVolume") ? itemPriceData.get("lowPriceVolume").getAsInt() : 0;
                    String name = i.getName();

                    if (name == null || highAlchValue == 0 || itemHighPrice == 0 || lowVolume == 0 || highVolume == 0) {
                        //Logger.warn("skipping item with values: " + name + " " + highAlchValue + " " + itemHighPrice + " " + lowVolume);
                        continue;
                    }

                    int profitPer = highAlchValue - getNaturePrice() - itemHighPrice - getConfigInt("price_modifier");
                    if (((highVolume + lowVolume) / 2) > 30 && profitPer > getConfigInt("min_profit")) {
                        if (!alchables.containsKey(name) && allowedToBuy(name)) {
                            Logger.log(Color.CYAN, "adding " + name + " to alchables with " + profitPer + " ppi");

                            if (!itemBlacklist.containsKey(name)) {
                                alchables.put(name, itemHighPrice);
                                buyLimits.put(name, itemLimit);
                                itemIds.put(name, id);
                            } else {
                                Logger.error("skipping suspicious/blacklisted item: " + name);
                            }
                        }
                    }
                } catch (Exception e) {
                    //Logger.error(e);
                }
            }

            rd.close();
        } catch (Exception e) {
            //Logger.error(e.getStackTrace());
        }
    }

    public boolean isItemBlacklisted(String item) {
        return itemBlacklist.containsKey(item);
    }

    public void loadFromProfile(String p) {
        String filePath = System.getProperty("user.dir") + "/WatMarketAlcher/" + p + ".json";

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
                if (key.equals("item_blacklist")) {
                    JsonObject items = jsonObject.getAsJsonObject(key);
                    for (String itemKey : items.keySet()) {
                        itemBlacklist.put(itemKey, items.get(itemKey).getAsInt());
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
