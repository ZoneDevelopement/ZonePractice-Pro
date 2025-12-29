package dev.nandi0813.practice_1_8_8.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;

import static dev.nandi0813.practice.manager.arena.ArenaManager.LOADED_CHUNKS;
import static dev.nandi0813.practice.manager.arena.ArenaManager.LOAD_CHUNKS;

public class ArenaListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if (LOAD_CHUNKS) {
            if (LOADED_CHUNKS.contains(e.getChunk())) {
                e.setCancelled(true);
            }
        }
    }

}
