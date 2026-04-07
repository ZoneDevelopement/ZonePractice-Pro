package dev.nandi0813.practice.manager.duel.guis;

import dev.nandi0813.practice.manager.gui.GUI;
import dev.nandi0813.practice.manager.gui.GUIType;
import org.bukkit.entity.Player;

/**
 * Abstract base class for bot-related GUIs
 *
 * This base class provides a foundation for bot configuration and selection GUIs,
 * designed to be extended for various gameplay modes including:
 * - Single bot duels
 * - Party games with multiple bots
 * - Custom tournament modes
 * - AI-powered team games
 *
 * Subclasses should implement:
 * - Custom GUI layouts and items
 * - Mode-specific selection callbacks
 * - Integration with respective game managers
 */
public abstract class BotGameplayGUI extends GUI {

    /**
     * Base constructor for bot-related GUIs
     * @param type The GUIType for this GUI
     */
    protected BotGameplayGUI(GUIType type) {
        super(type);
    }

    /**
     * Called when a player returns from this GUI to a parent GUI
     * Can be overridden to handle cleanup or state updates
     * @param player The player who closed the GUI
     */
    protected void onReturn(Player player) {
        // Override in subclasses if needed
    }

    /**
     * Get the display name for this GUI
     * Used for inventory title formatting
     * @return The display name
     */
    protected abstract String getDisplayName();

    /**
     * Helper method to format difficulty-related text
     * @param difficultyName The name of the bot difficulty
     * @param description A brief description
     * @return A formatted string
     */
    protected String formatDifficultyText(String difficultyName, String description) {
        return String.format("§6%s§r - §7%s", difficultyName, description);
    }
}

