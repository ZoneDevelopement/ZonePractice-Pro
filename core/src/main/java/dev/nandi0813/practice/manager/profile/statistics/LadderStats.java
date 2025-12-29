package dev.nandi0813.practice.manager.profile.statistics;

import dev.nandi0813.practice.manager.ladder.LadderManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

@Getter
@Setter
public class LadderStats {

    private final YamlConfiguration config;

    private int unRankedWins = 0;
    private int unRankedLosses = 0;
    private int unRankedWinStreak = 0;
    private int unRankedBestWinStreak = 0;
    private int unRankedLoseStreak = 0;
    private int unRankedBestLoseStreak = 0;

    private int rankedWins = 0;
    private int rankedLosses = 0;
    private int rankedWinStreak = 0;
    private int rankedBestWinStreak = 0;
    private int rankedLoseStreak = 0;
    private int rankedBestLoseStreak = 0;
    private int elo = LadderManager.getDEFAULT_ELO();

    private int kills = 0;
    private int deaths = 0;

    public LadderStats(YamlConfiguration config) {
        this.config = config;
    }

    public void increaseWins(boolean ranked) {
        if (ranked) {
            this.rankedWins++;
        } else {
            this.unRankedWins++;
        }
    }

    public void increaseLosses(boolean ranked) {
        if (ranked) {
            this.rankedLosses++;
        } else {
            this.unRankedLosses++;
        }
    }

    public void increaseWinStreak(boolean ranked) {
        if (ranked) {
            this.rankedWinStreak++;
            this.rankedLoseStreak = 0;

            if (this.rankedWinStreak > this.rankedBestWinStreak) {
                this.rankedBestWinStreak = this.rankedWinStreak;
            }
        } else {
            this.unRankedWinStreak++;
            this.unRankedLoseStreak = 0;

            if (this.unRankedWinStreak > this.unRankedBestWinStreak) {
                this.unRankedBestWinStreak = this.unRankedWinStreak;
            }
        }
    }

    public void increaseLoseStreak(boolean ranked) {
        if (ranked) {
            this.rankedLoseStreak++;
            this.rankedWinStreak = 0;

            if (this.rankedLoseStreak > this.rankedBestLoseStreak) {
                this.rankedBestLoseStreak = this.rankedLoseStreak;
            }
        } else {
            this.unRankedLoseStreak++;
            this.unRankedWinStreak = 0;

            if (this.unRankedLoseStreak > this.unRankedBestLoseStreak) {
                this.unRankedBestLoseStreak = this.unRankedLoseStreak;
            }
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
        this.kills++;
    }

    public void increaseDeaths() {
        this.deaths++;
    }

    public void setData(String ladderName, boolean ranked) {
        config.set("stats.ladder-stats." + ladderName + ".unranked.wins", unRankedWins);
        config.set("stats.ladder-stats." + ladderName + ".unranked.losses", unRankedLosses);
        config.set("stats.ladder-stats." + ladderName + ".unranked.win-streak", unRankedWinStreak);
        config.set("stats.ladder-stats." + ladderName + ".unranked.best-win-streak", unRankedBestWinStreak);
        config.set("stats.ladder-stats." + ladderName + ".unranked.lose-streak", unRankedLoseStreak);
        config.set("stats.ladder-stats." + ladderName + ".unranked.best-lose-streak", unRankedBestLoseStreak);

        if (ranked) {
            config.set("stats.ladder-stats." + ladderName + ".ranked.wins", rankedWins);
            config.set("stats.ladder-stats." + ladderName + ".ranked.losses", rankedLosses);
            config.set("stats.ladder-stats." + ladderName + ".ranked.win-streak", rankedWinStreak);
            config.set("stats.ladder-stats." + ladderName + ".ranked.best-win-streak", rankedBestWinStreak);
            config.set("stats.ladder-stats." + ladderName + ".ranked.lose-streak", rankedLoseStreak);
            config.set("stats.ladder-stats." + ladderName + ".ranked.best-lose-streak", rankedBestLoseStreak);
            config.set("stats.ladder-stats." + ladderName + ".ranked.elo", elo != 0 ? elo : LadderManager.getDEFAULT_ELO());
        }

        config.set("stats.ladder-stats." + ladderName + ".global.kills", kills);
        config.set("stats.ladder-stats." + ladderName + ".global.deaths", deaths);
    }

    public void getData(String ladderName, boolean ranked) {
        this.unRankedWins = config.getInt("stats.ladder-stats." + ladderName + ".unranked.wins");
        this.unRankedLosses = config.getInt("stats.ladder-stats." + ladderName + ".unranked.losses");
        this.unRankedWinStreak = config.getInt("stats.ladder-stats." + ladderName + ".unranked.win-streak");
        this.unRankedBestWinStreak = config.getInt("stats.ladder-stats." + ladderName + ".unranked.best-win-streak");
        this.unRankedLoseStreak = config.getInt("stats.ladder-stats." + ladderName + ".unranked.lose-streak");
        this.unRankedBestLoseStreak = config.getInt("stats.ladder-stats." + ladderName + ".unranked.best-lose-streak");

        if (ranked) {
            this.rankedWins = config.getInt("stats.ladder-stats." + ladderName + ".ranked.wins");
            this.rankedLosses = config.getInt("stats.ladder-stats." + ladderName + ".ranked.losses");
            this.rankedWinStreak = config.getInt("stats.ladder-stats." + ladderName + ".ranked.win-streak");
            this.rankedBestWinStreak = config.getInt("stats.ladder-stats." + ladderName + ".ranked.best-win-streak");
            this.rankedLoseStreak = config.getInt("stats.ladder-stats." + ladderName + ".ranked.lose-streak");
            this.rankedBestLoseStreak = config.getInt("stats.ladder-stats." + ladderName + ".ranked.best-lose-streak");
            this.elo = config.getInt("stats.ladder-stats." + ladderName + ".ranked.elo");

            if (this.elo == 0) {
                this.elo = LadderManager.getDEFAULT_ELO();
            }
        }

        this.kills = config.getInt("stats.ladder-stats." + ladderName + ".global.kills");
        this.deaths = config.getInt("stats.ladder-stats." + ladderName + ".global.deaths");
    }

    public void reset() {
        this.unRankedWins = 0;
        this.unRankedLosses = 0;
        this.unRankedWinStreak = 0;
        this.unRankedBestWinStreak = 0;
        this.unRankedLoseStreak = 0;
        this.unRankedBestLoseStreak = 0;

        this.rankedWins = 0;
        this.rankedLosses = 0;
        this.rankedWinStreak = 0;
        this.rankedBestWinStreak = 0;
        this.rankedLoseStreak = 0;
        this.rankedBestLoseStreak = 0;
        this.elo = LadderManager.getDEFAULT_ELO();

        this.kills = 0;
        this.deaths = 0;
    }

}
