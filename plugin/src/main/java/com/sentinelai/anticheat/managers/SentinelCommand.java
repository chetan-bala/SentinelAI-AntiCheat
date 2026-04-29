package com.sentinelai.anticheat.managers;

import com.sentinelai.anticheat.SentinelAI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class SentinelCommand implements CommandExecutor, TabCompleter {

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
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().load();
                sender.sendMessage("§aSentinelAI configuration reloaded!");
                break;
            case "status":
                sender.sendMessage("§6=== SentinelAI Status ===");
                sender.sendMessage("§6AI System: " + (plugin.isAiEnabled() ? "§aENABLED" : "§cDISABLED"));
                sender.sendMessage("§6API: " + (plugin.getConfigManager().isApiEnabled() ? "§aENABLED" : "§cDISABLED"));
                sender.sendMessage("§6Backend: §f" + plugin.getConfigManager().getApiUrl());
                break;
            case "version":
                sender.sendMessage("§aSentinelAI v" + plugin.getDescription().getVersion());
                break;
            default:
                sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== SentinelAI Commands ===");
        sender.sendMessage("§e/sentinel reload - Reload configuration");
        sender.sendMessage("§e/sentinel status - Show system status");
        sender.sendMessage("§e/sentinel version - Show version info");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            completions.add("reload");
            completions.add("status");
            completions.add("version");
            return completions;
        }
        return new ArrayList<>();
    }
}
