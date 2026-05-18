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

    @Getter
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
        setConfigIntIfNonZero("experience", experience);
        setConfigIntIfNonZero("winStreak", winStreak);
        setConfigIntIfNonZero("bestWinStreak", bestWinStreak);
        setConfigIntIfNonZero("loseStreak", loseStreak);
        setConfigIntIfNonZero("bestLoseStreak", bestLoseStreak);

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            getLadderStat(ladder).setData(ladder.getName().toLowerCase(), ladder.isRanked());
        }

        if (save)
            profileFile.saveFile();
    }

    private void setConfigIntIfNonZero(String path, int value) {
        if (value != 0)
            config.set(path, value);
        else
            config.set(path, null);
    }

    public void getData() {
        setExperience(getConfigInt("experience"));
        setWinStreak(getConfigInt("winStreak"));
        setBestWinStreak(getConfigInt("bestWinStreak"));
        setLoseStreak(getConfigInt("loseStreak"));
        setBestLoseStreak(getConfigInt("bestLoseStreak"));

        for (NormalLadder ladder : LadderManager.getInstance().getLadders()) {
            LadderStats ladderStat = new LadderStats(config);
            ladderStat.getData(ladder.getName().toLowerCase(), ladder.isRanked());
            ladderStats.put(ladder, ladderStat);
        }
    }

    private int getConfigInt(String path) {
        return config.isInt(path) ? config.getInt(path) : 0;
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
