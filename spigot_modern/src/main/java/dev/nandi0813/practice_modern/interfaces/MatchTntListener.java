package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.module.util.ClassImport;
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

}