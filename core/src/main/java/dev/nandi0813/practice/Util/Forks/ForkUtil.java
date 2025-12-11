package dev.nandi0813.practice.Util.Forks;

import org.bukkit.Bukkit;

public enum ForkUtil {
    ;
    private static final boolean IS_FOX_SPIGOT;
    private static final boolean IS_CARBON;

    static {
        IS_FOX_SPIGOT = detectFoxSpigot();
        IS_CARBON = detectCarbon();
    }

    private static boolean detectFoxSpigot() {
        try {
            Class.forName("pt.foxspigot.jar.knockback.KnockbackModule");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean detectCarbon() {
        try {
            if (!Bukkit.getServer().getVersion().contains("Carbon")) {
                return false;
            }
            Class.forName("xyz.refinedev.spigot.api.knockback.KnockbackAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isFoxSpigot() {
        return IS_FOX_SPIGOT;
    }

    public static boolean isCarbon() {
        return IS_CARBON;
    }
}
