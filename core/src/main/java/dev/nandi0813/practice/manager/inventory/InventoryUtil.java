package dev.nandi0813.practice.manager.inventory;

import dev.nandi0813.api.Utilities.PlayerNametag;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.nametag.NametagManager;
import dev.nandi0813.practice.manager.nametag.TabIntegration;
import dev.nandi0813.practice.manager.nametag.TeamPacketBlocker;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.PermanentConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
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
            PlayerNametag playerNametag = getLobbyNametag(profile);

            Component prefix = playerNametag.getPrefix();
            NamedTextColor nameColor = playerNametag.getColorOfName();
            Component suffix = playerNametag.getSuffix();
            int sortPriority = playerNametag.getSortPriority();

            if (PermanentConfig.NAMETAG_MANAGEMENT_ENABLED) {
                // ── Tab-list formatting ──────────────────────────────────────
                Component listName = prefix.append(Component.text(player.getName(), nameColor)).append(suffix);
                listName = listName
                        .replaceText(TextReplacementConfig.builder().match("%division%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentFullName() : Component.empty()).build())
                        .replaceText(TextReplacementConfig.builder().match("%division_short%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentShortName() : Component.empty()).build());

                TabIntegration tabIntegration = TeamPacketBlocker.getInstance().getTabIntegration();
                if (tabIntegration != null && tabIntegration.isAvailable()) {
                    tabIntegration.setTabListName(player, listName);
                } else {
                    ClassImport.getClasses().getPlayerUtil().setPlayerListName(player, listName);
                }

                // ── Nametag management (above-head prefix / suffix / color) ──
                NametagManager.getInstance().setNametag(player, prefix, nameColor, suffix, sortPriority);
            }
        }
    }

    public static PlayerNametag getLobbyNametag(Profile profile) {
            Group group = profile.getGroup();
            Component prefix = Component.empty(), suffix = Component.empty();
            NamedTextColor nameColor = NamedTextColor.GRAY;
            int sortPriority = 10;

            if (group != null) {
                prefix = group.getPrefix()
                        .replaceText(TextReplacementConfig.builder().match("%division%").replacement(profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "").build())
                        .replaceText(TextReplacementConfig.builder().match("%division_short%").replacement(profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "").build());

                suffix = group.getSuffix()
                        .replaceText(TextReplacementConfig.builder().match("%division%").replacement(profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "").build())
                        .replaceText(TextReplacementConfig.builder().match("%division_short%").replacement(profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "").build());

                nameColor = group.getNameColor();
                sortPriority = group.getSortPriority();
            }

            if (profile.getPrefix() != null) prefix = profile.getPrefix();
            if (profile.getSuffix() != null) suffix = profile.getSuffix();

            return new PlayerNametag(prefix, nameColor, suffix, sortPriority);
    }

}
