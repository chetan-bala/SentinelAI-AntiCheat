package com.sentinelai.anticheat.managers;

import com.sentinelai.anticheat.SentinelAI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class SentinelCommand implements CommandExecutor {

    private final SentinelAI plugin;

    public SentinelCommand(SentinelAI plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sentinelai.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6SentinelAI AntiCheat v1.0.0");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getConfigManager().load();
            sender.sendMessage("§aConfiguration reloaded!");
            return true;
        }

        return true;
    }
}
