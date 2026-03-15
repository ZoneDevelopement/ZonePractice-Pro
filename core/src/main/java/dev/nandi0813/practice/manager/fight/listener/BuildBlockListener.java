package dev.nandi0813.practice.manager.fight.listener;

import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.FightUtil;
import dev.nandi0813.practice.manager.fight.util.ListenerUtil;
import dev.nandi0813.practice.moved.ChangedBlock;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

/**
 * Unified MONITOR-priority listener that tags and tracks player-initiated block changes
 * (break and place) for all build-enabled {@link Spectatable} contexts.
 * <p>
 * Works for both {@link dev.nandi0813.practice.manager.fight.match.Match} and
 * {@link dev.nandi0813.practice.manager.fight.ffa.game.FFA} — and any future type that
 * implements {@link Spectatable} and is returned by
 * {@link FightUtil#getActiveBuildSpectatables()}.
 * <p>
 * All world-driven block events (pistons, liquid flow, form, spread, explosions, TNT,
 * falling blocks) are handled by the version-specific {@code MatchTntListener} which
 * already covers all Spectatables via the same {@code getActiveBuildSpectatables()} helper.
 */
public class BuildBlockListener implements Listener {

    // ========== HELPERS ==========

    /**
     * Finds the active build-enabled Spectatable whose cuboid contains the given block.
     */
    private static Spectatable getByBlock(Block block) {
        for (Spectatable s : FightUtil.getActiveBuildSpectatables()) {
            if (s.getCuboid() != null && s.getCuboid().contains(block.getLocation())) {
                return s;
            }
        }
        return null;
    }

    /** Track the block under a placed block if it will turn to dirt (grass → dirt). */
    private static void trackUnderBlockIfDirt(Block block, Spectatable spectatable) {
        Block under = block.getLocation().subtract(0, 1, 0).getBlock();
        if (ArenaUtil.turnsToDirt(under)) {
            spectatable.getFightChange().addArenaBlockChange(new ChangedBlock(under));
        }
    }

    // ========== BLOCK BREAK ==========

    /**
     * When a player breaks a block that was placed during the fight, track it for rollback.
     * Runs at MONITOR so the validation listeners (FFAListener / LadderTypeListener) have
     * already cancelled invalid breaks. Uses metadata to find the owning Spectatable — works
     * for both Match and FFA without any type-specific branching.
     * <p>
     * Also handles destroyable blocks (beds, nexuses, etc.) that are natural arena blocks:
     * these are detected by {@code containsDestroyableBlock} and processed via
     * {@link BlockUtil#breakBlock} to trigger the game-mode-specific logic.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();

        // Case 1: block was placed during the fight — track it for rollback
        if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
            Spectatable spectatable = BlockUtil.getMetadata(block, PLACED_IN_FIGHT, Spectatable.class);
            if (ListenerUtil.checkMetaData(spectatable)) return;
            if (!spectatable.isBuild()) return;

            spectatable.addBlockChange(new ChangedBlock(block));
            return;
        }

        // Case 2: natural arena block — check if it is a destroyable block (bed, nexus, etc.)
        Spectatable spectatable = getByBlock(block);
        if (spectatable == null || !spectatable.isBuild()) return;

        var ladder = (spectatable instanceof Match match) ? match.getLadder() : null;
        if (ArenaUtil.containsDestroyableBlock(ladder, block)) {
            BlockUtil.breakBlock(spectatable, block);
            e.setCancelled(true);
        }
    }

    // ========== BLOCK PLACE ==========

    /**
     * Tags a placed block with PLACED_IN_FIGHT metadata and tracks it for rollback.
     * Runs at MONITOR so validation listeners have already cancelled invalid placements.
     * Finds the owning Spectatable from cuboid lookup if not already tagged.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        Block block = e.getBlockPlaced();

        Spectatable spectatable;
        if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
            Spectatable s = BlockUtil.getMetadata(block, PLACED_IN_FIGHT, Spectatable.class);
            if (ListenerUtil.checkMetaData(s)) return;
            spectatable = s;
        } else {
            spectatable = getByBlock(block);
            if (spectatable == null || !spectatable.isBuild()) return;
            BlockUtil.setMetadata(block, PLACED_IN_FIGHT, spectatable);
        }

        spectatable.addBlockChange(new ChangedBlock(e));
        trackUnderBlockIfDirt(block, spectatable);
    }

}
