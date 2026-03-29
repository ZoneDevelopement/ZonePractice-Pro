package dev.nandi0813.practice.manager.ladder.settings.handlers;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.util.PlayerUtil;
import dev.nandi0813.practice.manager.ladder.settings.SettingHandler;
import org.bukkit.entity.Player;

/**
 * Handler for the HIT_DELAY setting.
 * Controls the attack cooldown multiplier (0-3, where 1.0 = normal speed of 20 ticks).
 * <p>
 * Examples:
 * - 0 = instant attacks (no cooldown)
 * - 0.5 = half speed (10 ticks)
 * - 1.0 = normal speed (20 ticks)
 * - 2.0 = double speed (40 ticks)
 * - 3.0 = triple speed (60 ticks)
 * <p>
 * IMPLEMENTATION LOCATION: This is applied in Round.startRound() when setting up players
 */
public class HitDelaySettingHandler implements SettingHandler<Double> {

    @Override
    public Double getValue(Match match) {
        return match.getLadder().getAttackCooldownModifier();
    }

    @Override
    public void onMatchStart(Match match) {
        // Apply attack cooldown to all players
        double cooldownMultiplier = getValue(match);
        for (Player player : match.getPlayers()) {
            // In 1.9+, attack cooldown bar/logic is driven by ATTACK_SPEED.
            PlayerUtil.setAttackSpeed(player, cooldownMultiplier);
        }
    }

    @Override
    public void onMatchEnd(Match match) {
        for (Player player : match.getPlayers()) {
            PlayerUtil.resetAttackSpeed(player);
        }
    }

}