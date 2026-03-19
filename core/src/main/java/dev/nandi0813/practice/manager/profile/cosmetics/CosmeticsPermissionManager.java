package dev.nandi0813.practice.manager.profile.cosmetics;

import dev.nandi0813.practice.manager.profile.cosmetics.armortrim.ArmorTrimTier;
import dev.nandi0813.practice.manager.profile.cosmetics.deatheffect.DeathEffect;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum CosmeticsPermissionManager {
    ;

    private static final int MAX_SHIELD_LAYOUTS = 21;

    private static final List<TrimPattern> REGISTERED_PATTERNS = new ArrayList<>();
    private static final List<TrimMaterial> REGISTERED_MATERIALS = new ArrayList<>();
    private static final List<DeathEffect> REGISTERED_DEATH_EFFECTS = new ArrayList<>();
    private static final Map<TrimPattern, String> PATTERN_IDS = new HashMap<>();
    private static final Map<TrimMaterial, String> MATERIAL_IDS = new HashMap<>();
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("([a-z0-9_.-]+):([a-z0-9_./-]+)");

    public static void registerAllPermissions() {
        PluginManager pluginManager = Bukkit.getPluginManager();

        registerPermission(pluginManager, "zpp.cosmetics.shield.use", "Use shield cosmetics.");
        registerPermission(pluginManager, "zpp.cosmetics.shield.layouts.*", "Use all shield layout slots.");
        registerPermission(pluginManager, "zpp.cosmetics.shield.layouts.unlimited", "Use unlimited shield layouts.");
        for (int layouts = 1; layouts <= MAX_SHIELD_LAYOUTS; layouts++) {
            registerPermission(pluginManager,
                    "zpp.cosmetics.shield.layouts." + layouts,
                    "Use up to " + layouts + " shield layouts.");
        }

        for (ArmorTrimTier tier : ArmorTrimTier.values()) {
            registerPermission(pluginManager, tier.getPermissionNode(), "Use " + tier.getDisplayName() + " armor tier cosmetics.");
        }

        REGISTERED_PATTERNS.clear();
        PATTERN_IDS.clear();
        var patternRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN);
        patternRegistry.keyStream().forEach(key -> {
            TrimPattern pattern = patternRegistry.get(key);
            if (pattern == null) {
                return;
            }

            String id = sanitizeId(key.getKey());
            REGISTERED_PATTERNS.add(pattern);
            PATTERN_IDS.put(pattern, id);
            registerPermission(pluginManager,
                    "zpp.cosmetics.armortrim.pattern." + id,
                    "Use armor trim pattern " + id + ".");
        });

        REGISTERED_MATERIALS.clear();
        MATERIAL_IDS.clear();
        var materialRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_MATERIAL);
        materialRegistry.keyStream().forEach(key -> {
            TrimMaterial material = materialRegistry.get(key);
            if (material == null) {
                return;
            }

            String id = sanitizeId(key.getKey());
            REGISTERED_MATERIALS.add(material);
            MATERIAL_IDS.put(material, id);
            registerPermission(pluginManager,
                    "zpp.cosmetics.armortrim.material." + id,
                    "Use armor trim material " + id + ".");
        });

        REGISTERED_PATTERNS.sort(Comparator.comparing(CosmeticsPermissionManager::getTrimId));
        REGISTERED_MATERIALS.sort(Comparator.comparing(CosmeticsPermissionManager::getTrimId));

        REGISTERED_DEATH_EFFECTS.clear();
        for (DeathEffect deathEffect : DeathEffect.values()) {
            String node = getDeathEffectPermissionNode(deathEffect);
            REGISTERED_DEATH_EFFECTS.add(deathEffect);
            registerPermission(pluginManager, node, "Use death effect " + deathEffect.getId() + ".");
        }
    }

    public static List<TrimPattern> getRegisteredPatterns() {
        return Collections.unmodifiableList(REGISTERED_PATTERNS);
    }

    public static List<TrimMaterial> getRegisteredMaterials() {
        return Collections.unmodifiableList(REGISTERED_MATERIALS);
    }

    public static List<DeathEffect> getRegisteredDeathEffects() {
        return Collections.unmodifiableList(REGISTERED_DEATH_EFFECTS);
    }

    public static boolean hasBasePermission(Player player, ArmorTrimTier tier) {
        if (player == null || tier == null) {
            return false;
        }

        return player.isOp()
                || player.hasPermission("zpp.cosmetics.armortrim.base.*")
                || player.hasPermission(tier.getPermissionNode());
    }

    public static boolean hasPatternPermission(Player player, TrimPattern pattern) {
        if (player == null || pattern == null) {
            return false;
        }

        return hasPatternPermission(player, "zpp.cosmetics.armortrim.pattern." + getTrimId(pattern));
    }

    public static boolean hasPatternPermission(Player player, String node) {
        if (player == null || node == null || node.isBlank()) {
            return false;
        }

        return player.isOp()
                || player.hasPermission("zpp.cosmetics.armortrim.pattern.*")
                || player.hasPermission(node);
    }

    public static boolean hasMaterialPermission(Player player, TrimMaterial material) {
        if (player == null || material == null) {
            return false;
        }

        return hasMaterialPermission(player, "zpp.cosmetics.armortrim.material." + getTrimId(material));
    }

    public static boolean hasMaterialPermission(Player player, String node) {
        if (player == null || node == null || node.isBlank()) {
            return false;
        }

        return player.isOp()
                || player.hasPermission("zpp.cosmetics.armortrim.material.*")
                || player.hasPermission(node);
    }

    public static String getDeathEffectPermissionNode(DeathEffect deathEffect) {
        String id = deathEffect == null ? "none" : deathEffect.getId();
        return DeathEffect.getPermissionNode(sanitizeId(id));
    }

    public static boolean hasDeathEffectPermission(Player player, DeathEffect deathEffect) {
        if (player == null || deathEffect == null) {
            return false;
        }

        if (deathEffect == DeathEffect.NONE) {
            return true;
        }

        return player.isOp()
                || player.hasPermission(DeathEffect.getPermissionNode("*"))
                || player.hasPermission(getDeathEffectPermissionNode(deathEffect));
    }

    public static boolean hasShieldPermission(Player player) {
        if (player == null) {
            return false;
        }

        return player.isOp()
                || player.hasPermission("zpp.cosmetics.shield.*")
                || player.hasPermission("zpp.cosmetics.shield.use");
    }

    public static int getMaxShieldLayouts(Player player) {
        if (player == null) {
            return 1;
        }

        if (player.isOp()
                || player.hasPermission("zpp.cosmetics.shield.*")
                || player.hasPermission("zpp.cosmetics.shield.layouts.*")
                || player.hasPermission("zpp.cosmetics.shield.layouts.unlimited")) {
            return MAX_SHIELD_LAYOUTS;
        }

        for (int layouts = MAX_SHIELD_LAYOUTS; layouts >= 1; layouts--) {
            if (player.hasPermission("zpp.cosmetics.shield.layouts." + layouts)) {
                return layouts;
            }
        }

        return 1;
    }

    public static String getTrimId(TrimPattern pattern) {
        if (pattern == null) {
            return "unknown";
        }

        String id = PATTERN_IDS.get(pattern);
        if (id != null) {
            return id;
        }

        for (Map.Entry<TrimPattern, String> entry : PATTERN_IDS.entrySet()) {
            if (entry.getKey().equals(pattern)) {
                return entry.getValue();
            }
        }

        return resolveTrimIdFallback(pattern);
    }

    public static String getTrimId(TrimMaterial material) {
        if (material == null) {
            return "unknown";
        }

        String id = MATERIAL_IDS.get(material);
        if (id != null) {
            return id;
        }

        for (Map.Entry<TrimMaterial, String> entry : MATERIAL_IDS.entrySet()) {
            if (entry.getKey().equals(material)) {
                return entry.getValue();
            }
        }

        return resolveTrimIdFallback(material);
    }

    private static String resolveTrimIdFallback(Object trimValue) {
        String raw = String.valueOf(trimValue).toLowerCase(Locale.ROOT);
        Matcher matcher = NAMESPACE_PATTERN.matcher(raw);
        if (matcher.find()) {
            return sanitizeId(matcher.group(2));
        }

        return sanitizeId(raw);
    }

    private static String sanitizeId(String value) {
        if (value == null || value.isBlank()) {
            return "unknown";
        }
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "");
    }

    private static void registerPermission(PluginManager pluginManager, String node, String description) {
        if (pluginManager.getPermission(node) != null) {
            return;
        }

        pluginManager.addPermission(new Permission(node, description, PermissionDefault.OP));
    }
}




