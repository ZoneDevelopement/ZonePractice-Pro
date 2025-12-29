package dev.nandi0813.practice.manager.fight.event.events.ffa.oitc;

import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.enums.EventStatus;
import dev.nandi0813.practice.manager.fight.event.events.ffa.interfaces.FFAListener;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

public class OITCListener extends FFAListener {

    @Override
    public void onEntityDamage(Event event, EntityDamageEvent e) {
        if (event instanceof OITC) {
            if (!(e.getEntity() instanceof Player player)) {
                return;
            }

            if (!event.getStatus().equals(EventStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            if (!e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
                e.setCancelled(true);
            }

            if (e.getCause().equals(EntityDamageEvent.DamageCause.VOID)) {
                event.killPlayer(player, true);
            }
        }
    }

    @Override
    public void onEntityDamageByEntity(Event event, EntityDamageByEntityEvent e) {
        if (event instanceof OITC oitc) {
            if (!(e.getEntity() instanceof Player target)) {
                return;
            }

            if (!oitc.getStatus().equals(EventStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            if (e.getDamager() instanceof Player attacker) {
                if (EventManager.getInstance().getEventByPlayer(attacker) instanceof OITC) {
                    if (target.getHealth() - e.getFinalDamage() <= 0) {
                        e.setDamage(0);
                        oitc.givePoints(attacker, 10);
                        oitc.killPlayer(target, false);
                    }
                } else
                    e.setCancelled(true);
            } else {
                if (e.getDamager() instanceof Arrow arrow) {

                    if (arrow.getShooter() instanceof Player attacker) {
                        if (attacker.equals(target)) {
                            e.setCancelled(true);
                            return;
                        }

                        Profile attackerProfile = ProfileManager.getInstance().getProfile(attacker);

                        if (attackerProfile.getStatus().equals(ProfileStatus.EVENT) && EventManager.getInstance().getEventByPlayer(attacker).equals(event)) {
                            attacker.getInventory().addItem(new ItemStack(Material.ARROW));
                            attacker.updateInventory();

                            e.setDamage(0);
                            oitc.givePoints(attacker, 10);
                            oitc.killPlayer(target, false);
                        } else
                            e.setCancelled(true);
                    } else
                        e.setCancelled(true);
                } else {
                    if (target.getHealth() - e.getFinalDamage() <= 0) {
                        e.setDamage(0);
                        event.killPlayer(target, false);
                    }
                }
            }
        }
    }

    @Override
    public void onProjectileLaunch(Event event, ProjectileLaunchEvent e) {

    }

    @Override
    public void onPlayerMove(Event event, PlayerMoveEvent e) {
        if (event instanceof OITC) {
            Cuboid cuboid = event.getEventData().getCuboid();

            if (!cuboid.contains(e.getTo())) {
                if (event.getStatus().equals(EventStatus.LIVE)) {
                    event.killPlayer(e.getPlayer(), true);
                } else {
                    ((OITC) event).teleport(e.getPlayer());
                }
            }
        }
    }

    @Override
    public void onPlayerInteract(Event event, PlayerInteractEvent e) {
        if (event instanceof OITC) {
            if (!e.getAction().equals(Action.RIGHT_CLICK_AIR) && !e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                return;
            }

            if (!event.getStatus().equals(EventStatus.LIVE)) {
                Player player = e.getPlayer();
                if (ClassImport.getClasses().getPlayerUtil().isItemInUse(player, Material.BOW)) {
                    e.setCancelled(true);
                    player.updateInventory();
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
