package dev.nandi0813.practice.manager.inventory;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.playerdisplay.nametag.NametagManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.group.Group;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public enum InventoryUtil {
    ;

    public static void setLobbyNametag(Player player, Profile profile) {
        if (!ConfigManager.getBoolean("PLAYER.LOBBY-NAMETAG.ENABLED")) {
            NametagManager.getInstance().reset(player.getName());
        } else {
            Group group = profile.getGroup();
            Component prefix = Component.empty(), suffix = Component.empty();
            NamedTextColor nameColor = NamedTextColor.WHITE;
            int sortPriority = 10;

            if (group != null) {
                prefix = group.getPrefix()
                        .replaceText(
                                TextReplacementConfig.builder()
                                        .match("%division%")
                                        .replacement(profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "")
                                        .build()
                        ).replaceText(
                                TextReplacementConfig.builder()
                                        .match("%division_short%")
                                        .replacement(profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "")
                                        .build()
                        );

                suffix = group.getSuffix()
                        .replaceText(
                                TextReplacementConfig.builder()
                                        .match("%division%")
                                        .replacement(profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getFullName()) : "")
                                        .build()
                        ).replaceText(
                                TextReplacementConfig.builder()
                                        .match("%division_short%")
                                        .replacement(profile.getStats().getDivision() != null ? Common.mmToNormal(profile.getStats().getDivision().getShortName()) : "")
                                        .build()
                        );

                nameColor = group.getNameColor();

                sortPriority = group.getSortPriority();
            }

            if (profile.getPrefix() != null) prefix = profile.getPrefix();
            if (profile.getSuffix() != null) suffix = profile.getSuffix();

            Component listName = prefix.append(Component.text(player.getName(), nameColor)).append(suffix);
            listName = listName
                    .replaceText(TextReplacementConfig.builder().match("%division%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentFullName() : Component.empty()).build())
                    .replaceText(TextReplacementConfig.builder().match("%division_short%").replacement(profile.getStats().getDivision() != null ? profile.getStats().getDivision().getComponentShortName() : Component.empty()).build());

            ClassImport.getClasses().getPlayerUtil().setPlayerListName(player, listName);

            NametagManager.getInstance().setNametag(player, prefix, nameColor, suffix, sortPriority);
        }
    }

}
