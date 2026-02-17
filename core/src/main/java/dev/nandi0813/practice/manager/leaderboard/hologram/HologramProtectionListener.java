package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.ZonePractice;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Arrays;

/**
 * Protects hologram armor stands from external removal/damage.
 *
 * <p>Protection mechanisms:</p>
 * <ul>
 *   <li>Rapid health check (every 5 ticks) to detect and respawn dead armor stands</li>
 *   <li>Damage event cancellation</li>
 *   <li>Death event recovery</li>
 *   <li>Chunk unload protection</li>
 * </ul>
 */
public class HologramProtectionListener implements Listener {

    private static final int HEALTH_CHECK_INTERVAL = 5; // ticks
    private static final int HEALTH_CHECK_DELAY = 20; // ticks

    private static HologramProtectionListener instance;

    private HologramProtectionListener() {}

    /**
     * Registers the protection listener and starts the health check task.
     */
    public static void register() {
        if (instance != null) return;

        instance = new HologramProtectionListener();
        Bukkit.getPluginManager().registerEvents(instance, ZonePractice.getInstance());
        instance.startHealthCheck();
    }

    /**
     * Starts a frequent health check to ensure all hologram armor stands are alive.
     */
    private void startHealthCheck() {
        Bukkit.getScheduler().runTaskTimer(ZonePractice.getInstance(), this::checkAndRepairHolograms,
                HEALTH_CHECK_DELAY, HEALTH_CHECK_INTERVAL);
    }

    /**
     * Checks all holograms and respawns any dead armor stands.
     */
    private void checkAndRepairHolograms() {
        HologramManager.getInstance().getHolograms().stream()
                .filter(Hologram::isEnabled)
                .flatMap(hologram -> hologram.getLines().stream())
                .filter(line -> !ArmorStandFactory.isAlive(line.getEntity()))
                .filter(line -> line.getLocation() != null && line.getLocation().getWorld() != null)
                .forEach(this::respawnLine);
    }

    /**
     * Respawns a hologram line's armor stand.
     */
    private void respawnLine(HologramLine line) {
        ArmorStand newStand = ArmorStandFactory.create(line.getLocation(), line.getText());
        line.setEntity(newStand);
    }

    /**
     * Cancels all damage to hologram armor stands.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (ArmorStandFactory.isHologramArmorStand(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    /**
     * Respawns hologram armor stands that somehow died.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();

        if (!ArmorStandFactory.isHologramArmorStand(entity)) {
            return;
        }

        ArmorStand deadStand = (ArmorStand) entity;
        String customName = deadStand.getCustomName();
        var location = deadStand.getLocation();

        // Schedule respawn for next tick
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () ->
                findAndRespawnLine(deadStand, location, customName));
    }

    /**
     * Finds the hologram line for a dead armor stand and respawns it.
     */
    private void findAndRespawnLine(ArmorStand deadStand, org.bukkit.Location location, String customName) {
        HologramManager.getInstance().getHolograms().stream()
                .flatMap(hologram -> hologram.getLines().stream())
                .filter(line -> isMatchingLine(line, deadStand, location))
                .findFirst()
                .ifPresent(line -> {
                    ArmorStand newStand = ArmorStandFactory.create(line.getLocation(),
                            customName != null ? customName : line.getText());
                    line.setEntity(newStand);
                });
    }

    /**
     * Checks if a line matches the dead armor stand.
     */
    private boolean isMatchingLine(HologramLine line, ArmorStand deadStand, org.bukkit.Location location) {
        if (line.getEntity() == deadStand) {
            return true;
        }
        return line.getLocation() != null && line.getLocation().distanceSquared(location) < 0.5;
    }

    /**
     * Prevents chunk unloading from affecting hologram armor stands.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        boolean hasHologram = Arrays.stream(event.getChunk().getEntities())
                .anyMatch(ArmorStandFactory::isHologramArmorStand);

        if (hasHologram) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
                if (!event.getChunk().isLoaded()) {
                    event.getChunk().load(true);
                }
            });
        }
    }
}
