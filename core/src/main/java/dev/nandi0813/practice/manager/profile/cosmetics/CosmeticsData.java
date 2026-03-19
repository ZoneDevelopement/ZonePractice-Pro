package dev.nandi0813.practice.manager.profile.cosmetics;

import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorSlot;
import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorTrimTier;
import dev.nandi0813.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
import dev.nandi0813.practice.manager.profile.cosmetics.shield.ShieldLayout;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CosmeticsData {

    private ArmorTrimTier activeTier = ArmorTrimTier.LEATHER;
    private DeathEffect deathEffect = DeathEffect.NONE;

    private final List<ShieldLayout> shieldLayouts = new ArrayList<>();
    private int activeShieldLayoutIndex = -1;

    private final Map<ArmorTrimTier, Map<ArmorSlot, SlotData>> tierData = new EnumMap<>(ArmorTrimTier.class);

    public CosmeticsData() {
        for (ArmorTrimTier tier : ArmorTrimTier.values()) {
            Map<ArmorSlot, SlotData> bySlot = new EnumMap<>(ArmorSlot.class);
            for (ArmorSlot armorSlot : ArmorSlot.values()) {
                bySlot.put(armorSlot, new SlotData());
            }
            tierData.put(tier, bySlot);
        }
    }

    public TrimPattern getPattern(ArmorTrimTier tier, ArmorSlot slot) {
        if (slot == null) {
            return null;
        }

        return getSlotData(tier, slot).pattern;
    }

    public TrimMaterial getMaterial(ArmorSlot slot) {
        return getMaterial(activeTier, slot);
    }

    public TrimMaterial getMaterial(ArmorTrimTier tier, ArmorSlot slot) {
        if (slot == null) {
            return null;
        }

        return getSlotData(tier, slot).material;
    }

    public void setPattern(ArmorTrimTier tier, ArmorSlot slot, TrimPattern pattern) {
        if (slot == null) {
            return;
        }

        getSlotData(tier, slot).pattern = pattern;
    }

    public void setMaterial(ArmorSlot slot, TrimMaterial material) {
        setMaterial(activeTier, slot, material);
    }

    public void setMaterial(ArmorTrimTier tier, ArmorSlot slot, TrimMaterial material) {
        if (slot == null) {
            return;
        }

        getSlotData(tier, slot).material = material;
    }

    private SlotData getSlotData(ArmorTrimTier tier, ArmorSlot slot) {
        ArmorTrimTier resolvedTier = tier == null ? ArmorTrimTier.LEATHER : tier;
        Map<ArmorSlot, SlotData> bySlot = tierData.get(resolvedTier);
        if (bySlot == null) {
            bySlot = new EnumMap<>(ArmorSlot.class);
            tierData.put(resolvedTier, bySlot);
        }

        return bySlot.computeIfAbsent(slot, k -> new SlotData());
    }

    public ShieldLayout getActiveShieldLayout() {
        if (activeShieldLayoutIndex < 0 || activeShieldLayoutIndex >= shieldLayouts.size()) return null;
        return shieldLayouts.get(activeShieldLayoutIndex);
    }

    public void setDeathEffect(DeathEffect deathEffect) {
        this.deathEffect = deathEffect == null ? DeathEffect.NONE : deathEffect;
    }

    public void setShieldLayouts(List<ShieldLayout> layouts) {
        this.shieldLayouts.clear();
        if (layouts != null) {
            this.shieldLayouts.addAll(layouts);
        }
    }

    private static final class SlotData {
        private TrimPattern pattern;
        private TrimMaterial material;
    }
}
