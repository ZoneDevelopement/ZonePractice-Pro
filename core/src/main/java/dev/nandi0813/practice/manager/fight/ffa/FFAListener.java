package dev.nandi0813.practice.manager.fight.ffa;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.DeathCause;
import dev.nandi0813.practice.manager.fight.util.ListenerUtil;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.NumberUtil;
import dev.nandi0813.practice.util.StringUtil;
import dev.nandi0813.practice.util.cooldown.CooldownObject;
import dev.nandi0813.practice.util.cooldown.GoldenAppleRunnable;
import dev.nandi0813.practice.util.cooldown.PlayerCooldown;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import static dev.nandi0813.practice.util.PermanentConfig.FIGHT_ENTITY;
import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

/**
 * FFA-specific event listener.
 *
 * <p>Block tracking / rollback events (break, place, piston, liquid, explosion, etc.)
 * are handled by the unified {@link dev.nandi0813.practice.manager.fight.listener.BuildBlockListener}.
 * This listener only handles player-specific FFA game logic (damage, movement, crafting, etc.)
 * and the build validation gates (cancel the event before MONITOR fires for BuildBlockListener).</p>
 */
public abstract class FFAListener implements Listener {

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
                ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(clickedBlock));
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

        if (!PlayerCooldown.isActive(player, CooldownObject.GOLDEN_APPLE)) {
            GoldenAppleRunnable goldenAppleRunnable = new GoldenAppleRunnable(player, ladder.getGoldenAppleCooldown());
            goldenAppleRunnable.begin();
        } else {
            e.setCancelled(true);

            Common.sendMMMessage(player, StringUtil.replaceSecondString(LanguageManager.getString("FFA.GAME.COOLDOWN.GOLDEN-APPLE"), PlayerCooldown.getLeftInDouble(player, CooldownObject.GOLDEN_APPLE)));
            player.updateInventory();
        }
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
            arrow.setMetadata(FIGHT_ENTITY, new FixedMetadataValue(ZonePractice.getInstance(), ffa));

            // Hide from every online player NOT in this FFA
            for (org.bukkit.entity.Player online : ZonePractice.getInstance().getServer().getOnlinePlayers()) {
                if (!ffa.getPlayers().containsKey(online) && !ffa.getSpectators().contains(online)) {
                    ClassImport.getClasses().getEntityHider().hideEntity(online, arrow);
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

    protected static void arrowDisplayHearth(Player shooter, Player target, double finalDamage) {
        if (!DISPLAY_ARROW_HIT) return;
        if (shooter == null || target == null) return;

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
     * Actual block tracking is done by {@link dev.nandi0813.practice.manager.fight.listener.BuildBlockListener}.
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

        // Blocks placed during the fight — allow breaking (tracking done by BuildBlockListener)
        if (block.hasMetadata(PLACED_IN_FIGHT)) {
            MetadataValue mv = BlockUtil.getMetadata(block, PLACED_IN_FIGHT);
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
                if (ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(ladder, block)) {
                    BlockUtil.breakBlock(ffa, block);
                }
            }
        }

        // When break-all-blocks is enabled on the player's current ladder, track the
        // natural arena block for rollback and allow the break.
        NormalLadder currentLadder = ffa.getPlayers().get(player);
        if (currentLadder != null && currentLadder.isBreakAllBlocks()) {
            ffa.getFightChange().addArenaBlockChange(ClassImport.createChangeBlock(block));
            return; // do NOT cancel — let the break happen
        }

        e.setCancelled(true);
    }

    /**
     * Validates FFA build rules (build enabled, arena boundary, build limits) and tags block.
     * Actual tracking is done by {@link dev.nandi0813.practice.manager.fight.listener.BuildBlockListener}.
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
        // Tagging and tracking handled by BuildBlockListener at MONITOR priority
    }

    /**
     * Validates FFA bucket rules and tags the target block.
     * Actual tracking is done by {@link dev.nandi0813.practice.manager.fight.listener.BuildBlockListener}.
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

        if (block.getLocation().getY() >= ListenerUtil.getCalculatedBuildLimit(ffa.getArena())) {
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
    public void onItemPickup(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        // Prevent picking up items (e.g. arrows) that have been hidden from this player
        if (!ClassImport.getClasses().getEntityHider().canSee(player, e.getItem())) {
            e.setCancelled(true);
            return;
        }

        e.setCancelled(false);
    }


}
