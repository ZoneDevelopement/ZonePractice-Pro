package dev.nandi0813.practice.manager.profile.cosmetics.armortrim;

import lombok.Getter;

/**
 * Enum representing the different armor slots available for cosmetics.
 */
@Getter
public enum ArmorSlot {
    HELMET("helmet", "Helmet"),
    CHESTPLATE("chestplate", "Chestplate"),
    LEGGINGS("leggings", "Leggings"),
    BOOTS("boots", "Boots"),
    SHIELD("shield", "Shield");
    private final String id;
    private final String displayName;
    ArmorSlot(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

}
