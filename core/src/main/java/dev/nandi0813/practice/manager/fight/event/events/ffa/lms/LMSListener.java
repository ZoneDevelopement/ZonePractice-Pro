package dev.nandi0813.practice.manager.fight.event.events.ffa.lms;

import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.events.ffa.interfaces.FFAListener;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class LMSListener extends FFAListener {

    @Override
    public void onEntityDamage(Event event, EntityDamageEvent e) {
        if (event instanceof LMS) {
            Player player = (Player) e.getEntity();

            if (!event.getStatus().equals(EventStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                event.killPlayer(player, true);
                return;
            }

            if (player.getHealth() - e.getFinalDamage() <= 0) {
                e.setDamage(0);
                player.setHealth(player.getMaxHealth());
                event.killPlayer(player, false);
            }
        }
    }

    @Override
    public void onEntityDamageByEntity(Event event, EntityDamageByEntityEvent e) {
    }

    @Override
    public void onProjectileLaunch(Event event, ProjectileLaunchEvent e) {
    }

    @Override
    public void onPlayerMove(Event event, PlayerMoveEvent e) {
        if (event instanceof LMS) {
            if (event.getStatus().equals(EventStatus.LIVE)) {
                Player player = e.getPlayer();
                LMS lms = (LMS) event;

                if (!lms.getEventData().getCuboid().contains(player.getLocation())) {
                    lms.killPlayer(player, true);
                }
            }
        }
    }

    @Override
    public void onPlayerInteract(Event event, PlayerInteractEvent e) {
    }

    @Override
    public void onPlayerEggThrow(Event event, PlayerEggThrowEvent e) {
    }

    @Override
    public void onPlayerDropItem(Event event, PlayerDropItemEvent e) {
    }

}
