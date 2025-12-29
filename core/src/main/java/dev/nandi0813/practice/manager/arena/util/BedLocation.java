package dev.nandi0813.practice.manager.arena.util;

import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

@Getter
public class BedLocation extends Location {

    private final BlockFace facing;

    public BedLocation(World world, double x, double y, double z, BlockFace facing) {
        super(world, x, y, z);
        this.facing = facing;
    }

    public Location getLocation() {
        return new Location(getWorld(), getX(), getY(), getZ());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BedLocation bedLocation)) return false;

        return
                this.getX() == bedLocation.getX() &&
                        this.getY() == bedLocation.getY() &&
                        this.getZ() == bedLocation.getZ() &&
                        this.getWorld() == bedLocation.getWorld() &&
                        this.getFacing() == bedLocation.getFacing();
    }

}
