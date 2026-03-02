package dev.nandi0813.practice_1_8_8;

import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.PermanentConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

public class EPCountdownListener implements Listener {

    @EventHandler
    public void onEnderPearlShoot(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (ClassImport.getClasses().getPlayerUtil().isItemInUse(player, Material.ENDER_PEARL)) {
            FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
            if (ffa != null) {
                int duration = ffa.getPlayers().get(player).getEnderPearlCooldown();
                if (duration <= 0) {
                    return;
                }

                ClassImport.getClasses().getItemCooldownHandler().handleEnderPearlFFA(
                        player,
                        ffa.getFightPlayers().get(player),
                        duration,
                        PermanentConfig.FFA_EXP_BAR,
                        e,
                        "FFA.GAME.COOLDOWN.ENDER-PEARL"
                );
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
                    player.updateInventory();
                    return;
                }

                ClassImport.getClasses().getItemCooldownHandler().handleEnderPearlMatch(
                        player,
                        match.getMatchPlayers().get(player),
                        duration,
                        PermanentConfig.MATCH_EXP_BAR,
                        e,
                        "MATCH.COOLDOWN.ENDER-PEARL"
                );
            }
        }
    }

}
