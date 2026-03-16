package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.ZonePractice;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Factory for creating and configuring hologram text displays.
 * Centralizes all text display creation logic to prevent code duplication.
 */
@UtilityClass
public class TextDisplayFactory {

    /**
     * Creates a new hologram text display at the specified location.
     *
     * @param location The location to spawn at (must have a valid world)
     * @param text The display text (will be color-coded)
     * @return The created TextDisplay, or null if location is invalid
     */
    @Nullable
    public TextDisplay create(@NotNull Location location, @NotNull String text) {
        if (location.getWorld() == null) {
            return null;
        }

        TextDisplay display = (TextDisplay) location.getWorld().spawnEntity(location, EntityType.TEXT_DISPLAY);
        configure(display, text);
        return display;
    }

    /**
     * Configures a text display for hologram display.
     *
     * @param display The text display to configure
     * @param text The display text (supports legacy formatting)
     */
    public void configure(@NotNull TextDisplay display, @NotNull String text) {
        display.text(deserializeText(text));
        display.setBillboard(Display.Billboard.CENTER);
        display.setDefaultBackground(false);
        display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        display.setShadowed(true);
        display.setGravity(false);
        display.setInvulnerable(true);
        display.setPersistent(false);
        display.setSilent(true);
        display.setSeeThrough(false);
    }

    /**
     * Updates the display text of a text display.
     *
     * @param display The text display to update
     * @param text The new display text
     */
    public void updateText(@NotNull TextDisplay display, @NotNull String text) {
        display.text(deserializeText(text));
    }

    private Component deserializeText(@NotNull String text) {
        if (text.contains("<") && text.contains(">")) {
            return ZonePractice.getMiniMessage().deserialize(text);
        }
        return LegacyComponentSerializer.legacySection().deserialize(text.replace('&', LegacyComponentSerializer.SECTION_CHAR));
    }

    /**
     * Checks if an entity is a hologram text display.
     *
     * @param entity The entity to check
     * @return true if it's a hologram text display
     */
    public boolean isHologramTextDisplay(@Nullable Entity entity) {
        return entity instanceof TextDisplay display
                && display.getBillboard() == Display.Billboard.CENTER
                && !display.isDefaultBackground()
                && display.isShadowed()
                && !display.isPersistent();
    }

    /**
     * Safely removes a text display if it exists and is not dead.
     *
     * @param display The text display to remove (can be null)
     */
    public void safeRemove(@Nullable TextDisplay display) {
        if (display != null && !display.isDead()) {
            display.remove();
        }
    }

    /**
     * Checks if a text display is alive and valid for use.
     *
     * @param display The text display to check (can be null)
     * @return true if the text display is alive
     */
    public boolean isAlive(@Nullable TextDisplay display) {
        return display != null && !display.isDead();
    }
}



