package org.lolwat.misc.utils;

import com.google.gson.Gson;
import org.dreambot.api.Client;
import org.dreambot.api.methods.ForumUser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WebUtils {
    private static String WEBHOOK_URL = "https://discord.com/api/webhooks/REPLACE_ME/REPLACE_ME";
    private static final String GPT_URL = "https://api.botbuddy.net/wat.php";
    private static Gson gson = new Gson();

    public static void postWebhook(String title, String message) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");

            Map<String, String> embed = new HashMap<>();
            embed.put("title", "[" + Client.getForumUser().getUsername() + "] " + title);
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

    public static String getRealResponse(String nm, String msg, String task) {
        try {
            String urlParameters = "nm=" + nm + "&msg=" + msg + "&task=" + task;
            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

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
            }

            WebUtils.postWebhook("Chat message", "Replying to " + nm + " with '" + response + "', they sent: '" + msg + "'.");
            return response.toString();
        } catch (Exception ex) {
            return "";
        }
    }
}