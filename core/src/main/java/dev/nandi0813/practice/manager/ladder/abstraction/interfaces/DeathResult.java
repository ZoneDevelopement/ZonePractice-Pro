package dev.nandi0813.practice.manager.ladder.abstraction.interfaces;

/**
 * Represents the result of a player death in a match.
 * Used by ladders to indicate how the death should be handled.
 */
public enum DeathResult {

    /**
     * The player is permanently eliminated from the round.
     * Standard death behavior - the round may end if this was the last player.
     */
    ELIMINATED,

    /**
     * The player will respawn after a cooldown period.
     * Used by ladders like Bridges, BedWars (when bed is intact), etc.
     */
    TEMPORARY_DEATH,

    /**
     * No action needed - the death was already handled by the ladder.
     * Used when the ladder has custom death handling logic.
     */
    NO_ACTION
}
