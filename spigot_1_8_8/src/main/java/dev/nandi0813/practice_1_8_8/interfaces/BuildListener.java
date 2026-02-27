package dev.nandi0813.practice_1_8_8.interfaces;

import dev.nandi0813.practice.module.interfaces.AbstractBuildListener;

/**
 * 1.8.8 implementation of the build-block listener.
 * All functionality is inherited from {@link AbstractBuildListener}:
 * explosions, TNT tracking via EntitySpawnEvent, pistons, block form,
 * liquid flow and block spread all work out of the box.
 * No additional API calls exist on 1.8.8 that need to be handled here.
 */
public class BuildListener extends AbstractBuildListener {
    // isTntBlockAlreadyTracked() returns false by default → EntitySpawnEvent
    // captures the TNT block with the Material.TNT override as needed for 1.8.8.
}