package dev.nandi0813.practice.manager.fight.match.util;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;

/**
 * The Elo system used for ranked match Elo changes.
 * <p>
 * {@link #SKILL} (default) uses the standard Elo formula
 * {@code newElo = oldElo + K * (result - expected)} where {@code expected} is based
 * on the rating difference between the two players.
 * {@link #RANDOM} keeps the legacy behavior: both players receive the same random
 * value from {@code QUEUE.RANKED.ELO-CHANGE}.
 * <p>
 * The active mode is resolved statically from {@code QUEUE.RANKED.ELO-SYSTEM.MODE}
 * on plugin load (and on config reload) via {@link #validateConfig()}. An invalid or
 * missing value logs a console warning and falls back to {@link #SKILL}.
 */
@Getter
public enum EloMode {

    SKILL,
    RANDOM;

    private static final String CONFIG_PATH = "QUEUE.RANKED.ELO-SYSTEM.MODE";

    @Getter
    private static EloMode activeMode = SKILL;

    /**
     * Reads {@code QUEUE.RANKED.ELO-SYSTEM.MODE} from the config and statically
     * resolves the active mode. Invalid or missing values fall back to {@link #SKILL}
     * and log a console warning.
     * <p>
     * Called once on plugin startup and on every config reload so the mode is always
     * in sync with what the admin set.
     */
    public static void validateConfig() {
        String configured = ConfigManager.getString(CONFIG_PATH);

        if (configured.isEmpty()) {
            activeMode = SKILL;
            Common.sendConsoleMMMessage("<yellow>[ZonePractice] " + CONFIG_PATH + " is not set, defaulting to SKILL.");
            return;
        }

        try {
            activeMode = EloMode.valueOf(configured.toUpperCase());
        } catch (IllegalArgumentException e) {
            activeMode = SKILL;
            Common.sendConsoleMMMessage("<red>[ZonePractice] Invalid " + CONFIG_PATH + ": '" + configured
                    + "'. Valid options: SKILL, RANDOM. Defaulting to SKILL.");
        }
    }

}