// SentinelAI AntiCheat - Complete Paper Plugin (All-in-One File)
// Minecraft Anti-Cheat with AI, Clip Recording, and Web Dashboard
// Free Hosting Ready: Render (Backend) + Vercel (Frontend) + GitHub

package com.sentinelai.anticheat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * MAIN PLUGIN CLASS
 * Initializes all systems and manages plugin lifecycle
 */
public class SentinelAI extends JavaPlugin {

    private static SentinelAI instance;
    private Gson gson;
    private ConfigManager configManager;
    private PlayerDataManager playerDataManager;
    private DetectionManager detectionManager;
    private ClipManager clipManager;
    private APIManager apiManager;
    public boolean aiEnabled;

    @Override
    public void onEnable() {
        instance = this;
        gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.load();

        playerDataManager = new PlayerDataManager(this);
        detectionManager = new DetectionManager(this);
        clipManager = new ClipManager(this);
        apiManager = new APIManager(this);

        Bukkit.getPluginManager().registerEvents(playerDataManager, this);
        Bukkit.getPluginManager().registerEvents(detectionManager, this);

        getCommand("sentinel").setExecutor(new SentinelCommand(this));

        getLogger().info("========== SentinelAI AntiCheat ==========");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info("AI System: " + (aiEnabled ? "ENABLED" : "DISABLED"));
        getLogger().info("API Endpoint: " + configManager.getApiUrl());
        getLogger().info("==========================================");
    }

    @Override
    public void onDisable() {
        playerDataManager.saveAllData();
        clipManager.cleanup();
        getLogger().info("SentinelAI AntiCheat disabled!");
    }

    public void sendFlag(String playerName, String reason, int vl, String clipData) {
        getLogger().warning("FLAG: " + playerName + " | " + reason + " | VL: " + vl);
        if (configManager.isApiEnabled()) apiManager.sendFlag(playerName, reason, vl, clipData);
        if (configManager.isDiscordEnabled()) apiManager.sendDiscordAlert(playerName, reason, vl);
    }

    public static SentinelAI getInstance() { return instance; }
    public Gson getGson() { return gson; }
    public ConfigManager getConfigManager() { return configManager; }
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public DetectionManager getDetectionManager() { return detectionManager; }
    public ClipManager getClipManager() { return clipManager; }
    public APIManager getApiManager() { return apiManager; }
}

/**
 * CONFIG MANAGER
 * Handles loading settings from config.yml
 */
class ConfigManager {
    private final SentinelAI plugin;
    public String apiUrl, apiKey, discordWebhook;
    public boolean apiEnabled, discordEnabled;

    public ConfigManager(SentinelAI plugin) { this.plugin = plugin; }

    public void load() {
        plugin.reloadConfig();
        apiUrl = plugin.getConfig().getString("api.url", "https://your-backend.onrender.com");
        apiKey = plugin.getConfig().getString("api.key", "sentinel-free-key-12345");
        apiEnabled = plugin.getConfig().getBoolean("api.enabled", true);
        discordEnabled = plugin.getConfig().getBoolean("discord.enabled", false);
        discordWebhook = plugin.getConfig().getString("discord.webhook", "");
        plugin.aiEnabled = plugin.getConfig().getBoolean("ai.enabled", true);
    }

    public int getMaxVL(String check) { return plugin.getConfig().getInt("checks." + check + ".max-vl", 10); }
    public boolean isCheckEnabled(String check) { return plugin.getConfig().getBoolean("checks." + check + ".enabled", true); }
    public String getApiUrl() { return apiUrl; }
    public String getApiKey() { return apiKey; }
    public boolean isApiEnabled() { return apiEnabled; }
    public boolean isDiscordEnabled() { return discordEnabled; }
    public String getDiscordWebhook() { return discordWebhook; }
}

/**
 * PLAYER DATA CLASS
 * Stores all tracked data for a player (position, CPS, VL, buffers)
 */
