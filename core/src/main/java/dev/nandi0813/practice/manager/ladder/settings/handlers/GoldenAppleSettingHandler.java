package dev.nandi0813.practice.manager.ladder.settings.handlers;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.util.ModernItemCooldownHandler;
import dev.nandi0813.practice.manager.ladder.settings.SettingHandler;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handler for the GOLDEN_APPLE_COOLDOWN setting.
 * Controls the cooldown time for consuming golden apples.
 * <p>
 * IMPLEMENTATION LOCATION: This replaces the logic in LadderSettingListener.onGoldenHeadConsume()
 */
public class GoldenAppleSettingHandler implements SettingHandler<Integer> {

    @Override
    public Integer getValue(Match match) {
        return match.getLadder().getGoldenAppleCooldown();
    }

    @Override
    public boolean handleEvent(Event event, Match match, Player player) {
        if (!(event instanceof PlayerItemConsumeEvent e)) {
            return false;
        }

        ItemStack item = e.getItem();
        if (item == null || !item.getType().equals(Material.GOLDEN_APPLE)) {
            return false;
        }

        int cooldown = getValue(match);
        if (cooldown < 1) {
            return false; // No cooldown
        }

        // Golden heads have their own consume logic/cooldown.
        if (dev.nandi0813.practice.manager.server.ServerManager.getInstance()
                .getGoldenHead().isGoldenHead(item)) {
            return false; // Golden heads don't have cooldown
        }

        ModernItemCooldownHandler.handleGoldenApple(player, cooldown, e);

        return e.isCancelled();
    }

}
