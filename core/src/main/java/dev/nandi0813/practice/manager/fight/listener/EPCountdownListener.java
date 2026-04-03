package dev.nandi0813.practice.manager.fight.listener;

import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.ModernItemCooldownHandler;
import io.papermc.paper.event.player.PlayerItemCooldownEvent;
import org.bukkit.Material;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class EPCountdownListener implements Listener {

    /**
     * Intercepts the vanilla ender pearl cooldown that Paper sets automatically on throw.
     * Instead of cancelling it and re-applying via setCooldown (which creates a race),
     * we simply override the tick count in-place using {@code e.setCooldownTicks()}.
     *
     * <ul>
     *   <li>If the ladder/FFA has a configured cooldown {@code > 0}: replace vanilla ticks with {@code duration * 20}.</li>
     *   <li>If the ladder/FFA has no configured cooldown (duration {@code <= 0}): cancel the vanilla cooldown entirely.</li>
     *   <li>If the player is not in a match or FFA: leave vanilla cooldown untouched.</li>
     * </ul>
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnderPearlCooldownSet(PlayerItemCooldownEvent e) {
        if (e.getType() != Material.ENDER_PEARL) {
            return;
        }

        Player player = e.getPlayer();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match != null) {
            int duration = match.getLadder().getEnderPearlCooldown();
            if (duration <= 0) {
                e.setCancelled(true);
            } else {
                e.setCooldown(duration * 20);
            }
            return;
        }

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa != null) {
            int duration = ffa.getPlayers().get(player).getEnderPearlCooldown();
            if (duration <= 0) {
                e.setCancelled(true);
            } else {
                e.setCooldown(duration * 20);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWindChargeCooldownSet(PlayerItemCooldownEvent e) {
        if (e.getType() != Material.WIND_CHARGE) {
            return;
        }

        Player player = e.getPlayer();
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) {
            return;
        }

        int duration = match.getLadder().getWindChargeCooldown();
        if (duration <= 0) {
            e.setCancelled(true);
        } else {
            e.setCooldown(duration * 20);
        }
    }

    @EventHandler
    public void onProjectileShoot(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof EnderPearl)) {
            return;
        }

        if (!(e.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa != null) {
            int duration = ffa.getPlayers().get(player).getEnderPearlCooldown();
            if (duration <= 0) {
                return;
            }

            ModernItemCooldownHandler.handleEnderPearl(player, duration, e);
            return;
        }

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match != null) {
            int duration = match.getLadder().getEnderPearlCooldown();
            if (duration <= 0) {
                return;
            }

            if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
                e.setCancelled(true);
                return;
            }

            ModernItemCooldownHandler.handleEnderPearl(player, duration, e);
        }
    }

    @EventHandler
    public void onWindChargeShoot(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof WindCharge)) {
            return;
        }

        if (!(e.getEntity().getShooter() instanceof Player windPlayer)) {
            return;
        }

        Match windMatch = MatchManager.getInstance().getLiveMatchByPlayer(windPlayer);
        if (windMatch == null) {
            return;
        }

        int duration = windMatch.getLadder().getWindChargeCooldown();
        if (duration <= 0) {
            return;
        }

        if (!windMatch.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.setCancelled(true);
        }
    }

}
