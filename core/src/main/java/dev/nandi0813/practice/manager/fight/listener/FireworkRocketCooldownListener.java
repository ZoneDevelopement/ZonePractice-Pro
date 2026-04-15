package dev.nandi0813.practice.manager.fight.listener;

import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.ModernItemCooldownHandler;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class FireworkRocketCooldownListener implements Listener {

    @EventHandler
    public void onFireworkSpawn(EntitySpawnEvent e) {

        if (!(e.getEntity() instanceof Firework firework)) return;
        if (!(firework.getShooter() instanceof Player player)) return;

        // FFA
        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa != null) {

            double duration = ffa.getPlayers().get(player).getFireworkRocketCooldown();
            if (duration <= 0) return;

            ModernItemCooldownHandler.handleFireworkRocket(player, duration, null);

            return;
        }

        // Match
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match != null) {

            double duration = match.getLadder().getFireworkRocketCooldown();
            if (duration <= 0) return;

            if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
                return;
            }

            ModernItemCooldownHandler.handleFireworkRocket(player, duration, null);
        }
    }
}