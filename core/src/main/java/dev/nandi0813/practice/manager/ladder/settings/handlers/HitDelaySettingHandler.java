package dev.nandi0813.practice.manager.ladder.settings.handlers;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.settings.SettingHandler;
import org.bukkit.entity.Player;

/**
 * Handler for the HIT_DELAY setting.
 * Controls the delay between attacks (maximum no damage ticks).
 * <p>
 * IMPLEMENTATION LOCATION: This is applied in Round.startRound() when setting up players
 */
public class HitDelaySettingHandler implements SettingHandler<Integer> {

    @Override
    public Integer getValue(Match match) {
        return match.getLadder().getHitDelay();
    }

    @Override
    public void onMatchStart(Match match) {
        // Apply hit delay to all players
        int hitDelay = getValue(match);
        for (Player player : match.getPlayers()) {
            player.setMaximumNoDamageTicks(hitDelay);
        }
    }

}