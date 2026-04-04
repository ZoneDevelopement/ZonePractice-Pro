package dev.nandi0813.practice.manager.ladder.settings.handlers;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.util.ModernItemCooldownHandler;
import dev.nandi0813.practice.manager.ladder.settings.SettingHandler;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class WindChargeSettingHandler implements SettingHandler<Integer> {

    @Override
    public Integer getValue(Match match) {
        return match.getLadder().getWindChargeCooldown();
    }

    @Override
    public boolean handleEvent(Event event, Match match, Player player) {
        if (!(event instanceof ProjectileLaunchEvent e)) {
            return false;
        }

        Projectile projectile = e.getEntity();
        if (!(projectile instanceof WindCharge)) {
            return false;
        }

        int cooldown = getValue(match);
        if (cooldown < 1) {
            return false;
        }

        ModernItemCooldownHandler.handleWindCharge(player, cooldown, e);
        return e.isCancelled();
    }
}


