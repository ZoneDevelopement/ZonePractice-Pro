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

    private Profile banner = null;
    private boolean banned = false;
    private String reason = null;
    private long time = 0L;

    public boolean ban(Profile banner, String reason) {
        if (!this.banned) {
            this.banned = true;
            this.banner = banner;
            this.reason = reason;
            this.time = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public boolean unban() {
        if (this.banned) {
            this.banned = false;
            this.reason = null;
            return true;
        }
        return false;
    }

    public void set(final @NotNull YamlConfiguration config, final String path) {
        if (this.isBanned()) {
            if (banner != null)
                config.set(path + ".banner", this.getBanner().getUuid().toString());
            config.set(path + ".banned", true);
            config.set(path + ".reason", this.getReason());
            config.set(path + ".bannedAt", this.getTime());
        } else
            config.set(path, null);
    }

    public void get(final @NotNull YamlConfiguration config, final String path) {
        if (config.isBoolean(path + ".banned"))
            this.banned = config.getBoolean(path + ".banned");

        if (this.isBanned()) {
            Bukkit.getScheduler().runTaskLater(ZonePractice.getInstance(), () ->
            {
                if (config.isString(path + ".banner"))
                    banner = ProfileManager.getInstance().getProfile(UUID.fromString(config.getString(path + ".banner")));
            }, 20L * 3);

            if (config.isString(path + ".reason"))
                this.reason = config.getString(path + ".reason");

            if (config.isLong(path + ".bannedAt"))
                this.time = config.getLong(path + ".bannedAt");
        }
    }

}
