package dev.nandi0813.practice.manager.arena.setup;

import lombok.Getter;

@Getter
public enum SetupMode {

    CORNERS("Corner Selection",
            "Left Click: Set Corner 1",
            "Right Click: Set Corner 2"),

    SPAWN_POINTS("Spawn Points",
            "Right Click: Add Player Spawn",
            "Left Click: Remove Last Spawn"),

    BED_LOCATIONS("Bed Locations",
            "Right Click on Bed: Set Team Bed",
            "Left Click: Remove Bed"),

    PORTALS("Portal Setup",
            "Right Click: Add Portal Center/Region",
            "Left Click: Remove Portal"),

    TOGGLE_STATUS("Arena Status",
            "Right Click: Enable Arena",
            "Left Click: Disable Arena");

    private final String displayName;
    private final String[] description;

    SetupMode(String displayName, String... description) {
        this.displayName = displayName;
        this.description = description;
    }

    public SetupMode next() {
        int nextIndex = (this.ordinal() + 1) % values().length;
        return values()[nextIndex];
    }

    public SetupMode previous() {
        int prevIndex = (this.ordinal() - 1 + values().length) % values().length;
        return values()[prevIndex];
    }
}