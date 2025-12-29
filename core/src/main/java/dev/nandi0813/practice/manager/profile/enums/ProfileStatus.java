package dev.nandi0813.practice.manager.profile.enums;

public enum ProfileStatus {

    OFFLINE, // Player is offline.
    LOBBY, // Player is in the lobby with the normal lobby inventory.
    EDITOR, // Player is in the kit editor GUI.
    CUSTOM_EDITOR, // Player is in the custom kit editor GUI.
    QUEUE, // Player is waiting in queue.
    MATCH, // Player is in a match.
    FFA,
    EVENT, // Player is in an event.
    SPECTATE, // Player is spectating a match.
    STAFF_MODE // Player is in the lobby and has staff-mode on.

}
