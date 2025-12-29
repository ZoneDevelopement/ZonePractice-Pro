package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.ffa.FFAManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class StatisticListener extends dev.nandi0813.practice.module.interfaces.StatisticListener implements Listener {

    @Override
    public void onClick(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (ClassImport.getClasses().getPlayerUtil().getPlayerMainHand(player) != null && player.getItemInHand().getType() == Material.FISHING_ROD)
            return;

        Statistic statistic = null;
        switch (profile.getStatus()) {
            case MATCH:
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                if (match != null && match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
                    statistic = match.getCurrentStat(player);
                }
                break;
            case FFA:
                FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
                if (ffa != null) {
                    statistic = ffa.getStatistics().get(player);
                }
                break;
        }

        if (!e.getAction().equals(Action.LEFT_CLICK_AIR) && !e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            return;
        }

        if (statistic == null || statistic.isSet()) {
            return;
        }

        CURRENT_CPS.putIfAbsent(player, 1);
        CURRENT_CPS.computeIfPresent(player, (key, val) -> val + 1);

        BukkitRunnable task = cpsRunnable(statistic, player);
        task.runTaskLaterAsynchronously(ZonePractice.getInstance(), 20L);
    }

    @EventHandler ( priority = EventPriority.LOWEST )
    public void onPlayerHit(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;

        if (!(e.getDamager() instanceof Player)) return;
        Player attacker = (Player) e.getDamager();
        Profile attackerProfile = ProfileManager.getInstance().getProfile(attacker);

        if (!(e.getEntity() instanceof Player)) return;
        Player defender = (Player) e.getEntity();
        Profile defenderProfile = ProfileManager.getInstance().getProfile(defender);

        Bukkit.getScheduler().runTaskAsynchronously(practice, () ->
        {
            Statistic attackerStats = null;
            Statistic defenderStats = null;
            switch (attackerProfile.getStatus()) {
                case MATCH:
                    if (!defenderProfile.getStatus().equals(ProfileStatus.MATCH))
                        return;

                    Match match = MatchManager.getInstance().getLiveMatchByPlayer(attacker);
                    if (match == null)
                        return;
                    if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE))
                        return;

                    attackerStats = match.getCurrentStat(attacker);
                    defenderStats = match.getCurrentStat(defender);
                    break;
                case FFA:
                    FFA ffa = FFAManager.getInstance().getFFAByPlayer(attacker);
                    if (ffa == null)
                        return;

                    attackerStats = ffa.getStatistics().get(attacker);
                    defenderStats = ffa.getStatistics().get(defender);
                    break;
            }

            if (attackerStats == null) return;
            if (attackerStats.isSet()) return;

            BukkitRunnable task = hitRunnable(attacker, attackerStats, defender, defenderStats);
            task.runTaskAsynchronously(ZonePractice.getInstance());
        });
    }

    @EventHandler ( priority = EventPriority.LOWEST )
    public void onPotionSplash(PotionSplashEvent e) {
        ThrownPotion potion = e.getPotion();
        if (!(potion.getShooter() instanceof Player)) return;
        Player player = (Player) potion.getShooter();

        Profile profile = ProfileManager.getInstance().getProfile(player);

        Statistic statistic = null;
        switch (profile.getStatus()) {
            case MATCH:
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                if (match == null)
                    return;

                statistic = match.getCurrentStat(player);
                break;
            case FFA:
                FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
                if (ffa == null)
                    return;

                statistic = ffa.getStatistics().get(player);
                break;
        }

        if (statistic == null || statistic.isSet()) {
            return;
        }

        // Check if the potion is a health potion
        if (potion.getItem().getDurability() == 16421) {
            statistic.setPotionThrown(statistic.getPotionThrown() + 1);

            if (!e.getAffectedEntities().contains(player)) {
                statistic.setPotionMissed(statistic.getPotionMissed() + 1);
            }
        }
    }

}