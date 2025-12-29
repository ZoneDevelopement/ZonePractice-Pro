package dev.nandi0813.practice.module.interfaces;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public abstract class ChangedBlock {

    @Getter
    public final Block block;
    @Getter
    public final Material material;
    @Getter
    public Location location;

    public ItemStack[] chestInventory;
    @Setter
    public BlockFace bedFace;

    protected ChangedBlock(final Block block) {
        this.block = block;
        this.location = block.getLocation();
        this.material = block.getType();

        saveChest(this.location);
        saveBed(this.location);
    }

    protected ChangedBlock(final BlockPlaceEvent e) {
        this.block = e.getBlockPlaced();
        this.location = block.getLocation();
        this.material = e.getBlockReplacedState().getType();
    }

    protected abstract void saveChest(Location loc);

    protected abstract void saveBed(Location loc);

    public abstract void reset();

}
