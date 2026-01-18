package dev.nandi0813.practice.manager.ladder.abstraction.interfaces;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface for ladders that support player respawning after death.
 * Ladders implementing this interface can define custom death handling
 * with temporary respawn mechanics.
 * <p>
 * Examples: Bridges, BedWars, FireballFight, BattleRush
 */
public interface RespawnableLadder {

    /**
     * Handles player death and determines the result.
     * This method is called by the Match when a player dies.
     *
     * @param player The player who died
     * @param match  The current match
     * @param round  The current round
     * @return The death result indicating how the death should be handled
     */
    DeathResult handlePlayerDeath(Player player, Match match, Round round);

    /**
     * Gets the respawn time in seconds for this ladder.
     *
     * @return The respawn time in seconds
     */
    int getRespawnTime();

    /**
     * Sets the respawn time for this ladder.
     *
     * @param respawnTime The respawn time in seconds
     */
    void setRespawnTime(int respawnTime);

    /**
     * Gets the language path for respawn messages.
     * Used to retrieve the appropriate language strings for death/respawn notifications.
     *
     * @return The language path prefix (e.g., "MATCH.BRIDGES", "MATCH.BEDWARS")
     */
    default String getRespawnLanguagePath() {
        return "MATCH.RESPAWN";
    }

    /**
     * Checks if this ladder supports respawning for the given match type.
     * Some ladders may only support respawning in certain match types.
     *
     * @param matchType The match type to check
     * @return true if respawning is supported for this match type
     */
    default boolean supportsRespawnForMatchType(MatchType matchType) {
        return List.of(MatchType.DUEL, MatchType.PARTY_SPLIT, MatchType.PARTY_VS_PARTY).contains(matchType);
    }

    /**
     * Called when a player respawns after a temporary death.
     * Override to implement custom respawn behavior.
     *
     * @param player The player respawning
     * @param match  The current match
     */
    default void onPlayerRespawn(Player player, Match match) {
        // Default implementation - teleport player back to their position
        match.teleportPlayer(player);
    }
}
