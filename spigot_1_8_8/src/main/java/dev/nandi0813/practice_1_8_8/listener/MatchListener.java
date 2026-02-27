package dev.nandi0813.practice_1_8_8.listener;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.listener.LadderTypeListener;
import dev.nandi0813.practice.manager.fight.match.util.KnockbackUtil;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.fight.util.FightUtil;
import dev.nandi0813.practice.manager.fight.util.ListenerUtil;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.enums.KnockbackType;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class MatchListener extends LadderTypeListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player player = (Player) e.getEntity();

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        if (ListenerUtil.cancelEvent(match, player)) {
            e.setDamage(0);
            e.setCancelled(true);
            return;
        }

        if (e instanceof EntityDamageByEntityEvent) {
            onEntityDamageByEntity((EntityDamageByEntityEvent) e);
        }

        if (match.getLadder() instanceof LadderHandle) {
            ((LadderHandle) match.getLadder()).handleEvents(e, match);
        }

        if (e.isCancelled()) {
            return;
        }

        if (player.getHealth() - e.getFinalDamage() <= 0) {
            e.setDamage(0);

            DeathCause cause = DeathCause.convert(e.getCause());

            if (e instanceof EntityDamageByEntityEvent) {
                Player killer = FightUtil.getKiller(((EntityDamageByEntityEvent) e).getDamager());

                match.killPlayer(player, killer, cause.getMessage().replace("%killer%", killer != null ? killer.getName() : "Unknown"));

                if (killer != null) {
                    Statistic statistic = match.getCurrentStat(killer);
                    statistic.setKills(statistic.getKills() + 1);
                }
            } else {
                match.killPlayer(player, null, cause.getMessage());
            }
        }
    }

    private static void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player target = (Player) e.getEntity();

        Player attacker = null;
        if (e.getDamager() instanceof Player) {
            attacker = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) e.getDamager();

            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();

                if (projectile instanceof Arrow) {
                    arrowDisplayHearth(attacker, target, e.getFinalDamage());
                }
            }
        }

        if (attacker == null) return;

        Profile attackerProfile = ProfileManager.getInstance().getProfile(attacker);
        Profile targetProfile = ProfileManager.getInstance().getProfile(target);

        if (attackerProfile == null) return;
        if (targetProfile == null) return;
        if (!attackerProfile.getStatus().equals(ProfileStatus.MATCH)) return;
        if (!targetProfile.getStatus().equals(ProfileStatus.MATCH)) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(attacker);
        if (!match.equals(MatchManager.getInstance().getLiveMatchByPlayer(target))) return;

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        boolean cancel = match.getCurrentStat(attacker).isSet() || match.getCurrentStat(target).isSet();

        if (!cancel) {
            cancel = TeamUtil.isSaveTeamMate(match, attacker, target);
        }

        if (cancel) {
            e.setCancelled(true);
            return;
        } else {
            if (match.getLadder() instanceof LadderHandle) {
                ((LadderHandle) match.getLadder()).handleEvents(e, match);
            }
        }

        // Always record the attacker for void-kill attribution,
        // regardless of whether the event was cancelled by a ladder handler.
        match.recordAttack(target, attacker);

        if (!e.isCancelled() && !match.getLadder().getLadderKnockback().getKnockbackType().equals(KnockbackType.DEFAULT)) {
            KnockbackUtil.setPlayerKnockback(target, match.getLadder().getLadderKnockback().getKnockbackType());
        }
    }

}