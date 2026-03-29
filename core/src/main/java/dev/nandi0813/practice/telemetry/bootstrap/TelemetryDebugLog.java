package dev.nandi0813.practice.telemetry.bootstrap;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.util.Common;

public enum TelemetryDebugLog {
    ;

    private static volatile boolean debugEnabled;

    public static void setDebugEnabled(boolean enabled) {
        debugEnabled = enabled;
    }

    public static boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static void info(String message) {
        if (!debugEnabled) {
            return;
        }

        ZonePractice.getInstance().getLogger().info(message);
    }

    public static void warning(String message) {
        if (!debugEnabled) {
            return;
        }

        ZonePractice.getInstance().getLogger().warning(message);
    }

    public static void console(String miniMessage) {
        if (!debugEnabled) {
            return;
        }

        Common.sendConsoleMMMessage(miniMessage);
    }
}

