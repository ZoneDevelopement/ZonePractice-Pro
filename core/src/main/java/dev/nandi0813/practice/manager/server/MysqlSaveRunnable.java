package dev.nandi0813.practice.manager.server;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.MysqlManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MysqlSaveRunnable extends BukkitRunnable {

    private final int interval = ConfigManager.getInt("MYSQL-DATABASE.SAVE-PERIOD");

    public void begin() {
        this.runTaskTimerAsynchronously(ZonePractice.getInstance(), interval * 60 * 20L, interval * 60 * 20L);
    }

    @Override
    public void run() {
        save();
    }

    public void save() {
        if (!MysqlManager.isConnected(true)) return;

        List<Profile> profiles = new ArrayList<>(ProfileManager.getInstance().getProfiles().values());
        MysqlManager.saveProfilesAsync(profiles);
    }

}
