package org.lolwat.misc.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.wrappers.items.Item;

public class WikiPricing {
    private static List<Map<String, Object>> itemMappings;

    private static final double PRICE_MODIFIER = 1.1;

    public static int getPrice(String name) {
        int itemId = getItemID(name);
        if (itemId == -1) {
            Logger.error("Item not found: " + name);
            return -1;
        }

        return getPrice(itemId);
    }

    public static int getPrice(int id) {
        try {
            String urlString = "https://prices.runescape.wiki/api/v1/osrs/latest?id=" + id;
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "BotBuddy/3.0");

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            connection.disconnect();

            Gson gson = new Gson();
            Map<?, ?> responseMap = gson.fromJson(content.toString(), Map.class);
            Map<?, ?> dataMap = (Map<?, ?>) responseMap.get("data");
            Map<?, ?> itemData = (Map<?, ?>) dataMap.get(String.valueOf(id));
            Number price = (Number) itemData.get("high");

            return (int) (price.intValue() * PRICE_MODIFIER);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void getItemMappings() {
        try {
            URL url = new URL("https://prices.runescape.wiki/api/v1/osrs/mapping");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "BotBuddy/3.0");

            int responseCode = con.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Logger.error("GET request didnt work. Response Code: " + responseCode);
                return;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            Gson gson = new Gson();
            itemMappings = gson.fromJson(content.toString(), new TypeToken<List<Map<String, Object>>>() {}.getType());
            Logger.log("Item mappings loaded successfully.");
        } catch (Exception e) {
            Logger.error("Error in getItemMappings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static int getItemID(String itemName) {
        if (itemMappings == null) {
            getItemMappings(); // Load item mappings if not already loaded
            if (itemMappings == null) {
                Logger.error("Failed to load item mappings.");
                return -1; // or handle this case as you see fit
            }
        }

        for (Map<String, Object> item : itemMappings) {
            if (item.get("name").equals(itemName)) {
                return (int) Double.parseDouble(item.get("id").toString());
            }
        }
        return -1; // Item not found
    }
}
