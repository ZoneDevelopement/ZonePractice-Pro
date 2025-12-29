package dev.nandi0813.practice.util;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;

public enum TPSUtil {
    ;

    private static Object minecraftServer;
    private static Field recentTps;

    public static double get1MinTPSRounded() {
        return round(getRecentTPS()[0]);
    }

    public static double[] getRecentTPS() {
        try {
            if (minecraftServer == null) {
                Server server = Bukkit.getServer();
                Field consoleField = server.getClass().getDeclaredField("console");
                consoleField.setAccessible(true);
                minecraftServer = consoleField.get(server);
            }

            if (recentTps == null) {
                recentTps = minecraftServer.getClass().getSuperclass().getDeclaredField("recentTps");
                recentTps.setAccessible(true);
            }

            return (double[]) recentTps.get(minecraftServer);
        } catch (IllegalAccessException | NoSuchFieldException ignored) {
        }
        return new double[]{20, 20, 20};
    }

    private static double round(double value) {
        if (value > 20.0)
            return 20.0;

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

}
