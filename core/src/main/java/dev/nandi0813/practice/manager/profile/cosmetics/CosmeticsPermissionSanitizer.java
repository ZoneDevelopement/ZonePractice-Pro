package dev.nandi0813.practice.manager.profile.cosmetics;

import dev.nandi0813.practice.manager.profile.Profile;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.EnumSet;

public enum CosmeticsPermissionSanitizer {
    ;

    public static boolean sanitize(Player player, Profile profile) {
        if (player == null || profile == null || profile.getCosmeticsData() == null) {
            return false;
        }

        boolean changed = false;

        // Shield customization is intentionally skipped until its dedicated rework.
        EnumSet<ArmorSlot> supportedSlots = EnumSet.of(
                ArmorSlot.HELMET,
                ArmorSlot.CHESTPLATE,
                ArmorSlot.LEGGINGS,
                ArmorSlot.BOOTS
        );

        for (ArmorTrimTier tier : ArmorTrimTier.values()) {
            boolean hasTierPermission = ArmorTrimPermissionManager.hasBasePermission(player, tier);

            for (ArmorSlot slot : supportedSlots) {
                TrimPattern pattern = profile.getCosmeticsData().getPattern(tier, slot);
                TrimMaterial material = profile.getCosmeticsData().getMaterial(tier, slot);

                if (!hasTierPermission) {
                    if (pattern != null) {
                        profile.getCosmeticsData().setPattern(tier, slot, null);
                        changed = true;
                    }

                    if (material != null) {
                        profile.getCosmeticsData().setMaterial(tier, slot, null);
                        changed = true;
                    }

                    continue;
                }

                if (pattern != null && !ArmorTrimPermissionManager.hasPatternPermission(player, pattern)) {
                    profile.getCosmeticsData().setPattern(tier, slot, null);
                    changed = true;
                }

                if (material != null && !ArmorTrimPermissionManager.hasMaterialPermission(player, material)) {
                    profile.getCosmeticsData().setMaterial(tier, slot, null);
                    changed = true;
                }
            }
        }

        ArmorTrimTier activeTier = profile.getCosmeticsData().getActiveTier();
        if (!ArmorTrimPermissionManager.hasBasePermission(player, activeTier)) {
            ArmorTrimTier replacement = null;
            for (ArmorTrimTier tier : ArmorTrimTier.values()) {
                if (ArmorTrimPermissionManager.hasBasePermission(player, tier)) {
                    replacement = tier;
                    break;
                }
            }

            if (replacement == null) {
                replacement = ArmorTrimTier.LEATHER;
            }

            if (replacement != activeTier) {
                profile.getCosmeticsData().setActiveTier(replacement);
                changed = true;
            }
        }

        if (changed) {
            profile.saveData();
        }

        return changed;
    }
}

