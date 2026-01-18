package dev.nandi0813.practice.module.interfaces;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.ArenaCopy;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.arena.util.BedLocation;
import dev.nandi0813.practice.manager.arena.util.PortalLocation;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.module.interfaces.actionbar.ActionBar;
import dev.nandi0813.practice.util.*;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ArenaCopyUtil implements Listener {

    @Getter
    private static List<Cuboid> copyingCuboids = new ArrayList<>();

    // OPTIMIZATION: Chunk-based physics blocker (O(1) instead of O(n))
    private static final java.util.Set<Long> copyingChunks = new java.util.HashSet<>();

    /**
     * Encodes chunk coordinates into a single long for fast lookup.
     */
    private static long getChunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }

    /**
     * Adds all chunks in a cuboid to the copying set.
     */
    private static void addCopyingChunks(Cuboid cuboid) {
        int minChunkX = cuboid.getLowerX() >> 4;
        int maxChunkX = cuboid.getUpperX() >> 4;
        int minChunkZ = cuboid.getLowerZ() >> 4;
        int maxChunkZ = cuboid.getUpperZ() >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                copyingChunks.add(getChunkKey(cx, cz));
            }
        }
    }

    /**
     * Removes all chunks in a cuboid from the copying set.
     */
    private static void removeCopyingChunks(Cuboid cuboid) {
        int minChunkX = cuboid.getLowerX() >> 4;
        int maxChunkX = cuboid.getUpperX() >> 4;
        int minChunkZ = cuboid.getLowerZ() >> 4;
        int maxChunkZ = cuboid.getUpperZ() >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                copyingChunks.remove(getChunkKey(cx, cz));
            }
        }
    }

    public Location createCopy(Profile profile, Arena arena) {
        final World copyWorld = ArenaWorldUtil.getArenasCopyWorld();
        final Location newLocation = getAvailableLocation();

        Cuboid cuboid = arena.getCuboid();
        Location reference = arena.getCuboid().getLowerNE();
        reference.setWorld(copyWorld);

        for (Player player : Bukkit.getOnlinePlayers())
            if (player.hasPermission("zpp.setup"))
                Common.sendMMMessage(player, LanguageManager.getString("ARENA.GENERATE-COPY").replace("%arena%", Common.serializeNormalToMMString(arena.getDisplayName())));

        if (newLocation != null) {
            Location corner1 = arena.getCorner1().clone();
            Location corner2 = arena.getCorner2().clone();
            Location position1 = arena.getPosition1().clone();
            Location position2 = arena.getPosition2().clone();

            corner1.setWorld(copyWorld);
            corner2.setWorld(copyWorld);
            position1.setWorld(copyWorld);
            position2.setWorld(copyWorld);

            ArenaCopy arenaCopy = new ArenaCopy(arena.getName() + "_" + (arena.getCopies().size() + 1), arena);
            this.copyArena(profile, arenaCopy, cuboid, reference, newLocation);

            arenaCopy.setCorner1(corner1.clone().subtract(reference).add(newLocation));
            arenaCopy.setCorner2(corner2.clone().subtract(reference).add(newLocation));
            arenaCopy.createCuboid();

            copyingCuboids.add(arenaCopy.getCuboid());
            addCopyingChunks(arenaCopy.getCuboid());  // Register chunks for O(1) physics blocking

            arenaCopy.setPosition1(position1.clone().subtract(reference).add(newLocation));
            arenaCopy.setPosition2(position2.clone().subtract(reference).add(newLocation));

            for (Location ffaPos : arena.getFfaPositions()) {
                Location ffaPosCopy = ffaPos.clone();
                ffaPosCopy.setWorld(copyWorld);
                arenaCopy.getFfaPositions().add(ffaPosCopy.clone().subtract(reference).add(newLocation));
            }

            if (arena.isBuildMax()) {
                int difference = arena.getCorner1().getBlockY() - arena.getBuildMaxValue();
                arenaCopy.setBuildMax(true);
                arenaCopy.setBuildMaxValue(arenaCopy.getCorner1().getBlockY() + Math.abs(difference));
            }

            if (arena.isDeadZone()) {
                int difference = arena.getCorner1().getBlockY() - arena.getDeadZoneValue();
                arenaCopy.setDeadZone(true);
                arenaCopy.setDeadZoneValue(arenaCopy.getCorner1().getBlockY() + Math.abs(difference));
            }

            if (arena.getBedLoc1() != null) {
                Location bedLoc1 = arena.getBedLoc1().clone();
                bedLoc1.setWorld(copyWorld);
                bedLoc1 = bedLoc1.subtract(reference).add(newLocation);
                arenaCopy.setBedLoc1(new BedLocation(bedLoc1.getWorld(), bedLoc1.getX(), bedLoc1.getY(), bedLoc1.getZ(), arena.getBedLoc1().getFacing()));
            }

            if (arena.getBedLoc2() != null) {
                Location bedLoc2 = arena.getBedLoc2().clone();
                bedLoc2.setWorld(copyWorld);
                bedLoc2 = bedLoc2.subtract(reference).add(newLocation);
                arenaCopy.setBedLoc2(new BedLocation(bedLoc2.getWorld(), bedLoc2.getX(), bedLoc2.getY(), bedLoc2.getZ(), arena.getBedLoc2().getFacing()));
            }

            if (arena.getPortalLoc1() != null) {
                Location portalLoc1Center = arena.getPortalLoc1().getCenter().clone();
                portalLoc1Center.setWorld(copyWorld);
                portalLoc1Center = portalLoc1Center.subtract(reference).add(newLocation);
                arenaCopy.setPortalLoc1(new PortalLocation(portalLoc1Center));
                arenaCopy.getPortalLoc1().setPortal();
            }

            if (arena.getPortalLoc2() != null) {
                Location portalLoc2Center = arena.getPortalLoc2().getCenter().clone();
                portalLoc2Center.setWorld(copyWorld);
                portalLoc2Center = portalLoc2Center.subtract(reference).add(newLocation);
                arenaCopy.setPortalLoc2(new PortalLocation(portalLoc2Center));
                arenaCopy.getPortalLoc2().setPortal();
            }
        }

        return newLocation;
    }

    protected static Location getAvailableLocation() {
        final World copyWorld = ArenaWorldUtil.getArenasCopyWorld();

        // Four thousand arenas fit in that line
        for (int x = -1000000; x <= 1000000; x = x + 1000) {
            Location location = copyWorld.getBlockAt(x, 60, 0).getLocation();
            if (!isCuboidContainsLocation(location)) return location;
        }
        return null;
    }

    protected static boolean isCuboidContainsLocation(Location location) {
        for (Cuboid cuboid : ArenaManager.getInstance().getArenaCuboids().keySet())
            if (cuboid.contains(location)) return true;
        return false;
    }

    protected abstract void copyArena(Profile profile, ArenaCopy arenaCopy, Cuboid copyFrom, Location reference, Location newLocation);

    protected void copyNormal(Profile profile, ArenaCopy arenaCopy, Cuboid copyFrom, Location reference, Location newLocation) {
        final World copyWorld = ArenaWorldUtil.getArenasCopyWorld();

        // OPTIMIZATION: Use iterator directly instead of pre-loading all blocks
        // Saves massive memory (100+ MB for large arenas)
        final Iterator<Block> blockIterator = copyFrom.iterator();

        // Calculate total blocks for progress tracking
        final int maxSize = copyFrom.getSizeX() * copyFrom.getSizeY() * copyFrom.getSizeZ();
        final int[] currentSize = {0};

        arenaCopy.getMainArena().setCopying(true);

        dev.nandi0813.practice.module.interfaces.actionbar.ActionBar actionBar = null;
        if (!profile.getActionBar().isLock()) {
            actionBar = profile.getActionBar();
            actionBar.setLock(true);
        }

        ActionBar finalActionBar = actionBar;
        if (finalActionBar != null) {
            finalActionBar.createActionBar();
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                int changeCounter = 0;
                int checkCounter = 0;

                try {
                    while (blockIterator.hasNext() && changeCounter < PermanentConfig.ARENA_COPY_MAX_CHANGES && checkCounter < PermanentConfig.ARENA_COPY_MAX_CHECKS) {
                        Block block = blockIterator.next();

                        // OPTIMIZATION: Skip AIR blocks immediately (doesn't count against limits)
                        if (block.getType() == Material.AIR) {
                            currentSize[0]++;
                            continue;
                        }

                        Location originLoc = block.getLocation();

                        currentSize[0]++;
                        checkCounter++;

                        double progress = NumberUtil.roundDouble(((double) currentSize[0] / maxSize) * 100.0);

                        if (finalActionBar != null) {
                            finalActionBar.setMessage(LanguageManager.getString("ARENA.ACTION-BAR-MSG")
                                    .replace("%arena%", Common.serializeNormalToMMString(arenaCopy.getMainArena().getDisplayName()))
                                    .replace("%progress_bar%", StatisticUtil.getProgressBar(progress))
                                    .replace("%progress_percent%", String.valueOf(progress)));
                        }

                        Location newLoc = new Location(copyWorld, originLoc.getX(), originLoc.getY(), originLoc.getZ()).clone().subtract(reference).add(newLocation);

                        Block newBlock = newLoc.getBlock();
                        copyBlock(block, newBlock);

                        changeCounter++;
                    }
                } catch (Exception e) {
                    cancelTask(this, arenaCopy, finalActionBar);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("zpp.setup"))
                            Common.sendMMMessage(player, LanguageManager.getString("ARENA.ERROR-DURING-COPY-GENERATE").replace("%arena%", Common.serializeNormalToMMString(arenaCopy.getMainArena().getDisplayName())));
                    }

                    Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());

                    return;
                }

                // Check if we're done
                if (!blockIterator.hasNext()) {
                    cancelTask(this, arenaCopy, finalActionBar);

                    arenaCopy.getMainArena().getCopies().add(arenaCopy);
                    ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arenaCopy.getMainArena()).get(GUIType.Arena_Copy).update();
                    ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arenaCopy.getMainArena()).get(GUIType.Arena_Main).update();
                    GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("zpp.setup"))
                            Common.sendMMMessage(player, LanguageManager.getString("ARENA.COPY-GENERATED").replace("%arena%", arenaCopy.getMainArena().getDisplayName()));
                    }
                }
            }
        }.runTaskTimer(ZonePractice.getInstance(), 0, 1L);
    }

    public abstract void deleteArena(final String arena, final Cuboid cuboid);

    protected void deleteNormal(final String arena, final Cuboid cuboid) {
        final Iterator<Block> iterator = cuboid.getBlocks().iterator();

        new BukkitRunnable() {
            @Override
            public void run() {
                int changeCounter = 0;
                int checkCounter = 0;

                try {
                    while (iterator.hasNext()) {
                        if (changeCounter < PermanentConfig.ARENA_COPY_MAX_CHANGES && checkCounter < PermanentConfig.ARENA_COPY_MAX_CHECKS) {
                            Location location = iterator.next().getLocation();

                            if (location.getBlock() != null && location.getBlock().getType() != null && !location.getBlock().getType().equals(Material.AIR)) {
                                location.getBlock().setType(Material.AIR);
                                changeCounter++;
                            }

                            checkCounter++;
                            iterator.remove();
                        } else return;
                    }
                } catch (Exception e) {
                    this.cancel();
                    Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());
                }
                this.cancel();
                ArenaManager.getInstance().getArenaCuboids().remove(cuboid);

                for (Player player : Bukkit.getOnlinePlayers())
                    if (player.hasPermission("zpp.setup"))
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.LAST-COPY-DELETED").replace("%arena%", arena));
            }
        }.runTaskTimer(ZonePractice.getInstance(), 0, 1);
    }

    @EventHandler
    public void onBlockPhysic(BlockPhysicsEvent e) {
        // OPTIMIZATION: O(1) chunk-based lookup instead of O(n) cuboid iteration
        int chunkX = e.getBlock().getX() >> 4;
        int chunkZ = e.getBlock().getZ() >> 4;
        long chunkKey = getChunkKey(chunkX, chunkZ);

        if (copyingChunks.contains(chunkKey)) {
            e.setCancelled(true);
        }
    }

    protected static void cancelTask(BukkitRunnable runnable, ArenaCopy arenaCopy, ActionBar actionBar) {
        runnable.cancel();
        arenaCopy.getMainArena().setCopying(false);

        ArenaCopyUtil.getCopyingCuboids().remove(arenaCopy.getCuboid());
        removeCopyingChunks(arenaCopy.getCuboid());  // Unregister chunks
        removeNonPlayerEntities(arenaCopy.getCuboid());

        if (actionBar != null) {
            actionBar.setDuration(3);
        }
    }

    protected static void removeNonPlayerEntities(Cuboid cuboid) {
        cuboid.getEntities().forEach(entity -> {
            if (!(entity instanceof Player)) {
                entity.remove();
            }
        });
    }

    protected abstract void copyBlock(Block oldBlock, Block newBlock);

}
