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
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;

public enum InventoryUtil {
    ;

    public static void setLobbyNametag(Player player, Profile profile) {
        if (!ConfigManager.getBoolean("PLAYER.LOBBY-NAMETAG.ENABLED")) {
            if (PermanentConfig.NAMETAG_MANAGEMENT_ENABLED) {
                NametagManager.getInstance().reset(player.getName());
            }
            return;
        }

        LobbyNametag nametag = getLobbyNametag(profile, player.getName(), player);

        if (!PermanentConfig.NAMETAG_MANAGEMENT_ENABLED) return;

        Component prefix = nametag.getPrefix();
        Component name   = nametag.getName();
        Component suffix = nametag.getSuffix();

        // Tab-list
        Component listName = prefix.append(name).append(suffix);
        TabIntegration tabIntegration = TeamPacketBlocker.getInstance().getTabIntegration();
        if (tabIntegration != null && tabIntegration.isAvailable()) {
            tabIntegration.setTabListName(player, listName);
        } else {
            PlayerUtil.setPlayerListName(player, listName);
        }

        // Above-head nametag
        NametagManager.getInstance().setNametag(player, prefix, nametag.getScoreboardNameColor(), suffix, nametag.getSortPriority());
    }

    /**
     * Resolves the lobby nametag without PlaceholderAPI support.
     * Use {@link #getLobbyNametag(Profile, String, Player)} when an online player is available.
     */
    public static LobbyNametag getLobbyNametag(Profile profile, String playerName) {
        return getLobbyNametag(profile, playerName, null);
    }

    /**
     * Resolves the lobby nametag with full PlaceholderAPI support.
     * When {@code player} is non-null, PAPI expansions such as {@code %luckperms_prefix%}
     * are resolved on the raw template strings before MiniMessage parsing, ensuring that
     * hex and legacy color codes injected by those expansions render correctly.
     * The name component inherits the trailing prefix color when it carries no explicit color.
     */
    public static LobbyNametag getLobbyNametag(Profile profile, String playerName, Player player) {
        Component prefix         = NameFormatUtil.resolvePrefix(profile, player);
        TextColor prefixEndColor = NameFormatUtil.extractTrailingColor(prefix);
        Component name           = NameFormatUtil.resolveName(profile, playerName, prefixEndColor);
        Component suffix         = NameFormatUtil.resolveSuffix(profile, player);

        NamedTextColor scoreboardNameColor = NameFormatUtil.resolveScoreboardColor(profile, playerName, NamedTextColor.GRAY);
        int sortPriority = profile.getGroup() != null ? profile.getGroup().getSortPriority() : 10;

        return new LobbyNametag(prefix, name, scoreboardNameColor, suffix, sortPriority);
    }

    public static PlayerNametag getLobbyNametag(Profile profile) {
        String playerName = profile.getPlayer() != null ? profile.getPlayer().getName() : "";
        LobbyNametag nametag = getLobbyNametag(profile, playerName);
        return new PlayerNametag(nametag.getPrefix(), nametag.getScoreboardNameColor(), nametag.getSuffix(), nametag.getSortPriority());
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

        public Component getPrefix() { return prefix; }
        public Component getName() { return name; }
        public NamedTextColor getScoreboardNameColor() { return scoreboardNameColor; }
        public Component getSuffix() { return suffix; }
        public int getSortPriority() { return sortPriority; }
    }
}