package dev.nandi0813.practice.manager.ladder.settings.handlers;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.ladder.settings.SettingHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.FoodLevelChangeEvent;

/**
 * Handler for the HUNGER setting.
 * Controls whether players lose hunger.
 * <p>
 * IMPLEMENTATION LOCATION: This replaces the logic in LadderSettingListener.onHunger()
 */
public class HungerSettingHandler implements SettingHandler<Boolean> {

    @Override
    public Boolean getValue(Match match) {
        return match.getLadder().isHunger();
    }

    @Override
    public boolean handleEvent(Event event, Match match, Player player) {
        if (!(event instanceof FoodLevelChangeEvent e)) {
            return false;
        }

        // If hunger is disabled, keep food level at 20
        if (!getValue(match)) {
            e.setFoodLevel(20);
            return true;
        }

        // Hunger is enabled, allow natural food level changes
        return false;
    }
}
