package dev.nandi0813.practice.manager.ladder.settings.handlers;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.settings.SettingHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityRegainHealthEvent;

/**
 * Handler for the REGENERATION setting.
 * Controls whether players regenerate health from saturation.
 * <p>
 * IMPLEMENTATION LOCATION: This replaces the logic in LadderSettingListener.onRegen()
 */
public class RegenerationSettingHandler implements SettingHandler<Boolean> {

    @Override
    public Boolean getValue(Match match) {
        return match.getLadder().isRegen();
    }

    @Override
    public boolean handleEvent(Event event, Match match, Player player) {
        if (!(event instanceof EntityRegainHealthEvent e)) {
            return false;
        }

        // If regeneration is enabled, don't interfere
        if (getValue(match)) {
            return false;
        }

        // Cancel saturation-based healing if regeneration is disabled
        if (e.getRegainReason() == EntityRegainHealthEvent.RegainReason.SATIATED) {
            e.setCancelled(true);
            return true;
        }

        return false;
    }
}
