package com.sentinelai.anticheat;

import com.sentinelai.anticheat.data.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SentinelAI extends JavaPlugin implements Listener {

    private ConfigManager configManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.load();

        Bukkit.getPluginManager().registerEvents(this, this);

        getLogger().info("========== SentinelAI AntiCheat ==========");
        getLogger().info("Version: 1.0.0");
        getLogger().info("Status: ENABLED");
        getLogger().info("==========================================");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("sentinel")) {
            if (args.length == 0) {
                sender.sendMessage("§6SentinelAI AntiCheat v1.0.0");
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                configManager.load();
                sender.sendMessage("§aConfiguration reloaded!");
                return true;
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.sendMessage("§aSentinelAI AntiCheat is monitoring you!");
        getLogger().info("Player " + p.getName() + " is being monitored");
    }
}
