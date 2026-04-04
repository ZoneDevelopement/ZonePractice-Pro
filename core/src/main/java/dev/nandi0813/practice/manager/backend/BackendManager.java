package dev.nandi0813.practice.manager.backend;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;
import lombok.Getter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public enum BackendManager {
    ;

    private static final String PRACTICE_STATS_SERVER_HASH_PATH = "TELEMETRY.PRACTICE_STATS.SERVER_HASH";
    private static final String PRACTICE_STATS_SERVER_HASH_PREFIX = "ZPPV1_";

    private static File file;
    @Getter
    private static YamlConfiguration config;

    public static void createFile(ZonePractice practice) {
        file = new File(practice.getDataFolder(), "backend.yml");
        config = new YamlConfiguration();

        // Only try to load if file exists, and catch errors if worlds aren't loaded yet
        if (file.exists()) {
            try {
                config.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                // Ignore errors during initial load - this is expected if worlds aren't loaded yet
                // The file will be reloaded later when loadLobby() is called
                Common.sendConsoleMMMessage("<yellow>Backend file exists but couldn't be fully loaded yet (worlds may not be ready). Will retry during initialization.");
            }
        } else {
            // Create new file if it doesn't exist
            save();
        }
    }

    public static void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            Common.sendConsoleMMMessage("<red>Error saving backend.yml: " + e.getMessage());
        } catch (Exception e) {
            // Catches NullPointerException thrown by SnakeYAML when a ConfigurationSerializable
            // (e.g. Location with a null world WeakReference) produces a null entry in its
            // serialize() map. Log clearly instead of letting the async task swallow the trace.
            Common.sendConsoleMMMessage("<red>Error serializing backend.yml (possible null world in lobby location): " + e.getMessage());
        }
    }

    public static void reload() {
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            Common.sendConsoleMMMessage("<red>Error reloading backend: " + e.getMessage());
        }
    }

    public static String getString(String loc) {
        return getConfig().getString(StringUtil.CC(loc));
    }

    public static boolean getBoolean(String loc) {
        return getConfig().getBoolean(loc);
    }

    public static int getInt(String loc) {
        return getConfig().getInt(loc);
    }

    public static double getDouble(String loc) {
        return getConfig().getDouble(loc);
    }

    public static Set<String> getConfigSectionKeys(String loc) {
        return Objects.requireNonNull(getConfig().getConfigurationSection(loc)).getKeys(false);
    }

    public static List<String> getList(String loc) {
        return getConfig().getStringList(loc);
    }

    public static String getPracticeStatsServerHash() {
        if (config == null) {
            return null;
        }

        String configuredHash = config.getString(PRACTICE_STATS_SERVER_HASH_PATH);
        if (configuredHash == null) {
            return null;
        }

        String normalized = normalizePracticeStatsServerHash(configuredHash);
        if (normalized == null) {
            return null;
        }

        // Migrate legacy values so next restart keeps the same prefixed format.
        if (!normalized.equals(configuredHash)) {
            config.set(PRACTICE_STATS_SERVER_HASH_PATH, normalized);
            save();
        }

        return normalized;
    }

    public static String getOrCreatePracticeStatsServerHash(String generatedHash) {
        String existing = getPracticeStatsServerHash();
        if (existing != null) {
            return existing;
        }

        String normalizedGenerated = normalizePracticeStatsServerHash(generatedHash);
        if (normalizedGenerated == null) {
            return null;
        }

        config.set(PRACTICE_STATS_SERVER_HASH_PATH, normalizedGenerated);
        save();
        return normalizedGenerated;
    }

    private static String normalizePracticeStatsServerHash(String hash) {
        if (hash == null) {
            return null;
        }

        String trimmed = hash.trim();
        if (trimmed.isEmpty()) {
            return null;
        }

        return trimmed.startsWith(PRACTICE_STATS_SERVER_HASH_PREFIX)
                ? trimmed
                : PRACTICE_STATS_SERVER_HASH_PREFIX + trimmed;
    }

}
