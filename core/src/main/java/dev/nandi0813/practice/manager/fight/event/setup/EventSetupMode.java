package dev.nandi0813.practice.manager.fight.event.setup;

import lombok.Getter;

@Getter
public enum EventSetupMode {

    CORNERS("Corner Selection", new String[]{
            "&b Left Click: &fSet Corner 1",
            "&b Right Click: &fSet Corner 2"
    }),

    SPAWN_POINTS("Spawn Points", new String[]{
            "&b Right Click Block: &fAdd Spawn Point",
            "&b Right Click Armor Stand: &fRemove That Spawn",
            "&b Left Click (Anywhere): &fRemove Last Spawn"
    }),

    TOGGLE_STATUS("Event Status", new String[]{
            "&b Right Click: &fEnable Event",
    });

    private final String displayName;
    private final String[] description;

    EventSetupMode(String displayName, String[] description) {
        this.displayName = displayName;
        this.description = description;
    }
}
