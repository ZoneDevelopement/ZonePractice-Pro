package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import org.bukkit.block.Block;

public interface ArenaUtil {

    boolean turnsToDirt(Block block);

    boolean containsDestroyableBlock(Ladder ladder, Block block);

    void loadArenaChunks(BasicArena arena);

}
