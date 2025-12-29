package dev.nandi0813.practice.util;

import org.bukkit.Bukkit;

public enum SoftDependUtil {
    ;

    public static boolean isPAPI_ENABLED = false;
    public static boolean isFAWE_ENABLED = false;
    public static boolean isLITEBANS_ENABLED = false;

    static {
        if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null) {
            isFAWE_ENABLED = true;
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            isPAPI_ENABLED = true;
        }

        if (Bukkit.getPluginManager().getPlugin("LiteBans") != null) {
            isLITEBANS_ENABLED = true;
        }
    }

}
