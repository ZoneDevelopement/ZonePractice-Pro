package dev.nandi0813.practice.manager.division;

import java.util.Comparator;

public class DivisionComparator implements Comparator<Division> {

    @Override
    public int compare(Division d1, Division d2) {
        int experienceComparison = Integer.compare(d1.getExperience(), d2.getExperience());

        if (experienceComparison == 0) {
            if (DivisionManager.getInstance().isCOUNT_BY_WINS()) {
                return Integer.compare(d1.getWin(), d2.getWin());
            } else if (DivisionManager.getInstance().isCOUNT_BY_ELO()) {
                return Integer.compare(d1.getElo(), d2.getElo());
            }
        }

        return experienceComparison;
    }

}
