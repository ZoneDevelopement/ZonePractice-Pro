package dev.nandi0813.practice.manager.arena.setup;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.manager.arena.util.ArenaUtil;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.ItemCreateUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Manages mannequin markers that show spawn positions in arena setup mode.
 * Mannequins face the direction the player will spawn and hold a sword.
 */
@Getter
public class SpawnMarkerManager {

    private static SpawnMarkerManager instance;

    public static SpawnMarkerManager getInstance() {
        if (instance == null) {
            instance = new SpawnMarkerManager();
        }
        return instance;
    }

    // Map: Arena -> List of marker mannequins
    private final Map<DisplayArena, List<Mannequin>> arenaMarkers = new HashMap<>();

    // Set of all marker mannequin UUIDs for quick lookup
    private final Set<UUID> markerStandIds = new HashSet<>();

    // Map: Main marker mannequin -> spawn index (for FFA arenas)
    private final Map<UUID, Integer> markerToSpawnIndex = new HashMap<>();

    private SpawnMarkerManager() {
    }

    /**
     * Shows all spawn position markers for an arena
     */
    public void showMarkers(DisplayArena arena) {
        if (arena == null) return;

        // Clear existing markers first
        clearMarkers(arena);

        List<Mannequin> markers = new ArrayList<>();

        if (arena instanceof Arena standardArena) {
            // Show position 1
            if (standardArena.getPosition1() != null) {
                Mannequin marker = createMarker(standardArena.getPosition1(), "&c&lSpawn 1");
                if (marker != null) markers.add(marker);
            }

            // Show position 2
            if (standardArena.getPosition2() != null) {
                Mannequin marker = createMarker(standardArena.getPosition2(), "&c&lSpawn 2");
                if (marker != null) markers.add(marker);
            }
        } else if (arena instanceof FFAArena ffaArena) {
            // Show all FFA spawn positions with two-line labels
            int index = 0; // Use 0-based index to match the list
            for (Location spawnLoc : ffaArena.getFfaPositions()) {
                // Create main marker with player model
                Mannequin marker = createMarker(spawnLoc, "&c&lFFA Spawn #" + (index + 1)); // Display as 1-based
                if (marker != null) {
                    markers.add(marker);
                    // Track this main marker to its spawn index
                    markerToSpawnIndex.put(marker.getUniqueId(), index);

                    // Create second mannequin above for instruction text (closer spacing)
                    Location labelLoc = spawnLoc.clone().add(0, 2.3, 0);
                    Mannequin labelStand = createLabelOnly(labelLoc, "&7(Right-click to remove)");
                    if (labelStand != null) {
                        markers.add(labelStand);
                    }
                }
                index++;
            }
        }

        if (!markers.isEmpty()) {
            arenaMarkers.put(arena, markers);
        }
    }

    /**
     * Creates a mannequin marker at the specified location
     */
    private Mannequin createMarker(@NotNull Location location, @NotNull String name) {
        // Spawn mannequin at exact player spawn position
        Location markerLoc = location.clone();
        Mannequin mannequin = (Mannequin) markerLoc.getWorld().spawnEntity(markerLoc, EntityType.MANNEQUIN);

        // Configure mannequin to look like a player marker while staying static.
        mannequin.setInvisible(false); // Show body to represent player
        mannequin.setGravity(false);
        mannequin.setCanPickupItems(false);
        mannequin.setCustomNameVisible(true);
        mannequin.customName(Component.text(dev.nandi0813.practice.util.StringUtil.CC(name)));
        mannequin.setAI(false);
        mannequin.setCollidable(false);
        mannequin.setSilent(true);
        mannequin.setImmovable(true);

        // Make it invulnerable and prevent interaction
        ArenaUtil.setMannequinInvulnerable(mannequin);

        // Give diamond sword to right hand
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ArenaUtil.setMannequinItemInHand(mannequin, sword, true);

        // Set player head (Steve head) for helmet
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        if (mannequin.getEquipment() != null) {
            mannequin.getEquipment().setHelmet(playerHead);
        }

        // Set red boots for visibility
        ItemStack boots = ItemCreateUtil.getRedBoots();
        if (mannequin.getEquipment() != null) {
            mannequin.getEquipment().setBoots(boots);
        }

        // Track this mannequin
        markerStandIds.add(mannequin.getUniqueId());

        return mannequin;
    }

    /**
     * Creates a small invisible mannequin just for displaying text label
     */
    private Mannequin createLabelOnly(Location location, String text) {
        if (location == null || location.getWorld() == null) return null;

        Mannequin labelStand = (Mannequin) location.getWorld().spawnEntity(location, EntityType.MANNEQUIN);

        // Configure as invisible text-only display
        labelStand.setInvisible(true); // Invisible
        labelStand.setGravity(false);
        labelStand.setCanPickupItems(false);
        labelStand.setCustomNameVisible(true);
        labelStand.customName(Component.text(dev.nandi0813.practice.util.StringUtil.CC(text)));
        labelStand.setAI(false);
        labelStand.setCollidable(false);
        labelStand.setSilent(true);
        labelStand.setImmovable(true);

        // Make it invulnerable
        ArenaUtil.setMannequinInvulnerable(labelStand);

        // Track this mannequin too
        markerStandIds.add(labelStand.getUniqueId());

        return labelStand;
    }

