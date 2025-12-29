package dev.nandi0813.practice.module;

import dev.nandi0813.practice.ZonePractice;
import org.bukkit.Bukkit;

public class VersionNotSupportedException extends RuntimeException {

    public VersionNotSupportedException(Throwable err) {
        super("Version is not supported! (" + Bukkit.getServer().getBukkitVersion() + ")", err);
        ZonePractice.getInstance().onDisable();
    }

}
