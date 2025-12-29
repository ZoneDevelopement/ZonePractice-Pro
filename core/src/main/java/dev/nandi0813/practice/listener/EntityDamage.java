package dev.nandi0813.practice.listener;

import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.manager.server.WorldEnum;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamage implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        if (!e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) return;
        if (!ServerManager.getInstance().getInWorld().containsKey(player)) return;

        if (ServerManager.getInstance().getInWorld().get(player).equals(WorldEnum.LOBBY)) {
            Location lobbyLocation = ServerManager.getLobby();
            if (lobbyLocation != null)
                player.teleport(lobbyLocation);
        }

        e.setCancelled(true);
    }

}
