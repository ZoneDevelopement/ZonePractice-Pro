package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.StringUtil;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for creating and configuring hologram armor stands.
 * Centralizes all armor stand creation logic to prevent code duplication.
 */
@UtilityClass
public class ArmorStandFactory {

    /**
     * Creates a new hologram armor stand at the specified location.
     *
     * @param location The location to spawn at (must have a valid world)
     * @param text The display text (will be color-coded)
     * @return The created ArmorStand, or null if location is invalid
     */
    @Nullable
    public ArmorStand create(@NotNull Location location, @NotNull String text) {
        if (location.getWorld() == null) {
            return null;
        }

        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        configure(stand, text);
        return stand;
    }

    /**
     * Configures an armor stand for hologram display.
     *
     * @param stand The armor stand to configure
     * @param text The display text (will be color-coded)
     */
    public void configure(@NotNull ArmorStand stand, @NotNull String text) {
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(StringUtil.CC(text));
        stand.setBasePlate(false);
        stand.setArms(false);
        ClassImport.getClasses().getArenaUtil().setArmorStandInvulnerable(stand);
    }

    /**
     * Updates the display text of an armor stand.
     *
     * @param stand The armor stand to update
     * @param text The new display text (will be color-coded)
     */
    public void updateText(@NotNull ArmorStand stand, @NotNull String text) {
        stand.setCustomName(StringUtil.CC(text));
    }

    /**
     * Checks if an entity is a hologram armor stand.
     * Hologram armor stands are invisible with custom names visible.
     *
     * @param entity The entity to check
     * @return true if it's a hologram armor stand
     */
    public boolean isHologramArmorStand(@Nullable Entity entity) {
        return entity instanceof ArmorStand stand
            && !stand.isVisible()
            && stand.isCustomNameVisible();
    }

    /**
     * Safely removes an armor stand if it exists and is not dead.
     *
     * @param stand The armor stand to remove (can be null)
     */
    public void safeRemove(@Nullable ArmorStand stand) {
        if (stand != null && !stand.isDead()) {
            stand.remove();
        }
    }

    /**
     * Checks if an armor stand is alive and valid for use.
     *
     * @param stand The armor stand to check (can be null)
     * @return true if the armor stand is alive
     */
    public boolean isAlive(@Nullable ArmorStand stand) {
        return stand != null && !stand.isDead();
    }
}

