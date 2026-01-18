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
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.Objects;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

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
        if (!ffa.isBuild()) return;

        FightChangeOptimized fightChange = ffa.getFightChange();
        if (fightChange == null) return;

        fightChange.addEntityChange(e.getEntity());
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        FFA ffa = FFAManager.getInstance().getFFAByPlayer(player);
        if (ffa == null) return;

        if (!ffa.isBuild()) {
            e.setCancelled(true);
            return;
        }

        if (e.getBlock().getLocation().getY() >= ListenerUtil.getCalculatedBuildLimit(ffa.getArena())) {
            Common.sendMMMessage(player, LanguageManager.getString("FFA.GAME.CANT-BUILD-OVER-LIMIT"));

            e.setCancelled(true);
            return;
        }

        Block block = e.getBlock();
        if (ALLOW_DESTROYABLE_BLOCK) {
            if (!block.hasMetadata(PLACED_IN_FIGHT)) {
                NormalLadder ladder = ffa.getPlayers().get(player);
                if (ladder != null) {
                    if (ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(ladder, block)) {
                        BlockUtil.breakBlock(ffa, block);
                    }

                    e.setCancelled(true);
                    return;
                }
            }
        }

        MetadataValue mv = BlockUtil.getMetadata(block, PLACED_IN_FIGHT);
        if (ListenerUtil.checkMetaData(mv)) {
            e.setCancelled(true);
            return;
        }

        ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(block));

        Block underBlock = block.getLocation().subtract(0, 1, 0).getBlock();
        if (underBlock.getType() == Material.DIRT) {
            ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(underBlock));
        }
    }

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
            return;
        }

        if (!e.isCancelled()) {
            block.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), ffa));
            ffa.getFightChange().addBlockChange(Objects.requireNonNull(ClassImport.createChangeBlock(e)));

            Block underBlock = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
            if (ClassImport.getClasses().getArenaUtil().turnsToDirt(underBlock))
                ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(underBlock));
        }
    }

    @EventHandler
    public void onLiquidFlow(BlockFromToEvent e) {
        Block block = e.getBlock();

        if (!block.hasMetadata(PLACED_IN_FIGHT)) return;
        MetadataValue mv = BlockUtil.getMetadata(block, PLACED_IN_FIGHT);

        if (ListenerUtil.checkMetaData(mv)) return;
        if (!(mv.value() instanceof FFA ffa)) return;
        if (!ffa.isBuild()) return;

        Block toBlock = e.getToBlock();
        if (!toBlock.getType().isSolid()) {
            toBlock.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), ffa));
            ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(toBlock));

            Block underBlock = toBlock.getLocation().subtract(0, 1, 0).getBlock();
            if (ClassImport.getClasses().getArenaUtil().turnsToDirt(underBlock)) {
                ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(underBlock));
            }
        }
    }

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
            return;
        }

        block.getRelative(e.getBlockFace()).setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), ffa));

        for (BlockFace face : BlockFace.values()) {
            Block relative = block.getRelative(face, 1);
            if (relative.hasMetadata(PLACED_IN_FIGHT)) {
                MetadataValue mv = BlockUtil.getMetadata(relative, PLACED_IN_FIGHT);
                if (ListenerUtil.checkMetaData(mv) || relative.getType().isSolid()) continue;

                relative.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), ffa));
                ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(relative));

                Block underBlock = relative.getLocation().subtract(0, 1, 0).getBlock();
                if (ClassImport.getClasses().getArenaUtil().turnsToDirt(underBlock))
                    ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(underBlock));
            }
        }
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

        e.setCancelled(false);
    }

    private void handleExplosion(List<Block> blockList, FFA ffa) {
        if (ffa == null) {
            return;
        }

        if (!ffa.isBuild()) {
            return;
        }

        blockList.removeIf(
                block -> !block.getType().equals(Material.TNT) &&
                        !block.hasMetadata(PLACED_IN_FIGHT)
        );

        for (Block block : blockList) {
            ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(block));
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        FFA ffa = FFAManager.getInstance().getOpenFFAs().stream()
                .filter(m -> m.getCuboid().contains(e.getLocation()))
                .findFirst()
                .orElse(null);

        if (ffa != null && !ffa.isBuild()) {
            e.setCancelled(true);
            return;
        }

        handleExplosion(e.blockList(), ffa);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        FFA ffa = FFAManager.getInstance().getOpenFFAs().stream()
                .filter(m -> m.getCuboid().contains(e.getBlock().getLocation()))
                .findFirst()
                .orElse(null);

        if (ffa != null && !ffa.isBuild()) {
            e.setCancelled(true);
            return;
        }

        handleExplosion(e.blockList(), ffa);
    }


}
