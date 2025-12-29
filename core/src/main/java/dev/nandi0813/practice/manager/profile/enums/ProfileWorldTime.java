package dev.nandi0813.practice.manager.profile.enums;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ProfileWorldTime {

    SUNRISE(LanguageManager.getString("PROFILE.WORLD-TIME-NAMES.SUNRISE"), 24000),
    DAY(LanguageManager.getString("PROFILE.WORLD-TIME-NAMES.DAY"), 6000),
    SUNSET(LanguageManager.getString("PROFILE.WORLD-TIME-NAMES.SUNSET"), 12000),
    NIGHT(LanguageManager.getString("PROFILE.WORLD-TIME-NAMES.NIGHT"), 18000);

    @Getter
    private final String name;
    @Getter
    private final long time;

    ProfileWorldTime(String name, long time) {
        this.name = name;
        this.time = time;
    }

    public static ProfileWorldTime getNextWorldTime(ProfileWorldTime profileWorldTime) {
        List<ProfileWorldTime> list = new ArrayList<>(Arrays.asList(ProfileWorldTime.values()));

        if (profileWorldTime != null) {
            int c = list.indexOf(profileWorldTime);

            if (list.size() - 1 == c)
                return list.get(0);
            else
                return list.get(c + 1);
        } else
            return list.get(0);
    }

}
