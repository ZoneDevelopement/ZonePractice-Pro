package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.StringUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single line in a hologram.
 * Each HologramLine manages exactly ONE ArmorStand entity with strict lifecycle management.
 *
 * This class prevents duplication by:
 * - Maintaining a direct reference to its ArmorStand entity
 * - Ensuring spawn() can only create one entity per instance
 * - Providing clean despawn() that removes the entity
 * - Tracking spawned state to prevent double-spawning
 */
@Getter
public class HologramLine {

    private ArmorStand entity;
    private Location location;
    private String text;
    private boolean spawned;

    /**
     * Creates a new HologramLine (not yet spawned).
     * Call spawn() to create the actual entity.
     */
    public HologramLine() {
        this.spawned = false;
        this.entity = null;
        this.location = null;
        this.text = "";
    }

    /**
     * Spawns the armor stand entity at the specified location with the given text.
     * This method is idempotent - calling it multiple times won't create duplicates.
     *
     * @param loc The location where the armor stand should spawn
     * @param text The text to display (supports color codes)
     * @return The spawned ArmorStand entity, or null if already spawned
     */
    @Nullable
    public ArmorStand spawn(@NotNull Location loc, @NotNull String text) {
        // Prevent double-spawning
        if (spawned && entity != null && !entity.isDead()) {
            return entity;
        }

        // Store the location and text
        this.location = loc.clone();
        this.text = text;

        // Safety check
        if (location.getWorld() == null) {
            return null;
        }

        // Spawn the armor stand
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        // Configure the armor stand for hologram display
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setCustomNameVisible(true);
        stand.setCustomName(StringUtil.CC(text));
        stand.setBasePlate(false);
        stand.setArms(false);

        // Make it invulnerable (version-compatible)
        ClassImport.getClasses().getArenaUtil().setArmorStandInvulnerable(stand);

        // Store reference and mark as spawned
        this.entity = stand;
        this.spawned = true;

        return stand;
    }

    /**
     * Despawns and removes the armor stand entity.
     * After calling this, the HologramLine can be respawned with spawn().
     */
    public void despawn() {
        if (!spawned) {
            return;
        }

        // Remove the entity
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }

        // Clear state
        entity = null;
        spawned = false;
    }

    /**
     * Updates the text displayed by this hologram line.
     * Only works if the line has been spawned.
     *
     * @param newText The new text to display (supports color codes)
     */
    public void updateText(@NotNull String newText) {
        this.text = newText;

        if (spawned && entity != null && !entity.isDead()) {
            entity.setCustomName(StringUtil.CC(newText));
        }
    }

    /**
     * Teleports this hologram line to a new location.
     * Useful for height adjustments or repositioning.
     *
     * @param newLoc The new location
     * @return true if teleported successfully, false otherwise
     */
    public boolean teleport(@NotNull Location newLoc) {
        if (!spawned || entity == null || entity.isDead()) {
            return false;
        }

        this.location = newLoc.clone();
        return entity.teleport(newLoc);
    }

    /**
     * Checks if this hologram line is currently valid (spawned and entity is alive).
     *
     * @return true if the entity exists and is alive, false otherwise
     */
    public boolean isValid() {
        return spawned && entity != null && !entity.isDead() && entity.isValid();
    }

    /**
     * Gets the current Y coordinate of this line.
     *
     * @return The Y coordinate, or 0 if not spawned
     */
    public double getY() {
        if (location != null) {
            return location.getY();
        }
        if (entity != null && !entity.isDead()) {
            return entity.getLocation().getY();
        }
        return 0;
    }

    /**
     * Updates both location and text in one operation.
     * More efficient than calling teleport() and updateText() separately.
     *
     * @param newLoc The new location
     * @param newText The new text
     * @return true if updated successfully, false otherwise
     */
    public boolean update(@NotNull Location newLoc, @NotNull String newText) {
        if (!spawned || entity == null || entity.isDead()) {
            return false;
        }

        this.location = newLoc.clone();
        this.text = newText;

        entity.teleport(newLoc);
        entity.setCustomName(StringUtil.CC(newText));

        return true;
    }

    /**
     * Force-cleans this line by removing the entity even if state is inconsistent.
     * This is a recovery method for when normal despawn() might not work.
     */
    public void forceClean() {
        if (entity != null && !entity.isDead()) {
            entity.remove();
        }
        entity = null;
        spawned = false;
    }
}
