package dev.nandi0813.practice.manager.fight.event.events.onevsall.tnttag;

import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventListenerInterface;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class TNTTagListener extends EventListenerInterface {

    @Override
    public void onEntityDamage(Event event, EntityDamageEvent e) {
        if (event instanceof TNTTag) {
            if (!e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void onEntityDamageByEntity(Event event, EntityDamageByEntityEvent e) {
        if (event instanceof TNTTag tntTag) {
            if (!(e.getEntity() instanceof Player target)) {
                return;
            }

            if (!(e.getDamager() instanceof Player attacker)) {
                return;
            }

            if (!(EventManager.getInstance().getEventByPlayer(attacker) instanceof TNTTag)) {
                return;
            }

            if (!event.getStatus().equals(EventStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            if (tntTag.getTaggedPlayers().contains(attacker) && !tntTag.getTaggedPlayers().contains(target)) {
                tntTag.setTag(attacker, target);
            }
        }
    }

    @Override
    public void onProjectileLaunch(Event event, ProjectileLaunchEvent e) {

    }

    @Override
    public void onPlayerQuit(Event event, PlayerQuitEvent e) {
        if (event instanceof TNTTag tntTag) {
            Player player = e.getPlayer();

            if (event.getStatus().equals(EventStatus.LIVE)) {
                if (tntTag.getTaggedPlayers().contains(player)) {
                    for (Player eventPlayer : tntTag.getPlayers()) {
                        if (eventPlayer.equals(player)) {
                            continue;
                        }
                        if (tntTag.getTaggedPlayers().contains(eventPlayer)) {
                            continue;
                        }

                        tntTag.sendMessage("&cSince " + player.getName() + " left the game, the new IT will be " + eventPlayer.getName() + ".", true);
                        tntTag.setTag(null, eventPlayer);
                        break;
                    }
                }
            }

            tntTag.removePlayer(player, true);
        }
    }

    @Override
    public void onPlayerMove(Event event, PlayerMoveEvent e) {
        if (event instanceof TNTTag tntTag) {
            // Only enforce boundary for active players during LIVE/START phase.
            // Spectators are free to move and the event may still fire during END.
            if (!event.getStatus().equals(EventStatus.LIVE) && !event.getStatus().equals(EventStatus.START)) {
                return;
            }
            Player player = e.getPlayer();
            if (!tntTag.getPlayers().contains(player)) {
                return;
            }
            Cuboid cuboid = event.getEventData().getCuboid();
            if (cuboid != null && !cuboid.contains(e.getTo())) {
                tntTag.teleportPlayer(player);
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
        if (event instanceof TNTTag) {
            e.setCancelled(true);
        }
    }

    @Override
    public void onInventoryClick(Event event, InventoryClickEvent e) {
        if (event instanceof TNTTag) {
            // Prevent all inventory interactions to protect TNT helmet and items
            e.setCancelled(true);
        }
    }
}
