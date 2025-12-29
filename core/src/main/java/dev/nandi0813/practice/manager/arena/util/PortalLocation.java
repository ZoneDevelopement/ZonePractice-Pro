package dev.nandi0813.practice.manager.arena.util;

import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Cuboid;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PortalLocation {

    private final Location center;
    @Getter
    private final Cuboid cuboid;

    public PortalLocation(World world, double x, double y, double z) {
        center = new Location(world, x, y, z); // Center
        cuboid = new Cuboid(center.clone().add(1, 0, 1), center.clone().add(-1, 0, -1));
    }

    public PortalLocation(Location location) {
        this(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    public Location getCenter() {
        return this.center.clone();
    }

    public void setPortal() {
        Material material = ClassImport.getClasses().getItemMaterialUtil().getEndPortal();

        for (Block block : cuboid.getBlocks()) {
            block.setType(material);
            block.getState().update();
        }
    }

    public boolean isOverlap(PortalLocation pl) {
        for (Block block1 : cuboid.getBlocks()) {
            for (Block block2 : pl.getCuboid().getBlocks()) {
                if (block1.getX() == block2.getX() && block1.getZ() == block2.getZ())
                    return true;
            }
        }
        return false;
    }

    public boolean isIn(Player player) {
        return cuboid.contains(player.getLocation());
    }

    public boolean isInsidePortalProtection(Location location, int maxDistance) {
        return (center.distance(location) + 1) <= maxDistance;
    }

    public boolean isInsidePortalProtection(Block block, int maxDistance) {
        return (center.distance(block.getLocation()) + 1) <= maxDistance;
    }

}
