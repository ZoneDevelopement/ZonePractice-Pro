package dev.nandi0813.practice_modern.listener;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.Brackets;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.listener.LadderTypeListener;
import dev.nandi0813.practice.manager.fight.match.util.KnockbackUtil;
import dev.nandi0813.practice.manager.fight.match.util.MatchFightPlayer;
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
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Bukkit;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class MatchListener extends LadderTypeListener implements Listener {

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

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

        if (match.getLadder() instanceof LadderHandle ladderHandle) {
            ladderHandle.handleEvents(e, match);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        e.setCancelled(true);

        DamageSource damageSource = e.getDamageSource();
        Player killer;
        if (damageSource.getCausingEntity() instanceof Entity damageEntity) {
            killer = FightUtil.getKiller(damageEntity);
        } else {
            killer = null;
        }

        DeathCause cause = dev.nandi0813.practice_modern.listener.FightUtil.convert(damageSource.getDamageType());
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                match.killPlayer(player, killer, cause.getMessage().replace("%killer%", killer != null ? killer.getName() : "Unknown")), 1L);

        if (killer != null) {
            Statistic statistic = match.getCurrentStat(killer);
            statistic.setKills(statistic.getKills() + 1);
        }
    }

    private static void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player target)) return;

        Player attacker = null;
        if (e.getDamager() instanceof Player) {
            attacker = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile projectile) {
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

        if (!attackerProfile.getStatus().equals(ProfileStatus.MATCH)) return;
        if (!targetProfile.getStatus().equals(ProfileStatus.MATCH)) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(attacker);
        if (match != MatchManager.getInstance().getLiveMatchByPlayer(target)) {
            e.setCancelled(true);
            return;
        }

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        boolean cancel = match.getCurrentStat(attacker).isSet() || match.getCurrentStat(target).isSet();

        if (!cancel) {
            cancel = TeamUtil.isSaveTeamMate(match, attacker, target);
        }

        if (cancel) {
            e.setCancelled(true);
            return;
        } else {
            if (match.getLadder() instanceof LadderHandle ladderHandle) {
                ladderHandle.handleEvents(e, match);
            }
        }

        // Always record the attacker for void-kill attribution,
        // regardless of whether the event was cancelled by a ladder handler.
        match.recordAttack(target, attacker);

        if (!e.isCancelled() && !match.getLadder().getLadderKnockback().getKnockbackType().equals(KnockbackType.DEFAULT)) {
            KnockbackUtil.setPlayerKnockback(target, match.getLadder().getLadderKnockback().getKnockbackType());
        }
    }

    @EventHandler
    public void onExpPickup(PlayerPickupExperienceEvent e) {
        Player player = e.getPlayer();

        if (SpectatorManager.getInstance().getSpectators().containsKey(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player player)) {
            return;
        }

        if (SpectatorManager.getInstance().getSpectators().containsKey(player)) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevents players from swapping items in their hands before they have chosen a kit.
     */
    @EventHandler
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus() == ProfileStatus.MATCH) {
            Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
            if (match != null) {
                MatchFightPlayer matchFightPlayer = match.getMatchPlayers().get(player);
                if (!matchFightPlayer.isHasChosenKit()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * Prevents players from swapping items in their hands before they have chosen a kit.
     * Or when they are in the kit editor.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (e.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
            switch (profile.getStatus()) {
                case MATCH -> {
                    Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                    if (match != null) {
                        MatchFightPlayer matchFightPlayer = match.getMatchPlayers().get(player);
                        if (!matchFightPlayer.isHasChosenKit()) {
                            e.setCancelled(true);
                        }
                    }
                }
                case EDITOR -> e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onWindCharge(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof WindCharge)) {
            return;
        }

        if (!(e.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        switch (profile.getStatus()) {
            case MATCH -> {
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                if (match != null && !match.getLadder().isBuild()) {
                    Common.sendMMMessage(player, LanguageManager.getString("MATCH.ONLY-CHARGE-WIND"));
                    e.setCancelled(true);
                }
            }
            case EVENT -> {
                Event event = EventManager.getInstance().getEventByPlayer(player);
                if (event instanceof Brackets) {
                    Common.sendMMMessage(player, LanguageManager.getString("MATCH.ONLY-CHARGE-WIND"));
                    e.setCancelled(true);
                }
            }
        }
    }

}
