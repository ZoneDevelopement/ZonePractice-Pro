package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.ZonePractice;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.Arrays;

/**
 * Protects hologram text displays from external removal/damage.
 *
 * <p>Protection mechanisms:</p>
 * <ul>
 *   <li>Rapid health check (every 5 ticks) to detect and respawn removed displays</li>
 *   <li>Damage event cancellation</li>
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
     * Starts a frequent health check to ensure all hologram displays are alive.
     */
    private void startHealthCheck() {
        Bukkit.getScheduler().runTaskTimer(ZonePractice.getInstance(), this::checkAndRepairHolograms,
                HEALTH_CHECK_DELAY, HEALTH_CHECK_INTERVAL);
    }

    /**
     * Checks all holograms and respawns any removed displays.
     */
    private void checkAndRepairHolograms() {
        HologramManager.getInstance().getHolograms().stream()
                .filter(Hologram::isEnabled)
                .flatMap(hologram -> hologram.getLines().stream())
                .filter(line -> !TextDisplayFactory.isAlive(line.getEntity()))
                .filter(line -> line.getLocation() != null && line.getLocation().getWorld() != null)
                .forEach(this::respawnLine);
    }

    /**
     * Respawns a hologram line's text display.
     */
    private void respawnLine(HologramLine line) {
        line.setEntity(TextDisplayFactory.create(line.getLocation(), line.getText()));
    }

    /**
     * Cancels all damage to hologram text displays.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event) {
        if (TextDisplayFactory.isHologramTextDisplay(event.getEntity())) {
            event.setCancelled(true);
        }
    }

    /**
     * Prevents chunk unloading from affecting hologram text displays.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkUnload(ChunkUnloadEvent event) {
        boolean hasHologram = Arrays.stream(event.getChunk().getEntities())
                .anyMatch(TextDisplayFactory::isHologramTextDisplay);

        if (hasHologram) {
            Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> {
                if (!event.getChunk().isLoaded()) {
                    event.getChunk().load(true);
                }
            });
        }
    }
}
