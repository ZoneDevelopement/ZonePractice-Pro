package dev.nandi0813.practice.manager.ladder.enums;

import dev.nandi0813.practice.manager.gui.setup.ladder.laddersettings.Settings.SettingType;
import lombok.Getter;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder class for creating LadderType configurations in a more readable and maintainable way.
 * Provides a fluent API for defining ladder properties.
 */
@Getter
public class LadderTypeConfig {

    private final String nameKey;
    private final Material icon;
    private final String descriptionKey;
    private final Class<?> classInstance;

    private boolean build = false;
    private boolean isPartyFFASupported = true;
    private boolean hasBed = false;
    private boolean hasPortal = false;

    private final List<SettingType> settingTypes = new ArrayList<>();

    private LadderTypeConfig(String nameKey, Material icon, String descriptionKey, Class<?> classInstance) {
        this.nameKey = nameKey;
        this.icon = icon;
        this.descriptionKey = descriptionKey;
        this.classInstance = classInstance;
    }

    /**
     * Creates a new ladder type configuration builder.
     *
     * @param nameKey        Language key for the ladder name
     * @param icon           Material icon for the ladder
     * @param descriptionKey Language key for the ladder description
     * @param classInstance  The ladder implementation class
     * @return A new builder instance
     */
    public static LadderTypeConfig builder(String nameKey, Material icon, String descriptionKey, Class<?> classInstance) {
        return new LadderTypeConfig(nameKey, icon, descriptionKey, classInstance);
    }

    /**
     * Marks this ladder as requiring build mechanics.
     */
    public LadderTypeConfig withBuild() {
        this.build = true;
        return this;
    }

    /**
     * Marks this ladder as not supporting Party FFA mode.
     */
    public LadderTypeConfig noPartyFFA() {
        this.isPartyFFASupported = false;
        return this;
    }

    /**
     * Marks this ladder as using bed mechanics (like BedWars).
     */
    public LadderTypeConfig withBed() {
        this.hasBed = true;
        return this;
    }

    /**
     * Marks this ladder as using portal mechanics (like Bridges, BattleRush).
     */
    public LadderTypeConfig withPortal() {
        this.hasPortal = true;
        return this;
    }

    /**
     * Adds a single setting type to this ladder.
     */
    public LadderTypeConfig withSetting(SettingType setting) {
        this.settingTypes.add(setting);
        return this;
    }

    /**
     * Adds multiple setting types to this ladder.
     */
    public LadderTypeConfig withSettings(SettingType... settings) {
        this.settingTypes.addAll(Arrays.asList(settings));
        return this;
    }

    /**
     * Adds common settings that most ladders use.
     * Includes: EDITABLE, HIT_DELAY, HUNGER, KNOCKBACK, WEIGHT_CLASS,
     * REGENERATION, ROUNDS, MAX_DURATION, START_COUNTDOWN, HEALTH_BELOW_NAME
     */
    public LadderTypeConfig withCommonSettings() {
        return withSettings(
                SettingType.EDITABLE,
                SettingType.HIT_DELAY,
                SettingType.HUNGER,
                SettingType.KNOCKBACK,
                SettingType.WEIGHT_CLASS,
                SettingType.REGENERATION,
                SettingType.ROUNDS,
                SettingType.MAX_DURATION,
                SettingType.START_COUNTDOWN,
                SettingType.HEALTH_BELOW_NAME
        );
    }

    /**
     * Adds pearl-related settings (ender pearl and golden apple cooldowns).
     */
    public LadderTypeConfig withPearlSettings() {
        return withSettings(
                SettingType.ENDER_PEARL_COOLDOWN,
                SettingType.GOLDEN_APPLE_COOLDOWN
        );
    }

    /**
     * Adds team-based settings.
     */
    public LadderTypeConfig withTeamSettings() {
        return withSettings(
                SettingType.DROP_INVENTORY_TEAM,
                SettingType.MULTI_ROUND_START_COUNTDOWN
        );
    }

    /**
     * Adds respawn-related settings for ladders that support respawning.
     */
    public LadderTypeConfig withRespawnSettings() {
        return withSettings(
                SettingType.RESPAWN_TIME,
                SettingType.MULTI_ROUND_START_COUNTDOWN
        );
    }

    /**
     * Adds build-related settings (TNT, temp build delay).
     */
    public LadderTypeConfig withBuildSettings() {
        return withSettings(
                SettingType.TNT_FUSE_TIME
        );
    }

    /**
     * Adds movement-related settings.
     */
    public LadderTypeConfig withMovementSettings() {
        return withSetting(SettingType.START_MOVING);
    }
}
