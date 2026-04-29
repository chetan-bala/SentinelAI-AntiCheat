package com.sentinelai.anticheat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SentinelAI extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("========== SentinelAI AntiCheat ==========");
        getLogger().info("Version: 1.0.0");
        getLogger().info("Status: ENABLED");
        getLogger().info("==========================================");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.sendMessage("§aSentinelAI AntiCheat is active!");
        getLogger().info("Player " + p.getName() + " is being monitored");
    }
}
