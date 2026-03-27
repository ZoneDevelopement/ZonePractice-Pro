package dev.nandi0813.practice.manager.profile.cosmetics.armortrim;

import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsData;
import dev.nandi0813.practice.manager.profile.cosmetics.CosmeticsPermissionManager;
import dev.nandi0813.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
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

        EnumSet<ArmorSlot> supportedSlots = EnumSet.of(
                ArmorSlot.HELMET,
                ArmorSlot.CHESTPLATE,
                ArmorSlot.LEGGINGS,
                ArmorSlot.BOOTS
        );

        for (ArmorTrimTier tier : ArmorTrimTier.values()) {
            boolean hasTierPermission = CosmeticsPermissionManager.hasBasePermission(player, tier);

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

                if (pattern != null && !CosmeticsPermissionManager.hasPatternPermission(player, pattern)) {
                    profile.getCosmeticsData().setPattern(tier, slot, null);
                    changed = true;
                }

                if (material != null && !CosmeticsPermissionManager.hasMaterialPermission(player, material)) {
                    profile.getCosmeticsData().setMaterial(tier, slot, null);
                    changed = true;
                }
            }
        }

        ArmorTrimTier activeTier = profile.getCosmeticsData().getActiveTier();
        if (!CosmeticsPermissionManager.hasBasePermission(player, activeTier)) {
            ArmorTrimTier replacement = null;
            for (ArmorTrimTier tier : ArmorTrimTier.values()) {
                if (CosmeticsPermissionManager.hasBasePermission(player, tier)) {
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

        DeathEffect deathEffect = profile.getCosmeticsData().getDeathEffect();
        if (deathEffect != null && !CosmeticsPermissionManager.hasDeathEffectPermission(player, deathEffect)) {
            profile.getCosmeticsData().setDeathEffect(DeathEffect.NONE);
            changed = true;
        }

        CosmeticsData.LobbyItemType lobbyItemType = profile.getCosmeticsData().getLobbyItemType();
        if (lobbyItemType != null && !CosmeticsPermissionManager.hasLobbyItemPermission(player, lobbyItemType)) {
            profile.getCosmeticsData().setLobbyItemType(CosmeticsData.LobbyItemType.NONE);
            changed = true;
        }

        int maxShieldLayouts = CosmeticsPermissionManager.getMaxShieldLayouts(player);
        var shieldLayouts = profile.getCosmeticsData().getShieldLayouts();
        while (shieldLayouts.size() > maxShieldLayouts) {
            shieldLayouts.remove(shieldLayouts.size() - 1);
            changed = true;
        }

        int activeShieldLayoutIndex = profile.getCosmeticsData().getActiveShieldLayoutIndex();
        if (!CosmeticsPermissionManager.hasShieldPermission(player)
                || activeShieldLayoutIndex < -1
                || activeShieldLayoutIndex >= shieldLayouts.size()) {
            if (activeShieldLayoutIndex != -1) {
                profile.getCosmeticsData().setActiveShieldLayoutIndex(-1);
                changed = true;
            }
        }

        if (changed) {
            profile.saveData();
        }

        return changed;
    }
}

