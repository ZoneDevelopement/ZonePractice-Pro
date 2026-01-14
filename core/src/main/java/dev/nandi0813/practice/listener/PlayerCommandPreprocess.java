package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandPreprocess implements Listener {

    @EventHandler ( ignoreCancelled = true, priority = EventPriority.HIGHEST )
    public void onReloadCommand(PlayerCommandPreprocessEvent e) {
        String cmd = e.getMessage().split(" ")[0].replace("/", "").replace("(?i)bukkit:", "");

        if (cmd.equalsIgnoreCase("reload") || cmd.equalsIgnoreCase("rl")) {
            e.setCancelled(true);
            Common.sendMMMessage(e.getPlayer(), LanguageManager.getString("RELOAD-DISABLED"));
        }
    }

}
