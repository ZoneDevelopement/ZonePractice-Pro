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
    public boolean requiresSupport(Block block) {
        Material type = block.getType();
        return org.bukkit.Tag.FLOWERS.isTagged(type)
                || org.bukkit.Tag.SAPLINGS.isTagged(type)
                || org.bukkit.Tag.CROPS.isTagged(type)
                || org.bukkit.Tag.WALL_POST_OVERRIDE.isTagged(type)   // torches, signs on walls, etc.
                || type == Material.DEAD_BUSH
                || type == Material.SHORT_GRASS
                || type == Material.TALL_GRASS
                || type == Material.FERN
                || type == Material.LARGE_FERN
                || type == Material.VINE
                || type == Material.SUGAR_CANE
                || type == Material.CACTUS
                || type == Material.SNOW
                || type == Material.TORCH
                || type == Material.SOUL_TORCH
                || type == Material.REDSTONE_WIRE
                || type == Material.REDSTONE_TORCH
                || type == Material.LEVER
                || type == Material.COMPARATOR
                || type == Material.REPEATER
                || type == Material.TRIPWIRE_HOOK
                || type == Material.TRIPWIRE
                || type == Material.LILY_PAD
                || type == Material.NETHER_WART;
    }

    @Override
    public void loadArenaChunks(BasicArena arena) {
        if (arena.getCuboid() == null) return;
        // getChunkAtAsync schedules real async chunk loading on the I/O thread —
        // no main-thread stall and no need to call chunk.load() manually.
        org.bukkit.World world = arena.getCuboid().getWorld();
        if (world == null) return;
        for (Chunk chunk : arena.getCuboid().getChunks()) {
            world.getChunkAtAsync(chunk.getX(), chunk.getZ());
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