class PlayerData {
    public final String playerName, uuid;
    public double lastX, lastY, lastZ;
    public float lastYaw, lastPitch;
    public long lastMoveTime;
    public int violationLevel = 0;
    public int cps = 0, clickCount = 0;
    public long lastClickReset = System.currentTimeMillis();

    // 30-second circular buffers (600 ticks at 20 TPS)
    private final Deque<PositionRecord> positionBuffer = new ArrayDeque<>(600);
    private final Deque<ActionRecord> actionBuffer = new ArrayDeque<>(600);

    public PlayerData(String playerName, String uuid) {
        this.playerName = playerName;
        this.uuid = uuid;
    }

    public void recordPosition(double x, double y, double z, float yaw, float pitch) {
        PositionRecord record = new PositionRecord(x, y, z, yaw, pitch, System.currentTimeMillis());
        if (positionBuffer.size() >= 600) positionBuffer.pollFirst();
        positionBuffer.addLast(record);
        lastX = x; lastY = y; lastZ = z;
        lastYaw = yaw; lastPitch = pitch;
        lastMoveTime = System.currentTimeMillis();
    }

    public void recordAction(String action) {
        ActionRecord record = new ActionRecord(action, System.currentTimeMillis());
        if (actionBuffer.size() >= 600) actionBuffer.pollFirst();
        actionBuffer.addLast(record);
    }

    public void incrementClicks() {
        clickCount++;
        if (System.currentTimeMillis() - lastClickReset >= 1000) {
            cps = clickCount;
            clickCount = 0;
            lastClickReset = System.currentTimeMillis();
        }
    }

    public void addVL(int amount) { violationLevel += amount; }
    public void resetVL() { violationLevel = 0; }

    public List<PositionRecord> getPositionBuffer() { return new ArrayList<>(positionBuffer); }
    public List<ActionRecord> getActionBuffer() { return new ArrayList<>(actionBuffer); }

    // Inner classes for buffer records
    static class PositionRecord {
        @Expose public double x, y, z;
        @Expose public float yaw, pitch;
        @Expose public long timestamp;
        public PositionRecord(double x, double y, double z, float yaw, float pitch, long timestamp) {
            this.x = x; this.y = y; this.z = z;
            this.yaw = yaw; this.pitch = pitch;
            this.timestamp = timestamp;
        }
    }

    static class ActionRecord {
        @Expose public String action;
        @Expose public long timestamp;
        public ActionRecord(String action, long timestamp) {
            this.action = action;
            this.timestamp = timestamp;
        }
    }
}

/**
 * PLAYER DATA MANAGER
 * Tracks player joins, quits, movement, clicks
 */
class PlayerDataManager implements Listener {
    private final SentinelAI plugin;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    public PlayerDataManager(SentinelAI plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerData data = new PlayerData(p.getName(), p.getUniqueId().toString());
        playerDataMap.put(p.getUniqueId(), data);
        Location loc = p.getLocation();
        data.recordPosition(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) { playerDataMap.remove(e.getPlayer().getUniqueId()); }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        PlayerData data = playerDataMap.get(p.getUniqueId());
        if (data == null) return;
        Location to = e.getTo();
        if (to != null && !e.getFrom().toVector().equals(to.toVector())) {
            data.recordPosition(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK) {
            PlayerData data = playerDataMap.get(e.getPlayer().getUniqueId());
            if (data != null) { data.incrementClicks(); data.recordAction("INTERACT"); }
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player) {
            PlayerData data = playerDataMap.get(((Player) e.getDamager()).getUniqueId());
            if (data != null) { data.incrementClicks(); data.recordAction("ATTACK"); }
        }
    }

    public PlayerData getPlayerData(Player p) { return playerDataMap.get(p.getUniqueId()); }
    public void saveAllData() { playerDataMap.clear(); }
}

/**
 * DETECTION MANAGER
 * Runs all cheat checks: Speed, Fly, KillAura, Reach, NoSlow, AutoClicker
 */
class DetectionManager implements Listener {
    private final SentinelAI plugin;
    private final Map<Player, Long> lastSpeedCheck = new HashMap<>();
    private final Map<Player, Integer> reachViolations = new HashMap<>();

