package dev.nandi0813.practice.manager.fight.event.setup;

import lombok.Getter;

@Getter
public enum EventSetupMode {

    CORNERS("Corner Selection", new String[]{
            "<aqua> Left Click: <white>Set Corner 1",
            "<aqua> Right Click: <white>Set Corner 2"
    }),

    SPAWN_POINTS("Spawn Points", new String[]{
            "<aqua> Right Click Block: <white>Add Spawn Point",
            "<aqua> Right Click Armor Stand: <white>Remove That Spawn",
            "<aqua> Left Click (Anywhere): <white>Remove Last Spawn"
    }),

    TOGGLE_STATUS("Event Status", new String[]{
            "<aqua> Right Click: <white>Enable Event",
    });

    private final String displayName;
    private final String[] description;

    EventSetupMode(String displayName, String[] description) {
        this.displayName = displayName;
        this.description = description;
    }
}
