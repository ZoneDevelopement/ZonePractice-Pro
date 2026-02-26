package dev.nandi0813.practice.manager.fight.match.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.runnable.game.BridgeArrowRunnable;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.fight.util.ListenerUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.type.Bridges;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.NumberUtil;
import dev.nandi0813.practice.util.PermanentConfig;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;
import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public abstract class LadderTypeListener implements Listener {

    // ========== HELPER METHODS ==========

    /**
     * Gets the match for a player if they are in MATCH status.
     *
     * @return Match or null if player is not in a match
     */
    protected Match getPlayerMatch(Player player) {
        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (!profile.getStatus().equals(ProfileStatus.MATCH)) return null;
        return MatchManager.getInstance().getLiveMatchByPlayer(player);
    }

    /**
     * Validates if a block placement/break is within build limits.
     * Sends appropriate error messages to the player.
     *
     * @return true if within limits, false otherwise
     */
    protected boolean isWithinBuildLimits(Block block, Match match, Player player) {
        // Check height limit
        // Note: The limit represents the maximum Y coordinate blocks can reach (top of block)
        // Since blocks occupy Y to Y+1, we check if the block's position (bottom) is >= limit
        if (block.getLocation().getY() >= ListenerUtil.getCalculatedBuildLimit(match.getArena())) {
            Common.sendMMMessage(player, LanguageManager.getString("MATCH.CANT-BUILD-OVER-LIMIT"));
            return false;
        }

        // Check side build limit
        if (match.getSideBuildLimit() != null && !match.getSideBuildLimit().contains(block)) {
            Common.sendMMMessage(player, LanguageManager.getString("MATCH.CANT-BUILD-OVER-LIMIT"));
            return false;
        }

        return true;
    }

    /**
     * Tracks a placed block and its metadata, including the block underneath if it's dirt.
     */
    protected void trackPlacedBlock(Block block, Match match) {
        match.addBlockChange(ClassImport.createChangeBlock(block));

        Block underBlock = block.getLocation().subtract(0, 1, 0).getBlock();
        if (ClassImport.getClasses().getArenaUtil().turnsToDirt(underBlock)) {
            match.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(underBlock));
        }
    }

    /**
     * Extracts match from item metadata.
     */
    protected Match getMatchFromItemMetadata(Item item) {
        if (!item.hasMetadata(HIDDEN_ITEM)) return null;

        MetadataValue metadataValue = BlockUtil.getMetadata(item, HIDDEN_ITEM);
        if (ListenerUtil.checkMetaData(metadataValue)) return null;
        if (!(metadataValue.value() instanceof Match)) return null;

        return (Match) metadataValue.value();
    }

    /**
     * Delegates event to ladder handle if available.
     *
     * @return true if event was handled by ladder
     */
    protected boolean delegateToLadderHandle(org.bukkit.event.Event event, Match match) {
        if (match.getLadder() instanceof LadderHandle ladderHandle) {
            return ladderHandle.handleEvents(event, match);
        }
        return false;
    }

    // ========== EVENT HANDLERS ==========

    protected static void arrowDisplayHearth(Player shooter, Player target, double finalDamage) {
        if (!PermanentConfig.DISPLAY_ARROW_HIT) return;
        if (shooter == null || target == null) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(shooter);
        if (match == null) return;

        if (match != MatchManager.getInstance().getLiveMatchByPlayer(target)) return;

        double health = NumberUtil.roundDouble((target.getHealth() - finalDamage) / 2);
        if (health <= 0) return;

        Common.sendMMMessage(shooter, LanguageManager.getString("MATCH.ARROW-HIT-PLAYER")
                .replace("%player%", target.getName())
                .replace("%health%", String.valueOf(health)));
    }


    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof ThrownExpBottle expBottle) {
            if (expBottle.getShooter() instanceof Player player) {
                Profile profile = ProfileManager.getInstance().getProfile(player);
                if (profile.getStatus().equals(ProfileStatus.MATCH)) {
                    Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                    if (match != null) {
                        if (!match.getLadder().isBuild()) {
                            Common.sendMMMessage(player, LanguageManager.getString("MATCH.ONLY-THROW-EXP-BOTTLES"));
                            e.setCancelled(true);
                        }
                    }
                }
            }
        }
    }


    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Entity entity = e.getEntity();
        MetadataValue mv = BlockUtil.getMetadata(entity, FIGHT_ENTITY);
        if (ListenerUtil.checkMetaData(mv)) return;

        if (!(mv.value() instanceof Match match)) return;

        if (match.getLadder() instanceof LadderHandle ladderHandle) {
            ladderHandle.handleEvents(e, match);
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Match match = getPlayerMatch(player);
        if (match == null) return;

        if (match.getCurrentStat(player).isSet()) {
            e.setCancelled(true);
            return;
        }

        RoundStatus roundStatus = match.getCurrentRound().getRoundStatus();
        if (!roundStatus.equals(RoundStatus.LIVE)) {
            ItemStack item = e.getItem();
            if (roundStatus.equals(RoundStatus.START) && item != null &&
                    (
                            item.getType().equals(Material.POTION) ||
                                    item.getType().equals(ClassImport.getClasses().getItemMaterialUtil().getSplashPotion()) ||
                                    item.getType().isEdible()
                    )) {
                e.setCancelled(false);
            }
        }

        delegateToLadderHandle(e, match);
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Match match = getPlayerMatch(player);
        if (match == null) return;

        if (!match.getLadder().isBuild()) {
            e.setCancelled(true);
            return;
        }

        if (ListenerUtil.cancelEvent(match, player)) {
            e.setCancelled(true);
            return;
        }

        if (e.getBlock().getType().equals(Material.FIRE)) {
            return;
        }

        delegateToLadderHandle(e, match);

        if (e.isCancelled()) return;

        Block block = e.getBlock();

        // Allow breaking blocks generated by liquid interaction (generators) regardless of height
        // These blocks have the PLACED_IN_FIGHT metadata from the BlockFormEvent
        if (block.hasMetadata(PLACED_IN_FIGHT)) {
            MetadataValue mv = BlockUtil.getMetadata(block, PLACED_IN_FIGHT);
            if (ListenerUtil.checkMetaData(mv)) {
                e.setCancelled(true);
                return;
            }

            // Block was placed/formed during the match, allow breaking
            trackPlacedBlock(block, match);
            return;
        }

        // For natural arena blocks or destroyable blocks, check build limits
        if (!isWithinBuildLimits(block, match, player)) {
            e.setCancelled(true);
            return;
        }

        // Handle destroyable blocks (beds, etc.)
        if (ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(match.getLadder(), block)) {
            BlockUtil.breakBlock(match, block);
        }

        e.setCancelled(true);
    }


    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Match match = getPlayerMatch(player);
        if (match == null) return;

        Ladder ladder = match.getLadder();
        if (!ladder.isBuild()) {
            e.setCancelled(true);
            return;
        }

        if (ListenerUtil.cancelEvent(match, player)) {
            e.setCancelled(true);
            return;
        }

        Block block = e.getBlockPlaced();
        if (!match.getArena().getCuboid().contains(block.getLocation())) {
            Common.sendMMMessage(player, LanguageManager.getString("MATCH.CANT-BUILD-OUTSIDE-ARENA"));
            e.setCancelled(true);
            return;
        }

        if (!isWithinBuildLimits(block, match, player)) {
            e.setCancelled(true);
            return;
        }

        if (delegateToLadderHandle(e, match)) {
            return;
        }

        if (!e.isCancelled()) {
            block.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(e));

            Block underBlock = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
            if (ClassImport.getClasses().getArenaUtil().turnsToDirt(underBlock))
                match.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(underBlock));
        }
    }


    // REMOVED: onLiquidFlow - Now handled by MatchTntListener.onBlockFromTo()
    // The old implementation had a bug where it only tracked non-solid blocks (!toBlock.getType().isSolid())
    // This caused cobblestone/obsidian from lava+water to not be tracked
    // The new MatchTntListener.onBlockFromTo() properly tracks ALL liquid flows


    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player player = e.getPlayer();
        Match match = getPlayerMatch(player);
        if (match == null) return;

        Ladder ladder = match.getLadder();
        Block block = e.getBlockClicked();

        if (!ladder.isBuild()) {
            e.setCancelled(true);
            return;
        }

        if (ListenerUtil.cancelEvent(match, player)) {
            e.setCancelled(true);
            return;
        }

        if (!match.getArena().getCuboid().contains(block.getLocation())) {
            Common.sendMMMessage(player, LanguageManager.getString("MATCH.CANT-BUILD-OUTSIDE-ARENA"));
            e.setCancelled(true);
            return;
        }

        if (!isWithinBuildLimits(block, match, player)) {
            e.setCancelled(true);
            return;
        }

        delegateToLadderHandle(e, match);
        if (e.isCancelled()) return;

        block.getRelative(e.getBlockFace()).setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), match));
        for (BlockFace face : BlockFace.values()) {
            Block relative = block.getRelative(face, 1);
            if (relative.hasMetadata(PLACED_IN_FIGHT)) {
                MetadataValue mv = BlockUtil.getMetadata(relative, PLACED_IN_FIGHT);
                if (ListenerUtil.checkMetaData(mv) || relative.getType().isSolid()) continue;

                relative.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), match));
                trackPlacedBlock(relative, match);
            }
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Match match = getPlayerMatch(player);
        if (match == null) return;

        RoundStatus roundStatus = match.getCurrentRound().getRoundStatus();
        BasicArena arena = match.getArena();
        Cuboid cuboid = arena.getCuboid();

        if ((match.getCurrentStat(player).isSet() || match.getCurrentRound().getTempKill(player) != null) && !arena.getCuboid().contains(e.getTo())) {
            if (roundStatus.equals(RoundStatus.LIVE))
                player.teleport(arena.getCuboid().getCenter());
            else
                match.teleportPlayer(player);

            return;
        }

        if (!roundStatus.equals(RoundStatus.LIVE) && !arena.getCuboid().contains(e.getTo())) {
            match.teleportPlayer(player);
            return;
        }

        if (!match.getLadder().isStartMove() && roundStatus.equals(RoundStatus.START)) {
            if (e.getTo().getX() != e.getFrom().getX() || e.getTo().getZ() != e.getFrom().getZ()) {
                player.teleport(e.getFrom());
                return;
            }
        }

        if (roundStatus.equals(RoundStatus.LIVE)) {
            int deadZone = cuboid.getLowerY();
            if (arena.isDeadZone())
                deadZone = arena.getDeadZoneValue();

            if (!match.getCurrentStat(player).isSet() && match.getCurrentRound().getTempKill(player) == null) {
                if (e.getTo().getBlockY() <= deadZone || !arena.getCuboid().contains(e.getTo())) {
                    match.killPlayer(player, null, DeathCause.VOID.getMessage());

                    if (!arena.getCuboid().contains(e.getTo()))
                        match.teleportPlayer(player);
                    return;
                }
            }
        }

        delegateToLadderHandle(e, match);
    }


    @EventHandler
    public void onCraft(CraftItemEvent e) {
        Player player = (Player) e.getWhoClicked();
        Match match = getPlayerMatch(player);
        if (match == null) return;

        if (!match.getLadder().getType().equals(LadderType.BUILD) || !match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.setCancelled(true);
            Common.sendMMMessage(player, LanguageManager.getString("MATCH.CANT-CRAFT"));
            return;
        }

        delegateToLadderHandle(e, match);
    }


    private static final String HIDDEN_ITEM = "ZPP_HIDDEN_ITEM";

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        if (ListenerUtil.cancelEvent(match, player)) {
            e.setCancelled(true);
            return;
        }

        if (delegateToLadderHandle(e, match)) {
            return;
        }

        Entity entity = e.getItemDrop();
        match.addEntityChange(entity);
        entity.setMetadata(HIDDEN_ITEM, new FixedMetadataValue(ZonePractice.getInstance(), match));
    }

    @EventHandler
    public void onTarget(EntityTargetEvent e) {
        if (!(e.getEntity() instanceof Item item1)) return;
        if (!(e.getTarget() instanceof Item item2)) return;

        Match match1 = getMatchFromItemMetadata(item1);
        Match match2 = getMatchFromItemMetadata(item2);

        if (match1 != match2) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        if (ListenerUtil.cancelEvent(match, player)) {
            e.setCancelled(true);
            return;
        }

        if (!ClassImport.getClasses().getEntityHider().canSee(player, e.getItem())) {
            e.setCancelled(true);
            return;
        }

        delegateToLadderHandle(e, match);
    }

    @EventHandler
    public void onGoldenAppleConsume(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        ItemStack item = e.getItem();
        if (item == null) return;

        delegateToLadderHandle(e, match);
    }

    @EventHandler
    public void onPlayerShootBow(EntityShootBowEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
            e.setCancelled(true);
            player.updateInventory();
            return;
        }

        if (match.getLadder() instanceof Bridges) {
            if (ConfigManager.getBoolean("MATCH-SETTINGS.LADDER-SETTINGS.BRIDGE.REGENERATING-ARROW.ENABLED")) {
                if (!PlayerCooldown.isActive(player, CooldownObject.BRIDGE_ARROW)) {
                    BridgeArrowRunnable bridgeArrowRunnable = new BridgeArrowRunnable(player, match);
                    bridgeArrowRunnable.begin();
                } else {
                    e.setCancelled(true);
                    player.updateInventory();
                }
            }
        }
    }

    /**
     * When a TNT block is primed (becomes a TNTPrimed entity), track its original position
     * for rollback. The block is already AIR at EntitySpawnEvent time, so we use the
     * Material.TNT override to ensure rollback restores TNT, not AIR.
     * Note: MatchTntListener also handles this for modern versions via TNTPrimeEvent (fires
     * before the block changes), but this covers the 1.8.8 path and acts as a fallback.
     */
    @EventHandler ( priority = EventPriority.MONITOR, ignoreCancelled = true )
    public void onTntPrimed(EntitySpawnEvent e) {
        if (!(e.getEntity() instanceof TNTPrimed)) return;

        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(e.getLocation()))
                .findFirst()
                .orElse(null);

        if (match == null) return;
        if (!match.getLadder().isBuild()) return;

        Block tntBlock = e.getLocation().getBlock();
        match.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(tntBlock, Material.TNT));
        // Also track the entity so rollback removes it even if it drifts outside the cuboid
        match.addEntityChange(e.getEntity());
    }

    /**
     * Tracks falling blocks (sand/gravel/concrete powder/etc.) for arena rollback.
     * <p>
     * This event fires BEFORE the block changes, so we capture the true original state.
     * Two cases are handled:
     * <ol>
     *   <li><b>Start falling</b> (block → AIR): the source block is still the correct material —
     *       record it so rollback knows to restore sand/gravel there. The entity is also registered
     *       via addEntityChange so rollback kills it even if it flies outside the cuboid.</li>
     *   <li><b>Landing</b> (AIR → block material): the source block is AIR; we schedule a
     *       1-tick delay so the landed block is written to the world before we capture it.
     *       Landing outside the cuboid is tracked too so rollback restores it to AIR.</li>
     * </ol>
     * Using LOWEST priority ensures the block in the world still holds its pre-change state
     * when we call createChangeBlock for the "start falling" case.
     */
    @EventHandler ( priority = EventPriority.LOWEST, ignoreCancelled = true )
    public void onFallingBlockChange(EntityChangeBlockEvent e) {
        if (!(e.getEntity() instanceof FallingBlock fallingBlock)) return;

        Block affectedBlock = e.getBlock();
        boolean isLanding = e.getTo() != Material.AIR;

        // Fast path: entity was tagged when it first started falling inside the arena
        if (fallingBlock.hasMetadata(PLACED_IN_FIGHT)) {
            MetadataValue mv = fallingBlock.getMetadata(PLACED_IN_FIGHT).get(0);
            if (!(mv.value() instanceof Match match)) return;

            if (isLanding) {
                // Landing: at LOWEST priority the block is still AIR — capture it NOW.
                // createChangeBlock(block, AIR) stores AIR as the restore target so rollback
                // removes the rogue sand block rather than keeping it.
                // Track even if landing outside the cuboid.
                if (!affectedBlock.hasMetadata(PLACED_IN_FIGHT)) {
                    match.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(affectedBlock, Material.AIR));
                }
            } else {
                // Start falling: block is still sand/gravel — capture original state now.
                // Register the entity so rollback removes it even if it escapes the cuboid.
                if (!affectedBlock.hasMetadata(PLACED_IN_FIGHT)) {
                    match.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(affectedBlock));
                }
                match.addEntityChange(fallingBlock);
            }
            return;
        }

        // Slow path: check if the affected location is inside any live build match
        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(affectedBlock.getLocation()))
                .findFirst()
                .orElse(null);

        if (match == null) return;
        if (!match.getLadder().isBuild()) return;

        // Tag the entity so the fast path works for the next change event (landing).
        // Register it so rollback removes it even if it escapes the cuboid.
        fallingBlock.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), match));
        match.addEntityChange(fallingBlock);

        if (isLanding) {
            // Landing: at LOWEST priority the block is still AIR — capture it NOW.
            if (!affectedBlock.hasMetadata(PLACED_IN_FIGHT)) {
                match.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(affectedBlock, Material.AIR));
            }
        } else {
            // Start falling: block is still sand/gravel — capture original state now
            if (!affectedBlock.hasMetadata(PLACED_IN_FIGHT)) {
                match.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(affectedBlock));
            }
        }
    }

}
