package dev.nandi0813.practice.util;

import dev.nandi0813.practice.manager.backend.ConfigManager;

public enum PermanentConfig {
    ;

    public static final int ARENA_COPY_MAX_CHANGES = 50 * (ConfigManager.getBoolean("ARENA.FAST-COPY") ? 3 : 1);
    public static final int ARENA_COPY_MAX_CHECKS = 500 * (ConfigManager.getBoolean("ARENA.FAST-COPY") ? 3 : 1);
    public static final boolean JOIN_TELEPORT_LOBBY = ConfigManager.getBoolean("PLAYER.JOIN-TELEPORT-LOBBY");

    public static final boolean MATCH_EXP_BAR = ConfigManager.getBoolean("MATCH-SETTINGS.ENDERPEARL.EXP-BAR");
    public static final boolean FFA_EXP_BAR = ConfigManager.getBoolean("FFA.ENDER-PEARL-EXP-BAR");
    public static final boolean DISPLAY_ARROW_HIT = ConfigManager.getBoolean("MATCH-SETTINGS.DISPLAY-ARROW-HIT-HEALTH");
    public static final boolean PARTY_SPLIT_TEAM_DAMAGE = ConfigManager.getBoolean("MATCH-SETTINGS.PARTY.SPLIT-TEAM-DAMAGE");
    public static final boolean PARTY_VS_PARTY_TEAM_DAMAGE = ConfigManager.getBoolean("MATCH-SETTINGS.PARTY.PARTY-VS-PARTY-TEAM-DAMAGE");

    public static final String FIGHT_ENTITY = "ZONEPRACTICE_PRO_FIGHT_ENTITY";
    public static final String PLACED_IN_FIGHT = "ZONE_PRACTICE_BLOCK_CHANGE";

}
