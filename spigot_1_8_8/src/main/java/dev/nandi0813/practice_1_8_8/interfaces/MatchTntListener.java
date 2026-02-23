package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

public class MatchTntListener implements Listener {

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Match match = MatchManager.getInstance().getLiveMatches().stream().filter(m -> m.getCuboid().contains(e.getLocation())).findFirst().orElse(null);

        if (match == null) {
            return;
        }

        Ladder ladder = match.getLadder();
        if (ladder instanceof LadderHandle) {
            ((LadderHandle) ladder).handleEvents(e, match);
        }

        if (!e.isCancelled()) {
            e.blockList().removeIf(block -> {
                if (block.getType().equals(Material.TNT)) return false;                     // keep → explodes
                if (ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(match.getLadder(), block)) return false; // keep → explodes
                if (block.hasMetadata(PLACED_IN_FIGHT)) return false;                       // keep → player placed → explodes
                if (block.getRelative(0, 1, 0).hasMetadata(PLACED_IN_FIGHT)) return true;  // remove → support under player block → protected
                return true;                                                                 // remove → pure arena block → protected
            });

            for (Block block : e.blockList())
                match.addBlockChange(ClassImport.createChangeBlock(block));
        }
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent e) {
        if (!(e.getEntity() instanceof TNTPrimed)) {
            return;
        }
        TNTPrimed tnt = (TNTPrimed) e.getEntity();

        Match match = MatchManager.getInstance().getLiveMatches().stream().filter(m -> m.getCuboid().contains(e.getLocation())).findFirst().orElse(null);
        if (match == null) {
            return;
        }

        if (e.isCancelled()) {
            return;
        }

        if (tnt.getSource() != null && tnt.getSource() instanceof Player) {
            tnt.setFuseTicks(20 * match.getLadder().getTntFuseTime());
        }
    }

    @EventHandler
    public void onBlockPistonExtend(org.bukkit.event.block.BlockPistonExtendEvent e) {
        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(e.getBlock().getLocation()))
                .findFirst()
                .orElse(null);

        if (match == null) return;
        if (!match.getLadder().isBuild()) {
            e.setCancelled(true);
            return;
        }

        // Track all blocks being pushed
        for (Block block : e.getBlocks()) {
            block.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(block));

            // Track the destination block
            Block destination = block.getRelative(e.getDirection());
            destination.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(destination));
        }
    }

    @EventHandler
    public void onBlockPistonRetract(org.bukkit.event.block.BlockPistonRetractEvent e) {
        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(e.getBlock().getLocation()))
                .findFirst()
                .orElse(null);

        if (match == null) return;
        if (!match.getLadder().isBuild()) {
            e.setCancelled(true);
            return;
        }

        // Track all blocks being pulled
        for (Block block : e.getBlocks()) {
            block.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(block));

            // Track the destination block
            Block destination = block.getRelative(e.getDirection());
            destination.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(destination));
        }
    }

    @EventHandler ( priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true )
    public void onBlockForm(org.bukkit.event.block.BlockFormEvent e) {
        Block block = e.getBlock();
        Location location = block.getLocation();

        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(location))
                .findFirst()
                .orElse(null);

        if (match == null) return;
        if (!match.getLadder().isBuild()) return;

        // Track cobblestone/obsidian/ice formation from water/lava
        // Schedule for 2 ticks to ensure the block has DEFINITELY formed
        org.bukkit.Bukkit.getScheduler().runTaskLater(dev.nandi0813.practice.ZonePractice.getInstance(), () -> {
            // Get the block directly from the world at this location
            Block formedBlock = location.getBlock();

            // Skip if air or if already tracked
            if (formedBlock.getType() == Material.AIR) return;
            if (formedBlock.hasMetadata(PLACED_IN_FIGHT)) return;

            formedBlock.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(formedBlock));
        }, 2L); // Wait 2 ticks instead of 1
    }

    @EventHandler ( priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true )
    public void onBlockFromTo(org.bukkit.event.block.BlockFromToEvent e) {
        Block fromBlock = e.getBlock();
        Block toBlock = e.getToBlock();

        Match match = null;

        // Try to get match from source block metadata (fast path - O(1))
        if (fromBlock.hasMetadata(PLACED_IN_FIGHT)) {
            org.bukkit.metadata.MetadataValue mv = fromBlock.getMetadata(PLACED_IN_FIGHT).get(0);
            if (mv.value() instanceof Match) {
                match = (Match) mv.value();
            }
        }

        // If source doesn't have metadata, search for match (slow path - only for natural flows)
        if (match == null) {
            match = MatchManager.getInstance().getLiveMatches().stream()
                    .filter(m -> m.getCuboid().contains(fromBlock.getLocation()))
                    .findFirst()
                    .orElse(null);

            if (match == null) return;
            if (!match.getLadder().isBuild()) return;

            // Mark source block for future flows from it
            fromBlock.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(fromBlock));
        }

        // Cancel liquid flow if match has ended
        if (match.getStatus().equals(MatchStatus.END)) {
            e.setCancelled(true);
            return;
        }

        // Delegate to ladder-specific handler if needed
        dev.nandi0813.practice.manager.ladder.abstraction.Ladder ladder = match.getLadder();
        if (ladder instanceof dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle) {
            ((dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle) ladder).handleEvents(e, match);
        }

        // If event was cancelled by ladder handler, don't track
        if (e.isCancelled()) return;

        // Always track the destination block
        if (!toBlock.hasMetadata(PLACED_IN_FIGHT)) {
            toBlock.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(toBlock));
        }
    }

    @EventHandler ( priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true )
    public void onBlockSpread(org.bukkit.event.block.BlockSpreadEvent e) {
        // The NEW block that was created by spreading
        Block newBlock = e.getNewState().getBlock();
        Block source = e.getSource();

        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(newBlock.getLocation()))
                .findFirst()
                .orElse(null);

        if (match == null) return;
        if (!match.getLadder().isBuild()) return;

        // Track spread blocks (fire, mushrooms, etc.) if they came from a tracked source
        if (source.hasMetadata(PLACED_IN_FIGHT)) {
            org.bukkit.Bukkit.getScheduler().runTask(dev.nandi0813.practice.ZonePractice.getInstance(), () -> {
                if (newBlock.hasMetadata(PLACED_IN_FIGHT)) return; // Already tracked

                newBlock.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
                match.addBlockChange(ClassImport.createChangeBlock(newBlock));
            });
        }
    }

}