package dev.nandi0813.practice.manager.spectator;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Utilities for preventing spectator bodies from blocking end crystal placement in arenas.
 */
public final class SpectatorCrystalPlacementUtil {

    private SpectatorCrystalPlacementUtil() {
    }

    public static void clearSpectatorsBlockingCrystalPlacement(PlayerInteractEvent event, Cuboid arenaCuboid) {
        if (!isCrystalPlacementAttempt(event)) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        Location crystalBase = clickedBlock.getLocation().add(0.5D, 1.0D, 0.5D);
        for (Entity entity : clickedBlock.getWorld().getNearbyEntities(crystalBase, 0.75D, 1.5D, 0.75D)) {
            if (!(entity instanceof Player nearbyPlayer)) {
                continue;
            }

            if (!isPlacementBlockingSpectator(nearbyPlayer, arenaCuboid)) {
                continue;
            }

            Location destination = nearbyPlayer.getLocation().clone().add(0.0D, 2.0D, 0.0D);
            if (!arenaCuboid.contains(destination)) {
                destination = arenaCuboid.getCenter().clone().add(0.0D, 1.0D, 0.0D);
            }

            nearbyPlayer.teleport(destination);
        }
    }

    private static boolean isCrystalPlacementAttempt(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return false;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return false;
        }

        Material baseType = clickedBlock.getType();
        if (baseType != Material.OBSIDIAN && baseType != Material.BEDROCK) {
            return false;
        }

        ItemStack item = event.getItem();
        return item != null && item.getType() == Material.END_CRYSTAL;
    }

    private static boolean isPlacementBlockingSpectator(Player player, Cuboid arenaCuboid) {
        if (!player.isOnline() || player.isDead() || !arenaCuboid.contains(player.getLocation())) {
            return false;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            return true;
        }

        if (SpectatorManager.getInstance().getSpectators().containsKey(player)) {
            return true;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        return profile != null && profile.getStatus() == ProfileStatus.SPECTATE;
    }
}

