package com.sentinelai.anticheat.managers;

import com.sentinelai.anticheat.SentinelAI;
import com.sentinelai.anticheat.data.ConfigManager;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class APIManager {

    private final SentinelAI plugin;

    public APIManager(SentinelAI plugin) {
        this.plugin = plugin;
    }

    public void sendFlag(String playerName, String reason, int vl, String clipData) {
        new Thread(() -> {
            try {
                ConfigManager config = plugin.getConfigManager();
                URL url = new URL(config.getApiUrl() + "/flag");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-Key", config.getApiKey());
                conn.setDoOutput(true);

                String payload = String.format(
                        "{\"playerName\":\"%s\",\"reason\":\"%s\",\"vl\":%d,\"clipData\":%s}",
                        playerName, reason, vl, clipData
                );

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200 && responseCode != 201) {
                    plugin.getLogger().warning("API Error: HTTP " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("API Error: " + e.getMessage());
            }
        }).start();
    }

    public void sendDiscordAlert(String playerName, String reason, int vl) {
        if (!plugin.getConfigManager().isDiscordEnabled()) return;

        new Thread(() -> {
            try {
                URL url = new URL(plugin.getConfigManager().getDiscordWebhook());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                String payload = String.format(
                        "{\"content\":\"**SentinelAI Flag**\\nPlayer: %s\\nReason: %s\\nVL: %d\"}",
                        playerName, reason, vl
                );

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Discord Error: " + e.getMessage());
            }
        }).start();
    }
}
