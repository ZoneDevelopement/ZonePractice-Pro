package dev.nandi0813.practice.manager.profile.cosmetics.shield;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.DyeColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.PatternType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A named shield design saved by a player.
 * Stores a base colour plus up to MAX_LAYERS banner pattern layers (colour + pattern).
 */
@Getter
@Setter
public class ShieldLayout {

    public static final int MAX_LAYERS = 6;

    /** Display name shown in the layout list GUI. */
    private String name;

    /** Base banner colour (null = white). */
    private DyeColor baseColor;

    /** Ordered list of pattern layers (like a real banner – bottom → top). */
    private final List<PatternLayer> layers;

    public ShieldLayout(String name, DyeColor baseColor) {
        this.name   = name;
        this.baseColor = baseColor;
        this.layers = new ArrayList<>();
    }

    /** Add a layer if MAX_LAYERS not reached. Returns true on success. */
    public boolean addLayer(DyeColor color, PatternType pattern) {
        if (layers.size() >= MAX_LAYERS) return false;
        layers.add(new PatternLayer(color, pattern));
        return true;
    }

    /** Remove the topmost layer. Returns true if something was removed. */
    public boolean removeTopLayer() {
        if (layers.isEmpty()) return false;
        layers.removeLast();
        return true;
    }

    // ── Serialisation helpers ────────────────────────────────────────

    /** Serialise to a single string for YAML storage: "name|BASE_COLOR|COLOR:PATTERN,COLOR:PATTERN,..." */
    public String serialise() {
        var bannerPatternRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BANNER_PATTERN);
        StringBuilder sb = new StringBuilder();
        sb.append(escapePipe(name)).append("|");
        sb.append(baseColor != null ? baseColor.name() : "WHITE");
        for (PatternLayer l : layers) {
            sb.append("|").append(l.color().name()).append(":").append(bannerPatternRegistry.getKeyOrThrow(l.pattern()));
        }
        return sb.toString();
    }

    /** Deserialise from the format produced by {@link #serialise()}. Returns null on error. */
    public static ShieldLayout deserialise(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String[] parts = raw.split("\\|", -1);
        if (parts.length < 2) return null;
        try {
            String  layoutName = unescapePipe(parts[0]);
            DyeColor base = DyeColor.valueOf(parts[1]);
            ShieldLayout layout = new ShieldLayout(layoutName, base);
            for (int i = 2; i < parts.length; i++) {
                String[] lp = parts[i].split(":", 2);
                if (lp.length == 2) {
                    DyeColor lc = DyeColor.valueOf(lp[0]);
                    PatternType pt = parsePatternType(lp[1]);
                    if (pt != null) {
                        layout.addLayer(lc, pt);
                    }
                }
            }
            return layout;
        } catch (Exception e) {
            return null;
        }
    }

    private static PatternType parsePatternType(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (!normalized.contains(":")) {
            normalized = "minecraft:" + normalized;
        }

        NamespacedKey key = NamespacedKey.fromString(normalized);
        if (key == null) {
            return null;
        }

        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.BANNER_PATTERN)
                .get(key);
    }

    private static String escapePipe(String s)   { return s.replace("|", "\\|"); }
    private static String unescapePipe(String s) { return s.replace("\\|", "|"); }

    // ── PatternLayer record ──────────────────────────────────────────

    public record PatternLayer(DyeColor color, PatternType pattern) {
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof PatternLayer(DyeColor color1, PatternType pattern1))) return false;
            return Objects.equals(color, color1) && Objects.equals(pattern, pattern1);
        }
    }
}
