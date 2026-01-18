package dev.nandi0813.practice.manager.ladder.abstraction.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.util.BlockUtil;
import dev.nandi0813.practice.manager.fight.util.ListenerUtil;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.fightmapchange.BlockPosition;
import dev.nandi0813.practice.util.fightmapchange.FightChangeOptimized;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public interface TempBuild {

    void setBuildDelay(int buildDelay);

    int getBuildDelay();

    static void onBucketEmpty(final @NotNull PlayerBucketEmptyEvent e, final @NotNull Match match, final int buildDelay) {
        if (e.isCancelled()) return;

        Player player = e.getPlayer();
        Block block = e.getBlockClicked();

        block.getRelative(e.getBlockFace()).setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), match));

        for (BlockFace face : BlockFace.values()) {
            Block relative = block.getRelative(face, 1);
            if (relative.hasMetadata(PLACED_IN_FIGHT)) {
                MetadataValue mv = BlockUtil.getMetadata(relative, PLACED_IN_FIGHT);
                if (ListenerUtil.checkMetaData(mv) || relative.getType().isSolid()) continue;

                match.getFightChange().addBlockChange(ClassImport.createChangeBlock(block), player, buildDelay);

                Block b2 = block.getLocation().subtract(0, 1, 0).getBlock();
                if (ClassImport.getClasses().getArenaUtil().turnsToDirt(b2))
                    match.getFightChange().addBlockChange(ClassImport.createChangeBlock(b2), player, buildDelay);
            }
        }
    }

    static void onBlockPlace(final @NotNull BlockPlaceEvent e, final @NotNull Match match, final int buildDelay) {
        if (e.isCancelled()) return;

        Player player = e.getPlayer();
        Block block = e.getBlockPlaced();

        block.setMetadata(PLACED_IN_FIGHT, new FixedMetadataValue(ZonePractice.getInstance(), match));

        match.getFightChange().addBlockChange(ClassImport.createChangeBlock(e), player, buildDelay);

        Block block2 = e.getBlockPlaced().getLocation().subtract(0, 1, 0).getBlock();
        if (ClassImport.getClasses().getArenaUtil().turnsToDirt(block2))
            match.getFightChange().addBlockChange(ClassImport.createChangeBlock(block2));
    }

    static void onBlockBreak(final @NotNull BlockBreakEvent e, final @NotNull Match match) {
        if (e.isCancelled()) return;

        Block block = e.getBlock();
        Location location = block.getLocation();
        FightChangeOptimized fightChange = match.getFightChange();

        if (!block.hasMetadata(PLACED_IN_FIGHT)) return;

        MetadataValue mv = BlockUtil.getMetadata(e.getBlock(), PLACED_IN_FIGHT);
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

}
