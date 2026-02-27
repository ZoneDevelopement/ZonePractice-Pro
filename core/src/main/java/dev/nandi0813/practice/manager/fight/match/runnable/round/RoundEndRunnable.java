package dev.nandi0813.practice.manager.fight.match.runnable.round;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.Round;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class RoundEndRunnable extends BukkitRunnable {

    private final Match match;
    private final Round round;

    @Getter
    private int seconds;
    @Getter
    private boolean running = false;

    @Getter
    private final boolean ended;

    /**
     * When {@code true} the runnable will NOT call {@link Match#startNextRound()} when
     * it reaches zero — used for non-ending rounds where the rollback callback is
     * responsible for starting the next round.
     */
    private final boolean suppressNextRound;

    public RoundEndRunnable(Round round, boolean ended) {
        this(round, ended, false);
    }

    public RoundEndRunnable(Round round, boolean ended, boolean suppressNextRound) {
        this.round = round;
        this.match = round.getMatch();
        this.ended = ended;
        this.suppressNextRound = suppressNextRound;

        if (ended)
            this.seconds = ConfigManager.getConfig().getInt("MATCH-SETTINGS.AFTER-COUNTDOWN");
        else
            this.seconds = 0;
    }

    public RoundEndRunnable begin() {
        this.round.setRoundStatus(RoundStatus.END);
        if (this.ended) match.setStatus(MatchStatus.END);

        running = true;
        this.runTaskTimer(ZonePractice.getInstance(), 0, 20L);

        return this;
    }

    @Override
    public void cancel() {
        if (running) {
            Bukkit.getScheduler().cancelTask(this.getTaskId());
            running = false;
        }

        this.round.setRoundEndRunnable(null);
    }

    @Override
    public void run() {
        if (seconds == 0) {
            this.cancel();

            if (ended)
                match.endMatch();
            else if (!suppressNextRound)
                match.startNextRound();
        } else
            seconds--;
    }

}
