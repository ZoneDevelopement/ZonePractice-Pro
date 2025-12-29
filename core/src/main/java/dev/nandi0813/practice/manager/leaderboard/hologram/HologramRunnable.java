package dev.nandi0813.practice.manager.leaderboard.hologram;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramRunnable extends BukkitRunnable {

    private final Hologram hologram;
    @Getter
    private boolean running = false;

    public HologramRunnable(Hologram hologram) {
        this.hologram = hologram;
    }

    public void begin() {
        if (hologram.getHologramType() == null)
            return;

        running = true;

        int updateTime;
        if (hologram.getHologramType() == HologramType.LADDER_DYNAMIC) {
            updateTime = ConfigManager.getInt("LEADERBOARD.HOLOGRAM.DYNAMIC-UPDATE");
        } else {
            updateTime = ConfigManager.getInt("LEADERBOARD.HOLOGRAM.STATIC-UPDATE");
        }

        this.runTaskTimer(ZonePractice.getInstance(), 20L, 20L * updateTime);
    }

    public void cancel(boolean spawnSetupHolo) {
        if (running) {
            Bukkit.getScheduler().cancelTask(this.getTaskId());
            running = false;

            hologram.setHologramRunnable(new HologramRunnable(hologram));
            if (spawnSetupHolo)
                hologram.setSetupHologram(SetupHologramType.SETUP);
        }
    }

    @Override
    public void run() {
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), hologram::updateContent);
    }

}
