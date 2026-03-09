package dev.nandi0813.practice_modern.listener;

import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

/**
 * Handles firework rocket cooldown for elytra boost in modern Minecraft versions.
 * This prevents spam-boosting with firework rockets when flying with elytra.
 * Delegates to {@link dev.nandi0813.practice.module.interfaces.ItemCooldownHandler}
 * so the modern implementation can apply a native hotbar visual cooldown.
 */
public class FireworkRocketCooldownListener implements Listener {

    @EventHandler
    public void onFireworkSpawn(EntitySpawnEvent e) {

        if (!(e.getEntity() instanceof Firework firework)) return;
        if (!(firework.getShooter() instanceof Player player)) return;

        // FFA
        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa != null) {

            int duration = ffa.getPlayers().get(player).getFireworkRocketCooldown();
            if (duration <= 0) return;

            ClassImport.getClasses().getItemCooldownHandler().handleFireworkRocketFFA(
                    player,
                    ffa.getFightPlayers().get(player),
                    duration,
                    null,
                    "MATCH.COOLDOWN.FIREWORK-ROCKET-COOLDOWN"
            );
            return;
        }

        // Match
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match != null) {

            int duration = match.getLadder().getFireworkRocketCooldown();
            if (duration <= 0) return;

            if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
                return;
            }

            ClassImport.getClasses().getItemCooldownHandler().handleFireworkRocketMatch(
                    player,
                    match.getMatchPlayers().get(player),
                    duration,
                    null,
                    "MATCH.COOLDOWN.FIREWORK-ROCKET-COOLDOWN"
            );
        }
    }
}