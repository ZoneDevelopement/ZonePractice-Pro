package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.material.Bed;
import org.bukkit.material.MaterialData;

public class ChangedBlock extends dev.nandi0813.practice.module.interfaces.ChangedBlock {

    private final MaterialData materialData;

    public ChangedBlock(final Block oldBlock) {
        super(oldBlock);
        this.materialData = oldBlock.getState().getData();
    }

    public ChangedBlock(final Block oldBlock, final Material originalMaterial) {
        super(oldBlock, originalMaterial);
        // Block is already AIR/changed; use the default MaterialData for the original material
        this.materialData = new org.bukkit.material.MaterialData(originalMaterial);
    }

    public ChangedBlock(final BlockPlaceEvent e) {
        super(e);
        this.materialData = e.getBlockReplacedState().getData();
    }

    protected void saveChest(Location loc) {
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

    protected void saveBed(Location loc) {
        Block block = loc.getBlock();

        if (block != null && block.getType() == Material.BED_BLOCK) {
            Bed bed = (Bed) block.getState().getData();
            if (bed.isHeadOfBed()) {
                this.location = block.getRelative(bed.getFacing().getOppositeFace(), 1).getLocation();
            }

            this.bedFace = bed.getFacing();
        }
    }

    public void reset() {
        if (location == null) return;

        if (bedFace != null) {
            ClassImport.getClasses().getBedUtil().placeBed(location, bedFace);
            return;
        }

        try {
            // Capture a single BlockState snapshot and apply all mutations to it before
            // committing — calling block.getState() multiple times returns independent
            // snapshots, so setData() on one has no effect on another's update() call.
            block.setType(material);
            org.bukkit.block.BlockState state = block.getState();
            state.setType(material);
            state.setData(materialData);
            state.update(true, false);

            if (chestInventory != null && block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                chest.getInventory().setContents(chestInventory);
            }
        } catch (IllegalArgumentException e) {
            // Handle MaterialData type incompatibilities (e.g., Tree, Torch)
            // Fall back to setting the material only, without custom MaterialData
            block.setType(material);
            org.bukkit.block.BlockState state = block.getState();
            state.setType(material);
            state.update(true, false);
        }
    }

}
