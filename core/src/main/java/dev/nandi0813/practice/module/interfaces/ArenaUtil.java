package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.ItemStack;

public interface ArenaUtil {

    boolean turnsToDirt(Block block);

    boolean containsDestroyableBlock(Ladder ladder, Block block);

    /**
     * Returns {@code true} if {@code block} requires a solid block directly below it to exist
     * (e.g. dead bush, tall grass, sapling, flower, torch, etc.).
     * <p>
     * These blocks are NOT included in the TNT {@code blockList} by Minecraft, yet they
     * silently disappear when their support is destroyed by an explosion. We must track them
     * manually before the explosion fires so the rollback can restore them.
     */
    boolean requiresSupport(Block block);

    void loadArenaChunks(BasicArena arena);

    void setArmorStandItemInHand(ArmorStand armorStand, ItemStack item, boolean rightHand);

    void setArmorStandInvulnerable(ArmorStand armorStand);

}
