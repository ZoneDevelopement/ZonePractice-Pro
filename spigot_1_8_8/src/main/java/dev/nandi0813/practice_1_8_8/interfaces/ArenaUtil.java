package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.BasicItem;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class ArenaUtil implements dev.nandi0813.practice.module.interfaces.ArenaUtil {

    @Override
    public boolean turnsToDirt(Block block) {
        Material type = block.getType();
        return
                type.equals(Material.GRASS) ||
                        type.equals(Material.MYCEL) ||
                        type.equals(Material.DIRT) &&
                                block.getData() == 2;
    }

    @Override
    public boolean containsDestroyableBlock(Ladder ladder, Block block) {
        if (!(ladder instanceof NormalLadder)) return false;
        NormalLadder normalLadder = (NormalLadder) ladder;

        if (!ladder.isBuild()) return false;
        if (normalLadder.getDestroyableBlocks().isEmpty()) return false;
        if (block == null) return false;

        for (BasicItem basicItem : normalLadder.getDestroyableBlocks()) {
            ItemStack itemStack = block.getState().getData().toItemStack();
            if (basicItem.getMaterial().equals(itemStack.getType()) && basicItem.getDamage() == itemStack.getDurability())
                return true;
        }
        return false;
    }

    @Override
    public void loadArenaChunks(BasicArena arena) {
        if (arena.getCuboid() != null) {
            for (Chunk chunk : arena.getCuboid().getChunks()) {
                if (!chunk.isLoaded()) {
                    chunk.load(true);
                }
            }
        }
    }

}
