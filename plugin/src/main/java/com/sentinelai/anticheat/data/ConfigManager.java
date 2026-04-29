package com.sentinelai.anticheat.data;

import com.sentinelai.anticheat.SentinelAI;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

    private final SentinelAI plugin;
    private String apiUrl;
    private String apiKey;
    private boolean apiEnabled;
    private boolean discordEnabled;
    private String discordWebhook;

    public ConfigManager(SentinelAI plugin) {
        this.plugin = plugin;
    }

    public void load(String apiUrl, String apiKey) {
        FileConfiguration config = plugin.getConfig();
        this.apiUrl = config.getString("api.url", apiUrl);
        this.apiKey = config.getString("api.key", apiKey);
        this.apiEnabled = config.getBoolean("api.enabled", true);
        this.discordEnabled = config.getBoolean("discord.enabled", false);
        this.discordWebhook = config.getString("discord.webhook", "");
    }

    public String getApiUrl() { return apiUrl; }
    public String getApiKey() { return apiKey; }
    public boolean isApiEnabled() { return apiEnabled; }
    public boolean isDiscordEnabled() { return discordEnabled; }
    public String getDiscordWebhook() { return discordWebhook; }

    public int getMaxVL(String check) {
        return plugin.getConfig().getInt("checks." + check + ".max-vl", 10);
    }

    public boolean isCheckEnabled(String check) {
        return plugin.getConfig().getBoolean("checks." + check + ".enabled", true);
    }
}