    public DetectionManager(SentinelAI plugin) { this.plugin = plugin; }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (p.isOp() || p.getGameMode().name().contains("CREATIVE")) return;
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(p);
        if (data == null) return;

        checkSpeed(p, data);
        checkFly(p, data);
        checkNoSlow(p, data);
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)) return;
        Player attacker = (Player) e.getDamager();
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(attacker);
        if (data == null || attacker.isOp() || attacker.getGameMode().name().contains("CREATIVE")) return;

        checkKillAura(attacker, data);
        checkReach(attacker, (Player) e.getEntity(), data);
        checkAutoClicker(attacker, data);
    }

    // SPEED CHECK: Detects abnormal horizontal movement
    private void checkSpeed(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("speed")) return;
        long now = System.currentTimeMillis();
        if (now - lastSpeedCheck.getOrDefault(p, 0L) < 100) return;
        lastSpeedCheck.put(p, now);

        double deltaX = p.getLocation().getX() - data.lastX;
        double deltaZ = p.getLocation().getZ() - data.lastZ;
        double dist = Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
        double maxSpeed = p.isSprinting() ? 0.35 : 0.25;
        if (p.isFlying()) maxSpeed = 0.5;

        if (dist > maxSpeed) {
            data.addVL(1);
            if (data.violationLevel >= plugin.getConfigManager().getMaxVL("speed")) {
                plugin.sendFlag(p.getName(), "Speed", data.violationLevel, plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else data.resetVL();
    }

    // FLY CHECK: Detects flying without permission
    private void checkFly(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("fly")) return;
        if (p.isFlying() || p.getAllowFlight() || p.isOnGround() || p.isInWater() || p.isInLava()) return;
        double deltaY = p.getLocation().getY() - data.lastY;
        if (deltaY > 0.5) {
            data.addVL(1);
            if (data.violationLevel >= plugin.getConfigManager().getMaxVL("fly")) {
                plugin.sendFlag(p.getName(), "Fly", data.violationLevel, plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else data.resetVL();
    }

    // KILLAURA CHECK: Detects impossible rotation speeds
    private void checkKillAura(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("killaura")) return;
        float deltaYaw = Math.abs(p.getLocation().getYaw() - data.lastYaw);
        float deltaPitch = Math.abs(p.getLocation().getPitch() - data.lastPitch);
        if (deltaYaw > 30 || deltaPitch > 30) {
            data.addVL(1);
            if (data.violationLevel >= plugin.getConfigManager().getMaxVL("killaura")) {
                plugin.sendFlag(p.getName(), "KillAura", data.violationLevel, plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else data.resetVL();
    }

    // REACH CHECK: Detects attacks from too far away
    private void checkReach(Player attacker, Player target, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("reach")) return;
        double dist = attacker.getLocation().distance(target.getLocation());
        if (dist > 3.5) {
            int vl = reachViolations.getOrDefault(attacker, 0) + 1;
            reachViolations.put(attacker, vl);
            if (vl >= plugin.getConfigManager().getMaxVL("reach")) {
                plugin.sendFlag(attacker.getName(), "Reach", vl, plugin.getClipManager().generateClip(data));
                reachViolations.put(attacker, 0);
            }
        } else reachViolations.put(attacker, 0);
    }

    // NOSLOW CHECK: Detects not slowing when blocking
    private void checkNoSlow(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("noslow")) return;
        if (!p.isBlocking() && !p.isHandRaised()) return;
        double deltaX = p.getLocation().getX() - data.lastX;
        double deltaZ = p.getLocation().getZ() - data.lastZ;
        double dist = Math.sqrt(deltaX*deltaX + deltaZ*deltaZ);
        double maxSpeed = (p.isSprinting() ? 0.35 : 0.25) * 0.6;
        if (dist > maxSpeed) {
            data.addVL(1);
            if (data.violationLevel >= plugin.getConfigManager().getMaxVL("noslow")) {
                plugin.sendFlag(p.getName(), "NoSlow", data.violationLevel, plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else data.resetVL();
    }

    // AUTOCLICKER CHECK: Detects impossible CPS
    private void checkAutoClicker(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("autoclicker")) return;
        if (data.cps > 20) {
            data.addVL(1);
            if (data.violationLevel >= plugin.getConfigManager().getMaxVL("autoclicker")) {
                plugin.sendFlag(p.getName(), "AutoClicker", data.violationLevel, plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else data.resetVL();
    }
}

/**
 * CLIP MANAGER
 * Generates JSON clips from player buffers (last 30 seconds)
 */
class ClipManager {
    private final SentinelAI plugin;

    public ClipManager(SentinelAI plugin) { this.plugin = plugin; }

    public String generateClip(PlayerData data) {
        Clip clip = new Clip();
        clip.playerName = data.playerName;
        clip.uuid = data.uuid;
        clip.timestamp = System.currentTimeMillis();
        clip.positions = data.getPositionBuffer();
        clip.actions = data.getActionBuffer();
        return plugin.getGson().toJson(clip);
    }

    public void cleanup() {}

    static class Clip {
        @Expose public String playerName, uuid;
        @Expose public long timestamp;
        @Expose public List<PlayerData.PositionRecord> positions;
        @Expose public List<PlayerData.ActionRecord> actions;
    }
}

/**
 * API MANAGER
 * Sends flags to backend and Discord webhooks
 */
class APIManager {
    private final SentinelAI plugin;

    public APIManager(SentinelAI plugin) { this.plugin = plugin; }

    public void sendFlag(String playerName, String reason, int vl, String clipData) {
        new Thread(() -> {
            try {
                URL url = new URL(plugin.getConfigManager().getApiUrl() + "/flag");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("X-API-Key", plugin.getConfigManager().getApiKey());
                conn.setDoOutput(true);

                String payload = String.format(
                    "{\"playerName\":\"%s\",\"reason\":\"%s\",\"vl\":%d,\"clipData\":%s}",
                    playerName, reason, vl, clipData
                );

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }

                if (conn.getResponseCode() != 200)
                    plugin.getLogger().warning("API Error: HTTP " + conn.getResponseCode());
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

                String payload = "{\"content\":\"**SentinelAI Flag**\\nPlayer: " + playerName + "\\nReason: " + reason + "\\nVL: " + vl + "\"}";

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.getBytes(StandardCharsets.UTF_8));
                }
                conn.disconnect();
            } catch (Exception e) {
                plugin.getLogger().warning("Discord Error: " + e.getMessage());
            }
        }).start();
    }
}

/**
 * COMMAND HANDLER
 * /sentinel reload | status | flags
 */
class SentinelCommand implements Command {
    private final SentinelAI plugin;

    public SentinelCommand(SentinelAI plugin) { this.plugin = plugin; }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("sentinelai.admin")) {
            sender.sendMessage("§cNo permission!");
            return true;
        }

        if (args.length == 0) { sendHelp(sender); return true; }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().load();
                sender.sendMessage("§aConfig reloaded!");
                break;
            case "status":
                sender.sendMessage("§6AI: " + plugin.aiEnabled);
                sender.sendMessage("§6API: " + plugin.getConfigManager().isApiEnabled());
                break;
            default: sendHelp(sender);
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6/sentinel reload - Reload config");
        sender.sendMessage("§6/sentinel status - Show status");
    }

    @Override public String getName() { return "sentinel"; }
    @Override public String getDescription() { return "SentinelAI command"; }
    @Override public String getUsage() { return "/sentinel <reload|status>"; }
    @Override public List<String> getAliases() { return Collections.emptyList(); }
    @Override public String getPermission() { return "sentinelai.admin"; }
    @Override public String getPermissionMessage() { return "§cNo permission!"; }
    @Override public List<String> tabComplete(CommandSender sender, String alias, String[] args) { return Collections.emptyList(); }
}
