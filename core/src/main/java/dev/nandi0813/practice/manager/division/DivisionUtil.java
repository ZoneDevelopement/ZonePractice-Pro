package dev.nandi0813.practice.manager.division;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.NumberUtil;

public enum DivisionUtil {
    ;

    public static double getExperienceProgress(final Profile profile, final Division division) {
        if (division.getExperience() == 0)
            return 100.0;

        double progress = (double) profile.getStats().getExperience() / division.getExperience() * 100.0;
        return NumberUtil.roundDouble(Math.min(progress, 100.0));
    }

    public static double getWinProgress(final Profile profile, final Division division) {
        if (!DivisionManager.getInstance().isCOUNT_BY_WINS() || division.getWin() == 0)
            return 100.0;

        double progress = (double) profile.getStats().getGlobalWins() / division.getWin() * 100.0;
        return NumberUtil.roundDouble(Math.min(progress, 100.0));
    }

    public static double getEloProgress(final Profile profile, final Division division) {
        if (!DivisionManager.getInstance().isCOUNT_BY_ELO() || division.getElo() == 0)
            return 100.0;

        double progress = (double) profile.getStats().getGlobalElo() / division.getElo() * 100.0;
        return NumberUtil.roundDouble(Math.min(progress, 100.0));
    }

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
