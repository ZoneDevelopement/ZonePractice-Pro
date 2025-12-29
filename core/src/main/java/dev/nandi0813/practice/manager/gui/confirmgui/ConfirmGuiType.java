package dev.nandi0813.practice.manager.gui.confirmgui;

import lombok.Getter;

@Getter
public enum ConfirmGuiType {

    ARENA_DELETE(""),
    LADDER_DELETE(""),
    LADDER_DISABLE(""),
    HOLOGRAM_DELETE(""),
    ARENA_COPY_DELETE(""),
    FFA_ARENA_DISABLE(""),
    FFA_ARENA_CLOSE(""),
    RESET_CUSTOM_KIT(""),
    FFA_ARENA_DELETE("");

    private final String description;

    ConfirmGuiType(String description) {
        this.description = description;
    }

}
