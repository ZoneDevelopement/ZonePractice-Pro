package dev.nandi0813.practice.manager.playerkit;

import dev.nandi0813.practice.manager.ladder.abstraction.playercustom.CustomLadder;
import dev.nandi0813.practice.manager.playerkit.guis.ShulkerBoxEditorGUI;
import dev.nandi0813.practice.manager.playerkit.items.KitItem;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PlayerKitEditing {

    private final CustomLadder customLadder;
    @Setter
    private KitItem kitItem;

    /** Non-null when the player is editing a slot INSIDE a shulker box. */
    @Setter
    private ShulkerBoxEditorGUI shulkerEditor = null;

    /** The shulker content slot index being modified (0-26), or -1 if not editing a shulker slot. */
    @Setter
    private int shulkerSlot = -1;

    public PlayerKitEditing(CustomLadder customLadder) {
        this.customLadder = customLadder;
    }

    /** True while editing a specific slot inside a shulker box. */
    public boolean isEditingShulker() {
        return shulkerEditor != null && shulkerSlot >= 0;
    }

    /** Clear shulker editing context. */
    public void clearShulkerContext() {
        this.shulkerEditor = null;
        this.shulkerSlot = -1;
    }
}
