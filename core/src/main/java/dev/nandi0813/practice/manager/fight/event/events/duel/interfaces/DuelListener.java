package dev.nandi0813.practice.manager.fight.event.events.duel.interfaces;

import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventListenerInterface;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public abstract class DuelListener extends EventListenerInterface {

    @Override
    public void onEntityDamageByEntity(Event event, EntityDamageByEntityEvent e) {
        if (event instanceof DuelEvent duelEvent) {
            if (e.getDamager() instanceof Player damager) {
                if (!duelEvent.isInFight(damager)) {
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void onPlayerQuit(Event event, PlayerQuitEvent e) {
        if (event instanceof DuelEvent duelEvent) {
            DuelFight duelFight = duelEvent.getFight(e.getPlayer());
            if (duelFight != null) {
                duelFight.endFight(e.getPlayer());
            } else {
                duelEvent.removePlayer(e.getPlayer(), true);
            }
        }
    }

    @Override
    public void onPlayerDropItem(Event event, PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void onPlayerMove(Event event, PlayerMoveEvent e) {
        if (event instanceof DuelEvent duelEvent) {
            if (duelEvent.getStatus().equals(EventStatus.LIVE)) {
                Player player = e.getPlayer();
                Cuboid cuboid = duelEvent.getEventData().getCuboid();

                if (!cuboid.contains(player.getLocation())) {
                    if (duelEvent.isInFight(player)) {
                        duelEvent.killPlayer(player, true);
                    } else {
                        player.teleport(cuboid.getCenter());
                    }
                }
            }
        }
    }

    @Override
    public void onPlayerInteract(Event event, PlayerInteractEvent e) {
    }

    @Override
    public void onInventoryClick(Event event, InventoryClickEvent e) {
        // Default: no action needed
    }

}
