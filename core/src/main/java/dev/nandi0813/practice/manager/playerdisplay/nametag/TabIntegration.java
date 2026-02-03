package dev.nandi0813.practice.manager.playerdisplay.nametag;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Integration with TAB API for safe nametag management.
 * <p>
 * This class provides a bridge to TAB's API, allowing us to set
 * nametag prefixes and suffixes through TAB instead of sending
 * our own scoreboard team packets.
 */
public class TabIntegration {

    private final TabAPI tabAPI;
    private final boolean available;

    public TabIntegration() {
        TabAPI api = null;
        boolean isAvailable;

        try {
            api = TabAPI.getInstance();
            isAvailable = api != null;
        } catch (NoClassDefFoundError | Exception e) {
            // TAB API not available
            isAvailable = false;
        }

        this.tabAPI = api;
        this.available = isAvailable;
    }

    public boolean isAvailable() {
        return available;
    }

    /**
     * Sets a player's nametag using TAB API.
     *
     * @param player       The player
     * @param prefix       The prefix component
     * @param nameColor    The name color to apply to the player's actual name
     * @param suffix       The suffix component
     * @param sortPriority The sort priority (currently unused in TAB integration)
     */
    @SuppressWarnings ( "unused" )
    public void setNametag(Player player, Component prefix, NamedTextColor nameColor, Component suffix, int sortPriority) {
        if (!available) return;

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            // Convert Components to legacy strings for TAB
            String prefixStr = prefix != null ? componentToLegacy(prefix) : "";
            String suffixStr = suffix != null ? componentToLegacy(suffix) : "";

            // Check if the original prefix was empty (before we add color codes)
            boolean originalPrefixEmpty = prefixStr.isEmpty();

            // CRITICAL FIX: Apply the name color to the prefix
            // In TAB, the "team color" is set via the prefix's last color code
            // So we need to ensure the prefix ends with the desired name color
            String colorCode = null;
            if (nameColor != null) {
                colorCode = getColorCode(nameColor);

                // Check if prefix already ends with a color code (case-insensitive)
                // Pattern matches: §0-9, §a-f, §A-F, §k-o, §K-O, §r, §R
                boolean endsWithColor = prefixStr.matches(".*§[0-9a-fA-Fk-oK-OrR]$");

                // Only append color code to prefix if it's NOT empty and doesn't already end with a color
                // For 1.8 compatibility: If prefix is empty, we'll handle coloring via setName instead
                if (!prefixStr.isEmpty() && !endsWithColor) {
                    prefixStr = prefixStr + colorCode;
                }
            }

            // TAB API 5.x uses the NameTagManager to set temporary values for NAMETAG (above head)
            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            if (nameTagManager != null) {
                // Set temporary prefix and suffix using TAB API for nametag
                // FIX: Only set prefix if it's not empty OR if it has actual content beyond just a color code
                // This prevents the unwanted space in 1.8 when only a color is applied
                if (!originalPrefixEmpty) {
                    nameTagManager.setPrefix(tabPlayer, prefixStr);
                } else {
                    // If original prefix was empty, set it to empty string to avoid the 1.8 space bug
                    nameTagManager.setPrefix(tabPlayer, "");
                }
                nameTagManager.setSuffix(tabPlayer, suffixStr);
            }

            // CRITICAL: Also set TABLIST formatting (player list when pressing Tab)
            // TAB has separate managers for nametag and tablist
            try {
                var tabListFormatManager = tabAPI.getTabListFormatManager();
                if (tabListFormatManager != null) {
                    // FIX: Same logic for tablist - only set prefix if it had actual content
                    if (!originalPrefixEmpty) {
                        tabListFormatManager.setPrefix(tabPlayer, prefixStr);
                    } else {
                        tabListFormatManager.setPrefix(tabPlayer, "");
                    }
                    tabListFormatManager.setSuffix(tabPlayer, suffixStr);

                    // Set the name with color for the tablist
                    // This ensures the player name itself is colored in the tablist
                    if (nameColor != null) {
                        tabListFormatManager.setName(tabPlayer, colorCode + player.getName());
                    } else {
                        // Reset to default name if no color
                        tabListFormatManager.setName(tabPlayer, null);
                    }
                }
            } catch (Exception e) {
                // TabListFormatManager might not be available in older TAB versions
                // Silently ignore
            }

        } catch (Exception e) {
            // Silently fail - TAB integration is optional
        }
    }

    /**
     * Resets a player's nametag to TAB's default.
     *
     * @param player The player
     */
    @SuppressWarnings ( "unused" )
    public void resetNametag(Player player) {
        if (!available) return;

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            // TAB API 5.x uses the NameTagManager to reset nametag values
            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            if (nameTagManager != null) {
                // Reset nametag prefix and suffix to default (null values)
                nameTagManager.setPrefix(tabPlayer, null);
                nameTagManager.setSuffix(tabPlayer, null);
            }

            // Also reset TABLIST formatting
            try {
                var tabListFormatManager = tabAPI.getTabListFormatManager();
                if (tabListFormatManager != null) {
                    // Reset tablist prefix, name, and suffix to defaults
                    tabListFormatManager.setPrefix(tabPlayer, null);
                    tabListFormatManager.setSuffix(tabPlayer, null);
                    tabListFormatManager.setName(tabPlayer, null);
                }
            } catch (Exception e) {
                // TabListFormatManager might not be available in older TAB versions
                // Silently ignore
            }

        } catch (Exception e) {
            // Silently fail
        }
    }

    /**
     * Converts an Adventure Component to a legacy color-coded string.
     */
    private String componentToLegacy(Component component) {
        if (component == null) return "";
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * Converts a NamedTextColor to a legacy color code (§x format).
     * This is needed for TAB API's setPlayerNameColor method.
     *
     * @param color The NamedTextColor to convert
     * @return Legacy color code string (e.g., "§a" for green)
     */
    private String getColorCode(NamedTextColor color) {
        if (color == null) return "§f"; // Default to white

        // Map NamedTextColor to legacy color codes
        if (color == NamedTextColor.BLACK) return "§0";
        if (color == NamedTextColor.DARK_BLUE) return "§1";
        if (color == NamedTextColor.DARK_GREEN) return "§2";
        if (color == NamedTextColor.DARK_AQUA) return "§3";
        if (color == NamedTextColor.DARK_RED) return "§4";
        if (color == NamedTextColor.DARK_PURPLE) return "§5";
        if (color == NamedTextColor.GOLD) return "§6";
        if (color == NamedTextColor.GRAY) return "§7";
        if (color == NamedTextColor.DARK_GRAY) return "§8";
        if (color == NamedTextColor.BLUE) return "§9";
        if (color == NamedTextColor.GREEN) return "§a";
        if (color == NamedTextColor.AQUA) return "§b";
        if (color == NamedTextColor.RED) return "§c";
        if (color == NamedTextColor.LIGHT_PURPLE) return "§d";
        if (color == NamedTextColor.YELLOW) return "§e";
        if (color == NamedTextColor.WHITE) return "§f";

        return "§f"; // Default to white if unknown
    }

}
