package dev.nandi0813.practice.manager.profile.statistics;

import dev.nandi0813.practice.manager.division.Division;
import dev.nandi0813.practice.manager.division.DivisionManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileFile;
import dev.nandi0813.practice.util.NumberUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ProfileStat {

    private final Profile profile;
    private final ProfileFile profileFile;
    private final YamlConfiguration config;

    private final Map<NormalLadder, LadderStats> ladderStats = new HashMap<>();

    @Getter
    @Setter
    private int winStreak = 0;
    @Getter
    @Setter
    private int bestWinStreak = 0;

    @Getter
    @Setter
    private int loseStreak = 0;
    @Getter
    @Setter
    private int bestLoseStreak = 0;

    @Getter
    @Setter
    private Division division;
    @Getter
    @Setter
    private int experience = 0;

    public ProfileStat(Profile profile) {
        this.profile = profile;
        this.profileFile = profile.getFile();
        this.config = profileFile.getConfig();
    }

    public LadderStats getLadderStat(NormalLadder ladder) {
        this.createLadderStat(ladder);
        return ladderStats.get(ladder);
    }

    public void createLadderStat(NormalLadder ladder) {
        if (!ladderStats.containsKey(ladder)) {
            ladderStats.put(ladder, new LadderStats(config));
        }
    }

    public void increaseWinStreak(NormalLadder ladder, boolean ranked) {
        LadderStats ladderStat = getLadderStat(ladder);
        ladderStat.increaseWinStreak(ranked);

        this.winStreak++;
        this.loseStreak = 0;

        if (this.winStreak > this.bestWinStreak) {
            this.bestWinStreak = this.winStreak;
        }
    }

    public void increaseLoseStreak(NormalLadder ladder, boolean ranked) {
        LadderStats ladderStat = getLadderStat(ladder);
        ladderStat.increaseLoseStreak(ranked);

        this.loseStreak++;
        this.winStreak = 0;

        if (this.loseStreak > this.bestLoseStreak) {
            this.bestLoseStreak = this.loseStreak;
        }
    }

    public void setData(boolean save) {
        if (experience != 0) config.set("experience", experience);
        else config.set("experience", null);

        if (winStreak != 0) config.set("winStreak", winStreak);
        else config.set("winStreak", null);

        if (bestWinStreak != 0) config.set("bestWinStreak", bestWinStreak);
        else config.set("bestWinStreak", null);

        if (loseStreak != 0) config.set("loseStreak", loseStreak);
        else config.set("loseStreak", null);

        if (bestLoseStreak != 0) config.set("bestLoseStreak", bestLoseStreak);
        else config.set("bestLoseStreak", null);

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            getLadderStat(ladder).setData(ladder.getName().toLowerCase(), ladder.isRanked());
        }

        if (save)
            profileFile.saveFile();
    }

    public void getData() {
        if (config.isInt("experience"))
            this.setExperience(config.getInt("experience"));

        if (config.isInt("winStreak"))
            this.setWinStreak(config.getInt("winStreak"));

        if (config.isInt("bestWinStreak"))
            this.setBestWinStreak(config.getInt("bestWinStreak"));

        if (config.isInt("loseStreak"))
            this.setLoseStreak(config.getInt("loseStreak"));

        if (config.isInt("bestLoseStreak"))
            this.setBestLoseStreak(config.getInt("bestLoseStreak"));

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            LadderStats ladderStat = new LadderStats(config);
            ladderStat.getData(ladder.getName().toLowerCase(), ladder.isRanked());
            ladderStats.put(ladder, ladderStat);
        }
    }

    public void loadDefaultStats(NormalLadder ladder) {
        LadderStats ladderStat = getLadderStat(ladder);
        ladderStat.reset();

        if (ladder.isRanked()) {
            this.setDivision(DivisionManager.getInstance().getDivision(profile));
        }
    }

    public double getLadderRatio(NormalLadder ladder, boolean ranked) {
        LadderStats ladderStat = getLadderStat(ladder);

        if ((ranked && !ladder.isRanked()) || (!ranked && !ladder.isUnranked())) return 0;

        int w = ranked ? ladderStat.getRankedWins() : ladderStat.getUnRankedWins();
        int l = ranked ? ladderStat.getRankedLosses() : ladderStat.getUnRankedLosses();

        if (l == 0) return w;
        return NumberUtil.roundDouble((double) w / l);
    }

    public double getOverallRatio(NormalLadder ladder) {
        LadderStats ladderStat = getLadderStat(ladder);

        int w = 0;
        if (ladder.isUnranked()) w += ladderStat.getUnRankedWins();
        if (ladder.isRanked()) w += ladderStat.getRankedWins();

        int l = 0;
        if (ladder.isUnranked()) l += ladderStat.getUnRankedLosses();
        if (ladder.isRanked()) l += ladderStat.getRankedLosses();

        if (l == 0) return w;
        return NumberUtil.roundDouble((double) w / l);
    }

    public int getGlobalElo() {
        int returnElo = 0;
        int count = 0;

        for (NormalLadder ladder : ladderStats.keySet()) {
            LadderStats ladderStat = getLadderStat(ladder);

            if (ladder.isEnabled() && ladder.isRanked()) {
                returnElo += ladderStat.getElo();
                count++;
            }
        }

        if (count == 0) return 0;
        return (returnElo / count);
    }

    public int getWins(boolean ranked) {
        int wins = 0;
        if (ranked)
            for (NormalLadder ladder : ladderStats.keySet()) {
                LadderStats ladderStat = getLadderStat(ladder);

                if (ladder.isEnabled() && ladder.isRanked()) {
                    wins += ladderStat.getRankedWins();
                }
            }
        else
            for (NormalLadder ladder : ladderStats.keySet()) {
                LadderStats ladderStat = getLadderStat(ladder);

                if (ladder.isEnabled()) {
                    wins += ladderStat.getUnRankedWins();
                }
            }
        return wins;
    }

    public int getLosses(boolean ranked) {
        int losses = 0;
        if (ranked)
            for (NormalLadder ladder : ladderStats.keySet()) {
                LadderStats ladderStat = getLadderStat(ladder);

                if (ladder.isEnabled() && ladder.isRanked()) {
                    losses += ladderStat.getRankedLosses();
                }
            }
        else
            for (NormalLadder ladder : ladderStats.keySet()) {
                LadderStats ladderStat = getLadderStat(ladder);

                if (ladder.isEnabled()) {
                    losses += ladderStat.getUnRankedLosses();
                }
            }
        return losses;
    }

    public double getRatio(boolean ranked) {
        int w = getWins(ranked);
        int l = getLosses(ranked);

        if (l == 0) return w;
        return NumberUtil.roundDouble((double) w / l);
    }

    public int getGlobalWins() {
        return getWins(false) + getWins(true);
    }

    public int getGlobalLosses() {
        return getLosses(false) + getLosses(true);
    }

    public double getGlobalRatio() {
        int w = getGlobalWins();
        int l = getGlobalLosses();

        if (l == 0) return w;
        return NumberUtil.roundDouble((double) w / l);
    }

    public int getKills() {
        int kills = 0;
        for (NormalLadder ladder : ladderStats.keySet()) {
            LadderStats ladderStat = getLadderStat(ladder);

            if (ladder.isEnabled()) {
                kills += ladderStat.getKills();
            }
        }
        return kills;
    }

    public int getDeaths() {
        int deaths = 0;
        for (NormalLadder ladder : ladderStats.keySet()) {
            LadderStats ladderStat = getLadderStat(ladder);

            if (ladder.isEnabled()) {
                deaths += ladderStat.getDeaths();
            }
        }
        return deaths;
    }

}
