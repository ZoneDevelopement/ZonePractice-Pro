package dev.nandi0813.practice_modern.interfaces;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.event.block.BlockPlaceEvent;

public class ChangedBlock extends dev.nandi0813.practice.module.interfaces.ChangedBlock {

    private final BlockData blockData;

    public ChangedBlock(Block block) {
        super(block);
        this.blockData = block.getBlockData();
    }

    public ChangedBlock(final BlockPlaceEvent e) {
        super(e);
        this.blockData = e.getBlockReplacedState().getBlockData();
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
            ClassImport.getClasses().getBedUtil().placeBed(location, bedFace);
            return;
        }

        Block currentBlock = location.getBlock();

        try {
            currentBlock.setType(material);
            currentBlock.setBlockData(blockData);
            currentBlock.getState().setType(material);
            currentBlock.getState().setBlockData(blockData);
            currentBlock.getState().update();

            if (chestInventory != null) {
                if (currentBlock.getState() instanceof Chest chest) {
                    chest.getInventory().setContents(chestInventory);
                    chest.update();
                }
            }
        } catch (Exception e) {
            // Handle BlockData compatibility issues
            // Just set the block type without the problematic block data
            currentBlock.setType(material);
            currentBlock.getState().setType(material);
            currentBlock.getState().update();
        }
    }

}
