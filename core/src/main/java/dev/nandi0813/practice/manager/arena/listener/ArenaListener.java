package dev.nandi0813.practice.manager.arena.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.BasicArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.match.MatchManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.manager.server.ServerManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.Cuboid;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import static dev.nandi0813.practice.manager.arena.ArenaManager.LOADED_CHUNKS;
import static dev.nandi0813.practice.manager.arena.ArenaManager.LOAD_CHUNKS;

public class ArenaListener implements Listener {

    @EventHandler
    public void onPlayerPortalEvent(PlayerPortalEvent e) {
        Player player = e.getPlayer();
        World world = player.getWorld();

        if (world.equals(ArenaWorldUtil.getArenasCopyWorld())) {
            e.setCancelled(true);
        } else if (world.equals(ArenaWorldUtil.getArenasWorld())) {
            e.setCancelled(true);
        }
    }

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
            case SPECTATE:
                return;
        }

        World playerWorld = player.getWorld();
        Location blockLoc = e.getBlock().getLocation();

        if (playerWorld.equals(ArenaWorldUtil.getArenasCopyWorld())) {
            Common.sendMMMessage(player, LanguageManager.getString("ARENA.CANT-EDIT-COPIES"));
            e.setCancelled(true);
        } else if (playerWorld.equals(ArenaWorldUtil.getArenasWorld())) {
            for (Cuboid cuboid : ArenaManager.getInstance().getArenaCuboids().keySet()) {
                if (cuboid.contains(blockLoc)) {
                    BasicArena arena = ArenaManager.getInstance().getArenaCuboids().get(cuboid);
                    Arena mainArena = ArenaUtil.getArena(arena);
                    if (mainArena == null)
                        return;

                    if (mainArena.isBuild() && !mainArena.getCopies().isEmpty()) {
                        e.setCancelled(true);
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.CANT-EDIT-ARENA-WITH-COPIES").replace("%arena%", mainArena.getDisplayName()));
                    } else if (!MatchManager.getInstance().getLiveMatchesByArena(arena).isEmpty()) {
                        e.setCancelled(true);
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.CANT-EDIT-ARENA-WITH-MATCH").replace("%arena%", mainArena.getDisplayName()));
                    }
                }
            }
        }
    }

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);

        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
            case SPECTATE:
                return;
        }

        World playerWorld = player.getWorld();
        Location blockLoc = e.getBlock().getLocation();

        if (playerWorld.equals(ArenaWorldUtil.getArenasCopyWorld())) {
            Common.sendMMMessage(player, LanguageManager.getString("ARENA.CANT-EDIT-COPIES"));
            e.setCancelled(true);
        } else if (playerWorld.equals(ArenaWorldUtil.getArenasWorld())) {
            for (Cuboid cuboid : ArenaManager.getInstance().getArenaCuboids().keySet()) {
                if (cuboid.contains(blockLoc)) {
                    BasicArena arena = ArenaManager.getInstance().getArenaCuboids().get(cuboid);
                    Arena mainArena = ArenaUtil.getArena(arena);
                    if (mainArena == null)
                        return;

                    if (mainArena.isBuild() && !mainArena.getCopies().isEmpty()) {
                        e.setCancelled(true);
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.CANT-EDIT-ARENA-WITH-COPIES").replace("%arena%", mainArena.getDisplayName()));
                    } else if (!MatchManager.getInstance().getLiveMatchesByArena(arena).isEmpty()) {
                        e.setCancelled(true);
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.CANT-EDIT-ARENA-WITH-MATCH").replace("%arena%", mainArena.getDisplayName()));
                    }
                }
            }
        }
    }

    @EventHandler ( priority = EventPriority.HIGHEST )
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Profile profile = ProfileManager.getInstance().getProfile(player);
        Action action = e.getAction();

        switch (profile.getStatus()) {
            case MATCH:
            case FFA:
            case EVENT:
            case SPECTATE:
                return;
        }

        if (!action.equals(Action.RIGHT_CLICK_BLOCK) && !action.equals(Action.LEFT_CLICK_BLOCK)) return;

        World playerWorld = player.getWorld();
        Location blockLoc = e.getClickedBlock().getLocation();

        if (playerWorld.equals(ArenaWorldUtil.getArenasCopyWorld())) {
            Common.sendMMMessage(player, LanguageManager.getString("ARENA.CANT-EDIT-COPIES"));
            e.setCancelled(true);
        } else if (playerWorld.equals(ArenaWorldUtil.getArenasWorld())) {
            for (Cuboid cuboid : ArenaManager.getInstance().getArenaCuboids().keySet()) {
                if (cuboid.contains(blockLoc)) {
                    BasicArena arena = ArenaManager.getInstance().getArenaCuboids().get(cuboid);
                    Arena mainArena = ArenaUtil.getArena(arena);
                    if (mainArena == null)
                        return;

                    if (mainArena.isBuild() && !mainArena.getCopies().isEmpty()) {
                        e.setCancelled(true);
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.CANT-EDIT-ARENA-WITH-COPIES").replace("%arena%", mainArena.getDisplayName()));
                    } else if (!MatchManager.getInstance().getLiveMatchesByArena(arena).isEmpty()) {
                        e.setCancelled(true);
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.CANT-EDIT-ARENA-WITH-MATCH").replace("%arena%", mainArena.getDisplayName()));
                    }
                }
            }
        }
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if (LOAD_CHUNKS) {
            if (LOADED_CHUNKS.contains(e.getChunk())) {
                // Use addPluginChunkTicket to force-keep the chunk loaded.
                // This is safe from recursion (unlike getChunkAtAsync which can
                // trigger chunk scheduling → more unloads → StackOverflowError).
                e.getChunk().addPluginChunkTicket(ZonePractice.getInstance());
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
