package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public final class ListenerUtil {
    private ListenerUtil() {
    }

    /**
     * If the block was placed during the fight, cancels the event when the
     * metadata owner is null (stale/ orphaned data) and returns true.
     * Returns false if the block is not a fight-placed block (caller should
     * continue with normal block-break validation).
     */
    public static boolean handlePlacedInFightBlock(Block block, BlockBreakEvent e) {
        if (!BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) return false;
        Object mv = BlockUtil.getMetadata(block, PLACED_IN_FIGHT, Object.class);
        if (checkMetaData(mv)) {
            e.setCancelled(true);
        }
        return true;
    }

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
                buildLimit = arena.getFfaPositions().getFirst().getBlockY() + arena.getBuildMaxValue();
            } else {
                buildLimit = arena.getPosition1().getBlockY() + arena.getBuildMaxValue();
            }
        }

        return buildLimit;
    }

    public static boolean checkMetaData(Object metadataValue) {
        return metadataValue == null;
    }

}
