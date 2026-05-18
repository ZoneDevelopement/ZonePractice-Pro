package dev.nandi0813.practice.manager.profile;

import dev.nandi0813.practice.ZonePractice;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Setter
public class RankedBan {

    private Profile banner;
    private boolean banned;
    private String reason;
    private long time;

    public boolean ban(Profile banner, String reason) {
        if (banned) return false;

        this.banned = true;
        this.banner = banner;
        this.reason = reason;
        this.time = System.currentTimeMillis();
        return true;
    }

    public boolean unban() {
        if (!banned) return false;

        this.banned = false;
        this.reason = null;
        return true;
    }

    public void saveToConfig(final @NotNull YamlConfiguration config, final String path) {
        if (!isBanned()) {
            config.set(path, null);
            return;
        }

        if (banner != null)
            config.set(path + ".banner", banner.getUuid().toString());

        config.set(path + ".banned", true);
        config.set(path + ".reason", reason);
        config.set(path + ".bannedAt", time);
    }

    public void loadFromConfig(final @NotNull YamlConfiguration config, final String path) {
        if (!config.isBoolean(path + ".banned")) return;

        banned = config.getBoolean(path + ".banned");

        if (!isBanned()) return;

        Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () -> {
            if (config.isString(path + ".banner"))
                banner = ProfileManager.getInstance().getProfile(UUID.fromString(config.getString(path + ".banner")));
        }, 20L * 3);

        if (config.isString(path + ".reason"))
            reason = config.getString(path + ".reason");

        if (config.isLong(path + ".bannedAt"))
            time = config.getLong(path + ".bannedAt");
    }

}
