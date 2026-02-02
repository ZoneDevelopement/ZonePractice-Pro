package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.module.util.ClassImport;
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
            e.blockList().removeIf(block ->
                    !ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(match.getLadder(), block) &&
                            !block.getType().equals(Material.TNT) &&
                            !block.hasMetadata(PLACED_IN_FIGHT)
            );

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

    @EventHandler
    public void onBlockForm(org.bukkit.event.block.BlockFormEvent e) {
        Block block = e.getBlock();

        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(block.getLocation()))
                .findFirst()
                .orElse(null);

        if (match == null) return;
        if (!match.getLadder().isBuild()) return;

        // Track cobblestone/obsidian/ice formation from water/lava
        block.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
        match.addBlockChange(ClassImport.createChangeBlock(block));
    }

    @EventHandler
    public void onBlockSpread(org.bukkit.event.block.BlockSpreadEvent e) {
        Block block = e.getBlock();

        Match match = MatchManager.getInstance().getLiveMatches().stream()
                .filter(m -> m.getCuboid().contains(block.getLocation()))
                .findFirst()
                .orElse(null);

        if (match == null) return;
        if (!match.getLadder().isBuild()) return;

        // Track spread blocks (fire, etc.)
        Block source = e.getSource();
        if (source.hasMetadata(PLACED_IN_FIGHT)) {
            block.setMetadata(PLACED_IN_FIGHT, new org.bukkit.metadata.FixedMetadataValue(dev.nandi0813.practice.ZonePractice.getInstance(), match));
            match.addBlockChange(ClassImport.createChangeBlock(block));
        }
    }

}