package dev.nandi0813.practice.manager.profile.enums;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum ProfilePrefixVisibility {

    NONE(LanguageManager.getString("PROFILE.PREFIX-VISIBILITY-NAMES.NONE"), false, false),
    PREFIX(LanguageManager.getString("PROFILE.PREFIX-VISIBILITY-NAMES.PREFIX"), true, false),
    SUFFIX(LanguageManager.getString("PROFILE.PREFIX-VISIBILITY-NAMES.SUFFIX"), false, true),
    PREFIX_AND_SUFFIX(LanguageManager.getString("PROFILE.PREFIX-VISIBILITY-NAMES.PREFIX-AND-SUFFIX"), true, true);

    @Getter
    private final String name;
    @Getter
    private final boolean showPrefix;
    @Getter
    private final boolean showSuffix;

    ProfilePrefixVisibility(String name, boolean showPrefix, boolean showSuffix) {
        this.name = name;
        this.showPrefix = showPrefix;
        this.showSuffix = showSuffix;
    }

    public static ProfilePrefixVisibility getNext(ProfilePrefixVisibility current) {
        List<ProfilePrefixVisibility> list = new ArrayList<>(Arrays.asList(ProfilePrefixVisibility.values()));

        if (current != null) {
            int index = list.indexOf(current);
            if (index == list.size() - 1) {
                return list.get(0);
            }
            return list.get(index + 1);
        }

        return list.get(0);
    }
}

