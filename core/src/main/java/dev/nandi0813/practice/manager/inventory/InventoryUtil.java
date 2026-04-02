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
            LobbyNametag playerNametag = getLobbyNametag(profile, player.getName());

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
        Component name = NameFormatUtil.resolveName(profile, playerName);
        Component suffix = NameFormatUtil.resolveSuffix(profile);

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
