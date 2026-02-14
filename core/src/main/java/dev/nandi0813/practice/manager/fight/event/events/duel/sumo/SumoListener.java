package dev.nandi0813.practice.manager.fight.event.events.duel.sumo;

import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelFight;
import dev.nandi0813.practice.manager.fight.event.events.duel.interfaces.DuelListener;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class SumoListener extends DuelListener {

    @Override
    public void onEntityDamage(Event event, EntityDamageEvent e) {
        if (event instanceof Sumo sumo) {
            Player player = (Player) e.getEntity();

            if (!sumo.getStatus().equals(EventStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            DuelFight duelFight = sumo.getFight(player);
            if (duelFight == null) {
                e.setCancelled(true);
                return;
            }

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                duelFight.endFight(player);
            } else {
                e.setDamage(0);
            }
        }
    }

    @Override
    public void onProjectileLaunch(Event event, ProjectileLaunchEvent e) {
    }

    @Override
    public void onPlayerMove(Event event, PlayerMoveEvent e) {
        super.onPlayerMove(event, e);

        if (event instanceof Sumo sumo) {
            Player player = e.getPlayer();

            if (event.getStatus().equals(EventStatus.START)) {
                if (sumo.isInFight(player)) {
                    Location from = e.getFrom();
                    Location to2 = e.getTo();

                    if ((to2.getX() != from.getX() || to2.getZ() != from.getZ()))
                        player.teleport(from);
                }
            } else if (event.getStatus().equals(EventStatus.LIVE)) {
                DuelFight duelFight = sumo.getFight(player);
                if (duelFight == null) return;

                // Check both the block at player's location and the block below (feet)
                Location playerLoc = player.getLocation();
                Material blockAtPlayer = playerLoc.getBlock().getType();
                Material blockBelow = playerLoc.clone().subtract(0, 1, 0).getBlock().getType();

                // Check if player is in/touching water
                if (blockAtPlayer.equals(Material.WATER) || blockAtPlayer.equals(ClassImport.getClasses().getItemMaterialUtil().getWater()) ||
                    blockBelow.equals(Material.WATER) || blockBelow.equals(ClassImport.getClasses().getItemMaterialUtil().getWater())) {
                    duelFight.endFight(player);
                }
                // Check if player is in/touching lava
                else if (blockAtPlayer.equals(Material.LAVA) || blockAtPlayer.equals(ClassImport.getClasses().getItemMaterialUtil().getLava()) ||
                         blockBelow.equals(Material.LAVA) || blockBelow.equals(ClassImport.getClasses().getItemMaterialUtil().getLava())) {
                    duelFight.endFight(player);
                }
            }
        }
    }

    @Override
    public void onPlayerEggThrow(Event event, PlayerEggThrowEvent e) {
    }

    @Override
    public void onPlayerDropItem(Event event, PlayerDropItemEvent e) {
    }

}
