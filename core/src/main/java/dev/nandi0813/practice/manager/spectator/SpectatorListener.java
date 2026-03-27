package dev.nandi0813.practice.manager.spectator;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.Brackets;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.GameMode;
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
import org.bukkit.projectiles.ProjectileSource;

public class SpectatorListener implements Listener {

    /**
     * Spectator restrictions apply to:
     * - real spectator profiles,
     * - Bukkit spectator game mode,
     * - dead or temp-dead players still inside active matches.
     */
    private boolean hasSpectatorRestrictions(Player player) {
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) {
            return false;
        }

        if (profile.getStatus() == ProfileStatus.SPECTATE) {
            return true;
        }

        if (profile.getStatus() != ProfileStatus.MATCH) {
            return false;
        }

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) {
            return false;
        }

        if (match.getType().equals(MatchType.DUEL)) {
            return false;
        }

        if (match.getStatus().equals(MatchStatus.END)) {
            return false;
        }

        return match.getCurrentStat(player).isSet() || match.getCurrentRound().getTempKill(player) != null;
    }

    private void ensureSpectatorFlight(Player player) {
        if (!hasSpectatorRestrictions(player)) {
            return;
        }

        if (!player.getAllowFlight()) {
            player.setAllowFlight(true);
        }

        if (!player.isFlying()) {
            player.setFlying(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Player attacker = null;

        if (e.getDamager() instanceof Player playerDamager) {
            attacker = playerDamager;
        } else if (e.getDamager() instanceof org.bukkit.entity.Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player shooter) {
                attacker = shooter;
            }
        }

        if (attacker != null && hasSpectatorRestrictions(attacker)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player player) {
            if (hasSpectatorRestrictions(player)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (hasSpectatorRestrictions(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (hasSpectatorRestrictions(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent e) {
        if (hasSpectatorRestrictions(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();
        ensureSpectatorFlight(player);

        // Some teleports or server versions can drop flight state right after teleport.
        ZonePractice plugin = ZonePractice.getInstance();
        if (plugin != null && plugin.isEnabled()) {
            org.bukkit.Bukkit.getScheduler().runTask(plugin, () -> ensureSpectatorFlight(player));
        }
    }

    @EventHandler
    public void onLeaveCuboid(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        ensureSpectatorFlight(player);

        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile != null && profile.getStatus().equals(ProfileStatus.SPECTATE)) {
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
        if (hasSpectatorRestrictions(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (hasSpectatorRestrictions(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (hasSpectatorRestrictions(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (hasSpectatorRestrictions(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if (hasSpectatorRestrictions(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent e) {
        if (hasSpectatorRestrictions(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        ProjectileSource shooter = e.getEntity().getShooter();
        if (shooter instanceof Player player && hasSpectatorRestrictions(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }

        if (hasSpectatorRestrictions(player)) {
            e.setFoodLevel(20);
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (hasSpectatorRestrictions(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile != null && profile.getStatus().equals(ProfileStatus.SPECTATE)) {
            Spectatable spectatable = SpectatorManager.getInstance().getSpectators().get(player);
            if (spectatable != null && !(spectatable instanceof Brackets))
                spectatable.removeSpectator(player);
        }
    }

}
