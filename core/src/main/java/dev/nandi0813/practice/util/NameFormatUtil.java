package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.enums.ProfilePrefixVisibility;
import dev.nandi0813.practice.manager.profile.group.Group;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public enum NameFormatUtil {
    ;

    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();

    // ── Color helpers ────────────────────────────────────────────────────────

    private static TextColor findFirstExplicitColor(Component component) {
        if (component == null) return null;
        if (component.color() != null) return component.color();
        for (Component child : component.children()) {
            TextColor childColor = findFirstExplicitColor(child);
            if (childColor != null) return childColor;
        }
        return null;
    }

    /**
     * Returns the last (trailing) explicit color in a component tree — i.e. the color
     * that would visually "bleed" into the next component appended after this one.
     */
    private static TextColor findLastExplicitColor(Component component) {
        if (component == null) return null;
        TextColor last = component.color(); // own color is the starting value
        for (Component child : component.children()) {
            TextColor childLast = findLastExplicitColor(child);
            if (childLast != null) last = childLast; // later children override
        }
        return last;
    }

    /**
     * Public wrapper — used by {@link dev.nandi0813.practice.manager.inventory.InventoryUtil}
     * to extract the trailing prefix color so it can be inherited by the name component.
     */
    public static TextColor extractTrailingColor(Component component) {
        return findLastExplicitColor(component);
    }

    // ── Template / placeholder helpers ──────────────────────────────────────

    public static Component parseConfiguredComponent(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        String normalized = raw;
        if (normalized.contains("&") || normalized.contains("\u00A7")) {
            normalized = StringUtil.legacyColorToMiniMessage(normalized);
        }
        return ZonePractice.getMiniMessage().deserialize(normalized);
    }

    public static Component applyDivisionPlaceholders(Component template, Profile profile) {
        if (template == null) return Component.empty();
        Component division = profile.getStats().getDivision() != null
                ? profile.getStats().getDivision().getComponentFullName() : Component.empty();
        Component divisionShort = profile.getStats().getDivision() != null
                ? profile.getStats().getDivision().getComponentShortName() : Component.empty();
        return template
                .replaceText(TextReplacementConfig.builder().matchLiteral("%division%").replacement(division).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%division_short%").replacement(divisionShort).build());
    }

    public static Component applyPlayerPlaceholders(Component template, String playerName) {
        if (template == null) return Component.empty();
        Component player = Component.text(playerName == null ? "" : playerName);
        return template
                .replaceText(TextReplacementConfig.builder().matchLiteral("%player%").replacement(player).build())
                .replaceText(TextReplacementConfig.builder().matchLiteral("%%player%%").replacement(player).build());
    }

    /**
     * Applies any PlaceholderAPI placeholders present in {@code component} by
     * serialising to MiniMessage, running PAPI, translating legacy/hex color codes,
     * then re-parsing.  Returns {@code component} unchanged if PAPI is not installed
     * or {@code player} is null.
     */
    public static Component applyPAPIPlaceholders(Component component, Player player) {
        if (component == null || player == null) return component;
        if (!SoftDependUtil.isPAPI_ENABLED) return component;
        String serialized = ZonePractice.getMiniMessage().serialize(component);
        String resolved   = PlaceholderAPI.setPlaceholders(player, serialized);
        String translated = StringUtil.translateColorsToMiniMessage(resolved);
        return ZonePractice.getMiniMessage().deserialize(translated);
    }

    public static String normalizePlayerNameTemplate(String rawTemplate) {
        if (rawTemplate == null || rawTemplate.isEmpty()) return rawTemplate;
        String normalized = rawTemplate;
        if (normalized.contains("&") || normalized.contains("\u00A7")) {
            normalized = StringUtil.legacyColorToMiniMessage(normalized);
        }
        boolean hasPlayerPlaceholder = normalized.contains("%player%") || normalized.contains("%%player%%");
        if (hasPlayerPlaceholder) return rawTemplate;
        String plainText = PLAIN_TEXT_SERIALIZER.serialize(ZonePractice.getMiniMessage().deserialize(normalized)).trim();
        if (!plainText.isEmpty()) return rawTemplate;
        return rawTemplate + "%player%";
    }

    // ── Internal rendering ───────────────────────────────────────────────────

    private static Component renderTemplate(String rawTemplate, Profile profile, String playerName) {
        return renderTemplate(rawTemplate, profile, playerName, null);
    }

    /**
     * Renders a raw config template string into a {@link Component}.
     * <p>
     * Processing order:
     * <ol>
     *   <li>PAPI placeholders resolved on the raw string (when {@code player} is provided)</li>
     *   <li>Legacy / hex color codes translated to MiniMessage tags</li>
     *   <li>Internal {@code %division%} and {@code %player%} tokens substituted</li>
     *   <li>MiniMessage deserialization</li>
     * </ol>
     */
    private static Component renderTemplate(String rawTemplate, Profile profile, String playerName, Player player) {
        if (rawTemplate == null || rawTemplate.isEmpty()) return Component.empty();

        String normalized = rawTemplate;

        if (player != null && SoftDependUtil.isPAPI_ENABLED) {
            normalized = PlaceholderAPI.setPlaceholders(player, normalized);
        }

        normalized = StringUtil.translateColorsToMiniMessage(normalized);

        String division = profile.getStats().getDivision() != null
                ? ZonePractice.getMiniMessage().serialize(profile.getStats().getDivision().getComponentFullName()) : "";
        String divisionShort = profile.getStats().getDivision() != null
                ? ZonePractice.getMiniMessage().serialize(profile.getStats().getDivision().getComponentShortName()) : "";

        normalized = normalized
                .replace("%division%", division).replace("%%division%%", division)
                .replace("%division_short%", divisionShort).replace("%%division_short%%", divisionShort);

        if (playerName != null) {
            normalized = normalized.replace("%%player%%", playerName).replace("%player%", playerName);
        }

        return ZonePractice.getMiniMessage().deserialize(normalized);
    }

    // ── Public resolution API ────────────────────────────────────────────────

    public static Component resolvePrefix(Profile profile) {
        return resolvePrefix(profile, null);
    }

    /**
     * Resolves the group/profile prefix for {@code profile}.  When {@code player} is
     * provided, PAPI placeholders (e.g. {@code %luckperms_prefix%}) are resolved on
     * the raw template string before MiniMessage parsing so that hex/legacy colors
     * injected by expansions render correctly.
     */
    public static Component resolvePrefix(Profile profile, Player player) {
        ProfilePrefixVisibility visibility = profile.getPrefixVisibility();
        if (visibility == null || !visibility.isShowPrefix()) return Component.empty();

        Group group = profile.getGroup();
        Component prefix = Component.empty();

        if (group != null && group.getPrefix() != null) {
            prefix = group.getPrefixTemplate() != null
                    ? renderTemplate(group.getPrefixTemplate(), profile, null, player)
                    : applyPAPIPlaceholders(group.getPrefix(), player);
        }

        if (profile.getPrefix() != null) {
            prefix = applyPAPIPlaceholders(profile.getPrefix(), player);
        }

        return applyDivisionPlaceholders(prefix, profile);
    }

    public static Component resolveSuffix(Profile profile) {
        return resolveSuffix(profile, null);
    }

    /**
     * Resolves the group/profile suffix for {@code profile} — see {@link #resolvePrefix(Profile, Player)}
     * for PAPI behaviour.
     */
    public static Component resolveSuffix(Profile profile, Player player) {
        ProfilePrefixVisibility visibility = profile.getPrefixVisibility();
        if (visibility == null || !visibility.isShowSuffix()) return Component.empty();

        Group group = profile.getGroup();
        Component suffix = Component.empty();

        if (group != null && group.getSuffix() != null) {
            suffix = group.getSuffixTemplate() != null
                    ? renderTemplate(group.getSuffixTemplate(), profile, null, player)
                    : applyPAPIPlaceholders(group.getSuffix(), player);
        }

        if (profile.getSuffix() != null) {
            suffix = applyPAPIPlaceholders(profile.getSuffix(), player);
        }

        return applyDivisionPlaceholders(suffix, profile);
    }

    public static Component resolveName(Profile profile, String playerName) {
        return resolveName(profile, playerName, null, null);
    }

    /**
     * Resolves the display name component for {@code profile} without PlaceholderAPI support.
     *
     * @param prefixColor When non-null, applied as the name color if the resolved name
     *                    has no explicit color.  This lets a plain {@code %player%} name
     *                    template automatically inherit the prefix color (e.g. red
     *                    "FOUNDER " makes the name red too).
     */
    public static Component resolveName(Profile profile, String playerName, TextColor prefixColor) {
        return resolveName(profile, playerName, null, prefixColor);
    }

    /**
     * Resolves the display name component for {@code profile} with full PlaceholderAPI support.
     * When {@code player} is non-null, PAPI expansions present in the name template (e.g.
     * {@code %luckperms_prefix%}) are resolved on the raw string before MiniMessage parsing,
     * matching the behaviour of {@link #resolvePrefix(Profile, Player)} and
     * {@link #resolveSuffix(Profile, Player)}.
     *
     * @param player      Online player used for PAPI resolution; may be {@code null}.
     * @param prefixColor When non-null, applied as the name color if the resolved name has no
     *                    explicit color, letting the name inherit the trailing prefix colour.
     */
    public static Component resolveName(Profile profile, String playerName, Player player, TextColor prefixColor) {
        Group group = profile.getGroup();

        Component nameComponent;
        if (profile.getNameTemplate() != null && !profile.getNameTemplate().isEmpty()) {
            nameComponent = renderTemplate(profile.getNameTemplate(), profile, playerName, player);
        } else if (group != null && group.getNameTemplate() != null) {
            nameComponent = renderTemplate(group.getNameTemplate(), profile, playerName, player);
        } else if (group != null && group.getNameFormat() != null) {
            nameComponent = applyPlayerPlaceholders(applyDivisionPlaceholders(group.getNameFormat(), profile), playerName);
        } else {
            nameComponent = Component.text(playerName == null ? "" : playerName, NamedTextColor.GRAY);
        }

        if (prefixColor != null && findFirstExplicitColor(nameComponent) == null) {
            nameComponent = nameComponent.color(prefixColor);
        }

        return nameComponent;
    }

    public static Component resolveFullName(Profile profile, String playerName) {
        return resolvePrefix(profile)
                .append(resolveName(profile, playerName))
                .append(resolveSuffix(profile));
    }

    /**
     * Resolves the full display name with PlaceholderAPI support.
     * PAPI is applied to prefix, name, and suffix templates when {@code player} is non-null.
     * The name component inherits the trailing prefix color when it has no explicit color.
     */
    public static Component resolveFullName(Profile profile, Player player, String playerName) {
        Component prefix = resolvePrefix(profile, player);
        Component name   = resolveName(profile, playerName, player, extractTrailingColor(prefix));
        Component suffix = resolveSuffix(profile, player);
        return prefix.append(name).append(suffix);
    }

    public static NamedTextColor resolveScoreboardColor(Profile profile, String playerName, NamedTextColor fallback) {
        TextColor color = findFirstExplicitColor(resolveName(profile, playerName));
        if (color != null) {
            return color instanceof NamedTextColor named ? named : NamedTextColor.nearestTo(color);
        }
        return fallback != null ? fallback : NamedTextColor.GRAY;
    }
}
