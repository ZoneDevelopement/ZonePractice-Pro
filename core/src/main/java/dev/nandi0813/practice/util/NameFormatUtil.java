package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.enums.ProfilePrefixVisibility;
import dev.nandi0813.practice.manager.profile.group.Group;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

public enum NameFormatUtil {
    ;

    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER = PlainTextComponentSerializer.plainText();

    private static TextColor findFirstExplicitColor(Component component) {
        if (component == null) {
            return null;
        }

        if (component.color() != null) {
            return component.color();
        }

        for (Component child : component.children()) {
            TextColor childColor = findFirstExplicitColor(child);
            if (childColor != null) {
                return childColor;
            }
        }

        return null;
    }

    /**
     * Returns the last (trailing) explicit color found in a component tree.
     * Traverses depth-first left-to-right, tracking the most recently seen color.
     * This reflects the color that would visually "bleed" into the next component
     * appended after this one — i.e. the color active at the end of the text.
     */
    private static TextColor findLastExplicitColor(Component component) {
        if (component == null) return null;

        // Use an array so the lambda/recursive call can update it
        TextColor[] last = {null};

        java.util.function.Consumer<Component>[] walker = new java.util.function.Consumer[1];
        walker[0] = c -> {
            if (c == null) return;
            // Own color sets the "current" color for this node
            if (c.color() != null) {
                last[0] = c.color();
            }
            // Then visit children in order — later children override earlier ones
            for (Component child : c.children()) {
                walker[0].accept(child);
            }
        };

        walker[0].accept(component);
        return last[0];
    }

    public static Component parseConfiguredComponent(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }

        String normalized = raw;
        if (normalized.contains("&") || normalized.contains("\u00A7")) {
            normalized = StringUtil.legacyColorToMiniMessage(normalized);
        }

