package com.sentinelai.anticheat.managers;

import com.google.gson.Gson;
import com.sentinelai.anticheat.SentinelAI;
import com.sentinelai.anticheat.data.PlayerData;

import java.util.List;

public class ClipManager {

    private final SentinelAI plugin;

    public ClipManager(SentinelAI plugin) {
        this.plugin = plugin;
    }

    public String generateClip(PlayerData data) {
        Clip clip = new Clip();
        clip.playerName = data.getPlayerName();
        clip.uuid = data.getUuid();
        clip.timestamp = System.currentTimeMillis();
        clip.positions = data.getPositionBuffer();
        clip.actions = data.getActionBuffer();
        return plugin.getGson().toJson(clip);
    }

    public void cleanup() {
        // Cleanup resources if needed
    }

    private static class Clip {
        public String playerName;
        public String uuid;
        public long timestamp;
        public List<PlayerData.PositionRecord> positions;
        public List<PlayerData.ActionRecord> actions;
    }
}
