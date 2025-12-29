package dev.nandi0813.practice.manager.spectator;

import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.Brackets;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class SpectatorListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player attacker) {
            Profile attackerProfile = ProfileManager.getInstance().getProfile(attacker);

            if (attackerProfile.getStatus().equals(ProfileStatus.SPECTATE)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            Profile profile = ProfileManager.getInstance().getProfile(player);
            if (profile == null) return;

            if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLeaveCuboid(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(player);
            if (spectatable != null) {
                Cuboid cuboid = spectatable.getCuboid();

                if (!cuboid.contains(e.getTo()))
                    player.teleport(cuboid.getCenter());
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player player) {
            Profile profile = ProfileManager.getInstance().getProfile(player);

            if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        Player player = (Player) e.getEntity();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            e.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        Player player = (Player) e.getWhoClicked();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(player);
            if (spectatable != null && !(spectatable instanceof Brackets))
                spectatable.removeSpectator(player);
        }
    }

}
