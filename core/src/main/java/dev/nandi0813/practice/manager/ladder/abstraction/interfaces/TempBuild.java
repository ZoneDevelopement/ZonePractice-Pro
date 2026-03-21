package dev.nandi0813.practice.manager.ladder.abstraction.interfaces;

import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.ChangedBlock;
import dev.nandi0813.practice.manager.fight.util.ListenerUtil;
import dev.nandi0813.practice.util.fightmapchange.BlockPosition;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public interface TempBuild {

    String TEMP_BUILD_BLOCK_ITEM = "ZONEPRACTICE_PRO_TEMP_BUILD_BLOCK_ITEM";

    static void onBucketEmpty(final @NotNull PlayerBucketEmptyEvent e, final @NotNull Match match, final int buildDelay) {
        if (e.isCancelled()) return;

        Player player = e.getPlayer();
        Block block = e.getBlockClicked();

        BlockUtil.setMetadata(block.getRelative(e.getBlockFace()), PLACED_IN_FIGHT, match);

        for (BlockFace face : BlockFace.values()) {
            Block relative = block.getRelative(face, 1);
            if (BlockUtil.hasMetadata(relative, PLACED_IN_FIGHT)) {
                Object mv = BlockUtil.getMetadata(relative, PLACED_IN_FIGHT, Object.class);
                if (ListenerUtil.checkMetaData(mv) || relative.getType().isSolid()) continue;

                match.getFightChange().addBlockChange(new ChangedBlock(block), player, buildDelay, e.getHand());

                Block b2 = block.getLocation().subtract(0, 1, 0).getBlock();
                if (ArenaUtil.turnsToDirt(b2))
                    match.getFightChange().addArenaBlockChange(new ChangedBlock(b2));
            }
        }
    }

    static void onBlockPlace(final @NotNull BlockPlaceEvent e, final @NotNull Match match, final int buildDelay) {
        if (e.isCancelled()) return;

        Player player = e.getPlayer();
        Block block = e.getBlockPlaced();

        BlockUtil.setMetadata(block, PLACED_IN_FIGHT, match);
        BlockUtil.setMetadata(block, TEMP_BUILD_BLOCK_ITEM, createPlacedReturnItem(e));

        match.getFightChange().addBlockChange(new ChangedBlock(e), player, buildDelay, e.getHand());

        Block block2 = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
        if (ArenaUtil.turnsToDirt(block2))
            match.getFightChange().addArenaBlockChange(new ChangedBlock(block2));
    }

    static void onBlockBreak(final @NotNull BlockBreakEvent e, final @NotNull Match match) {
        if (e.isCancelled()) return;

        Block block = e.getBlock();
        Location location = block.getLocation();
        FightChangeOptimized fightChange = match.getFightChange();

        if (!BlockUtil.hasMetadata(block, PLACED_IN_FIGHT)) return;

        Object mv = BlockUtil.getMetadata(e.getBlock(), PLACED_IN_FIGHT, Object.class);
        if (ListenerUtil.checkMetaData(mv)) {
            e.setCancelled(true);
            return;
        }

        long pos = BlockPosition.encode(location);
        FightChangeOptimized.BlockChangeEntry entry = fightChange.getBlocks().get(pos);
        if (entry != null && entry.getTempData() != null) {
            FightChangeOptimized.TempBlockData tempData = entry.getTempData();
            Player player = tempData.getPlayer();

            if (match.getPlayers().contains(player) && !match.getCurrentStat(player).isSet()) {
                e.setCancelled(true);
                tempData.reset(fightChange, entry.getChangedBlock(), pos);
            }
        }
    }

    private static @NotNull ItemStack createPlacedReturnItem(final @NotNull BlockPlaceEvent e) {
        ItemStack placedItem = e.getItemInHand();
        if (placedItem.getType().isAir()) {
            return new ItemStack(e.getBlockPlaced().getType(), 1);
        }

        ItemStack clone = placedItem.clone();
        clone.setAmount(1);
        return clone;
    }

}
