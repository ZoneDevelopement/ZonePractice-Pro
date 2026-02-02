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
     * @param nameColor    The name color (currently unused in TAB integration)
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

            // TAB API 5.x uses the NameTagManager to set temporary values
            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            if (nameTagManager != null) {
                // Set temporary prefix and suffix using TAB API
                nameTagManager.setPrefix(tabPlayer, prefixStr);
                nameTagManager.setSuffix(tabPlayer, suffixStr);
            }

            // Note: TAB doesn't have a direct way to set name color via API in all versions
            // The name color is usually managed through TAB's own configuration

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

            // TAB API 5.x uses the NameTagManager to reset values
            NameTagManager nameTagManager = tabAPI.getNameTagManager();
            if (nameTagManager != null) {
                // Reset to default (null values)
                nameTagManager.setPrefix(tabPlayer, null);
                nameTagManager.setSuffix(tabPlayer, null);
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

}
