package org.lolwat.misc.utils;

import com.google.gson.Gson;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DiscordUtils {
    private static String WEBHOOK_URL = "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME";
    private static Gson gson = new Gson();

    public static void postWebhook(String title, String message) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");

            Map<String, String> embed = new HashMap<>();
            embed.put("title", title);
            embed.put("description", message);

            Map<String, Object> payload = new HashMap<>();
            payload.put("embeds", new Map[]{embed});

            String jsonPayload = gson.toJson(payload);

            OutputStream os = http.getOutputStream();
            os.write(jsonPayload.getBytes());
            os.flush();
            os.close();
            http.getResponseCode();
        } catch (Exception e) {
            postWebhook("\uD83D\uDE21 \uD83D\uDD95 Error thrown: " + e.getMessage(), Arrays.toString(e.getStackTrace()));
        }
    }
}