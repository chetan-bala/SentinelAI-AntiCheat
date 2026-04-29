package com.sentinelai.anticheat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sentinelai.anticheat.managers.APIManager;
import com.sentinelai.anticheat.managers.ClipManager;
import com.sentinelai.anticheat.managers.DetectionManager;
import com.sentinelai.anticheat.managers.PlayerDataManager;
import com.sentinelai.anticheat.data.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class SentinelAI extends JavaPlugin {

    private static SentinelAI instance;
    private Gson gson;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private DetectionManager detectionManager;
    private ClipManager clipManager;
    private APIManager apiManager;
    private boolean aiEnabled;

    @Override
    public void onEnable() {
        instance = this;
        gson = new GsonBuilder().setPrettyPrinting().create();

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.load();

        playerDataManager = new PlayerDataManager(this);
        detectionManager = new DetectionManager(this);
        clipManager = new ClipManager(this);
        apiManager = new APIManager(this);

        registerEvents();
        registerCommands();

        logStartup();
    }

    @Override
    public void onDisable() {
        playerDataManager.saveAllData();
        clipManager.cleanup();
        getLogger().info("SentinelAI AntiCheat disabled!");
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(playerDataManager, this);
        Bukkit.getPluginManager().registerEvents(detectionManager, this);
    }

    private void registerCommands() {
        getCommand("sentinel").setExecutor(new SentinelCommand(this));
    }

    private void logStartup() {
        getLogger().info("========== SentinelAI AntiCheat ==========");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("AI System: " + (aiEnabled ? "ENABLED" : "DISABLED"));
        getLogger().info("API Endpoint: " + configManager.getApiUrl());
        getLogger().info("==========================================");
    }

    public void sendFlag(String playerName, String reason, int vl, String clipData) {
        getLogger().warning("FLAG: " + playerName + " | " + reason + " | VL: " + vl);
        if (configManager.isApiEnabled()) {
            apiManager.sendFlag(playerName, reason, vl, clipData);
        }
        if (configManager.isDiscordEnabled()) {
            apiManager.sendDiscordAlert(playerName, reason, vl);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sentinelai.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "reload":
                configManager.load();
                sender.sendMessage("§aConfig reloaded!");
                break;
            case "status":
                sender.sendMessage("§6AI: " + (aiEnabled ? "§aENABLED" : "§cDISABLED"));
                sender.sendMessage("§6API: " + (configManager.isApiEnabled() ? "§aENABLED" : "§cDISABLED"));
                break;
            default:
                sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6/sentinel reload - Reload config");
        sender.sendMessage("§6/sentinel status - Show status");
    }

    public static SentinelAI getInstance() { return instance; }
    public Gson getGson() { return gson; }
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public DetectionManager getDetectionManager() { return detectionManager; }
    public ClipManager getClipManager() { return clipManager; }
    public APIManager getApiManager() { return apiManager; }
    public boolean isAiEnabled() { return aiEnabled; }
}
