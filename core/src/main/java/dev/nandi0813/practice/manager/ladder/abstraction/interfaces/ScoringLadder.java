package dev.nandi0813.practice.manager.ladder.abstraction.interfaces;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import org.bukkit.entity.Player;

/**
 * Interface for ladders with custom win conditions that differ from standard death-based elimination.
 * Ladders implementing this interface define when rounds should end based on scoring mechanics.
 * <p>
 * Examples:
 * - Boxing: Round ends when a player reaches a certain hit count
 * - Bridges/BattleRush: Round ends when a player enters the opponent's portal
 */
public interface ScoringLadder {

    /**
     * Checks if the round should end based on ladder-specific win conditions.
     * Called after events that could trigger a win (hits, portal entry, etc.)
     *
     * @param match  The current match
     * @param round  The current round
     * @param player The player who triggered the check (e.g., the attacker, portal enterer)
     * @return true if the round should end
     */
    boolean shouldEndRound(Match match, Round round, Player player);

    /**
     * Gets the win condition message shown when a player wins.
     * This is used in end-of-round announcements.
     *
     * @return The win condition description
     */
    default String getWinConditionMessage() {
        return "";
    }

    /**
     * Checks if this scoring system is supported for the given match type.
     *
     * @param matchType The match type to check
     * @return true if scoring is supported for this match type
     */
    default boolean supportsScoringForMatchType(MatchType matchType) {
        return true;
    }

    /**
     * Gets a display-friendly name for the scoring mechanism.
     * Used in GUIs and messages.
     *
     * @return The scoring type display name
     */
    default String getScoringDisplayName() {
        return "Score";
    }
}
