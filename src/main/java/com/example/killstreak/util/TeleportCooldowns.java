package com.example.killstreak.util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TeleportCooldowns {
    private static final TeleportCooldowns instance = new TeleportCooldowns();
    private final Map<UUID, Long> map = new ConcurrentHashMap<>();
    public static TeleportCooldowns get() { return instance; }
    public boolean isOnCooldown(UUID id) { return map.containsKey(id) && System.currentTimeMillis() < map.get(id); }
    public void setCooldown(UUID id, long durationMs) { map.put(id, System.currentTimeMillis() + durationMs); }
}
