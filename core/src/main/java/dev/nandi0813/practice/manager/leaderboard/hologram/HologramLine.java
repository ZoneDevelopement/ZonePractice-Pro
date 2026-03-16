package dev.nandi0813.practice.manager.leaderboard.hologram;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a single line in a hologram display.
 * Each HologramLine manages exactly ONE TextDisplay entity with strict lifecycle management.
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Spawning/despawning text display entities</li>
 *   <li>Updating display text without flicker</li>
 *   <li>Auto-recovery when displays are removed externally</li>
 *   <li>Thread-safe state tracking</li>
 * </ul>
 */
@Getter
public class HologramLine {

    @Setter
    private TextDisplay entity;
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
     * Spawns the text display at the specified location.
     * This method is idempotent - calling multiple times won't create duplicates.
     *
     * @param loc  The spawn location
     * @param text The display text (supports color codes)
     * @return The spawned TextDisplay, or existing one if already spawned
     */
    @Nullable
    public TextDisplay spawn(@NotNull Location loc, @NotNull String text) {
        // Return existing if already alive
        if (spawned && TextDisplayFactory.isAlive(entity)) {
            return entity;
        }

        this.location = loc.clone();
        this.text = text;

        if (location.getWorld() == null) {
            return null;
        }

        this.entity = TextDisplayFactory.create(location, text);
        this.spawned = (entity != null);

        return entity;
    }

    /**
     * Despawns and removes the text display entity.
     * The line can be respawned later with {@link #spawn(Location, String)}.
     */
    public void despawn() {
        if (!spawned) {
            return;
        }

        TextDisplayFactory.safeRemove(entity);
        entity = null;
        spawned = false;
    }

    /**
     * Updates the display text.
     * If the display was externally removed, it will be automatically respawned.
     *
     * @param newText The new display text (supports color codes)
     */
    public void updateText(@NotNull String newText) {
        this.text = newText;

        // If entity is alive, just update the text
        if (TextDisplayFactory.isAlive(entity)) {
            TextDisplayFactory.updateText(entity, newText);
            return;
        }

        // Auto-respawn if entity was killed externally
        if (spawned && location != null && location.getWorld() != null) {
                this.entity = TextDisplayFactory.create(location, newText);
        }
    }

    /**
     * Teleports the text display to a new location.
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
     * Checks if this line has a valid, alive text display.
     *
     * @return true if the entity is alive
     */
    public boolean isValid() {
        return spawned && TextDisplayFactory.isAlive(entity);
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
        return TextDisplayFactory.isAlive(entity) ? entity.getLocation().getY() : 0;
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
        TextDisplayFactory.updateText(entity, newText);

        return true;
    }

    /**
     * Force-cleans this line by removing the entity regardless of state.
     * Use as a recovery method when normal despawn might not work.
     */
    public void forceClean() {
        TextDisplayFactory.safeRemove(entity);
        entity = null;
        spawned = false;
    }
}
