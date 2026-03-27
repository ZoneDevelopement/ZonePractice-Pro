package dev.nandi0813.practice.manager.server;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.manager.backend.LanguageManager;
import dev.nandi0813.practice.manager.backend.MysqlManager;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.ProfileManager;
import lombok.Getter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InactiveProfileRunnable extends BukkitRunnable {

    private final int deleteAfter = ConfigManager.getInt("PLAYER.DELETE-INACTIVE-USER.DAYS");
    @Getter
    private boolean running = false;

    public void begin() {
        running = true;
        this.runTaskTimerAsynchronously(ZonePractice.getInstance(), 20L * 30, 20L * 60 * 60 * 24);
    }

    @Override
    public void run() {
        int count = 0;

        List<Profile> profiles = new ArrayList<>(ProfileManager.getInstance().getProfiles().values());

        for (Profile profile : profiles) {
            long timeDiff = Math.abs(System.currentTimeMillis() - profile.getLastJoin());
            long daysDiff = TimeUnit.DAYS.convert(timeDiff, TimeUnit.MILLISECONDS);

            if (daysDiff > deleteAfter) {
                if (profile.getFile().getFile().delete()) {
                    ProfileManager.getInstance().getProfiles().remove(profile.getUuid());
                    deleteStatsFromMysql(profile);

                    count++;
                }
            }
        }

        if (count > 0)
            ServerManager.getInstance().alertPlayers("zpp.admin", LanguageManager.getString("PROFILE.INACTIVITY-REMOVED").replace("%count%", String.valueOf(count)));
    }

    private void deleteStatsFromMysql(Profile profile) {
        if (!MysqlManager.isConnected(false)) return;

        MysqlManager.deleteProfileStatsAsync(profile.getUuid());
    }

}
