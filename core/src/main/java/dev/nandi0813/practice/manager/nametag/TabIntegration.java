package dev.nandi0813.practice.manager.nametag;

import lombok.Getter;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.nametag.NameTagManager;
import me.neznamy.tab.api.tablist.TabListFormatManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;

/**
 * Integration with TAB API for nametag and tablist management.
 * <p>
 * <b>ARCHITECTURE PRINCIPLE:</b>
 * This class EXCLUSIVELY uses TAB's API when TAB is available.
 * It does NOT contain any internal nametag logic - that belongs in NametagManager.
 * <p>
 * <b>Key principles:</b>
 * <ul>
 *   <li>If TAB API is available: ALL nametag/tablist operations go through TAB API ONLY</li>
 *   <li>If TAB API is not available: This class does nothing (NametagManager handles it internally)</li>
 *   <li>NO mixing of TAB API and internal logic within this class</li>
 *   <li>NO fallback to internal methods if TAB API calls fail - fail silently</li>
 * </ul>
 * <p>
 * <b>Usage Pattern:</b>
 * <pre>
 * NametagManager (orchestrator)
 *   ├─ Detects if TAB is available
 *   ├─ If TAB available: delegates to TabIntegration → uses TAB API exclusively
 *   └─ If TAB not available: uses internal packet-based system
 * </pre>
 */
public class TabIntegration {

    private final TabAPI tabAPI;
    @Getter
    private final boolean available;
    @Getter
    private final boolean tablistFormattingEnabled;

    public TabIntegration() {
        TabAPI api = null;
        boolean isAvailable;
        boolean tablistEnabled = false;

        try {
            api = TabAPI.getInstance();
            isAvailable = api != null;

            // Check if TAB's tablist-name-formatting feature is enabled using their API
            if (isAvailable) {
                tablistEnabled = checkTablistFormattingEnabled(api);
            }
        } catch (NoClassDefFoundError | Exception e) {
            // TAB API not available
            isAvailable = false;
        }

        this.tabAPI = api;
        this.available = isAvailable;
        this.tablistFormattingEnabled = tablistEnabled;
    }

    /**
     * Checks if TAB's tablist-name-formatting feature is enabled using TAB's Developer API.
     * Returns true if TabListFormatManager is available and functional.
     */
    private boolean checkTablistFormattingEnabled(TabAPI api) {
        try {
            // Use TAB API to check if TabListFormatManager is available
            // If getTabListFormatManager() returns null, the feature is disabled
            TabListFormatManager manager = api.getTabListFormatManager();
            return manager != null;
        } catch (Exception e) {
            // If there's an exception, the feature is not available
            return false;
        }
    }

