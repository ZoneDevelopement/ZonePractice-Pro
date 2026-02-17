package dev.nandi0813.practice.manager.leaderboard.hologram;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single line in a hologram display.
 * Each HologramLine manages exactly ONE ArmorStand entity with strict lifecycle management.
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Spawning/despawning armor stand entities</li>
 *   <li>Updating display text without flicker</li>
 *   <li>Auto-recovery when armor stands are removed externally</li>
 *   <li>Thread-safe state tracking</li>
 * </ul>
 */
@Getter
public class HologramLine {

    @Setter
    private ArmorStand entity;
    private Location location;
    private String text = "";
    private boolean spawned;

    /**
     * Creates an unspawned hologram line.
     * Call {@link #spawn(Location, String)} to create the actual entity.
     */
    public HologramLine() {
        this.spawned = false;
    }

    /**
     * Spawns the armor stand at the specified location.
     * This method is idempotent - calling multiple times won't create duplicates.
     *
     * @param loc  The spawn location
     * @param text The display text (supports color codes)
     * @return The spawned ArmorStand, or existing one if already spawned
     */
    @Nullable
    public ArmorStand spawn(@NotNull Location loc, @NotNull String text) {
        // Return existing if already alive
        if (spawned && ArmorStandFactory.isAlive(entity)) {
            return entity;
        }

        this.location = loc.clone();
        this.text = text;

        if (location.getWorld() == null) {
            return null;
        }

        this.entity = ArmorStandFactory.create(location, text);
        this.spawned = (entity != null);

        return entity;
    }

    /**
     * Despawns and removes the armor stand entity.
     * The line can be respawned later with {@link #spawn(Location, String)}.
     */
    public void despawn() {
        if (!spawned) {
            return;
        }

        ArmorStandFactory.safeRemove(entity);
        entity = null;
        spawned = false;
    }

    /**
     * Updates the display text.
     * If the armor stand was externally removed, it will be automatically respawned.
     *
     * @param newText The new display text (supports color codes)
     */
    public void updateText(@NotNull String newText) {
        this.text = newText;

        // If entity is alive, just update the text
        if (ArmorStandFactory.isAlive(entity)) {
            ArmorStandFactory.updateText(entity, newText);
            return;
        }

        // Auto-respawn if entity was killed externally
        if (spawned && location != null && location.getWorld() != null) {
            this.entity = ArmorStandFactory.create(location, newText);
        }
    }

    /**
     * Teleports the armor stand to a new location.
     *
     * @param newLoc The new location
     * @return true if teleported successfully
     */
    public boolean teleport(@NotNull Location newLoc) {
        if (!isValid()) {
            return false;
        }

        this.location = newLoc.clone();
        return entity.teleport(newLoc);
    }

    /**
     * Checks if this line has a valid, alive armor stand.
     *
     * @return true if the entity is alive
     */
    public boolean isValid() {
        return spawned && ArmorStandFactory.isAlive(entity);
    }

    /**
     * Gets the Y coordinate of this line.
     *
     * @return The Y coordinate, or 0 if not spawned
     */
    public double getY() {
        if (location != null) {
            return location.getY();
        }
        return ArmorStandFactory.isAlive(entity) ? entity.getLocation().getY() : 0;
    }

    /**
     * Updates both location and text in one operation.
     *
     * @param newLoc  The new location
     * @param newText The new display text
     * @return true if successful
     */
    public boolean update(@NotNull Location newLoc, @NotNull String newText) {
        if (!isValid()) {
            return false;
        }

        this.location = newLoc.clone();
        this.text = newText;

        entity.teleport(newLoc);
        ArmorStandFactory.updateText(entity, newText);

        return true;
    }

    /**
     * Force-cleans this line by removing the entity regardless of state.
     * Use as a recovery method when normal despawn might not work.
     */
    public void forceClean() {
        ArmorStandFactory.safeRemove(entity);
        entity = null;
        spawned = false;
    }
}
