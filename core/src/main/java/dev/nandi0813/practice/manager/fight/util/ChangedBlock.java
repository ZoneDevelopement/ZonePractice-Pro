package dev.nandi0813.practice.manager.fight.util;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class ChangedBlock {

    @Getter
    public final Block block;
    @Getter
    public final Material material;
    @Getter
    public Location location;

    public ItemStack[] chestInventory;
    @Setter
    public BlockFace bedFace;

    private final BlockData blockData;

    public ChangedBlock(final Block block) {
        this.block = block;
        this.location = block.getLocation();
        this.material = block.getType();

        saveChest(this.location);
        saveBed(this.location);
        this.blockData = block.getBlockData();
    }

    /**
     * Constructor for when the block has already changed in the world but we know its original material.
     * Used for TNT blocks that became TNTPrimed entities before the explosion event fires.
     */
    public ChangedBlock(final Block block, final Material originalMaterial) {
        this.block = block;
        this.location = block.getLocation();
        this.material = originalMaterial;
        // No chest/bed state to save — the block is already gone
        this.blockData = org.bukkit.Bukkit.createBlockData(originalMaterial);
    }

    public ChangedBlock(final BlockState replacedState) {
        this.block = replacedState.getBlock();
        this.location = replacedState.getLocation();
        this.material = replacedState.getType();

        BlockState snapshot = replacedState.getBlock().getState();
        if (snapshot.getType() != replacedState.getType()) {
            snapshot = replacedState;
        }

        if (snapshot instanceof Chest chest) {
            chestInventory = chest.getInventory().getContents().clone();
        }

        if (snapshot.getBlockData() instanceof Bed bed) {
            bedFace = bed.getFacing();

            if (bed.getPart().equals(Bed.Part.HEAD)) {
                this.location = this.block.getRelative(bedFace.getOppositeFace(), 1).getLocation();
            }
        }

        this.blockData = replacedState.getBlockData().clone();
    }

    public ChangedBlock(final BlockPlaceEvent e) {
        this(e.getBlockReplacedState());
    }

    private void saveChest(Location loc) {
        try {
            Block block = loc.getBlock();

            if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
                Chest chest = (Chest) block.getState();
                chestInventory = chest.getInventory().getContents().clone();
            }
        } catch (Exception e) {
            Common.sendConsoleMMMessage(LanguageManager.getString("ARENA.ARENA-REGEN-FAILED-CHEST"));
        }
    }

    private void saveBed(Location loc) {
        Block block = loc.getBlock();

        if (block.getType().toString().contains("_BED")) {
            Bed bed = (Bed) block.getBlockData();
            bedFace = bed.getFacing();

            if (bed.getPart().equals(Bed.Part.HEAD)) {
                this.location = block.getRelative(bedFace.getOppositeFace(), 1).getLocation();
            }
        }
    }

    public void reset() {
        if (location == null) return;

        if (bedFace != null) {
            BedUtil.placeBed(location, bedFace);
            return;
        }

        Block currentBlock = location.getBlock();

        try {
            // Set the block data directly - this is the primary method
            currentBlock.setBlockData(blockData, false);

            // Handle chest inventory if present
            if (chestInventory != null) {
                BlockState state = currentBlock.getState();
                if (state instanceof Chest chest) {
                    chest.getInventory().setContents(chestInventory);
                    chest.update(true, false);
                }
            }
        } catch (Exception e) {
            // Handle BlockData compatibility issues
            // Just set the block type without the problematic block data
            try {
                currentBlock.setBlockData(material.createBlockData(), false);
            } catch (Exception ex) {
                // Ultimate fallback - just set material
                currentBlock.setType(material, false);
            }
        }
    }

}
