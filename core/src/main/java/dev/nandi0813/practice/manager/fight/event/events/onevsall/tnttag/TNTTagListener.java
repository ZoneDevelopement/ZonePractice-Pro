package dev.nandi0813.practice.manager.fight.event.events.onevsall.tnttag;

import dev.nandi0813.practice.ZonePractice;
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
import org.bukkit.util.Vector;

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

            e.setDamage(0);

            if (tntTag.getTaggedPlayers().contains(attacker) && !tntTag.getTaggedPlayers().contains(target)) {
                tntTag.setTag(attacker, target);

                // At this point in the event pipeline Minecraft has already written
                // the natural knockback vector into the target's velocity field.
                // Snapshot it now, before the PlayerItemHeldEvent (fired in the same
                // tick when the attacker rapidly switches hotbar slots) can zero it out.
                final Vector knockbackSnapshot = target.getVelocity().clone();

                // Register the UUID so onPlayerItemHeld can suppress the interfering
                // hotbar-switch for exactly this one-tick window.
                tntTag.getKnockbackPending().put(target.getUniqueId(), knockbackSnapshot);

                // Re-apply the exact same vector one tick later, after any
                // PlayerItemHeldEvent resets have already come and gone.
                Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
                    tntTag.getKnockbackPending().remove(target.getUniqueId());
                    if (target.isOnline()) {
                        target.setVelocity(knockbackSnapshot);
                    }
                }, 1L);
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

            // Clean up pending knockback state so we don't leak the entry.
            tntTag.getKnockbackPending().remove(player.getUniqueId());

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
            Cuboid cuboid = event.getEventData().getCuboid();
            if (!cuboid.contains(e.getTo())) {
                tntTag.teleportPlayer(e.getPlayer());
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

    /**
     * Suppresses hotbar-slot changes during the one-tick knockback re-apply window.
     *
     * <p>When the attacker rapidly switches from slot 1 → slot 3 in the same tick
     * as the hit, Minecraft fires a {@link PlayerItemHeldEvent} whose processing
     * resets the target's queued velocity back to zero.  The snapshot stored in
     * {@link TNTTag#getKnockbackPending()} is the exact vector Minecraft itself
     * computed; the scheduled task will restore it on the next tick regardless.
     * Cancelling the hotbar switch here is a belt-and-suspenders guard that also
     * ensures the attacker's item-state stays consistent with their last valid
     * selection before the spam-switch.</p>
     */
    @Override
    public void onPlayerItemHeld(Event event, PlayerItemHeldEvent e) {
        if (event instanceof TNTTag tntTag) {
            // If the switching player has a knockback snapshot in flight, cancel the
            // hotbar switch so it cannot interfere with the pending velocity restore.
            if (tntTag.getKnockbackPending().containsKey(e.getPlayer().getUniqueId())) {
                e.setCancelled(true);
            }
        }
    }

}
