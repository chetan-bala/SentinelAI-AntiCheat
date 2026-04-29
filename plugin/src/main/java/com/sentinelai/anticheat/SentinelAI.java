package com.sentinelai.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SentinelAI extends JavaPlugin implements Listener {

    private PlayerDataManager playerDataManager;
    private DetectionManager detectionManager;
    private ClipManager clipManager;
    private APIManager apiManager;
    private ConfigManager configManager;
    private boolean aiEnabled;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        configManager = new ConfigManager(this);
        configManager.load();
        
        playerDataManager = new PlayerDataManager(this);
        detectionManager = new DetectionManager(this);
        clipManager = new ClipManager(this);
        apiManager = new APIManager(this);
        
        Bukkit.getPluginManager().registerEvents(this, this);
        Bukkit.getPluginManager().registerEvents(playerDataManager, this);
        Bukkit.getPluginManager().registerEvents(detectionManager, this);
        
        aiEnabled = getConfig().getBoolean("ai.enabled", true);
        
        getLogger().info("========== SentinelAI AntiCheat ==========");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("AI System: " + (aiEnabled ? "ENABLED" : "DISABLED"));
        getLogger().info("==========================================");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.sendMessage("§aSentinelAI AntiCheat is active!");
        getLogger().info("Player " + p.getName() + " is being monitored");
    }
    
    public void sendFlag(String playerName, String reason, int vl, String clipData) {
        getLogger().warning("FLAG: " + playerName + " | " + reason + " | VL: " + vl);
    }
    
    // Getters
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public DetectionManager getDetectionManager() { return detectionManager; }
    public ClipManager getClipManager() { return clipManager; }
    public APIManager getApiManager() { return apiManager; }
    public boolean isAiEnabled() { return aiEnabled; }
}
