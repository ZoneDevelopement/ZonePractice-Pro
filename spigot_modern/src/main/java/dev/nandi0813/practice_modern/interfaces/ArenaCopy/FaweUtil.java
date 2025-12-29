package dev.nandi0813.practice_modern.interfaces.ArenaCopy;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BlockTypes;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.Location;

public enum FaweUtil {
    ;

    public static void deleteFAWE(final Cuboid cuboid) {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(cuboid.getWorld()))) {
            editSession.setBlocks(
                    (Region) new CuboidRegion(
                            BukkitAdapter.adapt(cuboid.getLowerNE()).toBlockPoint(),
                            BukkitAdapter.adapt(cuboid.getUpperSW()).toBlockPoint()
                    ),
                    BlockTypes.AIR
            );
            editSession.flushQueue();
        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>Error during delete operation: " + e.getMessage());
        }
    }

    public static void copyFAWE(final Cuboid copyFrom, final Location reference, final Location newLocation) {
        Location newLoc = copyFrom.getLowerNE().clone();
        newLoc.setWorld(ArenaWorldUtil.getArenasCopyWorld());

        try {
            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    BukkitAdapter.adapt(ArenaWorldUtil.getArenasWorld()),
                    new CuboidRegion(
                            BukkitAdapter.adapt(copyFrom.getLowerNE()).toBlockPoint(),
                            BukkitAdapter.adapt(copyFrom.getUpperSW()).toBlockPoint()),
                    BukkitAdapter.adapt(ArenaWorldUtil.getArenasCopyWorld()),
                    BukkitAdapter.adapt(newLoc.subtract(reference).add(newLocation)).toBlockPoint()
            );

            forwardExtentCopy.setCopyingEntities(true);

            Operations.complete(forwardExtentCopy);

        } catch (Exception e) {
            Common.sendConsoleMMMessage("<red>" + "Error during copy-paste operation: " + e.getMessage());
        }
    }

}
