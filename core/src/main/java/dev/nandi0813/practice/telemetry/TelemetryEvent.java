package dev.nandi0813.practice.telemetry;

/**
 * Combat action event types for time-series telemetry logging.
 * Events track granular combat interactions, movement, and ability usage.
 */
public enum TelemetryEvent {

    // Combat actions
    ATTACK_LANDED,
    ATTACK_MISSED,
    DAMAGE_TAKEN,
    POTION_THROWN,
    POTION_LANDED,
    POTION_MISSED,

    // Movement & positioning
    SPRINT_START,
    SPRINT_RESET,
    BLOCK_PLACE,
    BLOCK_BREAK,
    MOVEMENT_FORWARD,
    MOVEMENT_RETREAT,

    // Match/round lifecycle
    MATCH_START,
    MATCH_END,
    ROUND_START,
    ROUND_END,
    PLAYER_DEATH,
    PLAYER_RESPAWN,

    // Aim/reach metrics
    REACH_ATTEMPT,
    AIM_ADJUSTMENT,
    CAMERA_ANGLE_CHANGE

}


