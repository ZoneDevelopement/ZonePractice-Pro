package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.ffa.game.FFA;
import dev.nandi0813.practice.manager.fight.match.Match;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.interfaces.Spectatable;
import org.bukkit.block.Block;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.metadata.Metadatable;

public enum BlockUtil {
    ;

    public static void breakBlock(Match match, Block block) {
        if (match == null) return;

        match.addBlockChange(ClassImport.createChangeBlock(block));
        block.breakNaturally();
    }

    public static void breakBlock(FFA ffa, Block block) {
        if (ffa == null) return;

        ffa.getFightChange().addBlockChange(ClassImport.createChangeBlock(block));
        block.breakNaturally();
    }

    /**
     * Dispatches to the correct {@code breakBlock} overload based on the runtime type of
     * the {@link Spectatable} (Match or FFA). Tracks the block for rollback and breaks it.
     */
    public static void breakBlock(Spectatable spectatable, Block block) {
        if (spectatable instanceof Match match) {
            breakBlock(match, block);
        } else if (spectatable instanceof FFA ffa) {
            breakBlock(ffa, block);
        }
    }

    public static MetadataValue getMetadata(Metadatable metadatable, String tag) {
        for (MetadataValue mv : metadatable.getMetadata(tag)) {
            if (mv != null && mv.getOwningPlugin() == ZonePractice.getInstance()) {
                return mv;
            }
        }
        return null;
    }

}