    /**
     * Sets a player's nametag using ONLY TAB API.
     * This method sets the nametag (above head) through TAB's NameTagManager.
     * If tablist formatting is enabled in TAB, it also preserves the lobby tablist name.
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

            // Get TAB's NameTagManager for nametag (above head) management
            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            if (nameTagManager == null) return;

            // Convert Components to legacy strings for TAB API
            String prefixStr = componentToLegacy(prefix);
            String suffixStr = componentToLegacy(suffix);

            // Apply name color to the prefix
            // In TAB, the "team color" (name color) is set via the prefix's last color code
            String colorCode = getColorCode(nameColor);
            boolean originalPrefixEmpty = prefixStr.isEmpty();

            // Handle prefix with color code
            if (!originalPrefixEmpty) {
                // Has actual prefix text - append color code if not already present
                boolean endsWithColor = prefixStr.matches(".*§[0-9a-fA-Fk-oK-OrR]$");
                if (!endsWithColor) {
                    prefixStr = prefixStr + colorCode;
                }
                nameTagManager.setPrefix(tabPlayer, prefixStr);
            } else if (colorCode != null) {
                // No prefix text, but we have a color - set color as prefix
                nameTagManager.setPrefix(tabPlayer, colorCode);
            } else {
                // No prefix and no color
                nameTagManager.setPrefix(tabPlayer, "");
            }

            // Set the suffix
            nameTagManager.setSuffix(tabPlayer, suffixStr);

            // Preserve the lobby tablist name to prevent match nametag colors from affecting it
            // In Minecraft 1.21+, scoreboard team colors can bleed into the tab list
            if (tablistFormattingEnabled) {
                // TAB's tablist formatting is enabled - use TAB's TabListFormatManager
                setLobbyTabListName(player);
            } else {
                // TAB's tablist formatting is disabled - use our internal Bukkit method
                preserveTabListNameInternal(player);
            }

        } catch (Exception e) {
            // Silently fail - TAB integration is best-effort
        }
    }

    /**
     * Sets the player's tablist name to their lobby formatting using ONLY TAB API.
     * This method uses TabListFormatManager to set prefix, name, and suffix.
     * Only called when tablistFormattingEnabled is true.
     *
     * @param player The player whose tablist name should be set to lobby formatting
     */
    private void setLobbyTabListName(Player player) {
        if (!available || !tablistFormattingEnabled) return;

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            TabListFormatManager tabListFormatManager = tabAPI.getTabListFormatManager();
            if (tabListFormatManager == null) return;

            // Get the player's profile to calculate their lobby tab list name
            dev.nandi0813.practice.manager.profile.Profile profile =
                dev.nandi0813.practice.manager.profile.ProfileManager.getInstance().getProfile(player);
            if (profile == null) return;

            // Calculate lobby formatting components
            Component lobbyPrefix = Component.empty();
            Component lobbySuffix = Component.empty();
            NamedTextColor lobbyNameColor = NamedTextColor.GRAY;

            dev.nandi0813.practice.manager.profile.group.Group group = profile.getGroup();
            if (group != null) {
                lobbyPrefix = group.getPrefix();
                lobbySuffix = group.getSuffix();
                lobbyNameColor = group.getNameColor();
            }

            // Apply custom prefix/suffix if set
            if (profile.getPrefix() != null) lobbyPrefix = profile.getPrefix();
            if (profile.getSuffix() != null) lobbySuffix = profile.getSuffix();

            // Apply division placeholders to prefix and suffix
            if (profile.getStats().getDivision() != null) {
                lobbyPrefix = applyDivisionPlaceholder(lobbyPrefix, profile.getStats().getDivision());
                lobbySuffix = applyDivisionPlaceholder(lobbySuffix, profile.getStats().getDivision());
            } else {
                lobbyPrefix = removeDivisionPlaceholder(lobbyPrefix);
                lobbySuffix = removeDivisionPlaceholder(lobbySuffix);
            }

            // Convert components to legacy strings for TAB API
            String prefixStr = componentToLegacy(lobbyPrefix);
            String suffixStr = componentToLegacy(lobbySuffix);
            String nameStr = getColorCode(lobbyNameColor) + player.getName();

            // Use TAB's TabListFormatManager to set the tab list formatting
            tabListFormatManager.setPrefix(tabPlayer, prefixStr);
            tabListFormatManager.setName(tabPlayer, nameStr);
            tabListFormatManager.setSuffix(tabPlayer, suffixStr);

        } catch (Exception e) {
            // Silently fail - this is best-effort
        }
    }

    /**
     * Sets a player's tablist name using ONLY TAB API.
     * This is used for lobby nametag formatting where we want to show the full formatted name in tablist.
     * Only works if TAB's tablist-name-formatting feature is enabled.
     *
     * @param player The player
     * @param listName The full formatted component to display in tablist (prefix + colored name + suffix)
     */
    public void setTabListName(Player player, Component listName) {
        if (!available || !tablistFormattingEnabled) return;

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            TabListFormatManager tabListFormatManager = tabAPI.getTabListFormatManager();
            if (tabListFormatManager == null) return;

            // Convert Component to legacy string for TAB API
            String fullListName = componentToLegacy(listName);
            String playerName = player.getName();

            // Parse the formatted name into prefix, name, and suffix
            // The listName contains: prefix + playerName + suffix
            int nameIndex = fullListName.indexOf(playerName);

            if (nameIndex >= 0) {
                // Found the player name in the formatted string
                // Extract prefix (everything before the name, may include color codes)
                String prefix = fullListName.substring(0, nameIndex);

                // Extract the name portion with its color code
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
                // Fallback: couldn't parse, set the whole thing as name
                tabListFormatManager.setPrefix(tabPlayer, "");
                tabListFormatManager.setName(tabPlayer, fullListName);
                tabListFormatManager.setSuffix(tabPlayer, "");
            }

        } catch (Exception e) {
            // Silently fail - TAB integration is best-effort
        }
    }

    /**
     * Resets a player's nametag to TAB's default using ONLY TAB API.
     * This resets the nametag (above head) prefix and suffix to TAB's configured values.
     * NOTE: This does NOT reset tablist formatting - tablist remains as configured by TAB.
     *
     * @param player The player
     */
    public void resetNametag(Player player) {
        if (!available) return;

        try {
            TabPlayer tabPlayer = tabAPI.getPlayer(player.getUniqueId());
            if (tabPlayer == null) return;

            // Get TAB's NameTagManager to reset nametag values
            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            if (nameTagManager == null) return;

            // Reset nametag prefix and suffix to default (null values reset to TAB's config)
            nameTagManager.setPrefix(tabPlayer, null);
            nameTagManager.setSuffix(tabPlayer, null);

            // NOTE: We do NOT reset tablist formatting here
            // Tablist should remain as configured by TAB (group-based formatting from lobby)

        } catch (Exception e) {
            // Silently fail - TAB integration is best-effort
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

    /**
     * Applies division placeholders to a component.
     *
     * @param component The component to process
     * @param division  The division to apply
     * @return Component with division placeholders replaced
     */
    private Component applyDivisionPlaceholder(Component component, dev.nandi0813.practice.manager.division.Division division) {
        if (component == null || division == null) return component;

        return component
            .replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                .match("%division%")
                .replacement(division.getComponentFullName())
                .build())
            .replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                .match("%division_short%")
                .replacement(division.getComponentShortName())
                .build());
    }

    /**
     * Removes division placeholders from a component.
     *
     * @param component The component to process
     * @return Component with division placeholders removed
     */
    private Component removeDivisionPlaceholder(Component component) {
        if (component == null) return component;

        return component
            .replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                .match("%division%")
                .replacement(Component.empty())
                .build())
            .replaceText(net.kyori.adventure.text.TextReplacementConfig.builder()
                .match("%division_short%")
                .replacement(Component.empty())
                .build());
    }

    /**
     * Preserves the player's tablist name using internal Bukkit method.
     * This is called when TAB's tablist-name-formatting is disabled.
     * Prevents match nametag colors from bleeding into the tablist in 1.21+.
     *
     * @param player The player whose tablist name should be preserved
     */
    private void preserveTabListNameInternal(Player player) {
        try {
            // Get the player's profile to calculate their lobby tab list name
            dev.nandi0813.practice.manager.profile.Profile profile =
                dev.nandi0813.practice.manager.profile.ProfileManager.getInstance().getProfile(player);
            if (profile == null) return;

            // Calculate lobby formatting components
            Component lobbyPrefix = Component.empty();
            Component lobbySuffix = Component.empty();
            NamedTextColor lobbyNameColor = NamedTextColor.GRAY;

            dev.nandi0813.practice.manager.profile.group.Group group = profile.getGroup();
            if (group != null) {
                lobbyPrefix = group.getPrefix();
                lobbySuffix = group.getSuffix();
                lobbyNameColor = group.getNameColor();
            }

            // Apply custom prefix/suffix if set
            if (profile.getPrefix() != null) lobbyPrefix = profile.getPrefix();
            if (profile.getSuffix() != null) lobbySuffix = profile.getSuffix();

            // Apply division placeholders to prefix and suffix
            if (profile.getStats().getDivision() != null) {
                lobbyPrefix = applyDivisionPlaceholder(lobbyPrefix, profile.getStats().getDivision());
                lobbySuffix = applyDivisionPlaceholder(lobbySuffix, profile.getStats().getDivision());
            } else {
                lobbyPrefix = removeDivisionPlaceholder(lobbyPrefix);
                lobbySuffix = removeDivisionPlaceholder(lobbySuffix);
            }

            // Build the full tab list name
            Component tabListName = lobbyPrefix
                .append(Component.text(player.getName(), lobbyNameColor))
                .append(lobbySuffix);

            // Use internal Bukkit method to set the tablist name (not TAB API)
            dev.nandi0813.practice.module.util.ClassImport.getClasses()
                .getPlayerUtil()
                .setPlayerListName(player, tabListName);

        } catch (Exception e) {
            // Silently fail - this is best-effort
        }
    }

}
