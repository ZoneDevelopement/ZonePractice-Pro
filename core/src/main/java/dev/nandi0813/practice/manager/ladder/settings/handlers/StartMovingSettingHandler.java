package dev.nandi0813.practice.manager.ladder.settings.handlers;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.settings.SettingHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handler for the START_MOVING setting.
 * Controls whether players can move during the countdown phase.
 * <p>
 * IMPLEMENTATION LOCATION: This replaces the logic in StartListener.onPlayerMove()
 */
public class StartMovingSettingHandler implements SettingHandler<Boolean> {

    @Override
    public Boolean getValue(Match match) {
        return match.getLadder().isStartMove();
    }

    @Override
    public boolean handleEvent(Event event, Match match, Player player) {
        if (!(event instanceof PlayerMoveEvent e)) {
            return false;
        }

        // If start moving is enabled, don't interfere
        if (getValue(match)) {
            return false;
        }

        // Check if match is in countdown phase
        if (match.getCurrentRound().getRoundStatus() == dev.nandi0813.practice.manager.fight.match.enums.RoundStatus.START) {
            // Cancel movement if location changed (not just head rotation)
            if (e.getFrom().getX() != e.getTo().getX() ||
                    e.getFrom().getY() != e.getTo().getY() ||
                    e.getFrom().getZ() != e.getTo().getZ()) {
                e.setCancelled(true);
                return true;
            }
        }

        return false;
    }

}
