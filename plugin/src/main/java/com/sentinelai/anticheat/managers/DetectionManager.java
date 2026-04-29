package com.sentinelai.anticheat.managers;

import com.sentinelai.anticheat.SentinelAI;
import com.sentinelai.anticheat.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DetectionManager implements Listener {

    private final SentinelAI plugin;
    private final Map<UUID, Long> lastSpeedCheck = new HashMap<>();
    private final Map<UUID, Integer> reachViolations = new HashMap<>();
    private final Map<UUID, Float> lastYaw = new HashMap<>();
    private final Map<UUID, Float> lastPitch = new HashMap<>();

    public DetectionManager(SentinelAI plugin) {
        this.plugin = plugin;
    }

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
        Player target = (Player) e.getEntity();

        if (attacker.isOp() || attacker.getGameMode().name().contains("CREATIVE")) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(attacker);
        if (data == null) return;

        checkKillAura(attacker, data);
        checkReach(attacker, target, data);
        checkAutoClicker(attacker, data);
    }

    private void checkSpeed(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("speed")) return;

        long now = System.currentTimeMillis();
        if (now - lastSpeedCheck.getOrDefault(p.getUniqueId(), 0L) < 100) return;
        lastSpeedCheck.put(p.getUniqueId(), now);

        double deltaX = p.getLocation().getX() - data.getLastX();
        double deltaZ = p.getLocation().getZ() - data.getLastZ();
        double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double maxSpeed = p.isSprinting() ? 0.35 : 0.25;
        if (p.isFlying() || p.getAllowFlight()) maxSpeed = 0.5;

        if (dist > maxSpeed) {
            data.addVL(1);
            if (data.getViolationLevel() >= plugin.getConfigManager().getMaxVL("speed")) {
                plugin.sendFlag(p.getName(), "Speed", data.getViolationLevel(),
                        plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else {
            data.resetVL();
        }
    }

    private void checkFly(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("fly")) return;
        if (p.isFlying() || p.getAllowFlight() || p.isOnGround() || p.isInWater() || p.isInLava()) return;

        double deltaY = p.getLocation().getY() - data.getLastY();
        if (deltaY > 0.5) {
            data.addVL(1);
            if (data.getViolationLevel() >= plugin.getConfigManager().getMaxVL("fly")) {
                plugin.sendFlag(p.getName(), "Fly", data.getViolationLevel(),
                        plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else {
            data.resetVL();
        }
    }

    private void checkKillAura(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("killaura")) return;

        float currentYaw = p.getLocation().getYaw();
        float currentPitch = p.getLocation().getPitch();
        float lastY = lastYaw.getOrDefault(p.getUniqueId(), currentYaw);
        float lastP = lastPitch.getOrDefault(p.getUniqueId(), currentPitch);

        float deltaYaw = Math.abs(currentYaw - lastY);
        float deltaPitch = Math.abs(currentPitch - lastP);

        lastYaw.put(p.getUniqueId(), currentYaw);
        lastPitch.put(p.getUniqueId(), currentPitch);

        if (deltaYaw > 30 || deltaPitch > 30) {
            data.addVL(1);
            if (data.getViolationLevel() >= plugin.getConfigManager().getMaxVL("killaura")) {
                plugin.sendFlag(p.getName(), "KillAura", data.getViolationLevel(),
                        plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else {
            data.resetVL();
        }
    }

    private void checkReach(Player attacker, Player target, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("reach")) return;

        double dist = attacker.getLocation().distance(target.getLocation());
        if (dist > 3.5) {
            int vl = reachViolations.getOrDefault(attacker.getUniqueId(), 0) + 1;
            reachViolations.put(attacker.getUniqueId(), vl);
            if (vl >= plugin.getConfigManager().getMaxVL("reach")) {
                plugin.sendFlag(attacker.getName(), "Reach", vl,
                        plugin.getClipManager().generateClip(data));
                reachViolations.put(attacker.getUniqueId(), 0);
            }
        } else {
            reachViolations.put(attacker.getUniqueId(), 0);
        }
    }

    private void checkNoSlow(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("noslow")) return;
        if (!p.isBlocking() && !p.isHandRaised()) return;

        double deltaX = p.getLocation().getX() - data.getLastX();
        double deltaZ = p.getLocation().getZ() - data.getLastZ();
        double dist = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        double maxSpeed = (p.isSprinting() ? 0.35 : 0.25) * 0.6;
        if (dist > maxSpeed) {
            data.addVL(1);
            if (data.getViolationLevel() >= plugin.getConfigManager().getMaxVL("noslow")) {
                plugin.sendFlag(p.getName(), "NoSlow", data.getViolationLevel(),
                        plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else {
            data.resetVL();
        }
    }

    private void checkAutoClicker(Player p, PlayerData data) {
        if (!plugin.getConfigManager().isCheckEnabled("autoclicker")) return;

        if (data.getCps() > 20) {
            data.addVL(1);
            if (data.getViolationLevel() >= plugin.getConfigManager().getMaxVL("autoclicker")) {
                plugin.sendFlag(p.getName(), "AutoClicker", data.getViolationLevel(),
                        plugin.getClipManager().generateClip(data));
                data.resetVL();
            }
        } else {
            data.resetVL();
        }
    }
}
