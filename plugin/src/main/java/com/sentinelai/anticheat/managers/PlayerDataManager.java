package com.sentinelai.anticheat.managers;

import com.sentinelai.anticheat.SentinelAI;
import com.sentinelai.anticheat.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager implements Listener {

    private final SentinelAI plugin;
    private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

    public PlayerDataManager(SentinelAI plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PlayerData data = new PlayerData(p.getName(), p.getUniqueId().toString());
        playerDataMap.put(p.getUniqueId(), data);
        Location loc = p.getLocation();
        data.recordPosition(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        playerDataMap.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        PlayerData data = playerDataMap.get(p.getUniqueId());
        if (data == null) return;
        Location to = e.getTo();
        data.recordPosition(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        PlayerData data = playerDataMap.get(e.getPlayer().getUniqueId());
        if (data == null) return;
        data.incrementClicks();
        data.recordAction("INTERACT");
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        PlayerData data = playerDataMap.get(p.getUniqueId());
        if (data == null) return;
        data.incrementClicks();
        data.recordAction("ATTACK");
    }

    public PlayerData getPlayerData(Player p) {
        return playerDataMap.get(p.getUniqueId());
    }

    public void saveAllData() {
        playerDataMap.clear();
    }
}
