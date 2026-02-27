package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.FightUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

/**
 * Abstract base listener that handles all block-world tracking events for every
 * build-enabled {@link Spectatable} (Match, FFA, and future types).
 * <p>
 * Version-specific subclasses extend this class and add API calls that are only
 * available on that Minecraft version (e.g. {@code TNTPrimeEvent} / {@code BlockExplodeEvent}
 * on modern Paper, or the fallback {@code EntitySpawnEvent} TNT tracking on 1.8.8).
 * <p>
 * <b>Responsibilities of this class:</b>
 * <ul>
 *   <li>Explosions     — {@link EntityExplodeEvent} with creeper + ladder-delegation support</li>
 *   <li>TNT entity     — {@link EntitySpawnEvent} fallback TNT tracking (used as-is on 1.8.8,
 *                        superseded by {@code TNTPrimeEvent} on modern but still fires)</li>
 *   <li>Pistons        — {@link BlockPistonExtendEvent} / {@link BlockPistonRetractEvent}</li>
 *   <li>Block form     — {@link BlockFormEvent} (cobblestone/obsidian generators)</li>
 *   <li>Liquid flow    — {@link BlockFromToEvent} with Match-end cancel + ladder delegation</li>
 *   <li>Block spread   — {@link BlockSpreadEvent} (fire, mushrooms, etc.)</li>
 * </ul>
 * <p>
 * <b>Handled in subclasses only:</b>
 * <ul>
 *   <li>Modern: {@code TNTPrimeEvent} (fires before block changes → accurate TNT tracking),
 *               {@code BlockExplodeEvent} (block-triggered explosions)</li>
 *   <li>1.8.8:  TNT tracking entirely via {@link EntitySpawnEvent} (no TNTPrimeEvent available)</li>
 * </ul>
 */
public abstract class AbstractBuildListener implements Listener {

    // =========================================================================
    // HELPERS — shared by all subclasses
    // =========================================================================

    /**
     * Finds the active build-enabled {@link Spectatable} whose cuboid contains the location.
     * Returns {@code null} if none is found.
     */
    protected static Spectatable getByLocation(Location location) {
        return FightUtil.getActiveBuildSpectatables().stream()
                .filter(s -> s.getCuboid() != null && s.getCuboid().contains(location))
                .findFirst().orElse(null);
    }

    protected static Spectatable getByBlock(Block block) {
        return getByLocation(block.getLocation());
    }

