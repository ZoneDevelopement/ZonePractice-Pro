package dev.nandi0813.practice_modern.interfaces.ArenaCopy;

import dev.nandi0813.practice.manager.arena.arenas.ArenaCopy;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.Cuboid;
import dev.nandi0813.practice.util.SoftDependUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class ArenaCopyUtil extends dev.nandi0813.practice.module.interfaces.ArenaCopyUtil {

    @Override
    protected void copyBlock(Block oldBlock, Block newBlock) {
        // OPTIMIZATION: Set type without physics for massive speedup
        newBlock.setType(oldBlock.getType(), false);

        BlockState oldState = oldBlock.getState();
        BlockState newState = newBlock.getState();

        // Clone block data
        newState.setBlockData(oldState.getBlockData().clone());
        newState.update(true, false);  // force=true, applyPhysics=false

        newBlock.setBiome(oldBlock.getBiome());
    }

    @Override
    protected void copyArena(Profile profile, ArenaCopy arenaCopy, Cuboid copyFrom, Location reference, Location newLocation) {
        if (SoftDependUtil.isFAWE_ENABLED) {
            FaweUtil.copyFAWE(copyFrom, reference, newLocation);

            arenaCopy.getMainArena().getCopies().add(arenaCopy);
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arenaCopy.getMainArena()).get(GUIType.Arena_Copy).update();
            ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arenaCopy.getMainArena()).get(GUIType.Arena_Main).update();
            GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
        } else {
            this.copyNormal(profile, arenaCopy, copyFrom, reference, newLocation);
        }
    }

    @Override
    public void deleteArena(final String arena, final Cuboid cuboid) {
        if (SoftDependUtil.isFAWE_ENABLED) {
            FaweUtil.deleteFAWE(cuboid);
        } else {
            deleteNormal(arena, cuboid);
        }
    }

}
