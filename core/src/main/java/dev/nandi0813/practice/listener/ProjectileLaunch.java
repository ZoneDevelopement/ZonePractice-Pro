package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.util.PermanentConfig;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileLaunch implements Listener {

    @EventHandler
    public void onProjHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Arrow arrow)) return;

        // Arrows that belong to a fight (match or FFA) are managed by the fight system:
        // they persist on the ground for up to 5 minutes (vanilla behaviour) and are
        // cleaned up automatically when the arena rolls back.
        // Only remove arrows that are NOT part of any fight (e.g. shot by a lobby player).
        if (!arrow.hasMetadata(PermanentConfig.FIGHT_ENTITY)) {
            arrow.remove();
        }
    }

}
