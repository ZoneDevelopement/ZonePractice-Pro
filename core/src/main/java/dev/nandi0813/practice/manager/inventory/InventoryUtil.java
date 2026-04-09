package dev.nandi0813.practice.manager.inventory;

import dev.nandi0813.api.Utilities.PlayerNametag;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.manager.nametag.NametagManager;
import dev.nandi0813.practice.manager.nametag.TabIntegration;
import dev.nandi0813.practice.manager.nametag.TeamPacketBlocker;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.NameFormatUtil;
import dev.nandi0813.practice.util.PermanentConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public enum InventoryUtil {
    ;

    public static void setLobbyNametag(Player player, Profile profile) {
        if (!ConfigManager.getBoolean("PLAYER.LOBBY-NAMETAG.ENABLED")) {
            if (PermanentConfig.NAMETAG_MANAGEMENT_ENABLED) {
                NametagManager.getInstance().reset(player.getName());
            }
        } else {
            // Use the player-aware overload so PAPI placeholders (e.g. %luckperms_prefix%)
            // are resolved for both the tab list and the above-head nametag.
            LobbyNametag playerNametag = getLobbyNametag(profile, player.getName(), player);

            Component prefix = playerNametag.getPrefix();
            Component name = playerNametag.getName();
            NamedTextColor nameColor = playerNametag.getScoreboardNameColor();
            Component suffix = playerNametag.getSuffix();
            int sortPriority = playerNametag.getSortPriority();

            if (PermanentConfig.NAMETAG_MANAGEMENT_ENABLED) {
                // ── Tab-list formatting ──────────────────────────────────────
                Component listName = prefix.append(name).append(suffix);

                TabIntegration tabIntegration = TeamPacketBlocker.getInstance().getTabIntegration();
                if (tabIntegration != null && tabIntegration.isAvailable()) {
                    tabIntegration.setTabListName(player, listName);
                } else {
                    PlayerUtil.setPlayerListName(player, listName);
                }

                // ── Nametag management (above-head prefix / suffix / color) ──
                NametagManager.getInstance().setNametag(player, prefix, nameColor, suffix, sortPriority);
            }
        }
    }

    public static LobbyNametag getLobbyNametag(Profile profile, String playerName) {
        Component prefix = NameFormatUtil.resolvePrefix(profile);
        // Extract the trailing color of the prefix so the name can inherit it when
        // the name template has no explicit color (e.g. NAME: '%player%').
        net.kyori.adventure.text.format.TextColor prefixTrailingColor = NameFormatUtil.extractTrailingColor(prefix);
        Component name = NameFormatUtil.resolveName(profile, playerName, prefixTrailingColor);
        Component suffix = NameFormatUtil.resolveSuffix(profile);

        NamedTextColor scoreboardNameColor = NameFormatUtil.resolveScoreboardColor(profile, playerName, NamedTextColor.GRAY);
        int sortPriority = profile.getGroup() != null ? profile.getGroup().getSortPriority() : 10;

        return new LobbyNametag(prefix, name, scoreboardNameColor, suffix, sortPriority);
    }

    /**
     * Resolves the lobby nametag with PlaceholderAPI support.
     * PAPI placeholders such as %luckperms_prefix% in the prefix/suffix templates
     * are resolved against the given {@code player} before the components are returned.
     *
     * @param profile    The player's profile
     * @param playerName The player's name (used for %player% placeholder)
     * @param player     The online player used as the PAPI context (may be null to skip PAPI)
     * @return Resolved LobbyNametag with all placeholders applied
     */
    public static LobbyNametag getLobbyNametag(Profile profile, String playerName, Player player) {
        Component prefix = (player != null)
                ? NameFormatUtil.resolvePrefix(profile, player)
                : NameFormatUtil.resolvePrefix(profile);
        // Extract the trailing color of the resolved prefix so the name inherits it
        // when the name template has no explicit color (e.g. NAME: '%player%').
        net.kyori.adventure.text.format.TextColor prefixTrailingColor = NameFormatUtil.extractTrailingColor(prefix);
        Component name = NameFormatUtil.resolveName(profile, playerName, prefixTrailingColor);
        Component suffix = (player != null)
                ? NameFormatUtil.resolveSuffix(profile, player)
                : NameFormatUtil.resolveSuffix(profile);

        NamedTextColor scoreboardNameColor = NameFormatUtil.resolveScoreboardColor(profile, playerName, NamedTextColor.GRAY);
        int sortPriority = profile.getGroup() != null ? profile.getGroup().getSortPriority() : 10;

        return new LobbyNametag(prefix, name, scoreboardNameColor, suffix, sortPriority);
    }

    public static PlayerNametag getLobbyNametag(Profile profile) {
        String playerName = profile.getPlayer() != null ? profile.getPlayer().getName() : "";
        LobbyNametag lobbyNametag = getLobbyNametag(profile, playerName);
        return new PlayerNametag(
                lobbyNametag.getPrefix(),
                lobbyNametag.getScoreboardNameColor(),
                lobbyNametag.getSuffix(),
                lobbyNametag.getSortPriority()
        );
    }

    public static final class LobbyNametag {
        private final Component prefix;
        private final Component name;
        private final NamedTextColor scoreboardNameColor;
        private final Component suffix;
        private final int sortPriority;

        public LobbyNametag(Component prefix, Component name, NamedTextColor scoreboardNameColor, Component suffix, int sortPriority) {
            this.prefix = prefix;
            this.name = name;
            this.scoreboardNameColor = scoreboardNameColor;
            this.suffix = suffix;
            this.sortPriority = sortPriority;
        }

        public Component getPrefix() {
            return prefix;
        }

        public Component getName() {
            return name;
        }

        public NamedTextColor getScoreboardNameColor() {
            return scoreboardNameColor;
        }

        public Component getSuffix() {
            return suffix;
        }

        public int getSortPriority() {
            return sortPriority;
        }
    }

}