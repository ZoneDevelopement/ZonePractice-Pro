package dev.nandi0813.practice.manager.server;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.arena.ArenaManager;
import dev.nandi0813.practice.manager.backend.BackendManager;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.fight.event.EventManager;
import dev.nandi0813.practice.manager.ladder.LadderManager;
import dev.nandi0813.practice.manager.leaderboard.hologram.HologramManager;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import dev.nandi0813.practice.util.NumberUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSaveRunnable extends BukkitRunnable {

    @Getter
    private boolean running = false;

    private final long interval = ConfigManager.getInt("AUTO-SAVE.INTERVAL") * 60 * 20L;
    private final boolean alert = ConfigManager.getBoolean("AUTO-SAVE.ALERT");

    public void begin() {
        running = true;
        this.runTaskTimerAsynchronously(ZonePractice.getInstance(), interval, interval);
    }

    @Override
    public void run() {
        if (alert) {
            ServerManager.getInstance().alertPlayers("zpp.autosave.alert", LanguageManager.getString("AUTO-SAVE.STARTED"));

            Bukkit.getScheduler().runTaskLaterAsynchronously(ZonePractice.getInstance(), () ->
                    ServerManager.getInstance().alertPlayers("zpp.autosave.alert", LanguageManager.getString("AUTO-SAVE.ENDED")), NumberUtil.getRandomNumber(4, 10) * 20L);
        }

        save();
    }

    public void save() {
        EventManager.getInstance().saveEventData();
        ArenaManager.getInstance().saveArenas();
        LadderManager.getInstance().saveLadders();
        ProfileManager.getInstance().saveProfiles();
        Bukkit.getScheduler().runTask(ZonePractice.getInstance(), () -> HologramManager.getInstance().saveHolograms());
        BackendManager.save();
    }

}
