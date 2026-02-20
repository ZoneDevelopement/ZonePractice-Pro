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
            block.setType(material);
            block.getState().setType(material);
            block.getState().setData(materialData);
            block.getState().update(false);

            if (chestInventory != null && block.getState() instanceof Chest) {
                Chest chest = (Chest) block.getState();
                chest.getInventory().setContents(chestInventory);
            }
        } catch (IllegalArgumentException e) {
            // Handle MaterialData type incompatibilities (e.g., Tree, Torch)
            // Just set the block type without the problematic material data
            block.setType(material);
            block.getState().setType(material);
            block.getState().update(false);
        }
    }

}
