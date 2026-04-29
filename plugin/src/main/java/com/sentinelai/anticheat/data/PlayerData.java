package com.sentinelai.anticheat.data;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class PlayerData {

    private final String playerName;
    private final String uuid;

    private double lastX, lastY, lastZ;
    private float lastYaw, lastPitch;
    private long lastMoveTime;
    private int violationLevel = 0;
    private int cps = 0;
    private int clickCount = 0;
    private long lastClickReset = System.currentTimeMillis();

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
        long now = System.currentTimeMillis();
        if (now - lastClickReset >= 1000) {
            cps = clickCount;
            clickCount = 0;
            lastClickReset = now;
        }
    }

    public void addVL(int amount) { violationLevel += amount; }
    public void resetVL() { violationLevel = 0; }

    public List<PositionRecord> getPositionBuffer() { return new ArrayList<>(positionBuffer); }
    public List<ActionRecord> getActionBuffer() { return new ArrayList<>(actionBuffer); }

    public String getPlayerName() { return playerName; }
    public String getUuid() { return uuid; }
    public int getViolationLevel() { return violationLevel; }
    public int getCps() { return cps; }
    public double getLastX() { return lastX; }
    public double getLastY() { return lastY; }
    public double getLastZ() { return lastZ; }
    public float getLastYaw() { return lastYaw; }
    public float getLastPitch() { return lastPitch; }
    public long getLastMoveTime() { return lastMoveTime; }

    public static class PositionRecord {
        public final double x, y, z;
        public final float yaw, pitch;
        public final long timestamp;
        public PositionRecord(double x, double y, double z, float yaw, float pitch, long timestamp) {
            this.x = x; this.y = y; this.z = z;
            this.yaw = yaw; this.pitch = pitch;
            this.timestamp = timestamp;
        }
    }

    public static class ActionRecord {
        public final String action;
        public final long timestamp;
        public ActionRecord(String action, long timestamp) {
            this.action = action;
            this.timestamp = timestamp;
        }
    }
}
