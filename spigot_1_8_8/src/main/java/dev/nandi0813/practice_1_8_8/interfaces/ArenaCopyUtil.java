package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.manager.arena.arenas.ArenaCopy;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class ArenaCopyUtil extends dev.nandi0813.practice.module.interfaces.ArenaCopyUtil {

    @Override
    protected void copyBlock(Block oldBlock, Block newBlock) {
        try {
            // OPTIMIZATION: Disable physics during copy for massive speedup
            newBlock.setType(oldBlock.getType(), false);

            BlockState oldState = oldBlock.getState();
            BlockState newState = newBlock.getState();

            newState.setData(oldState.getData().clone());
            newState.update(true, false);  // force=true, applyPhysics=false

            newBlock.setBiome(oldBlock.getBiome());
        } catch (Exception e) {
            // Skip problematic blocks (e.g., MaterialData type incompatibilities like Torch)
            // This allows the copy process to continue without halting
        }
    }

    @Override
    protected void copyArena(Profile profile, ArenaCopy arenaCopy, Cuboid copyFrom, Location reference, Location newLocation) {
        copyNormal(profile, arenaCopy, copyFrom, reference, newLocation);
    }

    @Override
    public void deleteArena(final String arena, final Cuboid cuboid) {
        deleteNormal(arena, cuboid);
    }

}
