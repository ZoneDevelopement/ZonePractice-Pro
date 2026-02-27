package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.ladder.abstraction.Ladder;
import dev.nandi0813.practice.manager.ladder.abstraction.normal.NormalLadder;
import dev.nandi0813.practice.util.BasicItem;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
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
    public boolean requiresSupport(Block block) {
        switch (block.getType()) {
            case LONG_GRASS:        // tall grass / fern
            case DEAD_BUSH:
            case YELLOW_FLOWER:     // dandelion
            case RED_ROSE:          // all small flowers share this Material in 1.8.8
            case SAPLING:
            case TORCH:
            case REDSTONE_TORCH_ON:
            case REDSTONE_TORCH_OFF:
            case REDSTONE_WIRE:
            case LEVER:
            case STONE_BUTTON:
            case WOOD_BUTTON:
            case DIODE_BLOCK_ON:
            case DIODE_BLOCK_OFF:
            case REDSTONE_COMPARATOR_ON:
            case REDSTONE_COMPARATOR_OFF:
            case TRIPWIRE_HOOK:
            case TRIPWIRE:
            case SNOW:
            case SUGAR_CANE_BLOCK:
            case CROPS:             // wheat
            case CARROT:
            case POTATO:
            case NETHER_WARTS:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case CACTUS:
            case VINE:              // vines attach to the side of a block
            case WATER_LILY:
            case DOUBLE_PLANT:      // sunflower, lilac, rose bush, peony, tall grass (double)
                return true;
            default:
                return false;
        }
    }

    @Override
    public void loadArenaChunks(BasicArena arena) {
        if (arena.getCuboid() == null) return;
        // 1.8.8 has no async chunk-load API — stagger each chunk one tick apart so
        // the server never freezes trying to load all chunks in a single tick.
        org.bukkit.plugin.Plugin plugin = dev.nandi0813.practice.ZonePractice.getInstance();
        long delay = 0;
        for (Chunk chunk : arena.getCuboid().getChunks()) {
            final Chunk c = chunk;
            org.bukkit.Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!c.isLoaded()) {
                    c.load(true);
                }
            }, delay++);
        }
    }

    @Override
    public void setArmorStandItemInHand(ArmorStand armorStand, ItemStack item, boolean rightHand) {
        if (armorStand == null) return;

        // In 1.8.8, there's only one hand (right hand)
        armorStand.setItemInHand(item);
    }

    @Override
    public void setArmorStandInvulnerable(ArmorStand armorStand) {
        // 1.8.8 doesn't have setInvulnerable or setPersistent methods
        // We'll handle invulnerability through event cancellation instead
        // Armor stands in 1.8.8 are already non-persistent by default
    }

}
