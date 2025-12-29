package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public enum PAPIUtil {
    ;

    public static Component runThroughFormat(Player player, String line) {
        if (line == null || line.isEmpty()) {
            return Component.empty();
        }

        if (SoftDependUtil.isPAPI_ENABLED) {
            return ZonePractice.getMiniMessage().deserialize(PlaceholderAPI.setPlaceholders(player, line));
        }

        return ZonePractice.getMiniMessage().deserialize(line);
    }

}
