package dev.nandi0813.practice.Module.Util;

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
                        Bukkit.getLogger().warning("Could not extract MC version from: " + versionString);
                        bukkitVersion = null;
                        return null;
                    }

                    if (mcVersion.startsWith("1.8"))
                        bukkitVersion = BukkitVersion.v1_8_R3;
                    else if (mcVersion.equals("1.20.6"))
                        bukkitVersion = BukkitVersion.v1_20_R4;
                    else if (mcVersion.startsWith("1.21"))
                        bukkitVersion = BukkitVersion.v1_21_R3;
                    else {
                        // Unknown version - keep null but log for visibility
                        Bukkit.getLogger().warning("Unsupported MC version: " + mcVersion);
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
        v1_8_R3("1_8_8", "1.8.8/", false), // 1.8.8
        v1_20_R4("modern", "modern/", true), // 1.20.6
        v1_21_R3("modern", "modern/", true); // 1.21.4

        private final String moduleVersionExtension;
        private final String filePath;
        private final boolean secondHand;

        BukkitVersion(final String moduleVersionExtension, final String filePath, final boolean secondHand) {
            this.moduleVersionExtension = moduleVersionExtension;
            this.filePath = filePath;
            this.secondHand = secondHand;
        }
    }

}
