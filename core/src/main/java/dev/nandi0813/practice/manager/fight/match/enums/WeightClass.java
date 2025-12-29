package dev.nandi0813.practice.manager.fight.match.enums;

import dev.nandi0813.practice.manager.backend.ConfigManager;
import dev.nandi0813.practice.util.Common;
import dev.nandi0813.practice.util.StringUtil;

public enum WeightClass {

    UNRANKED(ConfigManager.getConfig().getString("MATCH-SETTINGS.WEIGHT-CLASS.UNRANKED")),
    RANKED(ConfigManager.getConfig().getString("MATCH-SETTINGS.WEIGHT-CLASS.RANKED"));

    private final String name;

    WeightClass(String name) {
        this.name = name;
    }

    public String getName() {
        return StringUtil.CC(this.name);
    }

    public String getMMName() {
        return Common.serializeNormalToMMString(this.name);
    }

}
