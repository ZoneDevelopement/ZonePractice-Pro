package dev.nandi0813.practice.manager.division;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.NumberUtil;

public enum DivisionUtil {
    ;

    /**
     * Returns the relative experience progress (0–100) towards {@code division}.
     * 0% = the previous division's EXP threshold (or 0 if there is none).
     * 100% = the target division's EXP threshold is met.
     */
    public static double getExperienceProgress(final Profile profile, final Division division) {
        if (division.getExperience() == 0)
            return 100.0;

        Division previous = DivisionManager.getInstance().getPreviousDivision(division);
        int base = (previous != null) ? previous.getExperience() : 0;
        int target = division.getExperience();
        int current = profile.getStats().getExperience();

        if (current >= target) return 100.0;
        if (current <= base || target <= base) return 0.0;

        double progress = (double) (current - base) / (target - base) * 100.0;
        return NumberUtil.roundDouble(Math.min(progress, 100.0));
    }

    /**
     * Returns the relative win progress (0–100) towards {@code division}.
     * 0% = the previous division's win threshold (or 0 if there is none).
     * 100% = the target division's win threshold is met.
     */
    public static double getWinProgress(final Profile profile, final Division division) {
        if (!DivisionManager.getInstance().isCOUNT_BY_WINS() || division.getWin() == 0)
            return 100.0;

        Division previous = DivisionManager.getInstance().getPreviousDivision(division);
        int base = (previous != null) ? previous.getWin() : 0;
        int target = division.getWin();
        int current = profile.getStats().getGlobalWins();

        if (current >= target) return 100.0;
        if (current <= base || target <= base) return 0.0;

        double progress = (double) (current - base) / (target - base) * 100.0;
        return NumberUtil.roundDouble(Math.min(progress, 100.0));
    }

    /**
     * Returns the relative ELO progress (0–100) towards {@code division}.
     * 0% = the previous division's ELO threshold (or 0 if there is none).
     * 100% = the target division's ELO threshold is met.
     */
    public static double getEloProgress(final Profile profile, final Division division) {
        if (!DivisionManager.getInstance().isCOUNT_BY_ELO() || division.getElo() == 0)
            return 100.0;

        Division previous = DivisionManager.getInstance().getPreviousDivision(division);
        int base = (previous != null) ? previous.getElo() : 0;
        int target = division.getElo();
        int current = profile.getStats().getGlobalElo();

        if (current >= target) return 100.0;
        if (current <= base || target <= base) return 0.0;

        double progress = (double) (current - base) / (target - base) * 100.0;
        return NumberUtil.roundDouble(Math.min(progress, 100.0));
    }

    /**
     * Returns the combined weighted progress (0–100) towards {@code division}.
     * EXP is always a factor; wins and ELO are added only when their respective
     * COUNT_BY_* flags are enabled.
     */
    public static double getDivisionProgress(final Profile profile, final Division division) {
        double totalProgress = getExperienceProgress(profile, division);
        int count = 1;

        if (DivisionManager.getInstance().isCOUNT_BY_WINS()) {
            totalProgress += getWinProgress(profile, division);
            count++;
        }

        if (DivisionManager.getInstance().isCOUNT_BY_ELO()) {
            totalProgress += getEloProgress(profile, division);
            count++;
        }

        return NumberUtil.roundDouble(totalProgress / count);
    }

}
