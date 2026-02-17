package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.fight.match.enums.MatchStatus;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.TNTPrimeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static dev.nandi0813.practice.util.PermanentConfig.PLACED_IN_FIGHT;

@SuppressWarnings ( "deprecation" )
// FixedMetadataValue is deprecated in Paper but still the standard way to set block metadata
public class MatchTntListener implements Listener {

    private void handleExplosion(Event event, List<Block> blockList, Match match) {
        if (match == null) {
            return;
        }

        Ladder ladder = match.getLadder();
        if (ladder instanceof LadderHandle) {
            ((LadderHandle) ladder).handleEvents(event, match);
        }

        blockList.removeIf(
                block -> !block.getType().equals(Material.TNT) &&
                        !block.hasMetadata(PLACED_IN_FIGHT) &&
                        !ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(match.getLadder(), block)
        );

        for (Block block : blockList) {
            match.addBlockChange(ClassImport.createChangeBlock(block));
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(e.getLocation()))
                .findFirst()
                .orElse(null);
        handleExplosion(e, e.blockList(), match);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(e.getBlock().getLocation()))
                .findFirst()
                .orElse(null);
        handleExplosion(e, e.blockList(), match);
    }

    private final Map<String, Integer> setFuseTick = new HashMap<>();

    private String getNormalizedLocationKey(Location loc) {
        return Objects.requireNonNull(loc.getWorld()).getName() + ":" + loc.getBlockX() + ":" + loc.getBlockY() + ":" + loc.getBlockZ();
    }

    @EventHandler
    public void onTntPrimeEvent(TNTPrimeEvent e) {
        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(e.getBlock().getLocation()))
                .findFirst()
                .orElse(null);

        if (match == null) {
            return;
        }

        if (!e.getCause().equals(TNTPrimeEvent.PrimeCause.EXPLOSION)) {
            String locationKey = getNormalizedLocationKey(e.getBlock().getLocation());
            setFuseTick.put(locationKey, match.getLadder().getTntFuseTime() * 20);
        }
    }

    @EventHandler
    public void onEntitySpawnEvent(EntitySpawnEvent e) {
        if (!(e.getEntity() instanceof TNTPrimed tntPrimed)) {
            return;
        }

        final String locationKey = getNormalizedLocationKey(tntPrimed.getLocation());
        if (setFuseTick.containsKey(locationKey)) {
            tntPrimed.setFuseTicks(setFuseTick.get(locationKey));
            setFuseTick.remove(locationKey);
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
            block.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(block));

            // Track the destination block
            Block destination = block.getRelative(e.getDirection());
            destination.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(ZonePractice.getInstance(), match));
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
            block.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(block));

            // Track the destination block
            Block destination = block.getRelative(e.getDirection());
            destination.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(ZonePractice.getInstance(), match));
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

        // Track cobblestone/obsidian/ice/concrete formation from water/lava/concrete powder
        // Schedule for 2 ticks to ensure the block has DEFINITELY formed
        org.bukkit.Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            // Get the block directly from the world at this location
            Block formedBlock = location.getBlock();

            // Skip if air or if already tracked
            if (formedBlock.getType() == Material.AIR) return;
            if (formedBlock.hasMetadata(PLACED_IN_FIGHT)) return;

            formedBlock.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(formedBlock));
        }, 2L); // Wait 2 ticks instead of running immediately
    }

    @EventHandler ( priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true )
    public void onBlockFromTo(org.bukkit.event.block.BlockFromToEvent e) {
        Block fromBlock = e.getBlock();
        Block toBlock = e.getToBlock();

        Spectatable spectatable = null;
        Match match = null;

        // Try to get Spectatable (Match/Event/FFA) from source block metadata (fast path - O(1))
        if (fromBlock.hasMetadata(PLACED_IN_FIGHT)) {
            org.bukkit.metadata.MetadataValue mv = fromBlock.getMetadata(PLACED_IN_FIGHT).get(0);
            if (mv.value() instanceof Spectatable) {
                spectatable = (Spectatable) mv.value();
                if (spectatable instanceof Match) {
                    match = (Match) spectatable;
                }
            }
        }

        // If source doesn't have metadata, search for match (slow path - only for natural flows)
        if (spectatable == null) {

            match = MatchManager.getInstance().getLiveMatches().stream()
                    .filter(m -> m.getCuboid().contains(fromBlock.getLocation()))
                    .findFirst()
                    .orElse(null);

            if (match == null) {
                return;
            }

            spectatable = match;

            if (!match.getLadder().isBuild()) {
                return;
            }

            // Mark source block for future flows from it
            fromBlock.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(ZonePractice.getInstance(), spectatable));
            match.addBlockChange(ClassImport.createChangeBlock(fromBlock));
        }

        // Only proceed with Match-specific logic if it's a Match
        if (match != null) {
            // Cancel liquid flow if match has ended
            if (match.getStatus().equals(MatchStatus.END)) {
                e.setCancelled(true);
                return;
            }

            // Delegate to ladder-specific handler if needed
            Ladder ladder = match.getLadder();
            if (ladder instanceof LadderHandle) {
                ((LadderHandle) ladder).handleEvents(e, match);
            }
        }

        // Always track the destination block
        if (!toBlock.hasMetadata(PLACED_IN_FIGHT)) {
            toBlock.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(ZonePractice.getInstance(), spectatable));
            spectatable.getFightChange().addBlockChange(ClassImport.createChangeBlock(toBlock));
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
            org.bukkit.Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
                if (newBlock.hasMetadata(PLACED_IN_FIGHT)) return; // Already tracked

                newBlock.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(ZonePractice.getInstance(), match));
                match.addBlockChange(ClassImport.createChangeBlock(newBlock));
            });
        }
    }

}