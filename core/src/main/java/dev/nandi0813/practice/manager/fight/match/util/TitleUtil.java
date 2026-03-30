package dev.nandi0813.practice.manager.fight.match.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Collection;

public enum TitleUtil {
    ;

    public static void sendTitle(Player player, Component titleText, long fadeInMs, long stayMs, long fadeOutMs) {
        sendTitle(player, titleText, Component.empty(), fadeInMs, stayMs, fadeOutMs);
    }

    public static void sendTitle(Player player, Component titleText, Component subtitleText, long fadeInMs, long stayMs, long fadeOutMs) {
        Title title = Title.title(
                titleText,
                subtitleText,
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

}

