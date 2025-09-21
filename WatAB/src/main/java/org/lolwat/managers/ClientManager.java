package org.lolwat.managers;

import lombok.Getter;
import lombok.Setter;
import org.dreambot.api.script.ScriptManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientManager {
    private final String endpoint;
    private final String username;
    private final String botName;
    private final int scriptId;

    @Getter
    private boolean canRun = false;

    @Getter @Setter
    private boolean running = true;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ClientManager(String endpoint, String username, String botName, int scriptId) {
        this.endpoint = endpoint;
        this.username = username;
        this.botName = botName;
        this.scriptId = scriptId;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(this::checkStatus, 0, 2, TimeUnit.MINUTES);
    }

    public void stop() {
        running = false;
        scheduler.shutdownNow();
    }

    private void checkStatus() {
        if (!running) {
            return;
        }

        updateStatus();

        if (!canRun) {
            ScriptManager.getScriptManager().stop();
            stop();
        }
    }

    private void updateStatus() {
        try {
            String postData = String.format("username=%s&botname=%s&scriptid=%s",
                    URLEncoder.encode(username, String.valueOf(StandardCharsets.UTF_8)),
                    URLEncoder.encode(botName, String.valueOf(StandardCharsets.UTF_8)),
                    URLEncoder.encode(String.valueOf(scriptId), String.valueOf(StandardCharsets.UTF_8)));

            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept", "application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData.getBytes(StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
            }

            String json = response.toString().trim();
            canRun = json.contains("\"success\":true");
        } catch (Exception e) {
            canRun = false;
        }
    }
}