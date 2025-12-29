package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;

public enum StartUpUtil {
    ;

    public static void loadStartUpProgressMap() {
        for (StartUpTypes startUpType : StartUpTypes.values())
            ZonePractice.getInstance().getStartUpProgress().put(startUpType, false);
    }

    public static boolean isStartUpReady() {
        for (boolean b : ZonePractice.getInstance().getStartUpProgress().values())
            if (!b) return false;

        return true;
    }

}
