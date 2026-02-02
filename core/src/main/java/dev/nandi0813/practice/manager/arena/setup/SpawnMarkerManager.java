package dev.nandi0813.practice.manager.arena.setup;

import dev.nandi0813.practice.manager.arena.arenas.Arena;
import dev.nandi0813.practice.manager.arena.arenas.FFAArena;
import dev.nandi0813.practice.manager.arena.arenas.interfaces.DisplayArena;
import dev.nandi0813.practice.module.util.ClassImport;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.util.*;

/**
 * Manages armor stand markers that show spawn positions in arena setup mode.
 * Armor stands face the direction the player will spawn and hold a sword.
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

    // Map: Arena -> List of marker armor stands
    private final Map<DisplayArena, List<ArmorStand>> arenaMarkers = new HashMap<>();

    // Set of all marker armor stand UUIDs for quick lookup
    private final Set<UUID> markerStandIds = new HashSet<>();

    // Map: Main marker armor stand -> spawn index (for FFA arenas)
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

        List<ArmorStand> markers = new ArrayList<>();

        if (arena instanceof Arena standardArena) {
            // Show position 1
            if (standardArena.getPosition1() != null) {
                ArmorStand marker = createMarker(standardArena.getPosition1(), "&c&lSpawn 1");
                if (marker != null) markers.add(marker);
            }

            // Show position 2
            if (standardArena.getPosition2() != null) {
                ArmorStand marker = createMarker(standardArena.getPosition2(), "&c&lSpawn 2");
                if (marker != null) markers.add(marker);
            }
        } else if (arena instanceof FFAArena ffaArena) {
            // Show all FFA spawn positions with two-line labels
            int index = 0; // Use 0-based index to match the list
            for (Location spawnLoc : ffaArena.getFfaPositions()) {
                // Create main marker with player model
                ArmorStand marker = createMarker(spawnLoc, "&c&lFFA Spawn #" + (index + 1)); // Display as 1-based
                if (marker != null) {
                    markers.add(marker);
                    // Track this main marker to its spawn index
                    markerToSpawnIndex.put(marker.getUniqueId(), index);

                    // Create second armor stand above for instruction text (closer spacing)
                    Location labelLoc = spawnLoc.clone().add(0, 2.3, 0);
                    ArmorStand labelStand = createLabelOnly(labelLoc, "&7(Right-click to remove)");
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
     * Creates an armor stand marker at the specified location
     */
    private ArmorStand createMarker(Location location, String name) {
        if (location == null || location.getWorld() == null) return null;

        // Spawn armor stand at exact player spawn position
        Location markerLoc = location.clone();
        ArmorStand armorStand = (ArmorStand) markerLoc.getWorld().spawnEntity(markerLoc, EntityType.ARMOR_STAND);

        // Configure armor stand to look like a player
        armorStand.setVisible(true); // Show body to represent player
        armorStand.setGravity(false);
        armorStand.setCanPickupItems(false);
        armorStand.setCustomNameVisible(true);
        armorStand.setCustomName(dev.nandi0813.practice.util.StringUtil.CC(name));
        armorStand.setMarker(false); // Don't use marker mode so it has full size
        armorStand.setBasePlate(false);
        armorStand.setArms(true);

        // Make it invulnerable and prevent interaction
        ClassImport.getClasses().getArenaUtil().setArmorStandInvulnerable(armorStand);

        // Give diamond sword to right hand
        ItemStack sword = ClassImport.getClasses().getItemMaterialUtil().getSword();
        ClassImport.getClasses().getArenaUtil().setArmorStandItemInHand(armorStand, sword, true);

        // Set arm pose to hold sword naturally (slight angle)
        armorStand.setRightArmPose(new EulerAngle(Math.toRadians(280), Math.toRadians(10), 0));

        // Set player head (Steve head) for helmet
        ItemStack playerHead = ClassImport.getClasses().getItemMaterialUtil().getDefaultPlayerHead();
        armorStand.setHelmet(playerHead);

        // Set red boots for visibility
        ItemStack boots = ClassImport.getClasses().getItemMaterialUtil().getRedBoots();
        armorStand.setBoots(boots);

        // Track this armor stand
        markerStandIds.add(armorStand.getUniqueId());

        return armorStand;
    }

    /**
     * Creates a small invisible armor stand just for displaying text label
     */
    private ArmorStand createLabelOnly(Location location, String text) {
        if (location == null || location.getWorld() == null) return null;

        ArmorStand labelStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);

        // Configure as invisible text-only display
        labelStand.setVisible(false); // Invisible
        labelStand.setGravity(false);
        labelStand.setCanPickupItems(false);
        labelStand.setCustomNameVisible(true);
        labelStand.setCustomName(dev.nandi0813.practice.util.StringUtil.CC(text));
        labelStand.setMarker(true); // Tiny marker mode
        labelStand.setBasePlate(false);
        labelStand.setSmall(true); // Make it small

        // Make it invulnerable
        ClassImport.getClasses().getArenaUtil().setArmorStandInvulnerable(labelStand);

        // Track this armor stand too
        markerStandIds.add(labelStand.getUniqueId());

        return labelStand;
    }

    /**
     * Clears all markers for a specific arena
     */
    public void clearMarkers(DisplayArena arena) {
        if (arena == null) return;

        List<ArmorStand> markers = arenaMarkers.remove(arena);
        if (markers != null) {
            for (ArmorStand marker : markers) {
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

    /**
     * Clears all markers for all arenas
     */
    public void clearAllMarkers() {
        for (List<ArmorStand> markers : arenaMarkers.values()) {
            if (markers != null) {
                for (ArmorStand marker : markers) {
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
     * Checks if an armor stand is a spawn marker
     */
    public boolean isMarker(ArmorStand armorStand) {
        return armorStand != null && markerStandIds.contains(armorStand.getUniqueId());
    }

    /**
     * Finds which arena a marker armor stand belongs to
     */
    public DisplayArena getArenaForMarker(ArmorStand armorStand) {
        if (armorStand == null) return null;

        for (Map.Entry<DisplayArena, List<ArmorStand>> entry : arenaMarkers.entrySet()) {
            if (entry.getValue().contains(armorStand)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Removes a specific marker armor stand and its associated spawn position
     *
     * @return true if the marker was found and removed
     */
    public boolean removeMarker(ArmorStand armorStand, DisplayArena arena) {
        if (armorStand == null || arena == null) return false;

        // Check if this armor stand is tracked with a spawn index (it's a main marker)
        Integer spawnIndex = markerToSpawnIndex.get(armorStand.getUniqueId());
        if (spawnIndex == null) return false; // Not a main marker or not tracked

        // Remove from FFA arena
        if (arena instanceof FFAArena ffaArena) {
            if (spawnIndex >= 0 && spawnIndex < ffaArena.getFfaPositions().size()) {
                ffaArena.getFfaPositions().remove(spawnIndex.intValue());
            }
        }

        // Clean up the mapping
        markerStandIds.remove(armorStand.getUniqueId());
        markerToSpawnIndex.remove(armorStand.getUniqueId());

        // Remove the armor stand from tracking list
        List<ArmorStand> markers = arenaMarkers.get(arena);
        if (markers != null) {
            markers.remove(armorStand);
        }

        armorStand.remove();

        return true;
    }

    /**
     * Removes all orphaned marker armor stands from a world.
     * This is useful for cleaning up armor stands that persisted after server restart
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

        // Find all armor stands in the world
        for (org.bukkit.entity.Entity entity : world.getEntities()) {
            if (entity instanceof ArmorStand armorStand) {
                // Check if this looks like one of our markers but isn't tracked
                if (armorStand.getCustomName() != null &&
                        !markerStandIds.contains(armorStand.getUniqueId())) {

                    String customName = armorStand.getCustomName();
                    // Check if it matches our marker naming patterns
                    if (customName.contains("Spawn") || customName.contains("Right-click to remove")) {
                        toRemove.add(armorStand);
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
