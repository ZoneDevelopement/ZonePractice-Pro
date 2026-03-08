package dev.nandi0813.practice.util;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum ChatFormatUtil {
    ;

    /**
     * Builds the fully-replaced party chat format string.
     */
    public static String buildPartyChatMessage(Player player, String rawMessage) {
        return LanguageManager.getString("GENERAL-CHAT.PARTY-CHAT")
                .replace("%%player%%", player.getName())
                .replace("%%message%%", rawMessage.replaceFirst("@", ""));
    }

    /**
     * Builds the fully-replaced staff chat format string.
     */
    public static String buildStaffChatMessage(Player player, String rawMessage) {
        return LanguageManager.getString("GENERAL-CHAT.STAFF-CHAT")
                .replace("%%player%%", player.getName())
                .replace("%%message%%", rawMessage);
    }

    /**
     * Collects all online players with the staff chat permission.
     */
    public static List<Player> getStaffRecipients() {
        List<Player> staff = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("zpp.staffmode.chat")) {
                staff.add(online);
            }
        }
        return staff;
    }

    /**
     * Resolves the server chat format string (group format or default),
     * then replaces all static placeholders (division, player, message).
     */
    public static String buildServerChatMessage(Profile profile, Player player, String message) {
        final String format;
        if (ConfigManager.getBoolean("PLAYER.GROUP-CHAT.ENABLED")) {
            Group group = profile.getGroup();
            if (group != null && group.getChatFormat() != null) {
                format = group.getChatFormat();
            } else {
                format = LanguageManager.getString("GENERAL-CHAT.SERVER-CHAT");
            }
        } else {
            format = LanguageManager.getString("GENERAL-CHAT.SERVER-CHAT");
        }

        String division      = profile.getStats().getDivision() != null ? profile.getStats().getDivision().getFullName()  : "";
        String divisionShort = profile.getStats().getDivision() != null ? profile.getStats().getDivision().getShortName() : "";

        return format
                .replace("%%division%%",       division)
                .replace("%%division_short%%", divisionShort)
                .replace("%%player%%",          player.getName())
                .replace("%%message%%",         message);
    }
}

