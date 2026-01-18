package dev.nandi0813.practice.manager.gui;

import lombok.Getter;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Caching system for public GUIs (leaderboards, etc.) to prevent performance issues
 * from regenerating data every time a player opens them.
 * <p>
 * Cache expires after configurable time (default 5 minutes).
 */
public class GUICache {

    private static final long DEFAULT_CACHE_DURATION_MS = TimeUnit.MINUTES.toMillis(5);

    @Getter
    private static class CachedGUI {
        private final Map<Integer, Inventory> inventories;
        private final long cachedAt;
        private final long cacheDurationMs;

        public CachedGUI(Map<Integer, Inventory> inventories, long cacheDurationMs) {
            this.inventories = new HashMap<>(inventories);
            this.cachedAt = System.currentTimeMillis();
            this.cacheDurationMs = cacheDurationMs;
        }

        public boolean isExpired() {
            return (System.currentTimeMillis() - cachedAt) >= cacheDurationMs;
        }

        public long getTimeUntilExpiry() {
            long elapsed = System.currentTimeMillis() - cachedAt;
            long remaining = cacheDurationMs - elapsed;
            return Math.max(0, remaining);
        }
    }

    // Cache storage: GUIType -> CachedGUI
    private static final Map<GUIType, CachedGUI> cache = new HashMap<>();

    /**
     * Check if cached GUI exists and is still valid.
     *
     * @param type The GUI type
     * @return true if cache exists and not expired
     */
    public static boolean isCacheValid(GUIType type) {
        CachedGUI cached = cache.get(type);
        if (cached == null) {
            return false;
        }

        if (cached.isExpired()) {
            // Auto-cleanup expired cache
            cache.remove(type);
            return false;
        }

        return true;
    }

    /**
     * Get cached inventories if available and valid.
     *
     * @param type The GUI type
     * @return Cached inventories map, or null if not cached/expired
     */
    public static Map<Integer, Inventory> getCached(GUIType type) {
        if (!isCacheValid(type)) {
            return null;
        }

        return cache.get(type).getInventories();
    }

    /**
     * Store inventories in cache with default duration (5 minutes).
     *
     * @param type        The GUI type
     * @param inventories The inventories to cache
     */
    public static void putCache(GUIType type, Map<Integer, Inventory> inventories) {
        putCache(type, inventories, DEFAULT_CACHE_DURATION_MS);
    }

    /**
     * Store inventories in cache with custom duration.
     *
     * @param type        The GUI type
     * @param inventories The inventories to cache
     * @param durationMs  Cache duration in milliseconds
     */
    public static void putCache(GUIType type, Map<Integer, Inventory> inventories, long durationMs) {
        cache.put(type, new CachedGUI(inventories, durationMs));
    }

    /**
     * Invalidate (clear) cache for specific GUI type.
     *
     * @param type The GUI type to invalidate
     */
    public static void invalidate(GUIType type) {
        cache.remove(type);
    }

    /**
     * Clear all cached GUIs.
     */
    public static void clearAll() {
        cache.clear();
    }

    /**
     * Get time remaining until cache expires (in seconds).
     *
     * @param type The GUI type
     * @return Seconds until expiry, or 0 if not cached/expired
     */
    public static long getTimeUntilExpiry(GUIType type) {
        CachedGUI cached = cache.get(type);
        if (cached == null || cached.isExpired()) {
            return 0;
        }

        return TimeUnit.MILLISECONDS.toSeconds(cached.getTimeUntilExpiry());
    }

    /**
     * Check if a GUI type should be cached.
     * Only public GUIs (leaderboards, etc.) should be cached.
     *
     * @param type The GUI type
     * @return true if this GUI type should use caching
     */
    public static boolean shouldCache(GUIType type) {
        return switch (type) {
            case Leaderboard_ELO,
                 Leaderboard_WIN,
                 Leaderboard_Selector,
                 Leaderboard_Profile -> true;
            default -> false;
        };
    }
}
