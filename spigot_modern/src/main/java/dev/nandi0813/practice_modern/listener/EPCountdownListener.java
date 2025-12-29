package dev.nandi0813.practice_modern.listener;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.Runnable.EnderPearlRunnable;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.PermanentConfig;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class EPCountdownListener implements Listener {

    @EventHandler
    public void onProjectileShoot(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof EnderPearl) {
            if (e.getEntity().getShooter() instanceof Player player) {
                FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
                if (ffa != null) {
                    int duration = ffa.getPlayers().get(player).getEnderPearlCooldown();
                    if (duration <= 0) {
                        return;
                    }

                    if (PlayerCooldown.isActive(player, CooldownObject.ENDER_PEARL)) {
                        Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("FFA.GAME.COOLDOWN.ENDER-PEARL"), PlayerCooldown.getLeftInDouble(player, CooldownObject.ENDER_PEARL)));

                        e.setCancelled(true);
                    } else {
                        EnderPearlRunnable enderPearlCountdown = new EnderPearlRunnable(player, ffa.getFightPlayers().get(player), duration, PermanentConfig.FFA_EXP_BAR);
                        enderPearlCountdown.begin();
                    }

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

                    if (PlayerCooldown.isActive(player, CooldownObject.ENDER_PEARL)) {
                        Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("MATCH.COOLDOWN.ENDER-PEARL"), PlayerCooldown.getLeftInDouble(player, CooldownObject.ENDER_PEARL)));

                        e.setCancelled(true);
                    } else {
                        EnderPearlRunnable enderPearlCountdown = new EnderPearlRunnable(player, match.getMatchPlayers().get(player), duration, PermanentConfig.MATCH_EXP_BAR);
                        enderPearlCountdown.begin();
                    }
                }
            }
        }
    }

}
