package dev.nandi0813.practice.manager.fight.ffa;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.util.*;
import dev.nandi0813.practice.manager.fight.util.Stats.Statistic;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.NumberUtil;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;
import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

/**
 * FFA-specific event listener.
 *
 * <p>Block tracking / rollback events (break, place, piston, liquid, explosion, etc.)
 * are handled by the unified {@link dev.nandi0813.practice.manager.fight.listener.BuildListener}.
 * This listener only handles player-specific FFA game logic (damage, movement, crafting, etc.)
 * and the build validation gates (cancel the event before MONITOR fires for BuildListener).</p>
 */
public class FFAListener implements Listener {

    @EventHandler
    public void onRegen(EntityRegainHealthEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        if (ffa.getPlayers().get(player).isRegen()) return;
        if (e.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) return;

        e.setCancelled(true);
    }


    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        if (!ffa.getPlayers().get(player).isHunger()) {
            e.setFoodLevel(20);
        }
    }

    private static final boolean ENABLE_TNT = ConfigManager.getBoolean("FFA.ENABLE_TNT");

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;
        if (!action.equals(Action.RIGHT_CLICK_AIR) && !action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        Block clickedBlock = e.getClickedBlock();
        if (action.equals(Action.RIGHT_CLICK_BLOCK) && clickedBlock != null) {
            if (clickedBlock.getType().equals(Material.TNT)) {
                if (!ffa.isBuild() || !ENABLE_TNT) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (clickedBlock.getType().equals(Material.CHEST) || clickedBlock.getType().equals(Material.TRAPPED_CHEST)) {
                if (!ffa.isBuild()) return;
                ffa.getFightChange().addBlockChange(new ChangedBlock(clickedBlock));
            }
        }
    }

    @EventHandler
    public void onGoldenHeadConsume(PlayerItemConsumeEvent e) {
        Player player = e.getPlayer();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        ItemStack item = e.getItem();
        if (item == null) return;

        if (!item.getType().equals(Material.GOLDEN_APPLE)) return;

        Ladder ladder = ffa.getPlayers().get(player);
        if (ladder.getGoldenAppleCooldown() < 1) return;

        ModernItemCooldownHandler.handleGoldenApple(player, ladder.getGoldenAppleCooldown(), e);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player player)) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        if (ffa.isBuild()) {
            // Build FFAs: track all projectiles for entity rollback cleanup
            FightChangeOptimized fightChange = ffa.getFightChange();
            if (fightChange != null) fightChange.addEntityChange(e.getEntity());
        }

        // For arrows in any FFA (build or non-build): tag with FIGHT_ENTITY so
        // ProjectileLaunch won't remove them on ground-hit, hide from players in
        // other arenas, and schedule a 5-minute vanilla-style self-removal.
        if (e.getEntity() instanceof Arrow arrow) {
            BlockUtil.setMetadata(arrow, FIGHT_ENTITY, ffa);

            // Hide from every online player NOT in this FFA
            for (org.bukkit.entity.Player online : ZonePractice.getInstance().getServer().getOnlinePlayers()) {
                if (!ffa.getPlayers().containsKey(online) && !ffa.getSpectators().contains(online)) {
                    ZonePractice.getEntityHider().hideEntity(online, arrow);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent e) {
        FFA ffa = FFAManager.getInstance().getFFAByPlayer(e.getPlayer());
        if (ffa == null) return;

        if (!ffa.getArena().getCuboid().contains(e.getTo()))
            e.setCancelled(true);
    }

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        ffa.removePlayer(player);
    }

    private static final boolean DISPLAY_ARROW_HIT = ConfigManager.getBoolean("FFA.DISPLAY-ARROW-HIT-HEALTH");

    protected static void arrowDisplayHearth(Player shooter, Player target, double finalDamage, EntityDamageByEntityEvent event) {
        if (!DISPLAY_ARROW_HIT) return;
        if (shooter == null || target == null) return;
        if (event.isCancelled()) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(shooter);
        if (ffa == null) return;

        double health = NumberUtil.roundDouble((target.getHealth() - finalDamage) / 2);
        if (health <= 0) return;

        Common.sendMMMessage(shooter, LanguageManager.getString("FFA.GAME.ARROW-HIT-PLAYER")
                .replace("%player%", target.getName())
                .replace("%health%", String.valueOf(health)));
    }

    private static final boolean ALLOW_DESTROYABLE_BLOCK = ConfigManager.getBoolean("FFA.ALLOW-DESTROYABLE-BLOCK");

    /**
     * Validates FFA build rules (build enabled, build limits).
     * Actual block tracking is done by {@link dev.nandi0813.practice.manager.fight.listener.BuildListener}.
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        if (!ffa.isBuild()) {
            e.setCancelled(true);
            return;
        }

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
        if (e.getBlock().getLocation().getY() >= ListenerUtil.getCalculatedBuildLimit(ffa.getArena())) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.GAME.CANT-BUILD-OVER-LIMIT"));
            e.setCancelled(true);
            return;
        }

        // Handle destroyable blocks
        if (ALLOW_DESTROYABLE_BLOCK) {
            NormalLadder ladder = ffa.getPlayers().get(player);
            if (ladder != null) {
                if (ArenaUtil.containsDestroyableBlock(ladder, block)) {
                    BlockUtil.breakBlock(ffa, block);
                }
            }
        }

        // When break-all-blocks is enabled on the player's current ladder, track the
        // natural arena block for rollback and allow the break.
        NormalLadder currentLadder = ffa.getPlayers().get(player);
        if (currentLadder != null && currentLadder.isBreakAllBlocks()) {
            ffa.getFightChange().addArenaBlockChange(new ChangedBlock(block));
            return; // do NOT cancel — let the break happen
        }

        e.setCancelled(true);
    }

    /**
     * Validates FFA build rules (build enabled, arena boundary, build limits) and tags block.
     * Actual tracking is done by {@link dev.nandi0813.practice.manager.fight.listener.BuildListener}.
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        if (!ffa.isBuild()) {
            e.setCancelled(true);
            return;
        }

        Block block = e.getBlockPlaced();
        FFAArena arena = ffa.getArena();

        if (!arena.getCuboid().contains(block.getLocation())) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.GAME.CANT-BUILD-OUTSIDE-ARENA"));
            e.setCancelled(true);
            return;
        }

        if (block.getLocation().getY() >= ListenerUtil.getCalculatedBuildLimit(arena)) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.GAME.CANT-BUILD-OVER-LIMIT"));
            e.setCancelled(true);
        }
        // Tagging and tracking handled by BuildListener at MONITOR priority
    }

    /**
     * Validates FFA bucket rules and tags the target block.
     * Actual tracking is done by {@link dev.nandi0813.practice.manager.fight.listener.BuildListener}.
     */
    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        Player player = e.getPlayer();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        if (!ffa.isBuild()) {
            e.setCancelled(true);
            return;
        }

        Block block = e.getBlockClicked();
        if (!ffa.getArena().getCuboid().contains(block.getLocation())) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.GAME.CANT-BUILD-OUTSIDE-ARENA"));
            e.setCancelled(true);
            return;
        }

        if (block.getRelative(e.getBlockFace()).getLocation().getY() >= ListenerUtil.getCalculatedBuildLimit(ffa.getArena())) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.GAME.CANT-BUILD-OVER-LIMIT"));
            e.setCancelled(true);
        }
        // Liquid source block captured for rollback at MONITOR priority by AbstractBuildListener.onBucketEmpty
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        Cuboid cuboid = ffa.getArena().getCuboid();
        if (!cuboid.contains(e.getTo())) {
            ffa.killPlayer(player, null, DeathCause.VOID.getMessage());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent e) {
        Player player = (Player) e.getWhoClicked();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        if (!ffa.isBuild()) {
            e.setCancelled(true);
            Common.sendMMMessage(player, LanguageManager.getString("FFA.GAME.CANT-CRAFT"));
        }
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        // Prevent picking up items (e.g. arrows) that have been hidden from this player
        if (!ZonePractice.getEntityHider().canSee(player, e.getItem())) {
            e.setCancelled(true);
            return;
        }

        e.setCancelled(false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();

        Profile profile = ProfileManager.getInstance().getProfile(player);
        if (profile == null) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        e.setCancelled(true);

        DamageSource damageSource = e.getDamageSource();

        // Void deaths are already handled by onPlayerMove in the core FFAListener.
        // Skip here to avoid sending the death message twice.
        if (damageSource.getDamageType().equals(DamageType.OUT_OF_WORLD)) {
            return;
        }

        Player killer = resolveKiller(player, ffa, damageSource);

        DeathCause cause = FightUtil.convert(damageSource.getDamageType());
        ffa.killPlayer(player, killer, cause.getMessage().replace("%killer%", killer != null ? killer.getName() : "Unknown"));

        if (killer != null) {
            Statistic statistic = ffa.getStatistics().get(killer);
            statistic.setKills(statistic.getKills() + 1);
        }
    }

    private Player resolveKiller(Player victim, FFA ffa, DamageSource damageSource) {
        Player killer = null;

        if (damageSource.getCausingEntity() instanceof Entity damageEntity) {
            killer = FightUtil.getKiller(damageEntity);
        }

        // Bukkit keeps killer attribution for recent direct/projectile PvP.
        if (killer == null) {
            killer = victim.getKiller();
        }

        // Fallback for delayed environmental deaths (e.g. fatal fall after knockback).
        if (killer == null) {
            killer = ffa.getLastAttacker(victim);
        }

        if (killer != null && !ffa.getPlayers().containsKey(killer)) {
            return null;
        }

        return killer;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player target)) {
            return;
        }

        Profile profile = ProfileManager.getInstance().getProfile(target);
        if (profile == null) return;

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(target);
        if (ffa == null) return;

        // Resolve the attacker (direct hit or projectile shooter)
        Player attacker = null;
        if (e.getDamager() instanceof Player damager) {
            attacker = damager;
        } else if (e.getDamager() instanceof Projectile projectile) {
            if (projectile.getShooter() instanceof Player shooter) {
                attacker = shooter;

                if (projectile instanceof Arrow) {
                    arrowDisplayHearth(shooter, target, e.getFinalDamage(), e);
                }
            }
        }

        // Record the attacker for void-kill attribution
        if (attacker != null) {
            ffa.recordAttack(target, attacker);
        }
    }

}
