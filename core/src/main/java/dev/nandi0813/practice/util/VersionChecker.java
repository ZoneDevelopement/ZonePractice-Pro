package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for detecting the running Bukkit/MC version.
 * Replaces the previous empty-enum pattern with a normal utility class.
 */
public enum VersionChecker {
    ;

    private static volatile BukkitVersion bukkitVersion;

    // Matches strings like "(MC: 1.8.8)" or "(MC: 1.21)"
    private static final Pattern MC_VERSION_PATTERN = Pattern.compile("\\(MC: ([0-9]+\\.[0-9]+(?:\\.[0-9]+)?)\\)");

    /**
     * Returns the detected BukkitVersion for the running server.
     * The result is cached after the first detection.
     */
    public static BukkitVersion getBukkitVersion() {
        if (bukkitVersion == null) {
            synchronized (VersionChecker.class) {
                if (bukkitVersion == null) {
                    final String versionString = Bukkit.getVersion();
                    final String mcVersion = extractMcVersion(versionString);

                    if (mcVersion == null) {
                        ZonePractice.getInstance().getLogger().warning("Could not extract MC version from: " + versionString);
                        bukkitVersion = null;
                        return null;
                    }

                    if (mcVersion.startsWith("1.21"))
                        bukkitVersion = BukkitVersion.v1_21_R3;
                    else {
                        // Unknown version - keep null but log for visibility
                        ZonePractice.getInstance().getLogger().warning("Unsupported MC version: " + mcVersion);
                        bukkitVersion = null;
                    }
                }
            }
        }
        return bukkitVersion;
    }

    private static String extractMcVersion(final String bukkitVersionString) {
        if (bukkitVersionString == null) return null;
        final Matcher m = MC_VERSION_PATTERN.matcher(bukkitVersionString);
        if (m.find()) return m.group(1);
        return null;
    }

    @Getter
    public enum BukkitVersion {
        v1_21_R3 // 1.21.X
    }

}
