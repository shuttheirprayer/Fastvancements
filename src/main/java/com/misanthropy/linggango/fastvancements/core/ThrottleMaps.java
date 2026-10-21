package com.misanthropy.linggango.fastvancements.core;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry of per-player throttle maps so their entries can be released when a player logs
 * out, preventing unbounded growth on long-running servers.
 */
public final class ThrottleMaps {

    private static final List<Map<UUID, ?>> MAPS = new CopyOnWriteArrayList<>();

    private ThrottleMaps() {}

    /** Registers a per-player map for lifecycle cleanup and returns it for convenient assignment. */
    public static <T extends Map<UUID, ?>> T track(T map) {
        MAPS.add(map);
        return map;
    }

    /** Drops all cached state for a player across every registered map. */
    public static void purge(UUID player) {
        for (Map<UUID, ?> map : MAPS) {
            map.remove(player);
        }
    }
}
