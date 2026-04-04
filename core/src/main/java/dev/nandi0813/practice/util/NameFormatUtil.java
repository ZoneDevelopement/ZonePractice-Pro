package dev.nandi0813.practice.util;

import dev.nandi0813.practice.ZonePractice;
import dev.nandi0813.practice.manager.profile.Profile;
import dev.nandi0813.practice.manager.profile.group.Group;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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
        if (rawTemplate == null || rawTemplate.isEmpty()) {
            return Component.empty();
        }

        String normalized = rawTemplate;
        if (normalized.contains("&") || normalized.contains("\u00A7")) {
            normalized = StringUtil.legacyColorToMiniMessage(normalized);
        }

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
        Group group = profile.getGroup();
        Component prefix = Component.empty();

        if (group != null && group.getPrefix() != null) {
            if (group.getPrefixTemplate() != null) {
                prefix = renderTemplate(group.getPrefixTemplate(), profile, null);
            } else {
                prefix = group.getPrefix();
            }
        }

        if (profile.getPrefix() != null) {
            prefix = profile.getPrefix();
        }

        return applyDivisionPlaceholders(prefix, profile);
    }

    public static Component resolveSuffix(Profile profile) {
        Group group = profile.getGroup();
        Component suffix = Component.empty();

        if (group != null && group.getSuffix() != null) {
            if (group.getSuffixTemplate() != null) {
                suffix = renderTemplate(group.getSuffixTemplate(), profile, null);
            } else {
                suffix = group.getSuffix();
            }
        }

        if (profile.getSuffix() != null) {
            suffix = profile.getSuffix();
        }

        return applyDivisionPlaceholders(suffix, profile);
    }

    public static Component resolveName(Profile profile, String playerName) {
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
        return nameComponent;
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