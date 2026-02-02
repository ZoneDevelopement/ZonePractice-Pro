package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.BasicItem;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class ArenaUtil implements dev.nandi0813.practice.module.interfaces.ArenaUtil {

    @Override
    public boolean turnsToDirt(Block block) {
        Material type = block.getType();
        return
                type.equals(Material.GRASS_BLOCK) ||
                        type.equals(Material.MYCELIUM) ||
                        type.equals(Material.DIRT_PATH) ||
                        type.equals(Material.FARMLAND) ||
                        type.equals(Material.WARPED_NYLIUM);
    }

    @Override
    public boolean containsDestroyableBlock(Ladder ladder, Block block) {
        if (!(ladder instanceof NormalLadder normalLadder)) return false;

        if (!ladder.isBuild()) return false;
        if (normalLadder.getDestroyableBlocks().isEmpty()) return false;
        if (block == null) return false;

        for (BasicItem basicItem : normalLadder.getDestroyableBlocks()) {
            if (block.getType().equals(basicItem.getMaterial()))
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

    @Override
    public void setArmorStandItemInHand(ArmorStand armorStand, ItemStack item, boolean rightHand) {
        if (armorStand == null) return;

        if (rightHand) {
            armorStand.setItem(EquipmentSlot.HAND, item);
        } else {
            armorStand.setItem(EquipmentSlot.OFF_HAND, item);
        }
    }

    @Override
    public void setArmorStandInvulnerable(ArmorStand armorStand) {
        if (armorStand == null) return;
        armorStand.setInvulnerable(true);
        // Make armor stands non-persistent so they don't survive server restarts
        // This prevents orphaned armor stands (markers and holograms) from appearing after restart
        armorStand.setPersistent(false);
    }

}
