package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.module.interfaces.AbstractBuildListener;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.TNTPrimeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BuildListener extends AbstractBuildListener {

    // =========================================================================
    // MODERN-ONLY: BlockExplodeEvent
    // =========================================================================

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        Spectatable spectatable = getByBlock(e.getBlock());
        handleExplosion(e, e.blockList(), spectatable);
    }

    // =========================================================================
    // MODERN-ONLY: TNTPrimeEvent — fires BEFORE the block changes, giving us
    // the accurate original material. Also records the custom fuse tick so
    // onEntitySpawnEvent (in the base class) can apply it when the entity spawns.
    // =========================================================================

    private final Map<String, Integer> setFuseTick = new HashMap<>();

    private String locationKey(Location loc) {
        return Objects.requireNonNull(loc.getWorld()).getName()
                + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    @EventHandler
    public void onTntPrimeEvent(TNTPrimeEvent e) {
        Spectatable spectatable = getByBlock(e.getBlock());
        if (spectatable == null) return;

        // Track the TNT block NOW — it is still TNT in the world at this point.
        if (spectatable.isBuild()) {
            spectatable.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(e.getBlock()));
        }

        // Record custom fuse time for match contexts
        if (spectatable instanceof Match match) {
            if (!e.getCause().equals(TNTPrimeEvent.PrimeCause.EXPLOSION)) {
                setFuseTick.put(locationKey(e.getBlock().getLocation()),
                        match.getLadder().getTntFuseTime() * 20);
            }
        }
    }

    // =========================================================================
    // OVERRIDES
    // =========================================================================

    /**
     * On modern Paper we already captured the TNT block via {@code TNTPrimeEvent},
     * so the base-class {@link #onEntitySpawnEvent} must not add a duplicate entry.
     */
    @Override
    protected boolean isTntBlockAlreadyTracked() {
        return true;
    }

    /**
     * Apply the fuse time stored by {@link #onTntPrimeEvent} instead of the
     * simple player-source check used by the 1.8.8 path.
     */
    @Override
    protected void onApplyFuseTime(TNTPrimed tnt, Match match) {
        final String key = locationKey(tnt.getLocation());
        if (setFuseTick.containsKey(key)) {
            tnt.setFuseTicks(setFuseTick.remove(key));
        }
    }

}