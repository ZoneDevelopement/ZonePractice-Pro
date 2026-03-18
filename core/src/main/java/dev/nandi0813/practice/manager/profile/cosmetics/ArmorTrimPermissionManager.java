package dev.nandi0813.practice.manager.profile.cosmetics;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum ArmorTrimPermissionManager {
    ;

    private static final List<TrimPattern> REGISTERED_PATTERNS = new ArrayList<>();
    private static final List<TrimMaterial> REGISTERED_MATERIALS = new ArrayList<>();
    private static final Map<TrimPattern, String> PATTERN_IDS = new HashMap<>();
    private static final Map<TrimMaterial, String> MATERIAL_IDS = new HashMap<>();
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("([a-z0-9_.-]+):([a-z0-9_./-]+)");

    public static void registerAllPermissions() {
        PluginManager pluginManager = Bukkit.getPluginManager();

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
                    "zpp.cosmetics.pattern." + id,
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
                    "zpp.cosmetics.material." + id,
                    "Use armor trim material " + id + ".");
        });

        REGISTERED_PATTERNS.sort(Comparator.comparing(ArmorTrimPermissionManager::getTrimId));
        REGISTERED_MATERIALS.sort(Comparator.comparing(ArmorTrimPermissionManager::getTrimId));
    }

    public static List<TrimPattern> getRegisteredPatterns() {
        return Collections.unmodifiableList(REGISTERED_PATTERNS);
    }

    public static List<TrimMaterial> getRegisteredMaterials() {
        return Collections.unmodifiableList(REGISTERED_MATERIALS);
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
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "");
    }

    private static void registerPermission(PluginManager pluginManager, String node, String description) {
        if (pluginManager.getPermission(node) != null) {
            return;
        }

        pluginManager.addPermission(new Permission(node, description, PermissionDefault.OP));
    }
}


