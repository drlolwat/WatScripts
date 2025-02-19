package org.lolwat.misc.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.dreambot.api.Client;
import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class WebUtils {
    private static long lastCallTime = 0;

    public static void sendWebhook(String message, boolean error) {
        String webhookUrl = "https://api.botbuddy.net/ws_discord.php";
        try {
            int responseCode = getResponseCode(message, webhookUrl, error);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                ScriptManager.getScriptManager().stop();
                Logger.error("script auth failed or something else went badly wrong");
            }
        } catch (Exception e) {
            Logger.log("webhook: " + e.getMessage());
        }
    }

    private static int getResponseCode(String message, String webhookUrl, boolean error) throws IOException {
        if(Client.getForumUser() == null)
            return 500;

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
        payload.addProperty("webhook_url", "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME");

        String jsonPayload = new Gson().toJson(payload);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return connection.getResponseCode();
    }

    public static String getRealResponse(String nm, String msg, String task) {
        try {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCallTime < 5 * 60 * 1000) {
                return "";
            }

            lastCallTime = currentTime;

            String urlParameters = "nm=" + nm + "&msg=" + msg + "&task=" + task;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

            String GPT_URL = "https://api.botbuddy.net/wat.php";
            HttpURLConnection con = (HttpURLConnection) new URL(GPT_URL).openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            con.setRequestProperty("charset", "utf-8");
            con.setRequestProperty("Content-Length", Integer.toString(postData.length));
            con.setUseCaches(false);
            con.setDoOutput(true);

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            }

            StringBuilder response = new StringBuilder();
            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                }
            } else {
                response.append("POST request did not work. Response Code: ").append(responseCode);
                Logger.error(response.toString());
                return "";
            }

            String formattedMessage = String.format(
                    "**Replying to**: %s\n**They sent**: %s\n**You sent**: %s",
                    nm,
                    msg,
                    response);

            if (response.toString().isEmpty()) {
                return "";
            }

            sendWebhook(formattedMessage, false);
            return response.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}