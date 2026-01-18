package dev.nandi0813.practice.manager.ladder.settings;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import dev.nandi0813.practice.manager.ladder.settings.handlers.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Central registry for all setting handlers.
 * This class maps each SettingType to its implementation handler.
 * <p>
 * Instead of looking through multiple listener classes to find where a setting is implemented,
 * you can look here to see exactly which handler processes each setting.
 */
public enum SettingHandlerRegistry {
    ;

    private static final Logger LOGGER = Logger.getLogger(SettingHandlerRegistry.class.getName());
    private static final Map<SettingType, SettingHandler<?>> HANDLERS = new EnumMap<>(SettingType.class);

    static {
        // Register all setting handlers here
        // This is the SINGLE source of truth for where each setting is implemented

        // Event-based handlers (actually process events)
        register(SettingType.REGENERATION, new RegenerationSettingHandler());
        register(SettingType.HUNGER, new HungerSettingHandler());
        register(SettingType.START_MOVING, new StartMovingSettingHandler());
        register(SettingType.GOLDEN_APPLE_COOLDOWN, new GoldenAppleSettingHandler());

        // Match lifecycle handlers (have onMatchStart/onMatchEnd logic)
        register(SettingType.HIT_DELAY, new HitDelaySettingHandler());
        register(SettingType.HEALTH_BELOW_NAME, new HealthBelowNameSettingHandler());
    }

    /**
     * Registers a handler for a specific setting type.
     *
     * @param type    The setting type
     * @param handler The handler implementation
     */
    private static void register(SettingType type, SettingHandler<?> handler) {
        HANDLERS.put(type, handler);
        // LOGGER.info("Registered handler for " + type.name() + ": " + handler.getClass().getSimpleName());
    }

    /**
     * Gets the handler for a specific setting type.
     *
     * @param type The setting type
     * @return The handler, or null if not registered
     */
    public static SettingHandler<?> getHandler(SettingType type) {
        return HANDLERS.get(type);
    }

    /**
     * Checks if a handler is registered for a setting type.
     *
     * @param type The setting type
     * @return true if a handler exists
     */
    public static boolean hasHandler(SettingType type) {
        return HANDLERS.containsKey(type);
    }

    /**
     * Processes an event through all relevant setting handlers for a match.
     * This is called by the centralized event listener.
     *
     * @param event  The event to process
     * @param match  The match context
     * @param player The player involved (can be null)
     */
    public static void processEvent(Event event, Match match, Player player) {
        // Get all setting types enabled for this ladder
        for (SettingType settingType : match.getLadder().getType().getSettingTypes()) {
            SettingHandler<?> handler = getHandler(settingType);
            if (handler != null) {
                try {
                    handler.handleEvent(event, match, player);
                } catch (Exception e) {
                    LOGGER.severe("Error handling event " + event.getEventName() +
                            " with handler " + handler.getClass().getSimpleName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * Gets all registered handlers.
     *
     * @return Map of setting types to their handlers
     */
    public static Map<SettingType, SettingHandler<?>> getAllHandlers() {
        return new EnumMap<>(HANDLERS);
    }

}
