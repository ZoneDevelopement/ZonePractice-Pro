package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.leaderboard.hologram.holograms.LadderDynamicHologram;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles periodic updates for a hologram.
 * Manages the update timer and ladder rotation for dynamic holograms.
 */
public class HologramRunnable extends BukkitRunnable {

    private static final String CONFIG_DYNAMIC_UPDATE = "LEADERBOARD.HOLOGRAM.DYNAMIC-UPDATE";
    private static final String CONFIG_STATIC_UPDATE = "LEADERBOARD.HOLOGRAM.STATIC-UPDATE";
    private static final long INITIAL_DELAY_TICKS = 20L;

    private final Hologram hologram;

    @Getter
    private boolean running;

    public HologramRunnable(Hologram hologram) {
        this.hologram = hologram;
        this.running = false;
    }

    /**
     * Starts the hologram update timer.
     */
    public void begin() {
        if (hologram.getHologramType() == null || running) {
            return;
        }

        running = true;
        int updateInterval = getUpdateInterval();
        this.runTaskTimer(ZonePractice.getInstance(), INITIAL_DELAY_TICKS, 20L * updateInterval);
    }

    /**
     * Gets the update interval based on hologram type.
     */
    private int getUpdateInterval() {
        return hologram.getHologramType() == HologramType.LADDER_DYNAMIC
                ? ConfigManager.getInt(CONFIG_DYNAMIC_UPDATE)
                : ConfigManager.getInt(CONFIG_STATIC_UPDATE);
    }

    /**
     * Cancels the update timer.
     *
     * @param showSetupHologram Whether to show the setup hologram after canceling
     */
    public void cancel(boolean showSetupHologram) {
        if (!running) {
            return;
        }

        Bukkit.getScheduler().cancelTask(this.getTaskId());
        running = false;

        hologram.setHologramRunnable(new HologramRunnable(hologram));

        if (showSetupHologram) {
            hologram.setSetupHologram(SetupHologramType.SETUP);
        }
    }

    @Override
    public void run() {
        // Rotate ladder for dynamic holograms
        if (hologram instanceof LadderDynamicHologram dynamicHologram) {
            dynamicHologram.rotateLadder();
        }

        // Update hologram content
        hologram.updateContent();
    }
}