        return ZonePractice.getMiniMessage().deserialize(normalized);
    }

    public static Component applyDivisionPlaceholders(Component template, Profile profile) {
        if (template == null) return Component.empty();

        Component division = profile.getStats().getDivision() != null
                ? profile.getStats().getDivision().getComponentFullName()
                : Component.empty();
        Component divisionShort = profile.getStats().getDivision() != null
                ? profile.getStats().getDivision().getComponentShortName()
                : Component.empty();

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

    public static String normalizePlayerNameTemplate(String rawTemplate) {
        if (rawTemplate == null || rawTemplate.isEmpty()) {
            return rawTemplate;
        }

        String normalized = rawTemplate;
        if (normalized.contains("&") || normalized.contains("\u00A7")) {
            normalized = StringUtil.legacyColorToMiniMessage(normalized);
        }

        boolean hasPlayerPlaceholder = normalized.contains("%player%") || normalized.contains("%%player%%");
        if (hasPlayerPlaceholder) {
            return rawTemplate;
        }

        String plainText = PLAIN_TEXT_SERIALIZER.serialize(ZonePractice.getMiniMessage().deserialize(normalized)).trim();
        if (!plainText.isEmpty()) {
            return rawTemplate;
        }

        return rawTemplate + "%player%";
    }

    private static Component renderTemplate(String rawTemplate, Profile profile, String playerName) {
        return renderTemplate(rawTemplate, profile, playerName, null);
    }

    private static Component renderTemplate(String rawTemplate, Profile profile, String playerName, Player player) {
        if (rawTemplate == null || rawTemplate.isEmpty()) {
            return Component.empty();
        }

        String normalized = rawTemplate;

        // Resolve PAPI placeholders first, on the raw string, so that tokens like
        // %luckperms_prefix% are expanded before any color translation or MiniMessage
        // parsing. This ensures hex/legacy colors injected by PAPI are translated too.
        if (player != null && dev.nandi0813.practice.util.SoftDependUtil.isPAPI_ENABLED) {
            normalized = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, normalized);
        }

        // Translate ALL legacy & / § color codes and hex formats to MiniMessage tags.
        // This must run after PAPI (which injects legacy codes) and before MiniMessage
        // deserialization (which does not understand & or &#RRGGBB syntax).
        normalized = StringUtil.translateColorsToMiniMessage(normalized);

        String division = profile.getStats().getDivision() != null
                ? ZonePractice.getMiniMessage().serialize(profile.getStats().getDivision().getComponentFullName())
                : "";
        String divisionShort = profile.getStats().getDivision() != null
                ? ZonePractice.getMiniMessage().serialize(profile.getStats().getDivision().getComponentShortName())
                : "";

        normalized = normalized
                .replace("%division%", division)
                .replace("%%division%%", division)
                .replace("%division_short%", divisionShort)
                .replace("%%division_short%%", divisionShort);

        if (playerName != null) {
            normalized = normalized
                    .replace("%%player%%", playerName)
                    .replace("%player%", playerName);
        }

        return ZonePractice.getMiniMessage().deserialize(normalized);
    }

    public static Component resolvePrefix(Profile profile) {
        return resolvePrefix(profile, null);
    }

    public static Component resolveSuffix(Profile profile) {
        return resolveSuffix(profile, null);
    }

    /**
     * Resolves the player prefix with full PlaceholderAPI support.
     * When a player is supplied, PAPI is resolved on raw template strings (before
     * MiniMessage parsing) so that hex/legacy colors injected by expansions like
     * LuckPerms render correctly in the above-head nametag.
     */
    public static Component resolvePrefix(Profile profile, Player player) {
        ProfilePrefixVisibility visibility = profile.getPrefixVisibility();
        if (visibility == null || !visibility.isShowPrefix()) {
            return Component.empty();
        }

        Group group = profile.getGroup();
        Component prefix = Component.empty();

        if (group != null && group.getPrefix() != null) {
            if (group.getPrefixTemplate() != null) {
                // Template string path: PAPI resolves on raw string before MiniMessage parsing
                prefix = renderTemplate(group.getPrefixTemplate(), profile, null, player);
            } else {
                // Pre-parsed Component path: round-trip through PAPI string resolver
                prefix = applyPAPIPlaceholders(group.getPrefix(), player);
            }
        }

        if (profile.getPrefix() != null) {
            prefix = applyPAPIPlaceholders(profile.getPrefix(), player);
        }

        return applyDivisionPlaceholders(prefix, profile);
    }

    /**
     * Resolves the player suffix with full PlaceholderAPI support.
     */
    public static Component resolveSuffix(Profile profile, Player player) {
        ProfilePrefixVisibility visibility = profile.getPrefixVisibility();
        if (visibility == null || !visibility.isShowSuffix()) {
            return Component.empty();
        }

        Group group = profile.getGroup();
        Component suffix = Component.empty();

        if (group != null && group.getSuffix() != null) {
            if (group.getSuffixTemplate() != null) {
                suffix = renderTemplate(group.getSuffixTemplate(), profile, null, player);
            } else {
                suffix = applyPAPIPlaceholders(group.getSuffix(), player);
            }
        }

        if (profile.getSuffix() != null) {
            suffix = applyPAPIPlaceholders(profile.getSuffix(), player);
        }

        return applyDivisionPlaceholders(suffix, profile);
    }

    /**
     * Returns the last (trailing) explicit color from a component — used by callers
     * that need to know what color "bleeds out" of the prefix into the name.
     */
    public static TextColor extractTrailingColor(Component component) {
        return findLastExplicitColor(component);
    }

    public static Component resolveName(Profile profile, String playerName) {
        return resolveName(profile, playerName, null);
    }

    /**
     * Resolves the player name component.
     *
     * @param prefixColor When non-null, used as the name color if the name template
     *                    produces a component with no explicit color. This allows the
     *                    name to visually continue the prefix color (e.g. a red
     *                    "FOUNDER " prefix makes the name also red when NAME is %player%).
     */
    public static Component resolveName(Profile profile, String playerName, TextColor prefixColor) {
        Group group = profile.getGroup();

        Component nameComponent;
        if (profile.getNameTemplate() != null && !profile.getNameTemplate().isEmpty()) {
            nameComponent = renderTemplate(profile.getNameTemplate(), profile, playerName);
        } else if (group != null && group.getNameTemplate() != null) {
            nameComponent = renderTemplate(group.getNameTemplate(), profile, playerName);
        } else if (group != null && group.getNameFormat() != null) {
            nameComponent = applyPlayerPlaceholders(applyDivisionPlaceholders(group.getNameFormat(), profile), playerName);
        } else {
            nameComponent = Component.text(playerName == null ? "" : playerName, NamedTextColor.GRAY);
        }

        // If the resolved name has no explicit color of its own, inherit from the prefix.
        // This makes "%player%" with no color tag follow the prefix color automatically.
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

    // ── PlaceholderAPI-aware overloads ──────────────────────────────────────

    /**
     * Applies any remaining PlaceholderAPI placeholders (e.g. %luckperms_prefix%) to a
     * Component by round-tripping through MiniMessage serialisation.
     * If PAPI is not installed the component is returned unchanged.
     *
     * @param component The component that may contain PAPI placeholder text
     * @param player    The player context for PAPI
     * @return Component with PAPI placeholders resolved
     */
    public static Component applyPAPIPlaceholders(Component component, Player player) {
        if (component == null || player == null) return component;
        if (!dev.nandi0813.practice.util.SoftDependUtil.isPAPI_ENABLED) return component;

        // Serialise back to MiniMessage, let PAPI replace its tokens, then translate
        // any legacy (&c, &#FF0000, §x...) or bare-hex colors PAPI may have injected
        // before re-parsing so they render correctly instead of appearing as raw text.
        String serialized = ZonePractice.getMiniMessage().serialize(component);
        String resolved   = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, serialized);
        String translated = dev.nandi0813.practice.util.StringUtil.translateColorsToMiniMessage(resolved);
        return ZonePractice.getMiniMessage().deserialize(translated);
    }

    /**
     * Resolves the full display name (prefix + name + suffix) with PAPI support.
     */
    public static Component resolveFullName(Profile profile, Player player, String playerName) {
        return resolvePrefix(profile, player)
                .append(resolveName(profile, playerName))
                .append(resolveSuffix(profile, player));
    }

    public static NamedTextColor resolveScoreboardColor(Profile profile, String playerName, NamedTextColor fallback) {
        Component nameComponent = resolveName(profile, playerName);

        TextColor color = findFirstExplicitColor(nameComponent);
        if (color != null) {
            if (color instanceof NamedTextColor named) {
                return named;
            }
            return NamedTextColor.nearestTo(color);
        }

        return fallback != null ? fallback : NamedTextColor.GRAY;
    }
}