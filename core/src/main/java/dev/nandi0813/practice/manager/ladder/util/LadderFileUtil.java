package dev.nandi0813.practice.manager.ladder.util;

import dev.nandi0813.practice.manager.fight.match.enums.MatchType;
import dev.nandi0813.practice.util.Common;

import java.util.ArrayList;
import java.util.List;

public enum LadderFileUtil {
    ;

    public static void getLadderMatchTypes(final List<MatchType> matchTypes, final List<String> values) {
        matchTypes.clear();

        for (String matchType : values) {
            try {
                matchTypes.add(MatchType.valueOf(matchType));
            } catch (IllegalArgumentException ignored) {
                Common.sendConsoleMMMessage("<red>Invalid match type: " + matchType + ". Skipping.");
            }
        }
    }

    public static List<String> getMatchTypeNames(List<MatchType> matchTypes) {
        List<String> list = new ArrayList<>();
        for (MatchType matchType : matchTypes)
            list.add(matchType.toString());
        return list;
    }

}
