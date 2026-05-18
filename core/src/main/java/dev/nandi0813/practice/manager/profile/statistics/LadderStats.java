package dev.nandi0813.practice.manager.profile.statistics;

import dev.nandi0813.practice.manager.ladder.LadderManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter
@Setter
public class LadderStats {

    private final YamlConfiguration config;

    private int unRankedWins;
    private int unRankedLosses;
    private int unRankedWinStreak;
    private int unRankedBestWinStreak;
    private int unRankedLoseStreak;
    private int unRankedBestLoseStreak;

    private int rankedWins;
    private int rankedLosses;
    private int rankedWinStreak;
    private int rankedBestWinStreak;
    private int rankedLoseStreak;
    private int rankedBestLoseStreak;
    private int elo = LadderManager.getDEFAULT_ELO();

    private int kills;
    private int deaths;

    public LadderStats(YamlConfiguration config) {
        this.config = config;
    }

    public void increaseWins(boolean ranked) {
        if (ranked)
            rankedWins++;
        else
            unRankedWins++;
    }

    public void increaseLosses(boolean ranked) {
        if (ranked)
            rankedLosses++;
        else
            unRankedLosses++;
    }

    public void increaseWinStreak(boolean ranked) {
        if (ranked) {
            rankedWinStreak++;
            rankedLoseStreak = 0;

            if (rankedWinStreak > rankedBestWinStreak)
                rankedBestWinStreak = rankedWinStreak;
        } else {
            unRankedWinStreak++;
            unRankedLoseStreak = 0;

            if (unRankedWinStreak > unRankedBestWinStreak)
                unRankedBestWinStreak = unRankedWinStreak;
        }
    }

    public void increaseLoseStreak(boolean ranked) {
        if (ranked) {
            rankedLoseStreak++;
            rankedWinStreak = 0;

            if (rankedLoseStreak > rankedBestLoseStreak)
                rankedBestLoseStreak = rankedLoseStreak;
        } else {
            unRankedLoseStreak++;
            unRankedWinStreak = 0;

            if (unRankedLoseStreak > unRankedBestLoseStreak)
                unRankedBestLoseStreak = unRankedLoseStreak;
        }
    }

    public void increaseElo(int elo) {
        this.elo += elo;
    }

    public void decreaseElo(int elo) {
        if (this.elo < 100)
            return;

        this.elo -= elo;
    }

    public void increaseKills() {
        kills++;
    }

    public void increaseDeaths() {
        deaths++;
    }

    public void setData(String ladderName, boolean ranked) {
        String base = "stats.ladder-stats." + ladderName;
        config.set(base + ".unranked.wins", unRankedWins);
        config.set(base + ".unranked.losses", unRankedLosses);
        config.set(base + ".unranked.win-streak", unRankedWinStreak);
        config.set(base + ".unranked.best-win-streak", unRankedBestWinStreak);
        config.set(base + ".unranked.lose-streak", unRankedLoseStreak);
        config.set(base + ".unranked.best-lose-streak", unRankedBestLoseStreak);

        if (ranked) {
            config.set(base + ".ranked.wins", rankedWins);
            config.set(base + ".ranked.losses", rankedLosses);
            config.set(base + ".ranked.win-streak", rankedWinStreak);
            config.set(base + ".ranked.best-win-streak", rankedBestWinStreak);
            config.set(base + ".ranked.lose-streak", rankedLoseStreak);
            config.set(base + ".ranked.best-lose-streak", rankedBestLoseStreak);
            config.set(base + ".ranked.elo", elo != 0 ? elo : LadderManager.getDEFAULT_ELO());
        }

        config.set(base + ".global.kills", kills);
        config.set(base + ".global.deaths", deaths);
    }

    public void getData(String ladderName, boolean ranked) {
        String base = "stats.ladder-stats." + ladderName;
        unRankedWins = config.getInt(base + ".unranked.wins");
        unRankedLosses = config.getInt(base + ".unranked.losses");
        unRankedWinStreak = config.getInt(base + ".unranked.win-streak");
        unRankedBestWinStreak = config.getInt(base + ".unranked.best-win-streak");
        unRankedLoseStreak = config.getInt(base + ".unranked.lose-streak");
        unRankedBestLoseStreak = config.getInt(base + ".unranked.best-lose-streak");

        if (ranked) {
            rankedWins = config.getInt(base + ".ranked.wins");
            rankedLosses = config.getInt(base + ".ranked.losses");
            rankedWinStreak = config.getInt(base + ".ranked.win-streak");
            rankedBestWinStreak = config.getInt(base + ".ranked.best-win-streak");
            rankedLoseStreak = config.getInt(base + ".ranked.lose-streak");
            rankedBestLoseStreak = config.getInt(base + ".ranked.best-lose-streak");
            elo = config.getInt(base + ".ranked.elo");

            if (elo == 0)
                elo = LadderManager.getDEFAULT_ELO();
        }

        kills = config.getInt(base + ".global.kills");
        deaths = config.getInt(base + ".global.deaths");
    }

    public void reset() {
        unRankedWins = 0;
        unRankedLosses = 0;
        unRankedWinStreak = 0;
        unRankedBestWinStreak = 0;
        unRankedLoseStreak = 0;
        unRankedBestLoseStreak = 0;

        rankedWins = 0;
        rankedLosses = 0;
        rankedWinStreak = 0;
        rankedBestWinStreak = 0;
        rankedLoseStreak = 0;
        rankedBestLoseStreak = 0;
        elo = LadderManager.getDEFAULT_ELO();

        kills = 0;
        deaths = 0;
    }

}