    /**
     * Tags {@code block} with {@code PLACED_IN_FIGHT} metadata pointing to
     * {@code spectatable} and records it for rollback.
     */
    protected static void tagAndTrack(Block block, Spectatable spectatable) {
        block.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), spectatable));
        spectatable.addBlockChange(ClassImport.createChangeBlock(block));
    }

    /**
     * Resolves the {@link Ladder} from a {@link Spectatable}.
     * Returns {@code null} when the Spectatable is not a {@link Match} (e.g. FFA).
     */
    protected static Ladder ladderOf(Spectatable spectatable) {
        return (spectatable instanceof Match) ? ((Match) spectatable).getLadder() : null;
    }

    // =========================================================================
    // EXPLOSIONS
    // =========================================================================

    /**
     * Tracks every block stacked directly above {@code base} that requires solid
     * support (dead bush, tall grass, torch, sapling, etc.) and is NOT already
     * tracked for rollback.
     * <p>
     * These blocks are never included in the TNT {@code blockList} by Minecraft —
     * they just silently pop off when their support disappears. We must capture their
     * state here, <em>before</em> the explosion fires, so the rollback can restore them.
     * We walk upward until we hit a block that does not require support.
     */
    private static void trackDependentBlocksAbove(Block base, Spectatable spectatable) {
        Block above = base.getRelative(0, 1, 0);
        while (ClassImport.getClasses().getArenaUtil().requiresSupport(above)) {
            if (!above.hasMetadata(PLACED_IN_FIGHT)) {
                spectatable.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(above));
            }
            above = above.getRelative(0, 1, 0);
        }
    }

    /**
     * Core explosion handler shared by {@link EntityExplodeEvent} and
     * (on modern) {@code BlockExplodeEvent}.
     * <ul>
     *   <li>Delegates to the ladder handler when the context is a {@link Match}.</li>
     *   <li>When {@code breakAllBlocks} is disabled: removes pure-arena blocks so they
     *       stay intact, tracks only player-placed blocks for rollback.</li>
     *   <li>When {@code breakAllBlocks} is enabled: keeps every arena block in the list
     *       (so the explosion destroys them) and tracks each one via
     *       {@code addArenaBlockChange} for rollback.</li>
     *   <li>Tracks dependent blocks above exploded blocks (dead bush, tall grass, etc.)
     *       that silently disappear when their support is destroyed.</li>
     * </ul>
     */
    protected void handleExplosion(org.bukkit.event.Event event, List<Block> blockList, Spectatable spectatable) {
        if (spectatable == null) return;

        if (spectatable instanceof Match match) {
            Ladder ladder = match.getLadder();
            if (ladder instanceof LadderHandle lh) {
                lh.handleEvents(event, match);
            }
        }

        final Ladder l = ladderOf(spectatable);
        final boolean breakAll = spectatable.isBreakAllBlocks();

        blockList.removeIf(block -> {
            if (block.getType().equals(Material.TNT)) return false;                    // keep → chain-explodes
            if (ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(l, block)) return false; // keep → destroyable
            if (block.hasMetadata(PLACED_IN_FIGHT)) return false;                      // keep → player placed
            if (breakAll) return false;                                                 // keep → break-all-blocks active
            if (block.getRelative(0, 1, 0).hasMetadata(PLACED_IN_FIGHT)) return true; // remove → support block protected
            return true;                                                                // remove → pure arena block
        });

        for (Block block : blockList) {
            if (block.getType() == Material.TNT || block.getType() == Material.AIR) continue;
            if (block.hasMetadata(PLACED_IN_FIGHT)) {
                spectatable.addBlockChange(ClassImport.createChangeBlock(block));
            } else {
                // Natural arena block — use addArenaBlockChange so no PLACED_IN_FIGHT
                // metadata is set (players can't break it manually) but it is restored.
                spectatable.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(block));
            }
            trackDependentBlocksAbove(block, spectatable);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Spectatable spectatable = getByLocation(e.getLocation());

        if (e.getEntity() instanceof Creeper) {
            if (spectatable == null) return;
            if (!spectatable.isBuild()) {
                e.blockList().clear();
                return;
            }
            // For matches: only allow creeper damage during a LIVE round
            if (spectatable instanceof Match match) {
                if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
                    e.blockList().clear();
                    return;
                }
            }
            final Ladder l = ladderOf(spectatable);
            final boolean breakAll = spectatable.isBreakAllBlocks();
            e.blockList().removeIf(block -> {
                if (block.getType().equals(Material.TNT)) return false;
                if (ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(l, block)) return false;
                if (block.hasMetadata(PLACED_IN_FIGHT)) return false;
                if (breakAll) return false;
                if (block.getRelative(0, 1, 0).hasMetadata(PLACED_IN_FIGHT)) return true;
                return true;
            });
            for (Block block : e.blockList()) {
                if (block.getType() == Material.TNT || block.getType() == Material.AIR) continue;
                if (block.hasMetadata(PLACED_IN_FIGHT)) {
                    spectatable.addBlockChange(ClassImport.createChangeBlock(block));
                } else {
                    spectatable.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(block));
                }
                trackDependentBlocksAbove(block, spectatable);
            }
            return;
        }

        if (spectatable == null) return;
        handleExplosion(e, e.blockList(), spectatable);
    }

    // =========================================================================
    // TNT ENTITY SPAWN (fallback — used as-is on 1.8.8; still fires on modern)
    // =========================================================================

    /**
     * Tracks a newly spawned {@link TNTPrimed} entity for rollback and applies
     * match-specific fuse-time overrides.
     * <p>
     * On 1.8.8 this is the primary TNT tracking hook (no {@code TNTPrimeEvent}).
     * On modern Paper the block is already captured earlier via {@code TNTPrimeEvent},
     * so here we only register the entity and apply the fuse time.
     *
     * @param tnt        the primed TNT entity
     * @param spectatable the owning fight context
     */
    protected void handleTntSpawn(TNTPrimed tnt, Spectatable spectatable) {
        if (spectatable == null) return;

        if (spectatable.isBuild()) {
            spectatable.addEntityChange(tnt);
        }

        if (spectatable instanceof Match match) {
            onApplyFuseTime(tnt, match);
        }
    }

    /**
     * Called by {@link #handleTntSpawn} to apply a version-specific fuse time to a
     * newly spawned TNT entity for a {@link Match}.
     * <p>
     * The default implementation covers the <b>1.8.8</b> path: set fuse time only
     * when a player lit the TNT (checked via {@code tnt.getSource()}).
     * The modern subclass overrides this to use its own fuse-tick map populated by
     * {@code TNTPrimeEvent}.
     */
    protected void onApplyFuseTime(TNTPrimed tnt, Match match) {
        if (tnt.getSource() instanceof org.bukkit.entity.Player) {
            tnt.setFuseTicks(20 * match.getLadder().getTntFuseTime());
        }
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent e) {
        if (!(e.getEntity() instanceof TNTPrimed tnt)) return;
        if (e.isCancelled()) return;

        Spectatable spectatable = getByLocation(e.getLocation());
        if (spectatable == null) return;

        // On 1.8.8: block is already AIR — track with Material.TNT override so rollback restores it
        // On modern: TNTPrimeEvent already captured the block; here we just register entity + fuse
        if (spectatable.isBuild() && !isTntBlockAlreadyTracked()) {
            // Use the event location (exact spawn point) rather than tnt.getLocation() to avoid
            // fractional coordinates resolving to the wrong block.
            spectatable.getFightChange().addArenaBlockChange(
                    ClassImport.createChangeBlock(e.getLocation().getBlock(), Material.TNT));
        }

        handleTntSpawn(tnt, spectatable);
    }

    /**
     * Returns {@code true} if the TNT block has already been captured before entity spawn
     * (i.e. via {@code TNTPrimeEvent} on modern Paper).
     * <p>
     * Subclasses on modern Paper should override this to return {@code true} so that
     * {@link #onEntitySpawnEvent} skips the redundant {@code addArenaBlockChange} call.
     * The 1.8.8 default returns {@code false} because there is no pre-spawn event.
     */
    protected boolean isTntBlockAlreadyTracked() {
        return false;
    }

    // =========================================================================
    // PISTONS
    // =========================================================================

    @EventHandler
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        Spectatable spectatable = getByBlock(e.getBlock());
        if (spectatable == null) return;
        if (!spectatable.isBuild()) {
            e.setCancelled(true);
            return;
        }
        for (Block block : e.getBlocks()) {
            tagAndTrack(block, spectatable);
            tagAndTrack(block.getRelative(e.getDirection()), spectatable);
        }
    }

    @EventHandler
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        Spectatable spectatable = getByBlock(e.getBlock());
        if (spectatable == null) return;
        if (!spectatable.isBuild()) {
            e.setCancelled(true);
            return;
        }
        for (Block block : e.getBlocks()) {
            tagAndTrack(block, spectatable);
            tagAndTrack(block.getRelative(e.getDirection()), spectatable);
        }
    }

    // =========================================================================
    // BLOCK FORM (cobblestone / obsidian generators, ice, etc.)
    // =========================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent e) {
        final Location location = e.getBlock().getLocation();
        final Spectatable spectatable = getByBlock(e.getBlock());
        if (spectatable == null || !spectatable.isBuild()) return;

        // 2-tick delay to guarantee the block has been written to the world
        org.bukkit.Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            Block formed = location.getBlock();
            if (formed.getType() == Material.AIR) return;
            if (formed.hasMetadata(PLACED_IN_FIGHT)) return;
            tagAndTrack(formed, spectatable);
        }, 2L);
    }

    // =========================================================================
    // LIQUID SOURCE — bucket placement
    // =========================================================================

    /**
     * Captures the block that will become the liquid source BEFORE the bucket is emptied.
     * At MONITOR priority the event is guaranteed to be allowed by the validation listeners
     * (FFAListener / LadderTypeListener). The target block is still AIR (or whatever it was)
     * at this point, so {@code createChangeBlock} records the correct pre-liquid state,
     * ensuring rollback restores it to AIR.
     * <p>
     * We also tag the block now so that the {@link #onBlockFromTo} slow path's
     * {@code getByBlock} lookup is skipped — flow events will directly use the fast path.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Spectatable spectatable = getByBlock(e.getBlockClicked());
        if (spectatable == null || !spectatable.isBuild()) return;

        // The liquid will be placed on the face the player clicked
        Block liquidSourceBlock = e.getBlockClicked().getRelative(e.getBlockFace());
        if (liquidSourceBlock.hasMetadata(PLACED_IN_FIGHT)) return; // already tracked

        // Capture while still AIR (or pre-existing material) so rollback restores correctly
        spectatable.getFightChange().addBlockChange(ClassImport.createChangeBlock(liquidSourceBlock));
        liquidSourceBlock.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), spectatable));
    }

    // =========================================================================
    // LIQUID FLOW
    // =========================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        Block fromBlock = e.getBlock();
        Block toBlock = e.getToBlock();

        Spectatable spectatable = null;
        Match match = null;

        // Fast path: source block already tagged — O(1) lookup
        if (fromBlock.hasMetadata(PLACED_IN_FIGHT)) {
            MetadataValue mv = fromBlock.getMetadata(PLACED_IN_FIGHT).get(0);
            if (mv.value() instanceof Spectatable s) {
                spectatable = s;
                if (spectatable instanceof Match m) match = m;
            }
        }

        // Slow path: source has no metadata — it is a natural arena liquid source.
        if (spectatable == null) {
            spectatable = getByBlock(fromBlock);
            if (spectatable == null) return;
            if (!spectatable.isBuild()) return;

            // Tag the block so future flow events from it hit the fast path and can be
            // properly cancelled after match end.
            fromBlock.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), spectatable));

            if (spectatable instanceof Match m) match = m;
        }

        // Match-specific guards: cancel after match ends, delegate to ladder
        if (match != null) {
            if (match.getStatus().equals(MatchStatus.END)) {
                e.setCancelled(true);
                return;
            }
            Ladder ladder = match.getLadder();
            if (ladder instanceof LadderHandle lh) {
                lh.handleEvents(e, match);
            }
            if (e.isCancelled()) return;
        }

        // Track all destination blocks so all flowing liquids inside the arena are
        // properly recorded for rollback — whether the source is player-placed or natural.
        if (!toBlock.hasMetadata(PLACED_IN_FIGHT)) {
            toBlock.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), spectatable));
            spectatable.getFightChange().addBlockChange(ClassImport.createChangeBlock(toBlock));
        }
    }

    // =========================================================================
    // BLOCK SPREAD (fire, mushrooms, etc.)
    // =========================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        Block source = e.getSource();
        if (!source.hasMetadata(PLACED_IN_FIGHT)) return;

        MetadataValue mv = source.getMetadata(PLACED_IN_FIGHT).get(0);
        if (!(mv.value() instanceof Spectatable spectatable)) return;
        if (!spectatable.isBuild()) return;

        final Block newBlock = e.getNewState().getBlock();
        org.bukkit.Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            if (newBlock.hasMetadata(PLACED_IN_FIGHT)) return;
            tagAndTrack(newBlock, spectatable);
        });
    }

    // =========================================================================
    // FALLING BLOCKS (sand, gravel, concrete powder, anvils, etc.)
    // =========================================================================

    /**
     * Tracks falling blocks for rollback. Runs at LOWEST so the block in the world
     * still holds its original material when {@code createChangeBlock} is called.
     * <p>
     * Two cases:
     * <ul>
     *   <li><b>Start falling</b> ({@code to == AIR}): capture the original block state
     *       and register the entity so rollback removes it even if it drifts out of the
     *       cuboid.</li>
     *   <li><b>Landing</b> ({@code to != AIR}): at LOWEST the landing spot is still AIR —
     *       record it as AIR so rollback restores it to AIR (removes the rogue block that
     *       fell in from outside or from a placed sand column).</li>
     * </ul>
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onFallingBlockChange(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock fallingBlock)) return;

        Block affectedBlock = e.getBlock();
        boolean isLanding = e.getTo() != Material.AIR;

        // Fast path: entity was tagged when it first started falling inside the arena
        if (fallingBlock.hasMetadata(PLACED_IN_FIGHT)) {
            MetadataValue mv = fallingBlock.getMetadata(PLACED_IN_FIGHT).get(0);
            if (!(mv.value() instanceof Spectatable spectatable)) return;

            if (isLanding) {
                // Landing spot is still AIR at LOWEST — record as AIR so rollback clears it
                if (!affectedBlock.hasMetadata(PLACED_IN_FIGHT)) {
                    spectatable.getFightChange().addArenaBlockChange(
                            ClassImport.createChangeBlock(affectedBlock, Material.AIR));
                }
            } else {
                // Start falling: block is still its original material — capture it now
                if (!affectedBlock.hasMetadata(PLACED_IN_FIGHT)) {
                    spectatable.getFightChange().addArenaBlockChange(
                            ClassImport.createChangeBlock(affectedBlock));
                }
                spectatable.addEntityChange(fallingBlock);
            }
            return;
        }

        // Slow path: first time we see this entity — find owning Spectatable by cuboid
        Spectatable spectatable = getByBlock(affectedBlock);
        if (spectatable == null || !spectatable.isBuild()) return;

        // Tag entity so fast path handles the landing event
        fallingBlock.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), spectatable));
        spectatable.addEntityChange(fallingBlock);

        if (isLanding) {
            if (!affectedBlock.hasMetadata(PLACED_IN_FIGHT)) {
                spectatable.getFightChange().addArenaBlockChange(
                        ClassImport.createChangeBlock(affectedBlock, Material.AIR));
            }
        } else {
            if (!affectedBlock.hasMetadata(PLACED_IN_FIGHT)) {
                spectatable.getFightChange().addArenaBlockChange(
                        ClassImport.createChangeBlock(affectedBlock));
            }
        }
    }
}
