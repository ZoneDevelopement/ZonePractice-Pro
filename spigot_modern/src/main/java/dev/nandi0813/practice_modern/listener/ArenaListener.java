package dev.nandi0813.practice_modern.listener;

import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.server.ServerManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import static dev.nandi0813.practice.manager.arena.ArenaManager.LOADED_CHUNKS;
import static dev.nandi0813.practice.manager.arena.ArenaManager.LOAD_CHUNKS;

public class ArenaListener implements Listener {

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if (LOAD_CHUNKS) {
            if (LOADED_CHUNKS.contains(e.getChunk())) {
                e.getChunk().getWorld().getChunkAtAsync(e.getChunk().getX(), e.getChunk().getZ());
            }
        }
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent e) {
        if (e.getWorld() == ArenaWorldUtil.getArenasWorld()) {
            e.setCancelled(true);
            return;
        }

        if (e.getWorld() == ArenaWorldUtil.getArenasCopyWorld()) {
            e.setCancelled(true);
            return;
        }

        if (ServerManager.getLobby() != null && ServerManager.getLobby().getWorld() == e.getWorld()) {
            e.setCancelled(true);
        }
    }

}
