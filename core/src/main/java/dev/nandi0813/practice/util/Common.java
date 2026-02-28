package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum Common {
    ;

    public static void send(CommandSender sender, Component component) {
        if (sender == null) return;

        try {
            sender.getClass().getMethod("sendMessage", Component.class).invoke(sender, component);
            return;
        } catch (Throwable ignored) { }

        if (sender instanceof Player p) {
            try {
                p.spigot().sendMessage(BungeeComponentSerializer.get().serialize(component));
                return;
            } catch (Throwable ignored) { }
        }

        String legacy = LegacyComponentSerializer.legacySection().serialize(component);
        sender.sendMessage(legacy.isEmpty() ? " " : legacy);
    }

    public static void sendMMMessage(Player player, String line) {
        if (line == null) {
            return;
        }

        if (line.contains("&") || line.contains("§")) {
            line = StringUtil.legacyColorToMiniMessage(line);
        }

        if (line.isEmpty()) {
            return;
        }

        if (SoftDependUtil.isPAPI_ENABLED) {
            line = PlaceholderAPI.setPlaceholders(player, line);
        }

        send(player, ZonePractice.getMiniMessage().deserialize(line));
    }

    public static void sendConsoleMMMessage(String string) {
        send(ZonePractice.getInstance().getServer().getConsoleSender(), ZonePractice.getMiniMessage().deserialize(string));
    }

    public static Component deserializeMiniMessage(String line) {
        return ZonePractice.getMiniMessage().deserialize(line);
    }

    public static String serializeComponentToLegacyString(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    public static String mmToNormal(String line) {
        if (line.contains("&") || line.contains("§")) {
            line = StringUtil.legacyColorToMiniMessage(line);
        }

        return StringUtil.CC(serializeComponentToLegacyString(deserializeMiniMessage(line)));
    }

    public static String serializeNormalToMMString(String normalString) {
        String normalized = normalString.replace('&', LegacyComponentSerializer.SECTION_CHAR);
        Component component = LegacyComponentSerializer.legacySection().deserialize(normalized);
        return ZonePractice.getMiniMessage().serialize(component);
    }

    public static String colorize(String message) {
        if (message == null) return "";
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> mmToNormal(List<String> list) {
        List<String> newList = new ArrayList<>();
        for (String line : list) {
            newList.add(mmToNormal(line));
        }
        return newList;
    }
}