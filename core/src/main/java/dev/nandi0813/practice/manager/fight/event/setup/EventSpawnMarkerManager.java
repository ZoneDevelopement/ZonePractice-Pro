package dev.nandi0813.practice.manager.fight.event.setup;

import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.module.util.ClassImport;
import dev.nandi0813.practice.util.Common;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Manages armor stand markers that show spawn positions in event setup mode.
 * Armor stands face the direction the player will spawn and hold a sword.
 */
@Getter
public class EventSpawnMarkerManager {

    private static EventSpawnMarkerManager instance;

    public static EventSpawnMarkerManager getInstance() {
        if (instance == null) {
            instance = new EventSpawnMarkerManager();
        }
        return instance;
    }

    // Maps each event to its list of marker armor stands
    private final Map<EventData, List<ArmorStand>> eventMarkers = new HashMap<>();

    // Maps armor stand UUID to spawn index for spawn removal
    private final Map<UUID, Integer> markerToSpawnIndex = new HashMap<>();

    /**
     * Shows all spawn position markers for an event
     */
    public void showMarkers(EventData eventData) {
        if (eventData == null) return;

        // Clear existing markers first
        clearMarkers(eventData);

        // Additionally, clear any orphaned armor stands near spawn locations that might have been left behind
        // This prevents duplicates from previous sessions or crashes
        if (eventData.getSpawns() != null && !eventData.getSpawns().isEmpty()) {
            for (Location spawnLoc : eventData.getSpawns()) {
                if (spawnLoc != null && spawnLoc.getWorld() != null) {
                    // Remove any nearby armor stands (within 3 blocks) to clean up orphans
                    spawnLoc.getWorld().getNearbyEntities(spawnLoc, 3, 3, 3).stream()
                            .filter(entity -> entity instanceof ArmorStand)
                            .forEach(entity -> {
                                ArmorStand stand = (ArmorStand) entity;
                                // Only remove armor stands that look like our markers
                                if (stand.getCustomName() != null &&
                                    (stand.getCustomName().contains("Spawn #") ||
                                     stand.getCustomName().contains("Right-click to remove"))) {
                                    stand.remove();
                                }
                            });
                }
            }
        }

        List<ArmorStand> markers = new ArrayList<>();

        List<Location> spawns = eventData.getSpawns();
        if (!spawns.isEmpty()) {
            int index = 0;
            for (Location spawnLoc : spawns) {
                ArmorStand marker = createMarker(spawnLoc, "&c&lSpawn #" + (index + 1));
                if (marker != null) {
                    markers.add(marker);
                    // Track this main marker to its spawn index
                    markerToSpawnIndex.put(marker.getUniqueId(), index);

                    // Create second armor stand above for instruction text
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
            eventMarkers.put(eventData, markers);
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
        armorStand.setCustomName(Common.colorize(name));

        // Make the armor stand face the same direction as the spawn location
        Location facingLoc = markerLoc.clone();
        armorStand.teleport(facingLoc);

        // Give it a sword to hold (to make it more visible)
        ItemStack sword = ClassImport.getClasses().getItemCreateUtil().createItem("&cSpawn Marker", org.bukkit.Material.DIAMOND_SWORD);
        armorStand.setItemInHand(sword);

        // Make it invulnerable and persistent
        armorStand.setRemoveWhenFarAway(false);
        armorStand.setMarker(false);

        return armorStand;
    }

    /**
     * Creates a small invisible armor stand just for displaying text label
     */
    private ArmorStand createLabelOnly(Location location, String text) {
        if (location == null || location.getWorld() == null) return null;

        ArmorStand labelStand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        labelStand.setVisible(false);
        labelStand.setGravity(false);
        labelStand.setCanPickupItems(false);
        labelStand.setCustomNameVisible(true);
        labelStand.setCustomName(Common.colorize(text));
        labelStand.setMarker(true); // Make it small
        labelStand.setRemoveWhenFarAway(false);

        return labelStand;
    }

    /**
     * Clears all markers for a specific event
     */
    public void clearMarkers(EventData eventData) {
        List<ArmorStand> markers = eventMarkers.get(eventData);
        if (markers != null) {
            for (ArmorStand marker : markers) {
                markerToSpawnIndex.remove(marker.getUniqueId());
                marker.remove();
            }
            eventMarkers.remove(eventData);
        }
    }

    /**
     * Clears all markers for all events
     */
    public void clearAllMarkers() {
        for (List<ArmorStand> markers : eventMarkers.values()) {
            for (ArmorStand marker : markers) {
                markerToSpawnIndex.remove(marker.getUniqueId());
                marker.remove();
            }
        }
        eventMarkers.clear();
    }

    /**
     * Updates markers for an event (re-creates them)
     */
    public void updateMarkers(EventData eventData) {
        // Always update markers - showMarkers will clear old ones first
        // This ensures markers appear even when adding the first spawn point
        showMarkers(eventData);
    }

    /**
     * Checks if markers are currently shown for an event
     */
    public boolean hasMarkers(EventData eventData) {
        return eventMarkers.containsKey(eventData) && !eventMarkers.get(eventData).isEmpty();
    }

    /**
     * Checks if an armor stand is a spawn marker
     */
    public boolean isMarker(ArmorStand armorStand) {
        for (List<ArmorStand> markers : eventMarkers.values()) {
            if (markers.contains(armorStand)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds which event a marker armor stand belongs to
     */
    public EventData findEventForMarker(ArmorStand armorStand) {
        for (Map.Entry<EventData, List<ArmorStand>> entry : eventMarkers.entrySet()) {
            if (entry.getValue().contains(armorStand)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Gets the spawn index for a given marker armor stand
     */
    public int getSpawnIndex(ArmorStand armorStand, EventData eventData) {
        return markerToSpawnIndex.getOrDefault(armorStand.getUniqueId(), -1);
    }
}
