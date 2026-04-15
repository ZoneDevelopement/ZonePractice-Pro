package dev.nandi0813.practice.manager.ladder.settings.handlers;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.settings.SettingHandler;
import org.bukkit.entity.Player;

/**
 * Handler for the HEALTH_BELOW_NAME setting.
 * Controls whether health is displayed below player names using PacketEvents.
 * <p>
 * IMPLEMENTATION: Delegates to the merged NametagManager below-name path
 * The actual display is managed by Match.addPlayerToBelowName() and Match.removePlayerFromBelowName()
 */
public class HealthBelowNameSettingHandler implements SettingHandler<Boolean> {

    @Override
    public Boolean getValue(Match match) {
        return match.getLadder().isHealthBelowName();
    }

    @Override
    public void onMatchStart(Match match) {
        if (!getValue(match)) {
            return; // Setting disabled
        }

        // Setup health display for all players using NametagManager's below-name path
        for (Player player : match.getPlayers()) {
            match.addPlayerToBelowName(player);
        }
    }

    @Override
    public void onMatchEnd(Match match) {
        // Clean up health display for all players
        // Note: This is also called in Match.removePlayer() but we call it here
        // to ensure cleanup even if players don't leave normally
        for (Player player : match.getPlayers()) {
            match.removePlayerFromBelowName(player);
        }
    }

}
