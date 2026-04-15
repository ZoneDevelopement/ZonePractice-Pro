package dev.nandi0813.practice.util;

import dev.nandi0813.practice.manager.backend.ConfigManager;

public enum PermanentConfig {
    ;

    public static final boolean ARENA_FAST_COPY_ENABLED = ConfigManager.getBoolean("ARENA.FAST-COPY.ENABLED");
    public static final boolean ARENA_COPY_FAWE_ENABLED = ConfigManager.getBoolean("ARENA.FAST-COPY.USE-FAWE");
    public static int ARENA_COPY_MAX_CHANGES = 50;
    public static int ARENA_COPY_MAX_CHECKS = 500;
    public static int MATCH_ROLLBACK_MAX_CHANGES = 100;
    public static int MATCH_ROLLBACK_MAX_CHECKS = 300;
    public static int EVENT_ROLLBACK_MAX_CHANGES = 50;
    public static int EVENT_ROLLBACK_MAX_CHECKS = 100;
    public static final boolean JOIN_TELEPORT_LOBBY = ConfigManager.getBoolean("PLAYER.JOIN-TELEPORT-LOBBY");
    public static final boolean NAMETAG_MANAGEMENT_ENABLED = ConfigManager.getBoolean("PLAYER.NAMETAG-MANAGEMENT.ENABLED");

    public static final boolean DISPLAY_ARROW_HIT = ConfigManager.getBoolean("MATCH-SETTINGS.DISPLAY-ARROW-HIT-HEALTH");
    public static final boolean PARTY_SPLIT_TEAM_DAMAGE = ConfigManager.getBoolean("MATCH-SETTINGS.PARTY.SPLIT-TEAM-DAMAGE");
    public static final boolean PARTY_VS_PARTY_TEAM_DAMAGE = ConfigManager.getBoolean("MATCH-SETTINGS.PARTY.PARTY-VS-PARTY-TEAM-DAMAGE");

    public static final String FIGHT_ENTITY = "ZONEPRACTICE_PRO_FIGHT_ENTITY";
    public static final String PLACED_IN_FIGHT = "ZONE_PRACTICE_BLOCK_CHANGE";

    static {
        if (ARENA_FAST_COPY_ENABLED) {
            int multiplier = ConfigManager.getInt("ARENA.FAST-COPY.MULTIPLIER");
            if (multiplier < 1) {
                multiplier = 1;
            } else if (multiplier > 100) {
                multiplier = 100;
            }

            ARENA_COPY_MAX_CHANGES = ARENA_COPY_MAX_CHANGES * multiplier;
            ARENA_COPY_MAX_CHECKS = ARENA_COPY_MAX_CHECKS * multiplier;
        }

        int rollbackMultiplier = ConfigManager.getInt("MATCH-SETTINGS.ROLLBACK.MULTIPLIER");
        if (rollbackMultiplier < 1) {
            rollbackMultiplier = 1;
        } else if (rollbackMultiplier > 100) {
            rollbackMultiplier = 100;
        }

        MATCH_ROLLBACK_MAX_CHANGES = MATCH_ROLLBACK_MAX_CHANGES * rollbackMultiplier;
        MATCH_ROLLBACK_MAX_CHECKS = MATCH_ROLLBACK_MAX_CHECKS * rollbackMultiplier;
        EVENT_ROLLBACK_MAX_CHANGES = EVENT_ROLLBACK_MAX_CHANGES * rollbackMultiplier;
        EVENT_ROLLBACK_MAX_CHECKS = EVENT_ROLLBACK_MAX_CHECKS * rollbackMultiplier;
    }

}
