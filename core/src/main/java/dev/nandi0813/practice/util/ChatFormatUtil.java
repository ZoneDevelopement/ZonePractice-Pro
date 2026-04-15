package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum ChatFormatUtil {
    ;

    private static String normalizeStaticSpacing(String formattedWithoutMessage) {
        if (formattedWithoutMessage == null || formattedWithoutMessage.isEmpty()) {
            return formattedWithoutMessage;
        }

        // Prevent accidental duplicate spaces in static template text.
        return formattedWithoutMessage.replaceAll(" {2,}", " ");
    }

    /**
     * Builds the fully-replaced party chat format string.
     */
    public static String buildPartyChatMessage(Player player, String rawMessage) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        String playerName = profile != null
                ? ZonePractice.getMiniMessage().serialize(NameFormatUtil.resolveFullName(profile, player.getName()))
                : player.getName();

        String template = LanguageManager.getString("GENERAL-CHAT.PARTY-CHAT")
                .replace("%%player%%", playerName)
                .replace("%%message%%", "%%message%%");

        return normalizeStaticSpacing(template)
                .replace("%%message%%", rawMessage.replaceFirst("@", ""));
    }

    /**
     * Builds the fully-replaced staff chat format string.
     */
    public static String buildStaffChatMessage(Player player, String rawMessage) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        String playerName = profile != null
                ? ZonePractice.getMiniMessage().serialize(NameFormatUtil.resolveFullName(profile, player.getName()))
                : player.getName();

        String template = LanguageManager.getString("GENERAL-CHAT.STAFF-CHAT")
                .replace("%%player%%", playerName)
                .replace("%%message%%", "%%message%%");

        return normalizeStaticSpacing(template)
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

        String decoratedPlayer = ZonePractice.getMiniMessage().serialize(NameFormatUtil.resolveFullName(profile, player.getName()));

        String template = format
                .replace("%%player%%", decoratedPlayer)
                .replace("%%message%%", "%%message%%");

        return normalizeStaticSpacing(template)
                .replace("%%message%%", message);
    }
}