    /**
     * Clears all markers for a specific arena
     */
    public void clearMarkers(DisplayArena arena) {
        if (arena == null) return;

        List<Mannequin> markers = arenaMarkers.remove(arena);
        if (markers != null) {
            for (Mannequin marker : markers) {
                if (marker != null) {
                    // Always clean up tracking data
                    markerStandIds.remove(marker.getUniqueId());
                    markerToSpawnIndex.remove(marker.getUniqueId()); // Clean up spawn index mapping

                    // Attempt to remove the mannequin if it's still valid
                    if (marker.isValid()) {
                        marker.remove();
                    }
                }
            }
        }
    }

    /**
     * Clears all markers for all arenas
     */
    public void clearAllMarkers() {
        for (List<Mannequin> markers : arenaMarkers.values()) {
            if (markers != null) {
                for (Mannequin marker : markers) {
                    if (marker != null) {
                        // Always clean up tracking data
                        markerStandIds.remove(marker.getUniqueId());
                        markerToSpawnIndex.remove(marker.getUniqueId()); // Clean up spawn index mapping

                        // Attempt to remove the armor stand if it's still valid
                        if (marker.isValid()) {
                            marker.remove();
                        }
                    }
                }
            }
        }
        arenaMarkers.clear();
        markerToSpawnIndex.clear(); // Clear all mappings
    }

    /**
     * Updates markers for an arena (re-creates them)
     */
    public void updateMarkers(DisplayArena arena) {
        clearMarkers(arena);
        showMarkers(arena);
    }

    /**
     * Finds the closest FFA spawn position to a given location
     * Returns the index of the spawn position or -1 if none found within range
     */
    public int findClosestFFASpawn(FFAArena arena, Location location, double maxDistance) {
        if (arena == null || location == null) return -1;

        List<Location> spawns = arena.getFfaPositions();
        if (spawns.isEmpty()) return -1;

        double closestDistanceSq = maxDistance * maxDistance;
        int closestIndex = -1;

        for (int i = 0; i < spawns.size(); i++) {
            Location spawn = spawns.get(i);
            if (spawn.getWorld() == null || !spawn.getWorld().equals(location.getWorld())) {
                continue;
            }

            double distanceSq = spawn.distanceSquared(location);
            if (distanceSq < closestDistanceSq) {
                closestDistanceSq = distanceSq;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    /**
     * Checks if markers are currently shown for an arena
     */
    public boolean hasMarkers(DisplayArena arena) {
        return arenaMarkers.containsKey(arena) && !arenaMarkers.get(arena).isEmpty();
    }

    /**
     * Checks if a mannequin is a spawn marker
     */
    public boolean isMarker(Mannequin mannequin) {
        return mannequin != null && markerStandIds.contains(mannequin.getUniqueId());
    }

    /**
     * Finds which arena a marker mannequin belongs to
     */
    public DisplayArena getArenaForMarker(Mannequin mannequin) {
        if (mannequin == null) return null;

        for (Map.Entry<DisplayArena, List<Mannequin>> entry : arenaMarkers.entrySet()) {
            if (entry.getValue().contains(mannequin)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Removes a specific marker mannequin and its associated spawn position
     *
     * @return true if the marker was found and removed
     */
    public boolean removeMarker(Mannequin mannequin, DisplayArena arena) {
        if (mannequin == null || arena == null) return false;

        // Check if this mannequin is tracked with a spawn index (it's a main marker)
        Integer spawnIndex = markerToSpawnIndex.get(mannequin.getUniqueId());
        if (spawnIndex == null) return false; // Not a main marker or not tracked

        // Remove from FFA arena
        if (arena instanceof FFAArena ffaArena) {
            if (spawnIndex >= 0 && spawnIndex < ffaArena.getFfaPositions().size()) {
                ffaArena.getFfaPositions().remove(spawnIndex.intValue());
            }
        }

        // Clean up the mapping
        markerStandIds.remove(mannequin.getUniqueId());
        markerToSpawnIndex.remove(mannequin.getUniqueId());

        // Remove the mannequin from tracking list
        List<Mannequin> markers = arenaMarkers.get(arena);
        if (markers != null) {
            markers.remove(mannequin);
        }

        mannequin.remove();

        return true;
    }

    /**
     * Removes all orphaned marker mannequins from a world.
     * This is useful for cleaning up mannequins that persisted after server restart
     * or were not properly removed due to timing issues.
     * <p>
     * Orphaned markers are identified by:
     * - Having a custom name starting with "&c&l" (our marker naming pattern)
     * - Being in the arenas world
     * - Not being tracked in our current marker lists
     *
     * @param world The world to clean up
     * @return The number of orphaned markers removed
     */
    public int cleanupOrphanedMarkers(org.bukkit.World world) {
        if (world == null) return 0;

        int removed = 0;
        List<org.bukkit.entity.Entity> toRemove = new ArrayList<>();

        // Find all mannequins in the world
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            if (entity instanceof Mannequin mannequin) {
                // Check if this looks like one of our markers but isn't tracked
                String customName = mannequin.customName() == null ? null : Common.serializeComponentToLegacyString(mannequin.customName());
                if (customName != null &&
                        !markerStandIds.contains(mannequin.getUniqueId())) {

                    // Check if it matches our marker naming patterns
                    if (customName.contains("Spawn") || customName.contains("Right-click to remove")) {
                        toRemove.add(mannequin);
                    }
                }
            }
        }

        // Remove the orphaned markers
        for (org.bukkit.entity.Entity entity : toRemove) {
            entity.remove();
            removed++;
        }

        return removed;
    }
}
