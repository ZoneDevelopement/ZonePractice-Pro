package dev.nandi0813.practice.manager.nametag;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;

/**
 * Detects TAB plugin and manages conflict resolution.
 * <p>
 * When TAB is detected with scoreboard-teams enabled, this class disables
 * ZonePracticePro's internal nametag system to prevent packet conflicts
 * that cause Network Protocol Errors and client crashes.
 * <p>
 * The conflict occurs because both plugins send PacketPlayOutScoreboardTeam
 * packets, causing race conditions where clients receive conflicting team
 * updates (e.g., removing players from teams that were just modified).
 */
public class TeamPacketBlocker {

    private static TeamPacketBlocker instance;

    @Getter
    private boolean tabPluginPresent = false;

    @Getter
    private boolean tabScoreboardTeamsEnabled = false;

    @Getter
    private boolean nametagSystemDisabled = false;

    @Getter
    private TabIntegration tabIntegration = null;

    private TeamPacketBlocker() {
    }

    public static TeamPacketBlocker getInstance() {
        if (instance == null) {
            instance = new TeamPacketBlocker();
        }
        return instance;
    }

    /**
     * Detects TAB plugin and checks if scoreboard-teams feature is enabled.
     * If enabled, disables our nametag system to prevent conflicts.
     */
    public void register() {
        // Check if TAB plugin is present
        Plugin tabPlugin = Bukkit.getPluginManager().getPlugin("TAB");
        tabPluginPresent = tabPlugin != null && tabPlugin.isEnabled();

        if (!tabPluginPresent) {
            // TAB not present; use internal nametag system.
            return;
        }

        // Check if TAB's scoreboard-teams feature is enabled
        tabScoreboardTeamsEnabled = checkTabScoreboardTeamsEnabled(tabPlugin);

        if (tabScoreboardTeamsEnabled) {
            nametagSystemDisabled = true;

            // Try to initialize TAB API integration
            try {
                tabIntegration = new TabIntegration();
                // If available, TAB API will be used externally; otherwise nametag features remain disabled.
            } catch (Throwable e) {
                // Initialization failed; nametag features will remain disabled.
            }
        }
    }

    /**
     * Checks if TAB's scoreboard-teams feature is enabled by reading its config.
     */
    private boolean checkTabScoreboardTeamsEnabled(Plugin tabPlugin) {
        try {
            File tabConfigFile = new File(tabPlugin.getDataFolder(), "config.yml");
            if (!tabConfigFile.exists()) {
                // Could not find TAB config; assume enabled to be safe.
                return true; // Assume enabled to be safe
            }

            org.bukkit.configuration.file.YamlConfiguration tabConfig =
                    org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(tabConfigFile);

            // Check if scoreboard-teams feature is enabled in TAB config
            // TAB config path: scoreboard-teams.enabled
            if (tabConfig.contains("scoreboard-teams.enabled")) {
                return tabConfig.getBoolean("scoreboard-teams.enabled", true);
            }

            // If the setting doesn't exist, check if the section exists (means it's enabled)
            if (tabConfig.contains("scoreboard-teams")) {
                return true;
            }

            // Default to true to be safe
            return true;

        } catch (Exception e) {
            // Error reading TAB config; assume enabled to be safe.
            return true; // Assume enabled to be safe
        }
    }

    /**
     * Unregisters the packet listener (no-op in new implementation).
     */
    public void unregister() {
        if (tabIntegration != null) {
            tabIntegration = null;
        }
    }

    /**
     * No-op methods for compatibility with existing code.
     * These are no longer needed since we don't block packets.
     */
    @SuppressWarnings ( "unused" )
    public void registerOurTeam(String teamName) {
        // No-op
    }

    @SuppressWarnings ( "unused" )
    public void unregisterOurTeam(String teamName) {
        // No-op
    }

    @SuppressWarnings ( "unused" )
    public boolean isOurTeam(String teamName) {
        // No-op
        return false;
    }

}
