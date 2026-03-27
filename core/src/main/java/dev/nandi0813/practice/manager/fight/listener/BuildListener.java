package dev.nandi0813.practice.manager.fight.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.ChangedBlock;
import dev.nandi0813.practice.manager.fight.util.FightUtil;
import dev.nandi0813.practice.manager.fight.util.ListenerUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
public class BuildListener implements Listener {

    // =========================================================================
    // HELPERS — shared by all subclasses
    // =========================================================================

    private final Map<String, Integer> setFuseTick = new HashMap<>();

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
     * Resolves explosion ownership using center-first lookup, then block-list fallbacks.
     * This covers edge cases where an explosion center is outside the arena cuboid but
     * still destroys blocks inside it (common with wandering creepers in FFA).
     */
    protected static Spectatable getByExplosion(Location center, List<Block> blockList) {
        Spectatable byCenter = getByLocation(center);
        if (byCenter != null) {
            return byCenter;
        }

        for (Block block : blockList) {
            if (!BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) continue;

            Spectatable tagged = BlockUtil.getMetadata(block, PLACED_IN_FIGHT, Spectatable.class);
            if (!ListenerUtil.checkMetaData(tagged) && tagged.isBuild()) {
                return tagged;
            }
        }

        for (Block block : blockList) {
            Spectatable byBlock = getByBlock(block);
            if (byBlock != null) {
                return byBlock;
            }
        }

        return null;
    }

    protected static Spectatable getByPlayer(Player player) {
        return FightUtil.getActiveBuildSpectatables().stream()
                .filter(s -> s.getActivePlayerList().contains(player))
                .findFirst().orElse(null);
    }

    /** Track the block under a placed block if it will naturally turn to dirt (grass -> dirt). */
    private static void trackUnderBlockIfDirt(Block block, Spectatable spectatable) {
        Block under = block.getRelative(0, -1, 0);
        if (ArenaUtil.turnsToDirt(under)) {
            spectatable.getFightChange().addArenaBlockChange(new ChangedBlock(under));
        }
    }

    /**
     * Tags {@code block} with {@code PLACED_IN_FIGHT} metadata pointing to
     * {@code spectatable} and records it for rollback.
     */
    protected static void tagAndTrack(Block block, Spectatable spectatable) {
        BlockUtil.setMetadata(block, PLACED_IN_FIGHT, spectatable);
        spectatable.addBlockChange(new ChangedBlock(block));
    }

    /**
     * Replaceable flora/support blocks are overwritten by placement and must be
     * snapshotted from BlockPlaceEvent#getBlockReplacedState for accurate rollback.
     */
    private static boolean shouldTrackReplacedState(BlockState replacedState) {
        Material replacedType = replacedState.getType();
        if (replacedType.isAir()) {
            return false;
        }

        return !replacedType.isSolid();
    }

    /**
     * Resolves the {@link Ladder} from a {@link Spectatable}.
     * Returns {@code null} when the Spectatable is not a {@link Match} (e.g. FFA).
     */
    protected static Ladder ladderOf(Spectatable spectatable) {
        return (spectatable instanceof Match) ? ((Match) spectatable).getLadder() : null;
    }

    /**
     * Applies explosion block filtering rules and tracks all surviving changed blocks for rollback.
     */
    private static void filterAndTrackExplosionBlocks(List<Block> blockList, Spectatable spectatable) {
        final Ladder l = ladderOf(spectatable);
        final boolean breakAll = spectatable.isBreakAllBlocks();

        blockList.removeIf(block -> {
            if (block.getType().equals(Material.TNT)) return false;                    // keep -> chain-explodes
            if (ArenaUtil.containsDestroyableBlock(l, block)) return false;            // keep -> destroyable
            if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) return false;           // keep -> player placed
            if (breakAll) return false;                                                 // keep -> break-all-blocks active
            if (BlockUtil.hasMetadata(block.getRelative(0, 1, 0), PLACED_IN_FIGHT)) return true; // remove -> support block protected
            return true;                                                                // remove -> pure arena block
        });

