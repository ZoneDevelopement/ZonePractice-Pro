package dev.nandi0813.practice.manager.fight.event.setup;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.fight.event.interfaces.EventData;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.ItemCreateUtil;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mannequin;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Manages mannequin markers that show spawn positions in event setup mode.
 * Mannequins face the direction the player will spawn and hold a sword.
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

    // Maps each event to its list of marker mannequins
    private final Map<EventData, List<Mannequin>> eventMarkers = new HashMap<>();

    // Maps mannequin UUID to spawn index for spawn removal
    private final Map<UUID, Integer> markerToSpawnIndex = new HashMap<>();

    /**
     * Shows all spawn position markers for an event
     */
    public void showMarkers(EventData eventData) {
        if (eventData == null) return;

        // Clear existing markers first
        clearMarkers(eventData);

        // Additionally, clear any orphaned mannequins near spawn locations that might have been left behind
        // This prevents duplicates from previous sessions or crashes
        if (eventData.getSpawns() != null && !eventData.getSpawns().isEmpty()) {
            for (Location spawnLoc : eventData.getSpawns()) {
                if (spawnLoc != null && spawnLoc.getWorld() != null) {
                    // Remove any nearby mannequins (within 3 blocks) to clean up orphans
                    spawnLoc.getWorld().getNearbyEntities(spawnLoc, 3, 3, 3).stream()
                            .filter(entity -> entity instanceof Mannequin)
                            .forEach(entity -> {
                                Mannequin mannequin = (Mannequin) entity;
                                String customName = mannequin.customName() == null ? null : Common.serializeComponentToLegacyString(mannequin.customName());
                                // Only remove mannequins that look like our markers
                                if (customName != null &&
                                    (customName.contains("Spawn #") ||
                                     customName.contains("Right-click to remove"))) {
                                    mannequin.remove();
                                }
                            });
                }
            }
        }

        List<Mannequin> markers = new ArrayList<>();

        List<Location> spawns = eventData.getSpawns();
        if (!spawns.isEmpty()) {
            int index = 0;
            for (Location spawnLoc : spawns) {
                Mannequin marker = createMarker(spawnLoc, "&c&lSpawn #" + (index + 1));
                if (marker != null) {
                    markers.add(marker);
                    // Track this main marker to its spawn index
                    markerToSpawnIndex.put(marker.getUniqueId(), index);

                    // Create second mannequin above for instruction text
                    Location labelLoc = spawnLoc.clone().add(0, 2.3, 0);
                    Mannequin labelStand = createLabelOnly(labelLoc);
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
     * Creates a mannequin marker at the specified location
     */
    private Mannequin createMarker(Location location, String name) {
        if (location == null || location.getWorld() == null) return null;

        // Spawn mannequin at exact player spawn position
        Location markerLoc = location.clone();
        Mannequin mannequin = (Mannequin) markerLoc.getWorld().spawnEntity(markerLoc, EntityType.MANNEQUIN);

        // Configure mannequin to look like a player marker while staying static.
        mannequin.setInvisible(false); // Show body to represent player
        mannequin.setGravity(false);
        mannequin.setCanPickupItems(false);
        mannequin.setCustomNameVisible(true);
        mannequin.customName(Component.text(Common.colorize(name)));
        mannequin.setAI(false);
        mannequin.setCollidable(false);
        mannequin.setSilent(true);
        mannequin.setImmovable(true);

        // Make the mannequin face the same direction (yaw) as the saved spawn location
        Location facingLoc = markerLoc.clone();
        facingLoc.setYaw(location.getYaw());
        facingLoc.setPitch(0.0f);
        mannequin.teleport(facingLoc);

        // Give it a sword to hold (to make it more visible)
        ItemStack sword = ItemCreateUtil.createItem("&cSpawn Marker", org.bukkit.Material.DIAMOND_SWORD);
        mannequin.getEquipment().setItemInMainHand(sword);

        // Make it invulnerable and non-persistent.
        mannequin.setInvulnerable(true);
        mannequin.setPersistent(false);
        mannequin.setRemoveWhenFarAway(false);

        return mannequin;
    }

    /**
     * Creates a small invisible mannequin just for displaying text label
     */
    private Mannequin createLabelOnly(Location location) {
        if (location == null || location.getWorld() == null) return null;

        Mannequin labelStand = (Mannequin) location.getWorld().spawnEntity(location, EntityType.MANNEQUIN);
        labelStand.setInvisible(true);
        labelStand.setGravity(false);
        labelStand.setCanPickupItems(false);
        labelStand.setCustomNameVisible(true);
        labelStand.customName(Component.text(Common.colorize("&7(Right-click to remove)")));
        labelStand.setAI(false);
        labelStand.setCollidable(false);
        labelStand.setSilent(true);
        labelStand.setImmovable(true);
        labelStand.setInvulnerable(true);
        labelStand.setPersistent(false);
        labelStand.setRemoveWhenFarAway(false);

        return labelStand;
    }

    /**
     * Clears all markers for a specific event
     */
    public void clearMarkers(EventData eventData) {
        List<Mannequin> markers = eventMarkers.get(eventData);
        if (markers != null) {
            for (Mannequin marker : markers) {
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
        for (List<Mannequin> markers : eventMarkers.values()) {
            for (Mannequin marker : markers) {
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
        clearMarkers(eventData);
        // Recreate markers next tick so removed entities are fully despawned first.
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> showMarkers(eventData));
    }

    /**
     * Checks if a mannequin is a spawn marker
     */
    public boolean isMarker(Mannequin mannequin) {
        for (List<Mannequin> markers : eventMarkers.values()) {
            if (markers.contains(mannequin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a mannequin is a removable spawn marker (not just a floating label).
     */
    public boolean isSpawnMarker(Mannequin mannequin) {
        return markerToSpawnIndex.containsKey(mannequin.getUniqueId());
    }

    /**
     * Gets the spawn index for a given marker mannequin
     */
    public int getSpawnIndex(Mannequin mannequin) {
        return markerToSpawnIndex.getOrDefault(mannequin.getUniqueId(), -1);
    }
}
