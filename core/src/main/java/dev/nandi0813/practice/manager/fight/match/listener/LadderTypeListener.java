package dev.nandi0813.practice.manager.fight.match.listener;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.fight.event.events.duel.brackets.Brackets;
import dev.nandi0813.practice.manager.fight.event.interfaces.Event;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.RoundStatus;
import dev.nandi0813.practice.manager.fight.match.runnable.game.BridgeArrowRunnable;
import dev.nandi0813.practice.manager.fight.match.util.KnockbackUtil;
import dev.nandi0813.practice.manager.fight.match.util.MatchFightPlayer;
import dev.nandi0813.practice.manager.fight.match.util.TeamUtil;
import dev.nandi0813.practice.manager.fight.util.*;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.manager.ladder.enums.KnockbackType;
import dev.nandi0813.practice.manager.ladder.enums.LadderType;
import dev.nandi0813.practice.manager.ladder.type.Bridges;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.profile.enums.ProfileStatus;
import dev.nandi0813.practice.manager.spectator.SpectatorCrystalPlacementUtil;
import dev.nandi0813.practice.manager.spectator.SpectatorManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.NumberUtil;
import dev.nandi0813.practice.util.PermanentConfig;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;

import static dev.nandi0813.practice.manager.arena.util.ArenaUtil.containsDestroyableBlock;
import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;
import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public class LadderTypeListener implements Listener {

    private static final String AXE_LADDER_SETTINGS_PATH = "MATCH-SETTINGS.LADDER-SETTINGS.AXE";
    private static final String SHIELD_STUN_ENABLED_PATH = AXE_LADDER_SETTINGS_PATH + ".SHIELD-STUN.ENABLED";
    private static final String SHIELD_STUN_DURATION_PATH = AXE_LADDER_SETTINGS_PATH + ".SHIELD-STUN.DURATION-TICKS";
    private static final String SHIELD_STUN_REQUIRE_AXE_PATH = AXE_LADDER_SETTINGS_PATH + ".SHIELD-STUN.REQUIRE-AXE";
    private static final String SHIELD_SKIP_VANILLA_TICK_PATH = AXE_LADDER_SETTINGS_PATH + ".SKIP-VANILLA-DAMAGE-TICK-WHEN-SHIELD-BLOCKED";
    private static final int SKYWARS_KILLER_EXP_LEVEL_REWARD = 5;
    private static final int SKYWARS_ENCHANT_LAPIS_AMOUNT = 3;

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
     * Extracts match from item metadata.
     */
    protected Match getMatchFromItemMetadata(Item item) {
        if (!BlockUtil.hasMetadata(item, HIDDEN_ITEM)) return null;

        Match metadataValue = BlockUtil.getMetadata(item, HIDDEN_ITEM, Match.class);
        if (ListenerUtil.checkMetaData(metadataValue)) return null;
        return metadataValue;
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

    private static void registerMatchProjectile(AbstractArrow projectile, Match match) {
        // Tag match-owned arrow-like projectiles (arrow, spectral, trident) for tracking.
        if (!BlockUtil.hasMetadata(projectile, FIGHT_ENTITY)) {
            BlockUtil.setMetadata(projectile, FIGHT_ENTITY, match);
        }

        // Register for entity rollback cleanup and visibility hiding from other matches
        if (!match.getFightChange().containsEntity(projectile)) {
            match.addEntityChange(projectile);
        }
    }

    private static Match resolveProjectileMatch(Projectile projectile) {
        Match taggedMatch = BlockUtil.getMetadata(projectile, FIGHT_ENTITY, Match.class);
        if (!ListenerUtil.checkMetaData(taggedMatch)) {
            return taggedMatch;
        }

        if (projectile.getShooter() instanceof Player shooter) {
            Match shooterMatch = MatchManager.getInstance().getLiveMatchByPlayer(shooter);
            if (shooterMatch != null) {
                BlockUtil.setMetadata(projectile, FIGHT_ENTITY, shooterMatch);
            }
            return shooterMatch;
        }

        return null;
    }

    // ========== EVENT HANDLERS ==========

    protected static void arrowDisplayHearth(Player shooter, Player target, double finalDamage, EntityDamageByEntityEvent event) {
        if (!PermanentConfig.DISPLAY_ARROW_HIT) return;
        if (shooter == null || target == null) return;
        if (event.isCancelled()) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(shooter);
        if (match == null) return;

        if (match != MatchManager.getInstance().getLiveMatchByPlayer(target)) return;

        double health = NumberUtil.roundDouble((target.getHealth() - finalDamage) / 2);
        if (health <= 0) return;

        Common.sendMMMessage(shooter, LanguageManager.getString("MATCH.ARROW-HIT-PLAYER")
                .replace("%player%", target.getName())
                .replace("%health%", String.valueOf(health)));
    }

    private static boolean isAxe(Material material) {
        return switch (material) {
            case WOODEN_AXE, STONE_AXE, GOLDEN_AXE, IRON_AXE, DIAMOND_AXE, NETHERITE_AXE -> true;
            default -> false;
        };
    }

    private static boolean isShieldBlockedHit(EntityDamageByEntityEvent e, Player target) {
        ItemStack activeItem = target.getActiveItem();
        if (!target.isBlocking() || activeItem.getType() != Material.SHIELD) {
            return false;
        }

        // A blocked shield hit should not deal HP damage.
        return e.getFinalDamage() <= 0.0D;
    }

    private static void enforceShieldDamageTickBypass(Player target) {
        if (!ConfigManager.getConfig().getBoolean(SHIELD_SKIP_VANILLA_TICK_PATH, true)) {
            return;
        }

        // Run one tick later so vanilla cannot restore the damage immunity window.
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
            if (!target.isOnline() || target.isDead()) {
                return;
            }
        });
    }

    private static void applyCustomShieldStunIfNeeded(EntityDamageByEntityEvent e, Player attacker, Player target) {
        if (!(e.getDamager() instanceof Player)) {
            return;
        }

        if (!ConfigManager.getConfig().getBoolean(SHIELD_STUN_ENABLED_PATH, true)) {
            return;
        }

        boolean requireAxe = ConfigManager.getConfig().getBoolean(SHIELD_STUN_REQUIRE_AXE_PATH, true);
        if (requireAxe && !isAxe(attacker.getInventory().getItemInMainHand().getType())) {
            return;
        }

        int durationTicks = Math.max(0, ConfigManager.getConfig().getInt(SHIELD_STUN_DURATION_PATH));
        if (durationTicks == 0) {
            return;
        }

        target.setCooldown(Material.SHIELD, durationTicks);
    }

    private static boolean isSkyWarsLiveMatch(Match match) {
        return match.getLadder().getType() == LadderType.SKYWARS
                && match.getCurrentRound().getRoundStatus() == RoundStatus.LIVE;
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

        // Tag snowballs launched in a Spleef-snowball-mode match so onProjectileHit
        // can route them to Spleef.handleEvents for snow-block destruction.
        if (e.getEntity() instanceof org.bukkit.entity.Snowball snowball) {
            if (snowball.getShooter() instanceof Player player) {
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                if (match != null && match.getLadder() instanceof dev.nandi0813.practice.manager.ladder.type.Spleef spleef
                        && spleef.isSnowballMode()) {
                    BlockUtil.setMetadata(snowball, FIGHT_ENTITY, match);
                }
            }
        }

        if (e.getEntity() instanceof AbstractArrow projectile && projectile.getShooter() instanceof Player player) {
            Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
            if (match != null) {
                registerMatchProjectile(projectile, match);
            }
        }
    }


    @EventHandler
    public void onProjectileHit(ProjectileHitEvent e) {
        Projectile projectile = e.getEntity();
        Match match = resolveProjectileMatch(projectile);
        if (match == null) {
            return;
        }

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
        if (roundStatus.equals(RoundStatus.LIVE) && match.getLadder().isBuild()) {
            SpectatorCrystalPlacementUtil.clearSpectatorsBlockingCrystalPlacement(e, match.getArena().getCuboid());
        }

        if (!roundStatus.equals(RoundStatus.LIVE)) {
            ItemStack item = e.getItem();
            if (roundStatus.equals(RoundStatus.START) && item != null &&
                    (
                            item.getType().equals(Material.POTION) ||
                                    item.getType().equals(Material.SPLASH_POTION) ||
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

        // Blocks placed during the fight — allow breaking (tracking done by BuildListener)
        if (BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) {
            Object mv = BlockUtil.getMetadata(block, PLACED_IN_FIGHT, Object.class);
            if (ListenerUtil.checkMetaData(mv)) {
                e.setCancelled(true);
            }
            return;
        }

        // For natural arena blocks or destroyable blocks, check build limits
        if (!isWithinBuildLimits(block, match, player)) {
            e.setCancelled(true);
            return;
        }

        // Handle destroyable blocks (beds, etc.)
        if (containsDestroyableBlock(match.getLadder(), block)) {
            BlockUtil.breakBlock(match, block);
        }

        // When break-all-blocks is enabled, track the natural arena block for rollback
        // and allow the break — the player can destroy any block in the arena.
        if (match.getLadder().isBreakAllBlocks()) {
            match.getFightChange().addArenaBlockChange(new ChangedBlock(block));
            return; // do NOT cancel — let the break happen
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

        delegateToLadderHandle(e, match);
        // Tagging and tracking handled by BuildListener at MONITOR priority
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

        if (!isWithinBuildLimits(block.getRelative(e.getBlockFace()), match, player)) {
            e.setCancelled(true);
            return;
        }

        delegateToLadderHandle(e, match);
        // Liquid source block captured for rollback at MONITOR priority by AbstractBuildListener.onBucketEmpty
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        Match match = getPlayerMatch(player);
        if (match == null) return;

        // Freeze players on their spawn position while the arena is regenerating
        // between rounds. Any horizontal movement is snapped back instantly.
        if (match.isRollingBack()) {
            if (e.getTo().getX() != e.getFrom().getX() || e.getTo().getZ() != e.getFrom().getZ()) {
                match.teleportPlayer(player);
            }
            return;
        }

        RoundStatus roundStatus = match.getCurrentRound().getRoundStatus();
        BasicArena arena = match.getArena();
        Cuboid cuboid = arena.getCuboid();
        boolean inRoundEndDelay = roundStatus.equals(RoundStatus.END) && match.getLadder().getRoundEndDelay() > 0;
        boolean allowEndDelayTeleport = !inRoundEndDelay || match.wasLastDeathVoid(player);

        if ((match.getCurrentStat(player).isSet() || match.getCurrentRound().getTempKill(player) != null) && !arena.getCuboid().contains(e.getTo())) {
            if (roundStatus.equals(RoundStatus.LIVE))
                player.teleport(arena.getCuboid().getCenter());
            else if (allowEndDelayTeleport)
                match.teleportPlayer(player);

            return;
        }

        if (!roundStatus.equals(RoundStatus.LIVE) && !arena.getCuboid().contains(e.getTo())) {
            if (allowEndDelayTeleport)
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

        boolean canCraftInLadder = match.getLadder().getType() == LadderType.BUILD || match.getLadder().getType() == LadderType.SKYWARS;
        if (!canCraftInLadder || !match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) {
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
        BlockUtil.setMetadata(entity, HIDDEN_ITEM, match);
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
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        if (ListenerUtil.cancelEvent(match, player)) {
            e.setCancelled(true);
            return;
        }

        if (!ZonePractice.getEntityHider().canSee(player, e.getItem())) {
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

        delegateToLadderHandle(e, match);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player player)) {
            return;
        }

        Match match = getPlayerMatch(player);
        if (match == null || !isSkyWarsLiveMatch(match)) {
            return;
        }

        if (e.getInventory() instanceof EnchantingInventory enchantingInventory) {
            enchantingInventory.setSecondary(new ItemStack(Material.LAPIS_LAZULI, SKYWARS_ENCHANT_LAPIS_AMOUNT));
        }
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
                    return;
                }
            }
        }

        // Tag the arrow so ProjectileLaunch won't immediately remove it on ground-hit,
        // register it for rollback cleanup (and hiding from players in other matches),
        // and schedule a 5-minute vanilla-style self-removal.
        if (e.getProjectile() instanceof AbstractArrow projectile) {
            registerMatchProjectile(projectile, match);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        if (ListenerUtil.cancelEvent(match, player)) {
            e.setDamage(0);
            e.setCancelled(true);
            return;
        }

        if (e instanceof EntityDamageByEntityEvent damageByEntityEvent) {
            if (shouldDelegateExplosiveByEntityDamageToLadder(damageByEntityEvent, match)) {
                if (match.getLadder() instanceof LadderHandle ladderHandle) {
                    ladderHandle.handleEvents(e, match);
                }
                return;
            }

            onEntityDamageByEntity(damageByEntityEvent);
            return;
        }

        if (match.getLadder() instanceof LadderHandle ladderHandle) {
            ladderHandle.handleEvents(e, match);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
        if (match == null) return;

        e.setCancelled(true);

        DamageSource damageSource = e.getDamageSource();
        Player killer;
        if (damageSource.getCausingEntity() instanceof Entity damageEntity) {
            killer = FightUtil.getKiller(damageEntity);
        } else {
            killer = null;
        }

        DeathCause cause = FightUtil.convert(damageSource.getDamageType());
        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
                match.killPlayer(player, killer, cause.getMessage().replace("%killer%", killer != null ? killer.getName() : "Unknown")), 1L);

        if (killer != null) {
            Statistic statistic = match.getCurrentStat(killer);
            if (statistic != null) {
                statistic.setKills(statistic.getKills() + 1);
            }

            if (match.getLadder().getType() == LadderType.SKYWARS) {
                killer.giveExpLevels(SKYWARS_KILLER_EXP_LEVEL_REWARD);
            }
        }
    }

    private static void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player target)) return;

        Player attacker = null;
        if (e.getDamager() instanceof Player) {
            attacker = (Player) e.getDamager();
        } else if (e.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player) {
                attacker = (Player) projectile.getShooter();

                if (projectile instanceof Arrow) {
                    arrowDisplayHearth(attacker, target, e.getFinalDamage(), e);
                }
            }
        }

        if (attacker == null) return;

        Profile attackerProfile = ProfileManager.getInstance().getProfile(attacker);
        Profile targetProfile = ProfileManager.getInstance().getProfile(target);

        if (attackerProfile == null || targetProfile == null) return;

        if (!attackerProfile.getStatus().equals(ProfileStatus.MATCH)) return;
        if (!targetProfile.getStatus().equals(ProfileStatus.MATCH)) return;

        Match match = MatchManager.getInstance().getLiveMatchByPlayer(attacker);
        Match targetMatch = MatchManager.getInstance().getLiveMatchByPlayer(target);
        if (match == null || match != targetMatch) {
            e.setCancelled(true);
            return;
        }

        if (!match.getCurrentRound().getRoundStatus().equals(RoundStatus.LIVE)) return;

        Statistic attackerStat = match.getCurrentStat(attacker);
        Statistic targetStat = match.getCurrentStat(target);
        if (attackerStat == null || targetStat == null) {
            e.setCancelled(true);
            return;
        }

        boolean cancel = attackerStat.isSet() || targetStat.isSet();

        if (!cancel) {
            cancel = TeamUtil.isSaveTeamMate(match, attacker, target);
        }

        if (cancel) {
            e.setCancelled(true);
            return;
        } else {
            if (match.getLadder() instanceof LadderHandle ladderHandle) {
                ladderHandle.handleEvents(e, match);
            }
        }

        // Always record the attacker for void-kill attribution,
        // regardless of whether the event was cancelled by a ladder handler.
        match.recordAttack(target, attacker);

        boolean shieldBlocked = isShieldBlockedHit(e, target);
        if (shieldBlocked) {
            enforceShieldDamageTickBypass(target);
            applyCustomShieldStunIfNeeded(e, attacker, target);
        }

        if (!e.isCancelled() && !match.getLadder().getLadderKnockback().getKnockbackType().equals(KnockbackType.DEFAULT)) {
            if (shieldBlocked) {
                // Shield blocks should recoil the attacker, not launch the blocker forward.
                KnockbackUtil.setPlayerKnockback(attacker, target, match.getLadder().getLadderKnockback().getKnockbackType());
            } else {
                KnockbackUtil.setPlayerKnockback(target, attacker, match.getLadder().getLadderKnockback().getKnockbackType());
            }
        }
    }

    @EventHandler
    public void onExpPickup(PlayerPickupExperienceEvent e) {
        Player player = e.getPlayer();

        if (SpectatorManager.getInstance().getSpectators().containsKey(player)) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent e) {
        if (!(e.getTarget() instanceof Player player)) {
            return;
        }

        if (SpectatorManager.getInstance().getSpectators().containsKey(player)) {
            e.setCancelled(true);
        }
    }

    /**
     * Prevents players from swapping items in their hands before they have chosen a kit.
     */
    @EventHandler
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus() == ProfileStatus.MATCH) {
            Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
            if (match != null) {
                MatchFightPlayer matchFightPlayer = match.getMatchPlayers().get(player);
                if (!matchFightPlayer.isHasChosenKit()) {
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * Prevents players from swapping items in their hands before they have chosen a kit.
     * Or when they are in the kit editor.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) {
            return;
        }
        Profile profile = ProfileManager.getInstance().getProfile(player);

        if (profile.getStatus() == ProfileStatus.MATCH) {
            Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
            if (match != null && isSkyWarsLiveMatch(match)) {
                InventoryType topType = e.getView().getTopInventory().getType();
                if (topType == InventoryType.CRAFTING || topType == InventoryType.WORKBENCH || topType == InventoryType.ENCHANTING) {
                    return;
                }
            }
        }

        if (e.getAction().equals(InventoryAction.HOTBAR_SWAP)) {
            switch (profile.getStatus()) {
                case MATCH -> {
                    Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                    if (match != null) {
                        MatchFightPlayer matchFightPlayer = match.getMatchPlayers().get(player);
                        if (!matchFightPlayer.isHasChosenKit()) {
                            e.setCancelled(true);
                        }
                    }
                }
                case EDITOR -> e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onWindCharge(ProjectileLaunchEvent e) {
        if (!(e.getEntity() instanceof WindCharge)) {
            return;
        }

        if (!(e.getEntity().getShooter() instanceof Player player)) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(player);
        switch (profile.getStatus()) {
            case MATCH -> {
                Match match = MatchManager.getInstance().getLiveMatchByPlayer(player);
                if (match != null && !match.getLadder().isBuild()) {
                    Common.sendMMMessage(player, LanguageManager.getString("MATCH.ONLY-CHARGE-WIND"));
                    e.setCancelled(true);
                }
            }
            case EVENT -> {
                Event event = EventManager.getInstance().getEventByPlayer(player);
                if (event instanceof Brackets) {
                    Common.sendMMMessage(player, LanguageManager.getString("MATCH.ONLY-CHARGE-WIND"));
                    e.setCancelled(true);
                }
            }
        }
    }

    private static boolean shouldDelegateExplosiveByEntityDamageToLadder(EntityDamageByEntityEvent event, Match match) {
        Entity damager = event.getDamager();
        if (!(damager instanceof TNTPrimed) && !(damager instanceof Fireball)) {
            return false;
        }

        LadderType ladderType = match.getLadder().getType();
        return ladderType == LadderType.FIREBALL_FIGHT || ladderType == LadderType.TNT_SUMO;
    }

}
