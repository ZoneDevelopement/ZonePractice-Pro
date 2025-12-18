package dev.nandi0813.practice.Util;

import dev.nandi0813.practice.ZonePractice;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.bungeecord.BungeeComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public enum Common {
    ;

    private static final MiniMessage MM = MiniMessage.miniMessage();

    /** Works on Paper (Audience) and falls back on Spigot. */
    public static void send(CommandSender sender, Component component) {
        if (sender == null) return;

        // 1) Paper / modern: CommandSender has sendMessage(Component) because it is an Audience.
        try {
            sender.getClass().getMethod("sendMessage", Component.class).invoke(sender, component);
            return;
        } catch (Throwable ignored) { }

        // 2) Spigot fallback for players: player.spigot().sendMessage(BaseComponent...)
        if (sender instanceof Player p) {
            try {
                p.spigot().sendMessage(BungeeComponentSerializer.get().serialize(component));
                return;
            } catch (Throwable ignored) { }
        }

        // 3) Final fallback: legacy string
        String legacy = LegacyComponentSerializer.legacySection().serialize(component);
        sender.sendMessage(legacy.isEmpty() ? " " : legacy);
    }

    public static void sendMMMessage(Player player, String line) {
        if (line.contains("&") || line.contains("§")) line = StringUtil.legacyColorToMiniMessage(line);

        if (SoftDependUtil.isPAPI_ENABLED) {
            line = PlaceholderAPI.setPlaceholders(player, line);
        }

        send(player, MM.deserialize(line));
    }

    public static void sendConsoleMMMessage(String string) {
        send(ZonePractice.getInstance().getServer().getConsoleSender(), MM.deserialize(string));
    }

    public static Component deserializeMiniMessage(String line) {
        return MM.deserialize(line);
    }

    public static String serializeComponentToLegacyString(Component component) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component);
    }

    public static String mmToNormal(String line) {
        if (line.contains("&") || line.contains("§")) line = StringUtil.legacyColorToMiniMessage(line);
        return StringUtil.CC(serializeComponentToLegacyString(deserializeMiniMessage(line)));
    }

    public static String serializeNormalToMMString(String normalString) {
        String normalized = normalString.replace('&', LegacyComponentSerializer.SECTION_CHAR);
        Component component = LegacyComponentSerializer.legacySection().deserialize(normalized);
        return MM.serialize(component);
    }

    public static List<String> mmToNormal(List<String> list) {
        List<String> newList = new ArrayList<>();
        for (String line : list) {
            newList.add(mmToNormal(line));
        }
        return newList;
    }
}