package dev.nandi0813.practice.manager.fight.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.ListenerUtil;
import dev.nandi0813.practice.manager.fight.util.FightUtil;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

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
        if (ClassImport.getClasses().getArenaUtil().turnsToDirt(under)) {
            spectatable.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(under));
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
        if (block.hasMetadata(PLACED_IN_FIGHT)) {
            MetadataValue mv = BlockUtil.getMetadata(block, PLACED_IN_FIGHT);
            if (ListenerUtil.checkMetaData(mv)) return;
            if (!(mv.value() instanceof Spectatable spectatable)) return;
            if (!spectatable.isBuild()) return;

            spectatable.addBlockChange(ClassImport.createChangeBlock(block));
            return;
        }

        // Case 2: natural arena block — check if it is a destroyable block (bed, nexus, etc.)
        Spectatable spectatable = getByBlock(block);
        if (spectatable == null || !spectatable.isBuild()) return;

        var ladder = (spectatable instanceof Match match) ? match.getLadder() : null;
        if (ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(ladder, block)) {
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
        if (block.hasMetadata(PLACED_IN_FIGHT)) {
            MetadataValue mv = BlockUtil.getMetadata(block, PLACED_IN_FIGHT);
            if (ListenerUtil.checkMetaData(mv) || !(mv.value() instanceof Spectatable s)) return;
            spectatable = s;
        } else {
            spectatable = getByBlock(block);
            if (spectatable == null || !spectatable.isBuild()) return;
            block.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), spectatable));
        }

        spectatable.addBlockChange(ClassImport.createChangeBlock(e));
        trackUnderBlockIfDirt(block, spectatable);
    }

}
