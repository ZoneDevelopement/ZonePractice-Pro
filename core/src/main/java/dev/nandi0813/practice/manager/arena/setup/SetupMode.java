package dev.nandi0813.practice.manager.arena.setup;

import lombok.Getter;

@Getter
public enum SetupMode {

    CORNERS("Corner Selection", new String[]{
            "&b Left Click: &fSet Corner 1",
            "&b Right Click: &fSet Corner 2"
    }),

    POSITIONS("Spawn Points (Standard)", new String[]{
            "&b Left Click Block: &fSet Position 1 (Blue)",
            "&b Right Click Block: &fSet Position 2 (Red)"
    }),

    FFA_POSITIONS("Spawn Points (FFA)", new String[]{
            "&b Right Click Block: &fAdd Spawn Point",
            "&b Right Click Armor Stand: &fRemove That Spawn",
            "&b Left Click (Anywhere): &fRemove Last Spawn"
    }),

    BUILD_MAX("Build Height Limit", new String[]{
            "&b Right Click: &fSet to Current Y-Level",
            "&b Left Click: &fDisable Build Limit"
    }),

    DEAD_ZONE("Dead Zone (Void)", new String[]{
            "&b Right Click: &fSet to Current Y-Level",
            "&b Left Click: &fDisable Dead Zone"
    }),

    BED_LOCATIONS("Bed Locations", new String[]{
            "&b Left Click: &fSet Blue Bed",
            "&b Right Click: &fSet Red Bed"
    }),

    PORTALS("Portal Setup", new String[]{
            "&b Right Click: &fAdd Portal Region",
            "&b Left Click: &fRemove Portal Region"
    }),

    TOGGLE_STATUS("Arena Status", new String[]{
            "&b Right Click: &fEnable Arena",
    });

    private final String displayName;
    private final String[] description;

    SetupMode(String displayName, String[] description) {
        this.displayName = displayName;
        this.description = description;
    }
}