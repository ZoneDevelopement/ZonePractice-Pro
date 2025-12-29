package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public enum ListenerUtil {
    ;

    public static boolean cancelEvent(Match match, Player player) {
        if (match.getCurrentStat(player).isSet())
            return true;

        return !match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE);
    }


    public static int getCalculatedBuildLimit(BasicArena arena) {
        int buildLimit;

        if (arena.isBuildMax())
            buildLimit = arena.getBuildMaxValue();
        else {
            if (arena instanceof FFAArena) {
                buildLimit = arena.getFfaPositions().get(0).getBlockY() + arena.getBuildMaxValue();
            } else {
                buildLimit = arena.getPosition1().getBlockY() + arena.getBuildMaxValue();
            }
        }

        return buildLimit;
    }

    public static boolean checkMetaData(MetadataValue metadataValue) {
        return metadataValue == null || metadataValue.value() == null;
    }

}
