package dev.nandi0813.practice.manager.fight.match;

import dev.nandi0813.api.Event.Match.MatchRoundEndEvent;
import dev.nandi0813.api.Event.Match.MatchRoundStartEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.enums.TeamEnum;
import dev.nandi0813.practice.manager.fight.match.interfaces.Team;
import dev.nandi0813.practice.manager.fight.match.runnable.round.RoundEndRunnable;
import dev.nandi0813.practice.manager.fight.match.runnable.round.RoundStartRunnable;
import dev.nandi0813.practice.manager.fight.match.util.MatchFightPlayer;
import dev.nandi0813.practice.manager.fight.match.util.MatchUtil;
import dev.nandi0813.practice.manager.fight.match.util.TempKillPlayer;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.entityhider.PlayerHider;
import dev.nandi0813.practice.util.playerutil.PlayerUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Getter
public abstract class Round extends BukkitRunnable {

    protected final Match match;
    protected final int roundNumber;
    @Setter
    protected RoundStatus roundStatus = RoundStatus.START; // Ha azt írja hogy nincs használva bugos

    protected final Map<UUID, Statistic> statistics = new HashMap<>();

    // Ladder specific variables
    protected final Map<TeamEnum, Boolean> bedStatus = new EnumMap<>(TeamEnum.class);
    protected final List<TempKillPlayer> tempDead = new ArrayList<>();

    protected int durationTime = 0;
    protected RoundStartRunnable roundStartRunnable;
    @Setter
    protected RoundEndRunnable roundEndRunnable;

    protected Round(Match match, int roundNumber) {
        this.match = match;
        this.roundNumber = roundNumber;

        if (MatchUtil.isLadderBedRelated(match.getLadder())) {
            for (TeamEnum team : TeamEnum.values())
                this.bedStatus.put(team, true);

            // Set the beds
            ClassImport.getClasses().getBedUtil().placeBed(match.getArena().getBedLoc1().getLocation(), match.getArena().getBedLoc1().getFacing());
            ClassImport.getClasses().getBedUtil().placeBed(match.getArena().getBedLoc2().getLocation(), match.getArena().getBedLoc2().getFacing());
        }

        for (Player player : match.getPlayers()) {
            UUID playerUuid = ProfileManager.getInstance().getUuids().get(player);

            Statistic statistic = new Statistic(playerUuid);
            this.statistics.put(playerUuid, statistic);
        }
    }

    public void startRound() {
        this.roundStartRunnable = new RoundStartRunnable(this).begin();

        for (Player player : match.getPlayers()) {
            match.teleportPlayer(player);

            PlayerUtil.setFightPlayer(player);

            MatchFightPlayer matchFightPlayer = match.getMatchPlayers().get(player);
            matchFightPlayer.setKitChooserOrKit(match instanceof Team ? ((Team) match).getTeam(player) : TeamEnum.TEAM1);

            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                    player.addPotionEffects(match.getLadder().getKitData().getEffects()), 2L);
        }

        this.showPlayersToEachOther();

        Bukkit.getPluginManager().callEvent(new MatchRoundStartEvent(this.match, this.roundNumber));
    }

    public void endRound() {
        Bukkit.getPluginManager().callEvent(new MatchRoundEndEvent(this.match, this.roundNumber));

        this.closeAllStatistics();

        for (TempKillPlayer tempKillPlayer : new ArrayList<>(this.tempDead))
            tempKillPlayer.cancel(true);

        boolean isEndMatch = match.isEndMatch();

        // Determine whether the arena needs to be rolled back before the next round.
        boolean needsReset = !isEndMatch && (
                !this.match.getLadder().getType().equals(LadderType.BRIDGES) ||
                this.match.getLadder().isResetBuildAfterRound()
        );

        if (this.roundStartRunnable != null)
            this.roundStartRunnable.cancel();

        if (isEndMatch) {
            // Match is over — start the end countdown normally.
            if (this.roundEndRunnable == null) {
                this.roundEndRunnable = new RoundEndRunnable(this, true).begin();
            } else {
                if (!this.roundEndRunnable.isEnded()) {
                    this.roundEndRunnable.cancel();
                    this.roundEndRunnable = new RoundEndRunnable(this, true).begin();
                }
            }
        } else if (needsReset) {
            // Non-ending round that requires an arena rollback.
            // resetMap fires startNextRound() only after every block is restored.
            // suppressNextRound=true so the runnable doesn't also call startNextRound().
            this.roundEndRunnable = new RoundEndRunnable(this, false, true).begin();
            this.match.resetMap(this.match::startNextRound);
        } else {
            // Non-ending round, no reset needed — start the next round immediately.
            this.roundEndRunnable = new RoundEndRunnable(this, false).begin();
        }

        if (this.isRunning())
            Bukkit.getScheduler().cancelTask(this.getTaskId());

        // Send the end message to the players
        this.sendEndMessage(isEndMatch);
    }

    public abstract void sendEndMessage(boolean endMatch);

    public abstract Match getMatch();

    public void closeAllStatistics() {
        for (Statistic statistic : statistics.values())
            statistic.end(false);
    }

    public TempKillPlayer getTempKill(Player player) {
        for (TempKillPlayer tempKillPlayer : this.tempDead)
            if (tempKillPlayer.getPlayer().equals(player))
                return tempKillPlayer;
        return null;
    }

    /*
     * Runnable stuff
     */
    @Getter
    @Setter
    private boolean running = false;

    public void beginRunnable() {
        this.runTaskTimer(ZonePractice.getInstance(), 0, 20L);
        this.running = true;
    }

    @Override
    public void run() {
        durationTime++;
    }

    public String getFormattedTime() {
        return StringUtil.formatMillisecondsToMinutes(durationTime * 1000L);
    }

    private void showPlayersToEachOther() {
        for (Player player1 : this.match.players) {
            for (Player player2 : this.match.players) {
                if (player1 != player2) {
                    PlayerHider.getInstance().showPlayer(player1, player2);
                }
            }
        }
    }

}
