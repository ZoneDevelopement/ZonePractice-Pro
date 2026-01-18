package dev.nandi0813.practice.manager.ladder.settings;

import dev.nandi0813.practice.manager.fight.match.Match;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Base interface for all setting handlers.
 * Each SettingType should have a corresponding handler that implements this interface.
 * This centralizes all setting logic in one place instead of scattered across listeners.
 *
 * @param <T> The type of value this setting manages (Boolean, Integer, etc.)
 */
public interface SettingHandler<T> {

    /**
     * Gets the current value of this setting for a match.
     *
     * @param match The match to get the setting value from
     * @return The current value of this setting
     */
    T getValue(Match match);

    /**
     * Handles an event related to this setting.
     * This method is called by the centralized event processor when an event occurs.
     * <p>
     * Most settings are passive (only provide values) and don't need to override this.
     * Only event-based settings (REGENERATION, HUNGER, etc.) should override.
     *
     * @param event  The Bukkit event to handle
     * @param match  The match context
     * @param player The player involved (can be null)
     * @return true if the event was handled/modified, false otherwise
     */
    default boolean handleEvent(Event event, Match match, Player player) {
        return false; // Default: no event handling
    }

    /**
     * Validates that this setting is properly configured for the match.
     *
     * @param match The match to validate
     * @return true if valid, false otherwise
     */
    default boolean validate(Match match) {
        return true;
    }

    /**
     * Called when the match starts, allowing the setting to initialize.
     *
     * @param match The match that is starting
     */
    default void onMatchStart(Match match) {
        // Override if needed
    }

    /**
     * Called when the match ends, allowing the setting to cleanup.
     *
     * @param match The match that is ending
     */
    default void onMatchEnd(Match match) {
        // Override if needed
    }
}
