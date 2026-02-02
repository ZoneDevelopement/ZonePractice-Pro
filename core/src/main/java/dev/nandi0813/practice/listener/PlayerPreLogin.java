package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Prevents players from joining the server until the plugin is fully loaded.
 * Also prevents duplicate username logins.
 */
public class PlayerPreLogin implements Listener {

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        // Check if plugin is fully loaded
        if (!ZonePractice.isFullyLoaded()) {
            String message = LanguageManager.getString("PLUGIN-LOADING-MESSAGE");
            if (message == null || message.isEmpty()) {
                message = "<red>The server is still loading, please wait...";
            }
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Common.mmToNormal(message));
            return;
        }

        // Check for duplicate username (different UUID, same name)
        String joiningPlayerName = event.getName();
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(joiningPlayerName) &&
                    !onlinePlayer.getUniqueId().equals(event.getUniqueId())) {
                String message = LanguageManager.getString("DUPLICATE-USERNAME-KICK");
                if (message == null || message.isEmpty()) {
                    message = "<red>A player with your username is already logged in!";
                }
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, Common.mmToNormal(message));
                return;
            }
        }
    }
}
