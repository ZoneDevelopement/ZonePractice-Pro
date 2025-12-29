package dev.nandi0813.practice_modern.modern_version;

import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.interfaces.LadderHandle;
import dev.nandi0813.practice.module.util.ClassImport;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;

public class BlockExplode implements Listener {

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent e) {
        Match match = MatchManager.getInstance().getLiveMatches().stream().filter(m -> m.getCuboid().contains(e.getBlock().getLocation())).findFirst().orElse(null);

        if (match == null) {
            return;
        }

        Ladder ladder = match.getLadder();
        if (ladder instanceof LadderHandle ladderHandle) {
            ladderHandle.handleEvents(e, match);
        }

        if (!e.isCancelled()) {
            e.blockList().removeIf(block ->
                    block.getType() != Material.TNT &&
                            !ClassImport.getClasses().getArenaUtil().containsDestroyableBlock(match.getLadder(), block)
            );

            for (Block block : e.blockList()) {
                match.addBlockChange(ClassImport.createChangeBlock(block));
            }
        }
    }

}
