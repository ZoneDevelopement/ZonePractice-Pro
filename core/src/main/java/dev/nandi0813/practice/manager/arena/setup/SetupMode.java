package dev.nandi0813.practice.manager.arena.setup;

import lombok.Getter;

@Getter
public enum SetupMode {

    CORNERS("Corner Selection", new String[]{
            "<aqua> Left Click: <white>Set Corner 1",
            "<aqua> Right Click: <white>Set Corner 2"
    }),

    POSITIONS("Spawn Points (Standard)", new String[]{
            "<aqua> Left Click Block: <white>Set Position 1 (Blue)",
            "<aqua> Right Click Block: <white>Set Position 2 (Red)"
    }),

    FFA_POSITIONS("Spawn Points (FFA)", new String[]{
            "<aqua> Right Click Block: <white>Add Spawn Point",
            "<aqua> Right Click Armor Stand: <white>Remove That Spawn",
            "<aqua> Left Click (Anywhere): <white>Remove Last Spawn"
    }),

    BUILD_MAX("Build Height Limit", new String[]{
            "<aqua> Right Click: <white>Set to Current Y-Level",
            "<aqua> Left Click: <white>Disable Build Limit"
    }),

    DEAD_ZONE("Dead Zone (Void)", new String[]{
            "<aqua> Right Click: <white>Set to Current Y-Level",
            "<aqua> Left Click: <white>Disable Dead Zone"
    }),

    BED_LOCATIONS("Bed Locations", new String[]{
            "<aqua> Left Click: <white>Set Blue Bed",
            "<aqua> Right Click: <white>Set Red Bed"
    }),

    PORTALS("Portal Setup", new String[]{
            "<aqua> Right Click: <white>Add Portal Region",
            "<aqua> Left Click: <white>Remove Portal Region"
    }),

    TOGGLE_STATUS("Arena Status", new String[]{
            "<aqua> Right Click: <white>Enable Arena",
    });

    private final String displayName;
    private final String[] description;

    SetupMode(String displayName, String[] description) {
        this.displayName = displayName;
        this.description = description;
    }
}