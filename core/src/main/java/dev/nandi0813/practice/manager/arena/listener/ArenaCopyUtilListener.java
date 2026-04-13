package dev.nandi0813.practice.manager.arena.listener;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.ArenaCopy;
import dev.nandi0813.practice.manager.arena.util.ArenaWorldUtil;
import dev.nandi0813.practice.manager.arena.util.BedLocation;
import dev.nandi0813.practice.manager.arena.util.FaweUtil;
import dev.nandi0813.practice.manager.arena.util.PortalLocation;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.gui.GUIManager;
import dev.nandi0813.practice.manager.gui.GUIType;
import dev.nandi0813.practice.manager.gui.setup.arena.ArenaGUISetupManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.util.*;
import dev.nandi0813.practice.util.actionbar.ActionBar;
import dev.nandi0813.practice.util.actionbar.ActionBarPriority;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ArenaCopyUtilListener implements Listener {

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
        final Cuboid sourceCuboid = arena.getCuboid();
        final Location newLocation = getAvailableLocation(sourceCuboid);

        Location reference = sourceCuboid.getLowerNE();

        for (Player player : Bukkit.getOnlinePlayers())
            if (player.hasPermission("zpp.setup"))
                Common.sendMMMessage(player, LanguageManager.getString("ARENA.GENERATE-COPY").replace("%arena%", Common.serializeNormalToMMString(arena.getDisplayName())));

        if (newLocation != null) {
            Location position1 = arena.getPosition1().clone();
            Location position2 = arena.getPosition2().clone();
            position1.setWorld(copyWorld);
            position2.setWorld(copyWorld);

            ArenaCopy arenaCopy = new ArenaCopy(arena.getName() + "_" + (arena.getCopies().size() + 1), arena);
            arenaCopy.setCorner1(newLocation.clone());
            arenaCopy.setCorner2(newLocation.clone().add(sourceCuboid.getSizeX() - 1, sourceCuboid.getSizeY() - 1, sourceCuboid.getSizeZ() - 1));
            arenaCopy.createCuboid();

            arenaCopy.getMainArena().setCopying(true);
            copyingCuboids.add(arenaCopy.getCuboid());
            addCopyingChunks(arenaCopy.getCuboid());  // Register chunks for O(1) physics blocking

            this.copyArena(profile, arenaCopy, sourceCuboid, reference, newLocation);

            arenaCopy.setPosition1(translateLocation(position1, reference, newLocation));
            arenaCopy.setPosition2(translateLocation(position2, reference, newLocation));

            for (Location ffaPos : arena.getFfaPositions()) {
                Location ffaPosCopy = ffaPos.clone();
                ffaPosCopy.setWorld(copyWorld);
                arenaCopy.getFfaPositions().add(translateLocation(ffaPosCopy, reference, newLocation));
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
                bedLoc1 = translateLocation(bedLoc1, reference, newLocation);
                arenaCopy.setBedLoc1(new BedLocation(bedLoc1.getWorld(), bedLoc1.getX(), bedLoc1.getY(), bedLoc1.getZ(), arena.getBedLoc1().getFacing()));
            }

            if (arena.getBedLoc2() != null) {
                Location bedLoc2 = arena.getBedLoc2().clone();
                bedLoc2.setWorld(copyWorld);
                bedLoc2 = translateLocation(bedLoc2, reference, newLocation);
                arenaCopy.setBedLoc2(new BedLocation(bedLoc2.getWorld(), bedLoc2.getX(), bedLoc2.getY(), bedLoc2.getZ(), arena.getBedLoc2().getFacing()));
            }

            if (arena.getPortalLoc1() != null) {
                Location portalLoc1Center = arena.getPortalLoc1().getCenter().clone();
                portalLoc1Center.setWorld(copyWorld);
                portalLoc1Center = translateLocation(portalLoc1Center, reference, newLocation);
                arenaCopy.setPortalLoc1(new PortalLocation(portalLoc1Center));
                arenaCopy.getPortalLoc1().setPortal();
            }

            if (arena.getPortalLoc2() != null) {
                Location portalLoc2Center = arena.getPortalLoc2().getCenter().clone();
                portalLoc2Center.setWorld(copyWorld);
                portalLoc2Center = translateLocation(portalLoc2Center, reference, newLocation);
                arenaCopy.setPortalLoc2(new PortalLocation(portalLoc2Center));
                arenaCopy.getPortalLoc2().setPortal();
            }
        }

        return newLocation;
    }

    private static Location translateLocation(Location original, Location reference, Location newLocation) {
        return new Location(
                newLocation.getWorld(),
                original.getX() - reference.getBlockX() + newLocation.getBlockX(),
                original.getY() - reference.getBlockY() + newLocation.getBlockY(),
                original.getZ() - reference.getBlockZ() + newLocation.getBlockZ(),
                original.getYaw(),
                original.getPitch()
        );
    }

    protected static Location getAvailableLocation(Cuboid sourceCuboid) {
        final World copyWorld = ArenaWorldUtil.getArenasCopyWorld();
        final int maxX = sourceCuboid.getSizeX() - 1;
        final int maxY = sourceCuboid.getSizeY() - 1;
        final int maxZ = sourceCuboid.getSizeZ() - 1;

        // Four thousand arenas fit in that line
        for (int x = -1000000; x <= 1000000; x = x + 1000) {
            Location location = copyWorld.getBlockAt(x, 60, 0).getLocation();
            Cuboid candidate = new Cuboid(
                    copyWorld,
                    location.getBlockX(),
                    location.getBlockY(),
                    location.getBlockZ(),
                    location.getBlockX() + maxX,
                    location.getBlockY() + maxY,
                    location.getBlockZ() + maxZ
            );
            if (!isCuboidContainsLocation(candidate)) {
                return location;
            }
        }
        return null;
    }

    private static boolean overlaps(Cuboid a, Cuboid b) {
        if (!a.getWorld().equals(b.getWorld())) {
            return false;
        }
        return a.getLowerX() <= b.getUpperX() && a.getUpperX() >= b.getLowerX()
                && a.getLowerY() <= b.getUpperY() && a.getUpperY() >= b.getLowerY()
                && a.getLowerZ() <= b.getUpperZ() && a.getUpperZ() >= b.getLowerZ();
    }

    protected static boolean isCuboidContainsLocation(Cuboid candidate) {
        for (Cuboid cuboid : ArenaManager.getInstance().getArenaCuboids().keySet()) {
            if (overlaps(cuboid, candidate)) {
                return true;
            }
        }
        return false;
    }

    protected void copyNormal(Profile profile, ArenaCopy arenaCopy, Cuboid copyFrom, Location reference, Location newLocation) {
        final World copyWorld = ArenaWorldUtil.getArenasCopyWorld();

        final Iterator<Block> blockIterator = copyFrom.iterator();
        final int maxSize = copyFrom.getSizeX() * copyFrom.getSizeY() * copyFrom.getSizeZ();
        final int[] currentSize = {0};

        ActionBar actionBar = profile.getActionBar();
        final String ACTION_BAR_ID = "arena_copy";

        new BukkitRunnable() {
            @Override
            public void run() {
                int changeCounter = 0;
                int checkCounter = 0;

                try {
                    while (blockIterator.hasNext() && changeCounter < PermanentConfig.ARENA_COPY_MAX_CHANGES && checkCounter < PermanentConfig.ARENA_COPY_MAX_CHECKS) {
                        Block block = blockIterator.next();

                        if (block.getType() == Material.AIR) {
                            currentSize[0]++;
                            continue;
                        }

                        Location originLoc = block.getLocation();

                        currentSize[0]++;
                        checkCounter++;

                        int dx = originLoc.getBlockX() - reference.getBlockX();
                        int dy = originLoc.getBlockY() - reference.getBlockY();
                        int dz = originLoc.getBlockZ() - reference.getBlockZ();
                        Block newBlock = copyWorld.getBlockAt(newLocation.getBlockX() + dx, newLocation.getBlockY() + dy, newLocation.getBlockZ() + dz);
                        copyBlock(block, newBlock);

                        changeCounter++;
                    }

                    double progress = NumberUtil.roundDouble(((double) currentSize[0] / maxSize) * 100.0);

                    String message = LanguageManager.getString("ARENA.ACTION-BAR-MSG")
                            .replace("%arena%", Common.serializeNormalToMMString(arenaCopy.getMainArena().getDisplayName()))
                            .replace("%progress_bar%", Common.serializeNormalToMMString(StatisticUtil.getProgressBar(progress)))
                            .replace("%progress_percent%", Common.serializeNormalToMMString(String.valueOf(progress)));

                    actionBar.setMessage(ACTION_BAR_ID, message, -1, ActionBarPriority.HIGH);

                } catch (Exception e) {
                    cancelTask(this, arenaCopy, actionBar, ACTION_BAR_ID);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("zpp.setup"))
                            Common.sendMMMessage(player, LanguageManager.getString("ARENA.ERROR-DURING-COPY-GENERATE").replace("%arena%", Common.serializeNormalToMMString(arenaCopy.getMainArena().getDisplayName())));
                    }

                    Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());

                    return;
                }

                if (!blockIterator.hasNext()) {
                    cancelTask(this, arenaCopy, actionBar, ACTION_BAR_ID);

                    completeCopy(arenaCopy);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("zpp.setup"))
                            Common.sendMMMessage(player, LanguageManager.getString("ARENA.COPY-GENERATED").replace("%arena%", arenaCopy.getMainArena().getDisplayName()));
                    }
                }
            }
        }.runTaskTimer(ZonePractice.getInstance(), 0, 1L);
    }

    protected void deleteNormal(final String arena, final Cuboid cuboid) {
        // OPTIMIZATION: Use iterator directly instead of creating a full list in memory
        final Iterator<Block> iterator = cuboid.iterator();

        // Register chunks for physics blocking during deletion
        addCopyingChunks(cuboid);
        copyingCuboids.add(cuboid);

        new BukkitRunnable() {
            @Override
            public void run() {
                int changeCounter = 0;
                int checkCounter = 0;

                try {
                    while (iterator.hasNext() && changeCounter < PermanentConfig.ARENA_COPY_MAX_CHANGES && checkCounter < PermanentConfig.ARENA_COPY_MAX_CHECKS) {
                        Block block = iterator.next();
                        checkCounter++;

                        // OPTIMIZATION: Skip AIR blocks immediately (doesn't count against change limit)
                        if (block.getType() == Material.AIR) {
                            continue;
                        }

                        block.setBlockData(Material.AIR.createBlockData());
                        changeCounter++;
                    }
                } catch (Exception e) {
                    this.cancel();
                    Common.sendConsoleMMMessage("<red>Error: " + e.getMessage());

                    // Cleanup on error
                    removeCopyingChunks(cuboid);
                    copyingCuboids.remove(cuboid);
                    ArenaManager.getInstance().getArenaCuboids().remove(cuboid);
                    return;
                }

                // Check if deletion is complete
                if (!iterator.hasNext()) {
                    this.cancel();

                    // Cleanup
                    removeCopyingChunks(cuboid);
                    copyingCuboids.remove(cuboid);
                    ArenaManager.getInstance().getArenaCuboids().remove(cuboid);

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player.hasPermission("zpp.setup"))
                            Common.sendMMMessage(player, LanguageManager.getString("ARENA.LAST-COPY-DELETED").replace("%arena%", arena));
                    }
                }
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

    protected static void cancelTask(BukkitRunnable runnable, ArenaCopy arenaCopy, ActionBar actionBar, String actionBarId) {
        runnable.cancel();
        arenaCopy.getMainArena().setCopying(false);

        ArenaCopyUtilListener.getCopyingCuboids().remove(arenaCopy.getCuboid());
        removeCopyingChunks(arenaCopy.getCuboid());  // Unregister chunks
        removeNonPlayerEntities(arenaCopy.getCuboid());

        if (actionBar != null) {
            actionBar.removeMessage(actionBarId);
        }
    }

    /**
     * Removes all non-player entities from a cuboid.
     * NOTE: Skips hologram text displays to prevent leaderboard holograms from disappearing.
     */
    protected static void removeNonPlayerEntities(Cuboid cuboid) {
        cuboid.getEntities().forEach(entity -> {
            if (!(entity instanceof Player)) {
                // Skip hologram text displays
                if (dev.nandi0813.practice.manager.leaderboard.hologram.TextDisplayFactory.isHologramTextDisplay(entity)) {
                    return;
                }
                entity.remove();
            }
        });
    }

    protected void copyBlock(Block oldBlock, Block newBlock) {
        try {
            // OPTIMIZATION: Set type without physics for massive speedup
            newBlock.setType(oldBlock.getType(), false);

            BlockState oldState = oldBlock.getState();
            BlockState newState = newBlock.getState();

            // Clone block data
            newState.setBlockData(oldState.getBlockData().clone());
            newState.update(true, false);  // force=true, applyPhysics=false

            newBlock.setBiome(oldBlock.getBiome());
        } catch (Exception e) {
            // Keep at least the correct material even when BlockData fails to clone.
            newBlock.setType(oldBlock.getType(), false);
        }
    }

    private void completeCopy(ArenaCopy arenaCopy) {
        arenaCopy.getMainArena().getCopies().add(arenaCopy);
        ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arenaCopy.getMainArena()).get(GUIType.Arena_Copy).update();
        ArenaGUISetupManager.getInstance().getArenaSetupGUIs().get(arenaCopy.getMainArena()).get(GUIType.Arena_Main).update();
        GUIManager.getInstance().searchGUI(GUIType.Arena_Summary).update();
    }

    protected void copyArena(Profile profile, ArenaCopy arenaCopy, Cuboid copyFrom, Location reference, Location newLocation) {
        if (SoftDependUtil.isFAWE_ENABLED && PermanentConfig.ARENA_COPY_FAWE_ENABLED) {
            boolean success = FaweUtil.copyFAWEWithResult(copyFrom, reference, newLocation);

            arenaCopy.getMainArena().setCopying(false);
            ArenaCopyUtilListener.getCopyingCuboids().remove(arenaCopy.getCuboid());
            removeCopyingChunks(arenaCopy.getCuboid());
            removeNonPlayerEntities(arenaCopy.getCuboid());

            if (!success) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("zpp.setup")) {
                        Common.sendMMMessage(player, LanguageManager.getString("ARENA.ERROR-DURING-COPY-GENERATE")
                                .replace("%arena%", Common.serializeNormalToMMString(arenaCopy.getMainArena().getDisplayName())));
                    }
                }
                return;
            }

            completeCopy(arenaCopy);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.hasPermission("zpp.setup")) {
                    Common.sendMMMessage(player, LanguageManager.getString("ARENA.COPY-GENERATED")
                            .replace("%arena%", arenaCopy.getMainArena().getDisplayName()));
                }
            }
        } else {
            this.copyNormal(profile, arenaCopy, copyFrom, reference, newLocation);
        }
    }

    public void deleteArena(final String arena, final Cuboid cuboid) {
        if (SoftDependUtil.isFAWE_ENABLED) {
            FaweUtil.deleteFAWE(cuboid);
        } else {
            deleteNormal(arena, cuboid);
        }
    }

}
