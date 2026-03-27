package dev.nandi0813.practice.manager.fight.match.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;

public enum TitleUtil {
    ;

    public static void sendTitle(Player player, Component titleText, long fadeInMs, long stayMs, long fadeOutMs) {
        Title title = Title.title(
                titleText,
                Component.empty(),
                Title.Times.times(
                        Duration.ofMillis(fadeInMs),
                        Duration.ofMillis(stayMs),
                        Duration.ofMillis(fadeOutMs)
                )
        );

        player.showTitle(title);
    }

    public static void sendTitleToAll(Collection<? extends Player> players, Component titleText, long fadeInMs, long stayMs, long fadeOutMs) {
        for (Player player : players) {
            sendTitle(player, titleText, fadeInMs, stayMs, fadeOutMs);
        }
    }

    public static void sendTitleWithSubtitle(Player player, String titleText, String subtitleText, NamedTextColor color, long fadeInMs, long stayMs, long fadeOutMs) {
        Component titleComponent = Component.text(titleText)
                .color(color)
                .decorate(TextDecoration.BOLD);
        Component subtitleComponent = Component.text(subtitleText)
                .color(color);

        Title title = Title.title(
                titleComponent,
                subtitleComponent,
                Title.Times.times(
                        Duration.ofMillis(fadeInMs),
                        Duration.ofMillis(stayMs),
                        Duration.ofMillis(fadeOutMs)
                )
        );

        player.showTitle(title);
    }

    public static void sendTitleWithSubtitleToAll(Collection<? extends Player> players, String titleText, String subtitleText, NamedTextColor color, long fadeInMs, long stayMs, long fadeOutMs) {
        for (Player player : players) {
            sendTitleWithSubtitle(player, titleText, subtitleText, color, fadeInMs, stayMs, fadeOutMs);
        }
    }
}

