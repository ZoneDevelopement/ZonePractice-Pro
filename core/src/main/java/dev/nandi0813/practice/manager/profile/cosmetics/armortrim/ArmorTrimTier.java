package dev.nandi0813.practice.manager.profile.cosmetics.armortrim;

import dev.nandi0813.practice.manager.backend.GUIFile;
import lombok.Getter;
import org.bukkit.Material;

import java.util.Locale;

/**
 * Represents the armor base tier used for cosmetics preview and selection.
 */
public enum ArmorTrimTier {
    LEATHER("leather", "Leather", Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS),
    GOLD("gold", "Gold", Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS),
    IRON("iron", "Iron", Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS),
    DIAMOND("diamond", "Diamond", Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS),
    NETHERITE("netherite", "Netherite", Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS);

    @Getter
    private final String id;
    private final String defaultDisplayName;
    private final Material defaultHelmetMaterial;
    private final Material defaultChestplateMaterial;
    private final Material defaultLeggingsMaterial;
    private final Material defaultBootsMaterial;

    ArmorTrimTier(String id, String defaultDisplayName, Material defaultHelmetMaterial, Material defaultChestplateMaterial, Material defaultLeggingsMaterial, Material defaultBootsMaterial) {
        this.id = id;
        this.defaultDisplayName = defaultDisplayName;
        this.defaultHelmetMaterial = defaultHelmetMaterial;
        this.defaultChestplateMaterial = defaultChestplateMaterial;
        this.defaultLeggingsMaterial = defaultLeggingsMaterial;
        this.defaultBootsMaterial = defaultBootsMaterial;
    }

    public String getDisplayName() {
        String configKey = "GUIS.COSMETICS.ARMOR-TIERS." + this.name() + ".DISPLAY-NAME";
        String configValue = GUIFile.getString(configKey);
        return !configValue.isBlank() ? configValue : defaultDisplayName;
    }

    public String getPermissionNode() {
        return "zpp.cosmetics.armortrim.base." + id;
    }

    public Material getMaterial(ArmorSlot slot) {
        if (slot == null) {
            return Material.AIR;
        }

        String configKey = "GUIS.COSMETICS.ARMOR-TIERS." + this.name() + "." + getConfigSlotKey(slot);
        String materialName = GUIFile.getString(configKey);
        
        if (!materialName.isBlank()) {
            try {
                return Material.valueOf(materialName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
                // Fall through to default
            }
        }

        return switch (slot) {
            case HELMET -> defaultHelmetMaterial;
            case CHESTPLATE -> defaultChestplateMaterial;
            case LEGGINGS -> defaultLeggingsMaterial;
            case BOOTS -> defaultBootsMaterial;
            case SHIELD -> Material.SHIELD;
        };
    }

    private static String getConfigSlotKey(ArmorSlot slot) {
        return switch (slot) {
            case HELMET -> "HELMET-MATERIAL";
            case CHESTPLATE -> "CHESTPLATE-MATERIAL";
            case LEGGINGS -> "LEGGINGS-MATERIAL";
            case BOOTS -> "BOOTS-MATERIAL";
            case SHIELD -> "SHIELD-MATERIAL";
        };
    }

    public ArmorTrimTier next() {
        ArmorTrimTier[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }

    public static ArmorTrimTier fromId(String id) {
        if (id == null || id.isBlank()) {
            return LEATHER;
        }

        String normalized = id.toLowerCase(Locale.ROOT);
        for (ArmorTrimTier tier : values()) {
            if (tier.id.equals(normalized)) {
                return tier;
            }
        }

        return LEATHER;
    }
}