        for (Block block : blockList) {
            if (block.getType() == Material.TNT || block.getType() == Material.AIR) continue;
            if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
                spectatable.addBlockChange(new ChangedBlock(block));
            } else {
                // Natural arena block -> use addArenaBlockChange so no PLACED_IN_FIGHT metadata is set.
                spectatable.getFightChange().addArenaBlockChange(new ChangedBlock(block));
            }
            trackDependentBlocksAbove(block, spectatable);
            trackGravityBlocksAbove(block, spectatable);
        }
    }

    // =========================================================================
    // PLAYER-DRIVEN BLOCK EVENTS (merged from BuildBlockListener)
    // =========================================================================

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();

        // Case 1: block was placed during the fight -> track it for rollback.
        if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
            Spectatable spectatable = BlockUtil.getMetadata(block, PLACED_IN_FIGHT, Spectatable.class);
            if (ListenerUtil.checkMetaData(spectatable)) return;
            if (!spectatable.isBuild()) return;

            spectatable.addBlockChange(new ChangedBlock(block));
            return;
        }

        // Case 2: natural arena block -> only allow configured destroyable blocks.
        Spectatable spectatable = getByBlock(block);
        if (spectatable == null || !spectatable.isBuild()) return;

        var ladder = (spectatable instanceof Match match) ? match.getLadder() : null;
        if (ArenaUtil.containsDestroyableBlock(ladder, block)) {
            BlockUtil.breakBlock(spectatable, block);
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        BlockState replacedState = event.getBlockReplacedState();
        Spectatable spectatable;
        boolean needsMetadata = false;

        if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
            spectatable = BlockUtil.getMetadata(block, PLACED_IN_FIGHT, Spectatable.class);
            if (ListenerUtil.checkMetaData(spectatable)) {
                return;
            }
        } else {
            spectatable = getByPlayer(event.getPlayer());
            needsMetadata = true;
        }

        if (spectatable == null || !spectatable.isBuild()) {
            return;
        }

        if (needsMetadata) {
            BlockUtil.setMetadata(block, PLACED_IN_FIGHT, spectatable);
        }

        if (shouldTrackReplacedState(replacedState)) {
            spectatable.getFightChange().addArenaBlockChange(new ChangedBlock(replacedState));
        } else {
            spectatable.addBlockChange(new ChangedBlock(event));
        }

        trackUnderBlockIfDirt(block, spectatable);
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
        while (ArenaUtil.requiresSupport(above)) {
            if (!BlockUtil.hasMetadata(above, PLACED_IN_FIGHT)) {
                spectatable.getFightChange().addArenaBlockChange(new ChangedBlock(above));
            }
            above = above.getRelative(0, 1, 0);
        }
    }

    /**
     * Tracks contiguous gravity blocks (sand, gravel, concrete powder, anvils, etc.)
     * above an exploded block before physics turns them into FallingBlock entities.
     */
    private static void trackGravityBlocksAbove(Block base, Spectatable spectatable) {
        Block above = base.getRelative(0, 1, 0);
        while (!above.getType().isAir() && above.getType().hasGravity()) {
            if (!BlockUtil.hasMetadata(above, PLACED_IN_FIGHT)) {
                spectatable.getFightChange().addArenaBlockChange(new ChangedBlock(above));
            }
            above = above.getRelative(0, 1, 0);
        }
    }

    /**
            trackGravityBlocksAbove(block, spectatable);
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
    protected void handleExplosion(Event event, List<Block> blockList, Spectatable spectatable) {
        if (spectatable == null) return;

        if (spectatable instanceof Match match) {
            Ladder ladder = match.getLadder();
            if (ladder instanceof LadderHandle lh) {
                lh.handleEvents(event, match);
            }
        }

        filterAndTrackExplosionBlocks(blockList, spectatable);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Spectatable spectatable = getByExplosion(e.getLocation(), e.blockList());

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
            // Use handleExplosion to properly track blocks and delegate to ladder handlers
            handleExplosion(e, e.blockList(), spectatable);
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
            onApplyFuseTime(tnt);
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
    protected void onApplyFuseTime(TNTPrimed tnt) {
        final String key = locationKey(tnt.getLocation());
        if (setFuseTick.containsKey(key)) {
            tnt.setFuseTicks(setFuseTick.remove(key));
        }
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent e) {
        if (e.getEntity() instanceof FallingBlock fallingBlock) {
            if (e.isCancelled()) return;

            Block sourceBlock = e.getLocation().getBlock();
            Spectatable spectatable = getByBlock(sourceBlock);
            if (spectatable == null || !spectatable.isBuild()) return;

            BlockUtil.setMetadata(fallingBlock, PLACED_IN_FIGHT, spectatable);
            spectatable.addEntityChange(fallingBlock);

            if (!BlockUtil.hasMetadata(sourceBlock, PLACED_IN_FIGHT)) {
                Material originalMaterial = fallingBlock.getBlockData().getMaterial();
                spectatable.getFightChange().addArenaBlockChange(
                        new ChangedBlock(sourceBlock, originalMaterial));
            }
            return;
        }

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
                    new ChangedBlock(e.getLocation().getBlock(), Material.TNT));
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
        return true;
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

    /**
     * Tracks blocks that turn to dirt when another block forms on top (e.g., grass
     * becoming dirt when cobblestone is generated via lava). This runs at LOWEST priority
     * to capture the block's state BEFORE it changes to dirt.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFormLowest(BlockFormEvent e) {
        final Block formed = e.getBlock();
        final Spectatable spectatable = getByBlock(formed);
        if (spectatable == null || !spectatable.isBuild()) return;

        Block below = formed.getRelative(0, -1, 0);
        // At LOWEST, the block below is still grass/mycelium/etc. before conversion.
        // Capture its current state so rollback restores the exact original type.
        if (ArenaUtil.turnsToDirt(below)) {
            Material originalMaterial = below.getType();
            spectatable.getFightChange().addArenaBlockChange(
                    new ChangedBlock(below, originalMaterial));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockForm(BlockFormEvent e) {
        final Block formed = e.getBlock();
        final Spectatable spectatable = getByBlock(formed);
        if (spectatable == null || !spectatable.isBuild()) return;


        // Track generated block directly so rollback does not miss short-lived form changes.
        spectatable.getFightChange().addArenaBlockChange(new ChangedBlock(formed));
        if (!BlockUtil.hasMetadata(formed, PLACED_IN_FIGHT)) {
            BlockUtil.setMetadata(formed, PLACED_IN_FIGHT, spectatable);
        }
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
        if (BlockUtil.hasMetadata(liquidSourceBlock, PLACED_IN_FIGHT)) return; // already tracked

        // Capture while still AIR (or pre-existing material) so rollback restores correctly
        spectatable.getFightChange().addBlockChange(new ChangedBlock(liquidSourceBlock));
        BlockUtil.setMetadata(liquidSourceBlock, PLACED_IN_FIGHT, spectatable);
    }

    // =========================================================================
    // LIQUID FLOW
    // =========================================================================

    /**
     * Tracks blocks that turn to dirt when lava flows on top (e.g., grass
     * becoming dirt when lava spreads over it). This runs at LOWEST priority
     * to capture the block's state BEFORE it changes to dirt.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromToLowest(BlockFromToEvent e) {
        Block toBlock = e.getToBlock();
        Spectatable spectatable = getByBlock(toBlock);
        if (spectatable == null || !spectatable.isBuild()) return;

        // Only handle lava flows (lava turns grass to dirt naturally)
        Block fromBlock = e.getBlock();
        if (!fromBlock.isLiquid()) return;

        // At LOWEST, the destination block is still grass/mycelium/etc. before conversion.
        // Capture its current state so rollback restores the exact original type.
        if (ArenaUtil.turnsToDirt(toBlock)) {
            Material originalMaterial = toBlock.getType();
            spectatable.getFightChange().addArenaBlockChange(
                    new ChangedBlock(toBlock, originalMaterial));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent e) {
        Block fromBlock = e.getBlock();
        Block toBlock = e.getToBlock();

        Spectatable spectatable = null;
        Match match = null;

        // Fast path: source block already tagged — O(1) lookup
        if (BlockUtil.hasMetadata(fromBlock, PLACED_IN_FIGHT)) {
            Spectatable s = BlockUtil.getMetadata(fromBlock, PLACED_IN_FIGHT, Spectatable.class);
            if (s != null) {
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
            BlockUtil.setMetadata(fromBlock, PLACED_IN_FIGHT, spectatable);

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
        if (!BlockUtil.hasMetadata(toBlock, PLACED_IN_FIGHT)) {
            BlockUtil.setMetadata(toBlock, PLACED_IN_FIGHT, spectatable);
            spectatable.getFightChange().addBlockChange(new ChangedBlock(toBlock));
        }
    }

    // =========================================================================
    // BLOCK SPREAD (fire, mushrooms, etc.)
    // =========================================================================

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent e) {
        Block source = e.getSource();

        Spectatable spectatable = null;

        if (BlockUtil.hasMetadata(source, PLACED_IN_FIGHT)) {
            Spectatable s = BlockUtil.getMetadata(source, PLACED_IN_FIGHT, Spectatable.class);
            if (s != null) {
                spectatable = s;
            }
        }

        if (spectatable == null) {
            spectatable = getByBlock(source);
            if (spectatable == null) return;
        }

        if (!spectatable.isBuild()) return;

        // Cancel fire spread during rollback to prevent fire from re-igniting
        // freshly-restored flammable blocks while the multi-tick rollback is in progress.
        if (spectatable.getFightChange() != null && spectatable.getFightChange().isRollingBack()) {
            e.setCancelled(true);
            return;
        }

        final Spectatable finalSpectatable = spectatable;
        final Block newBlock = e.getNewState().getBlock();
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            if (BlockUtil.hasMetadata(newBlock, PLACED_IN_FIGHT)) return;
            tagAndTrack(newBlock, finalSpectatable);
        });
    }

    // =========================================================================
    // BLOCK FADE (grass → dirt, ice melt, etc.)
    // =========================================================================

    /**
     * Tracks blocks that fade to another type (e.g. grass/mycelium turning to dirt when
     * water is above and blocks light, or ice melting). Runs at LOWEST so the block still
     * holds its original material when captured.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent e) {
        Block block = e.getBlock();
        if (!ArenaUtil.turnsToDirt(block)) return;

        Spectatable spectatable = getByBlock(block);
        if (spectatable == null || !spectatable.isBuild()) return;

        // At LOWEST the block is still grass/mycelium/etc. — capture the original type.
        spectatable.getFightChange().addArenaBlockChange(
                new ChangedBlock(block, block.getType()));
    }

    // =========================================================================
    // BLOCK BURN (fire destroying blocks)
    // =========================================================================

    /**
     * Tracks blocks destroyed by fire so they are restored during rollback.
     * Runs at LOWEST so the block still holds its original material when captured.
     * <p>
     * Also tracks adjacent fire blocks (the igniting fire and fire above) so that
     * rollback removes the fire along with restoring the burned block.
     */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent e) {
        Block block = e.getBlock();

        Spectatable spectatable = null;

        // Fast path: block already tagged
        if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
            Spectatable s = BlockUtil.getMetadata(block, PLACED_IN_FIGHT, Spectatable.class);
            if (s != null) {
                spectatable = s;
            }
        }

        // Slow path: natural arena block — look up by cuboid
        if (spectatable == null) {
            spectatable = getByBlock(block);
            if (spectatable == null) return;
        }

        if (!spectatable.isBuild()) return;

        // Cancel burns during rollback to prevent fire from destroying
        // freshly-restored blocks while the multi-tick rollback is in progress.
        if (spectatable.getFightChange() != null && spectatable.getFightChange().isRollingBack()) {
            e.setCancelled(true);
            return;
        }

        // Track the block's original state for rollback
        if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
            spectatable.addBlockChange(new ChangedBlock(block));
        } else {
            spectatable.getFightChange().addArenaBlockChange(new ChangedBlock(block));
        }

        // Track adjacent fire blocks so rollback removes the fire that caused/surrounds the burn.
        // Without this, restoring the burned block leaves fire sitting on top of it.
        trackAdjacentFire(block, spectatable);

        // After the burn event, the burned block may be replaced with fire.
        // Schedule a 1-tick delayed check to track it if so.
        final Spectatable finalSpectatable = spectatable;
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            String typeName = block.getType().name();
            if ((typeName.equals("FIRE") || typeName.equals("SOUL_FIRE")) && !BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
                tagAndTrack(block, finalSpectatable);
            }
        });
    }

    /**
     * Checks all six faces around a block for fire (FIRE / SOUL_FIRE) and tracks
     * any untracked fire blocks for rollback so they are removed when the arena resets.
     */
    private void trackAdjacentFire(Block center, Spectatable spectatable) {
        final Block[] adjacent = {
                center.getRelative(0, 1, 0),   // above
                center.getRelative(0, -1, 0),  // below
                center.getRelative(1, 0, 0),   // east
                center.getRelative(-1, 0, 0),  // west
                center.getRelative(0, 0, 1),   // south
                center.getRelative(0, 0, -1)   // north
        };

        for (Block adj : adjacent) {
            String typeName = adj.getType().name();
            if (!typeName.equals("FIRE") && !typeName.equals("SOUL_FIRE")) continue;
            if (BlockUtil.hasMetadata(adj, PLACED_IN_FIGHT)) continue;

            tagAndTrack(adj, spectatable);
        }
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
        if (BlockUtil.hasMetadata(fallingBlock, PLACED_IN_FIGHT)) {
            Spectatable spectatable = BlockUtil.getMetadata(fallingBlock, PLACED_IN_FIGHT, Spectatable.class);
            if (spectatable == null) return;

            if (isLanding) {
                // Landing spot is still AIR at LOWEST — record as AIR so rollback clears it
                if (!BlockUtil.hasMetadata(affectedBlock, PLACED_IN_FIGHT)) {
                    spectatable.getFightChange().addArenaBlockChange(
                            new ChangedBlock(affectedBlock, Material.AIR));
                }
            } else {
                // Start falling: block is still its original material — capture it now
                if (!BlockUtil.hasMetadata(affectedBlock, PLACED_IN_FIGHT)) {
                    spectatable.getFightChange().addArenaBlockChange(
                            new ChangedBlock(affectedBlock));
                }
                spectatable.addEntityChange(fallingBlock);
            }
            return;
        }

        // Slow path: first time we see this entity — find owning Spectatable by cuboid
        Spectatable spectatable = getByBlock(affectedBlock);
        if (spectatable == null || !spectatable.isBuild()) return;

        // Tag entity so fast path handles the landing event
        BlockUtil.setMetadata(fallingBlock, PLACED_IN_FIGHT, spectatable);
        spectatable.addEntityChange(fallingBlock);

        if (isLanding) {
            if (!BlockUtil.hasMetadata(affectedBlock, PLACED_IN_FIGHT)) {
                spectatable.getFightChange().addArenaBlockChange(
                        new ChangedBlock(affectedBlock, Material.AIR));
            }
        } else {
            if (!BlockUtil.hasMetadata(affectedBlock, PLACED_IN_FIGHT)) {
                spectatable.getFightChange().addArenaBlockChange(
                        new ChangedBlock(affectedBlock));
            }
        }
    }

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
            spectatable.getFightChange().addArenaBlockChange(new ChangedBlock(e.getBlock()));
        }

        // Record custom fuse time for match contexts
        if (spectatable instanceof Match match) {
            if (!e.getCause().equals(TNTPrimeEvent.PrimeCause.EXPLOSION)) {
                setFuseTick.put(locationKey(e.getBlock().getLocation()),
                        match.getLadder().getTntFuseTime() * 20);
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        Spectatable spectatable = getByExplosion(e.getBlock().getLocation(), e.blockList());
        handleExplosion(e, e.blockList(), spectatable);
    }

}
