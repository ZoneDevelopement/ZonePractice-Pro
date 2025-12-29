package dev.nandi0813.practice.util.playerutil;

import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import org.apache.commons.lang.StringUtils;

public enum ProfileStatData {
    ;

    // %all_unranked_wins%
    // %all_unranked_losses%
    // %all_ranked_wins%
    // %all_ranked_losses%
    // %ladder_unranked_%ladder%_wins%
    // %ladder_unranked_%ladder%_losses%
    // %ladder_ranked_%ladder%_wins%
    // %ladder_ranked_%ladder%_losses%
    // %ladder_%ladder%_elo%
    // %global_elo%

    public static String replace(Profile profile, String original) {
        String[] values = StringUtils.substringsBetween(original, "%", "%");

        if (values != null) {
            for (String s : values) {
                String replace = getData(profile, s);

                if (replace != null)
                    original = original.replaceAll("%" + s + "%", replace);
            }
        }

        return original;
    }

    public static String getData(final Profile profile, final String identifier) {
        if (identifier.equalsIgnoreCase("global_elo")) {
            return String.valueOf(profile.getStats().getGlobalElo());
        } else if (identifier.contains("all")) {
            if (identifier.equalsIgnoreCase("all_unranked_wins"))
                return String.valueOf(profile.getStats().getWins(false));
            else if (identifier.equalsIgnoreCase("all_unranked_losses"))
                return String.valueOf(profile.getStats().getLosses(false));
            else if (identifier.equalsIgnoreCase("all_ranked_wins"))
                return String.valueOf(profile.getStats().getWins(true));
            else if (identifier.equalsIgnoreCase("all_ranked_losses"))
                return String.valueOf(profile.getStats().getLosses(true));
        } else if (identifier.contains("ladder")) {
            NormalLadder ladder = null;
            for (NormalLadder sLadder : LadderManager.getInstance().getLadders()) {
                if (identifier.toLowerCase().contains(sLadder.getName().toLowerCase())) {
                    ladder = sLadder;
                    break;
                }
            }

            if (ladder != null) {
                if (identifier.contains("unranked")) {
                    if (identifier.contains("wins"))
                        return String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedWins());
                    else if (identifier.contains("losses"))
                        return String.valueOf(profile.getStats().getLadderStat(ladder).getUnRankedLosses());
                } else if (identifier.contains("ranked")) {
                    if (identifier.contains("wins"))
                        return String.valueOf(profile.getStats().getLadderStat(ladder).getRankedWins());
                    else if (identifier.contains("losses"))
                        return String.valueOf(profile.getStats().getLadderStat(ladder).getRankedLosses());
                } else if (identifier.contains("elo")) {
                    return String.valueOf(profile.getStats().getLadderStat(ladder).getElo());
                }
            }
        }

        return null;
    }

}
