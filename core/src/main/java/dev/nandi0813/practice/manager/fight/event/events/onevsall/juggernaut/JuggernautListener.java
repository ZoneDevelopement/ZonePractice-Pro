package dev.nandi0813.practice.manager.fight.event.events.onevsall.juggernaut;

import dev.nandi0813.practice.manager.fight.event.EventManager;
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

public class JuggernautListener extends FFAListener {

    @Override
    public void onEntityDamage(Event event, EntityDamageEvent e) {
        if (event instanceof Juggernaut) {
            Player player = (Player) e.getEntity();

            if (!event.getStatus().equals(EventStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                event.killPlayer(player, false);
                return;
            }

            if (player.getHealth() - e.getFinalDamage() <= 0) {
                e.setDamage(0);
                event.killPlayer(player, false);
            }
        }
    }

    @Override
    public void onEntityDamageByEntity(Event event, EntityDamageByEntityEvent e) {
        if (event instanceof Juggernaut juggernaut) {
            if (!(e.getDamager() instanceof Player attacker)) {
                return;
            }

            if (!(EventManager.getInstance().getEventByPlayer(attacker) instanceof Juggernaut)) {
                return;
            }

            if (juggernaut.getJuggernaut() != attacker && juggernaut.getJuggernaut() != e.getEntity()) {
                e.setCancelled(true);
            }
        }
    }

    @Override
    public void onProjectileLaunch(Event event, ProjectileLaunchEvent e) {

    }

    @Override
    public void onPlayerMove(Event event, PlayerMoveEvent e) {
        if (event instanceof Juggernaut) {
            if (event.getStatus().equals(EventStatus.LIVE)) {
                Player player = e.getPlayer();
                Juggernaut juggernaut = (Juggernaut) event;

                if (!juggernaut.getEventData().getCuboid().contains(player.getLocation())) {
                    juggernaut.killPlayer(player, false);
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
