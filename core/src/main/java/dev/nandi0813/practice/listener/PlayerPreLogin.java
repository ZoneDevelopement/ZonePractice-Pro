package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

/**
 * Prevents players from joining the server until the plugin is fully loaded.
 * This is essential to avoid NullPointerExceptions and race conditions during startup.
 */
public class PlayerPreLogin implements Listener {

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!ZonePractice.isFullyLoaded()) {
            String message = LanguageManager.getString("PLUGIN-LOADING-MESSAGE");
            if (message == null || message.isEmpty()) {
                message = "§cThe server is still loading, please wait...";
            }
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, message);
        }
    }
}
