package dev.nandi0813.practice.manager.fight.event.interfaces;

import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public abstract class EventListenerInterface {

    public abstract void onEntityDamage(final Event event, final EntityDamageEvent e);

    public abstract void onEntityDamageByEntity(final Event event, final EntityDamageByEntityEvent e);

    public abstract void onProjectileLaunch(final Event event, final ProjectileLaunchEvent e);

    public abstract void onPlayerQuit(final Event event, final PlayerQuitEvent e);

    public abstract void onPlayerMove(final Event event, final PlayerMoveEvent e);

    public abstract void onPlayerInteract(final Event event, final PlayerInteractEvent e);

    public abstract void onPlayerEggThrow(final Event event, final PlayerEggThrowEvent e);

    public abstract void onPlayerDropItem(final Event event, final PlayerDropItemEvent e);

    public abstract void onInventoryClick(final Event event, final InventoryClickEvent e);

}
