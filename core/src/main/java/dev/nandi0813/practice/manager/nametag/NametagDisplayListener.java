package dev.nandi0813.practice.manager.nametag;

import dev.nandi0813.practice.ZonePractice;
import io.papermc.paper.event.player.PlayerClientLoadedWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class NametagDisplayListener implements Listener {

    private final Map<UUID, BukkitTask> pendingSneakRefresh = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClientLoad(PlayerClientLoadedWorldEvent event) {
        NametagManager manager = NametagManager.getInstance();
        manager.onPlayerJoin(event.getPlayer());
        manager.refreshAllNametags();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        if (event.getTo() == null) {
            return;
        }

        if (event.getFrom().getWorld() == event.getTo().getWorld()
                && event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        NametagManager.getInstance().onPlayerMove(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        NametagManager manager = NametagManager.getInstance();
        manager.onPlayerMove(event.getPlayer());
        manager.refreshAllNametags();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        NametagManager manager = NametagManager.getInstance();
        manager.onVisibilityStateChange(event.getPlayer());
        manager.refreshAllNametags();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        BukkitTask oldTask = pendingSneakRefresh.remove(player.getUniqueId());
        if (oldTask != null) {
            oldTask.cancel();
        }

        BukkitTask task = Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            pendingSneakRefresh.remove(player.getUniqueId());
            if (player.isOnline()) {
                NametagManager.getInstance().onVisibilityStateChange(player);
            }
        });
        pendingSneakRefresh.put(player.getUniqueId(), task);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        BukkitTask task = pendingSneakRefresh.remove(event.getPlayer().getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        NametagManager.getInstance().onVisibilityStateChange(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (player.isOnline()) {
                NametagManager.getInstance().onVisibilityStateChange(player);
            }
        }, 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        PotionEffectType modifiedType = event.getModifiedType();
        if (modifiedType == null || !modifiedType.equals(PotionEffectType.INVISIBILITY)) {
            return;
        }

        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            if (player.isOnline()) {
                NametagManager.getInstance().onVisibilityStateChange(player);
            }
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        NametagManager manager = NametagManager.getInstance();
        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            manager.onVisibilityStateChange(event.getPlayer());
            return;
        }

        Player player = event.getPlayer();
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            if (player.isOnline() && player.getGameMode() != GameMode.SPECTATOR) {
                manager.onVisibilityStateChange(player);
                manager.refreshAllNametags();
            }
        });
    }
}


