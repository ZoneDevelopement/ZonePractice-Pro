package dev.nandi0813.practice.manager.nametag;

import lombok.Getter;
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
    @Getter
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

    /**
     * Sets a player's nametag using TAB API.
     *
     * @param player       The player
     * @param prefix       The prefix component
     * @param nameColor    The name color to apply to the player's actual name
     * @param suffix       The suffix component
     * @param sortPriority The sort priority (currently unused in TAB integration)
     */
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
                // Handle prefix setting based on whether there's actual content or just color
                if (!originalPrefixEmpty) {
                    // Has actual prefix text (possibly with color appended)
                    nameTagManager.setPrefix(tabPlayer, prefixStr);
                } else if (colorCode != null) {
                    // No prefix text, but we have a color to apply
                    // Set the color code as prefix so the name gets colored
                    nameTagManager.setPrefix(tabPlayer, colorCode);
                } else {
                    // No prefix and no color
                    nameTagManager.setPrefix(tabPlayer, "");
                }
                nameTagManager.setSuffix(tabPlayer, suffixStr);
            }

            // NOTE: We do NOT modify tablist formatting here
            // Tablist should remain as configured by TAB (group-based formatting from lobby)
            // Only the nametag (above head) changes during matches

        } catch (Exception e) {
            // Silently fail - TAB integration is optional
        }
    }

    /**
     * Sets a player's tablist name using TAB API.
     * This is used for lobby nametag formatting where we want to show the full formatted name in tablist.
     *
     * @param player The player
     * @param listName The full formatted component to display in tablist (prefix + colored name + suffix)
     */
    public void setTabListName(Player player, Component listName) {
        if (!available) return;

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            // Convert Component to legacy string for TAB
            String fullListName = componentToLegacy(listName);

            // TAB API 5.x uses TabListFormatManager to set tablist formatting
            try {
                var tabListFormatManager = tabAPI.getTabListFormatManager();
                if (tabListFormatManager != null) {
                    // We need to split the formatted name into prefix, name, and suffix
                    // The listName contains: prefix + playerName + suffix
                    // We need to find where the player's actual name is in the string

                    String playerName = player.getName();
                    int nameIndex = fullListName.indexOf(playerName);

                    if (nameIndex >= 0) {
                        // Found the player name in the formatted string
                        // Extract prefix (everything before the name, may include color codes)
                        String prefix = fullListName.substring(0, nameIndex);

                        // Extract the name portion (may have color code before it)
                        // Find the last color code before the name
                        String nameWithColor = playerName;
                        int lastColorBeforeName = prefix.lastIndexOf('§');
                        if (lastColorBeforeName >= 0 && lastColorBeforeName + 1 < prefix.length()) {
                            // Extract the color code and apply it to the name
                            String colorCode = prefix.substring(lastColorBeforeName);
                            nameWithColor = colorCode + playerName;
                            // Remove the trailing color code from prefix (it's now part of the name)
                            prefix = prefix.substring(0, lastColorBeforeName);
                        }

                        // Extract suffix (everything after the name)
                        String suffix = fullListName.substring(nameIndex + playerName.length());

                        // Set the components through TAB API
                        tabListFormatManager.setPrefix(tabPlayer, prefix);
                        tabListFormatManager.setName(tabPlayer, nameWithColor);
                        tabListFormatManager.setSuffix(tabPlayer, suffix);
                    } else {
                        // Fallback: couldn't parse, set the whole thing as prefix + name
                        // This handles cases where the name might be modified or not found
                        tabListFormatManager.setPrefix(tabPlayer, "");
                        tabListFormatManager.setName(tabPlayer, fullListName);
                        tabListFormatManager.setSuffix(tabPlayer, "");
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

            // NOTE: We do NOT reset tablist formatting here
            // Tablist should remain as configured by TAB (group-based formatting from lobby)

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
