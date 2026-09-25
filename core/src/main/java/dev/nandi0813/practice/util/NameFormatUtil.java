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

public final class NameFormatUtil {

    private NameFormatUtil() {
    }

    private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER =
            PlainTextComponentSerializer.plainText();

    private static TextColor findFirstExplicitColor(Component component) {
        if (component == null) {
            return null;
        }

        if (component.color() != null) {
            return component.color();
        }

        for (Component child : component.children()) {
            TextColor color = findFirstExplicitColor(child);
            if (color != null) {
                return color;
            }
        }

        return null;
    }

    /**
     * Returns the last explicitly defined color in the component tree.
     * This color is used when the following component should inherit the
     * trailing color of the previous component.
     */
    private static TextColor findLastExplicitColor(Component component) {
        if (component == null) {
            return null;
        }

        TextColor lastColor = component.color();

        for (Component child : component.children()) {
            TextColor childColor = findLastExplicitColor(child);
            if (childColor != null) {
                lastColor = childColor;
            }
        }

        return lastColor;
    }

    /**
     * Extracts the trailing color from a component.
     *
     * @param component component to inspect
     * @return trailing explicit color, or {@code null} if none is defined
     */
    public static TextColor extractTrailingColor(Component component) {
        return findLastExplicitColor(component);
    }

    /**
     * Parses a configured legacy/MiniMessage string into an Adventure component.
     *
     * @param raw raw configured text
     * @return parsed component, or an empty component when the input is empty
     */
    public static Component parseConfiguredComponent(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }

        return ZonePractice.getMiniMessage()
                .deserialize(StringUtil.legacyToMiniMessage(raw));
    }

    /**
     * Replaces division placeholders inside an Adventure component.
     *
     * @param template component containing division placeholders
     * @param profile profile used to resolve the current division
     * @return component with division placeholders resolved
     */
    public static Component applyDivisionPlaceholders(
            Component template,
            Profile profile
    ) {
        if (template == null) {
            return Component.empty();
        }

        Component division = Component.empty();
        Component divisionShort = Component.empty();

        if (profile != null
                && profile.getStats() != null
                && profile.getStats().getDivision() != null) {

            division = profile.getStats()
                    .getDivision()
                    .getComponentFullName();

            divisionShort = profile.getStats()
                    .getDivision()
                    .getComponentShortName();
        }

        return template
                .replaceText(replace("%division%", division))
                .replaceText(replace("%%division%%", division))
                .replaceText(replace("%division_short%", divisionShort))
                .replaceText(replace("%%division_short%%", divisionShort));
    }

    /**
     * Replaces player name placeholders inside an Adventure component.
     *
     * @param template component containing player placeholders
     * @param playerName player name to insert
     * @return component with player placeholders resolved
     */
    public static Component applyPlayerPlaceholders(
            Component template,
            String playerName
    ) {
        if (template == null) {
            return Component.empty();
        }

        Component player = Component.text(
                playerName == null ? "" : playerName
        );

        return template
                .replaceText(replace("%player%", player))
                .replaceText(replace("%%player%%", player));
    }

    private static TextReplacementConfig replace(
            String placeholder,
            Component replacement
    ) {
        return TextReplacementConfig.builder()
                .matchLiteral(placeholder)
                .replacement(replacement)
                .build();
    }

    /**
     * Applies PlaceholderAPI placeholders to an already parsed component.
     * PlaceholderAPI is skipped when it is unavailable or no player is provided.
     *
     * @param component component containing PAPI placeholders
     * @param player player used for placeholder resolution
     * @return resolved component
     */
    public static Component applyPAPIPlaceholders(
            Component component,
            Player player
    ) {
        if (component == null
                || player == null
                || !SoftDependUtil.isPAPI_ENABLED) {
            return component;
        }

        String serialized = ZonePractice.getMiniMessage()
                .serialize(component);

        String resolved = PlaceholderAPI.setPlaceholders(
                player,
                serialized
        );

        return ZonePractice.getMiniMessage()
                .deserialize(StringUtil.legacyToMiniMessage(resolved));
    }

    /**
     * Ensures a name template contains a player placeholder.
     *
     * @param rawTemplate configured name template
     * @return normalized name template
     */
    public static String normalizePlayerNameTemplate(String rawTemplate) {
        if (rawTemplate == null || rawTemplate.isEmpty()) {
            return rawTemplate;
        }

        rawTemplate = StringUtil.stripObfuscationTags(rawTemplate);

        if (rawTemplate.contains("%player%")
                || rawTemplate.contains("%%player%%")) {
            return rawTemplate;
        }

        try {
            Component component = ZonePractice.getMiniMessage()
                    .deserialize(StringUtil.legacyToMiniMessage(rawTemplate));

            String plainText = PLAIN_TEXT_SERIALIZER
                    .serialize(component)
                    .trim();

            if (!plainText.isEmpty()) {
                return rawTemplate;
            }
        } catch (Exception ignored) {
        }

        return rawTemplate + "%player%";
    }

    /**
     * Renders a raw configured template.
     *
     * <p>Processing order:</p>
     * <ol>
     *     <li>PlaceholderAPI expansion</li>
     *     <li>Legacy color conversion</li>
     *     <li>MiniMessage parsing</li>
     *     <li>Internal division and player placeholders</li>
     * </ol>
     */
    private static Component renderTemplate(
            String rawTemplate,
            Profile profile,
            String playerName,
            Player player
    ) {
        if (rawTemplate == null || rawTemplate.isEmpty()) {
            return Component.empty();
        }

        String normalized = rawTemplate;

        if (player != null && SoftDependUtil.isPAPI_ENABLED) {
            normalized = PlaceholderAPI.setPlaceholders(
                    player,
                    normalized
            );
        }

        normalized = StringUtil.legacyToMiniMessage(normalized);

        Component component = ZonePractice.getMiniMessage()
                .deserialize(normalized);

        component = applyDivisionPlaceholders(
                component,
                profile
        );

        if (playerName != null) {
            component = applyPlayerPlaceholders(
                    component,
                    playerName
            );
        }

        return component;
    }

    /**
     * Resolves the visible group/profile prefix.
     *
     * @param profile profile to resolve
     * @return resolved prefix
     */
    public static Component resolvePrefix(Profile profile) {
        return resolvePrefix(profile, null);
    }

    /**
     * Resolves the visible group/profile prefix with PlaceholderAPI support.
     *
     * @param profile profile to resolve
     * @param player player used for PlaceholderAPI resolution
     * @return resolved prefix
     */
    public static Component resolvePrefix(
            Profile profile,
            Player player
    ) {
        if (profile == null) {
            return Component.empty();
        }

        ProfilePrefixVisibility visibility =
                profile.getPrefixVisibility();

        if (visibility == null || !visibility.isShowPrefix()) {
            return Component.empty();
        }

        Group group = profile.getGroup();
        Component prefix = Component.empty();

        if (group != null) {
            if (group.getPrefixTemplate() != null
                    && !group.getPrefixTemplate().isEmpty()) {

                prefix = renderTemplate(
                        group.getPrefixTemplate(),
                        profile,
                        null,
                        player
                );
            } else if (group.getPrefix() != null) {
                prefix = applyPAPIPlaceholders(
                        group.getPrefix(),
                        player
                );
            }
        }

        if (profile.getPrefix() != null) {
            prefix = applyPAPIPlaceholders(
                    profile.getPrefix(),
                    player
            );
        }

        return applyDivisionPlaceholders(
                prefix,
                profile
        );
    }

    /**
     * Resolves the visible group/profile suffix.
     *
     * @param profile profile to resolve
     * @return resolved suffix
     */
    public static Component resolveSuffix(Profile profile) {
        return resolveSuffix(profile, null);
    }

    /**
     * Resolves the visible group/profile suffix with PlaceholderAPI support.
     *
     * @param profile profile to resolve
     * @param player player used for PlaceholderAPI resolution
     * @return resolved suffix
     */
    public static Component resolveSuffix(
            Profile profile,
            Player player
    ) {
        if (profile == null) {
            return Component.empty();
        }

        ProfilePrefixVisibility visibility =
                profile.getPrefixVisibility();

        if (visibility == null || !visibility.isShowSuffix()) {
            return Component.empty();
        }

        Group group = profile.getGroup();
        Component suffix = Component.empty();

        if (group != null) {
            if (group.getSuffixTemplate() != null
                    && !group.getSuffixTemplate().isEmpty()) {

                suffix = renderTemplate(
                        group.getSuffixTemplate(),
                        profile,
                        null,
                        player
                );
            } else if (group.getSuffix() != null) {
                suffix = applyPAPIPlaceholders(
                        group.getSuffix(),
                        player
                );
            }
        }

        if (profile.getSuffix() != null) {
            suffix = applyPAPIPlaceholders(
                    profile.getSuffix(),
                    player
            );
        }

        return applyDivisionPlaceholders(
                suffix,
                profile
        );
    }

    /**
     * Resolves a player's display name without PlaceholderAPI support.
     *
     * @param profile player profile
     * @param playerName player name
     * @return resolved display name
     */
    public static Component resolveName(
            Profile profile,
            String playerName
    ) {
        return resolveName(
                profile,
                playerName,
                null,
                null
        );
    }

    /**
     * Resolves a player's display name and optionally inherits the prefix color.
     *
     * @param profile player profile
     * @param playerName player name
     * @param prefixColor color inherited from the prefix
     * @return resolved display name
     */
    public static Component resolveName(
            Profile profile,
            String playerName,
            TextColor prefixColor
    ) {
        return resolveName(
                profile,
                playerName,
                null,
                prefixColor
        );
    }

    /**
     * Resolves a player's display name with PlaceholderAPI support.
     *
     * <p>If the name template has no explicit color, the trailing prefix color
     * is inherited when one is provided.</p>
     *
     * @param profile player profile
     * @param playerName player name
     * @param player player used for PlaceholderAPI resolution
     * @param prefixColor color inherited from the prefix
     * @return resolved display name
     */
    public static Component resolveName(
            Profile profile,
            String playerName,
            Player player,
            TextColor prefixColor
    ) {
        if (profile == null) {
            return Component.text(
                    playerName == null ? "" : playerName,
                    NamedTextColor.GRAY
            );
        }

        Group group = profile.getGroup();
        Component nameComponent;

        if (profile.getNameTemplate() != null
                && !profile.getNameTemplate().isEmpty()) {

            nameComponent = renderTemplate(
                    profile.getNameTemplate(),
                    profile,
                    playerName,
                    player
            );

        } else if (group != null
                && group.getNameTemplate() != null
                && !group.getNameTemplate().isEmpty()) {

            nameComponent = renderTemplate(
                    group.getNameTemplate(),
                    profile,
                    playerName,
                    player
            );

        } else if (group != null
                && group.getNameFormat() != null) {

            nameComponent = applyPlayerPlaceholders(
                    applyDivisionPlaceholders(
                            group.getNameFormat(),
                            profile
                    ),
                    playerName
            );

        } else {
            nameComponent = Component.text(
                    playerName == null ? "" : playerName,
                    NamedTextColor.GRAY
            );
        }

        if (prefixColor != null
                && findFirstExplicitColor(nameComponent) == null) {

            nameComponent = nameComponent.color(prefixColor);
        }

        return nameComponent;
    }

    /**
     * Resolves the complete display name without PlaceholderAPI support.
     *
     * @param profile player profile
     * @param playerName player name
     * @return prefix, name and suffix combined into one component
     */
    public static Component resolveFullName(
            Profile profile,
            String playerName
    ) {
        if (profile == null) {
            return Component.text(
                    playerName == null ? "" : playerName,
                    NamedTextColor.GRAY
            );
        }

        Component prefix = resolvePrefix(profile);

        Component name = resolveName(
                profile,
                playerName,
                extractTrailingColor(prefix)
        );

        Component suffix = resolveSuffix(profile);

        return prefix
                .append(name)
                .append(suffix);
    }

    /**
     * Resolves the complete display name with PlaceholderAPI support.
     *
     * <p>The player's name inherits the trailing prefix color when the name
     * itself does not define an explicit color.</p>
     *
     * @param profile player profile
     * @param player player used for PlaceholderAPI resolution
     * @param playerName player name
     * @return prefix, name and suffix combined into one component
     */
    public static Component resolveFullName(
            Profile profile,
            Player player,
            String playerName
    ) {
        if (profile == null) {
            return Component.text(
                    playerName == null ? "" : playerName,
                    NamedTextColor.GRAY
            );
        }

        Component prefix = resolvePrefix(
                profile,
                player
        );

        Component name = resolveName(
                profile,
                playerName,
                player,
                extractTrailingColor(prefix)
        );

        Component suffix = resolveSuffix(
                profile,
                player
        );

        return prefix
                .append(name)
                .append(suffix);
    }

    /**
     * Resolves the first explicit name color for scoreboard usage.
     *
     * @param profile player profile
     * @param playerName player name
     * @param fallback color returned when the name has no explicit color
     * @return resolved scoreboard color
     */
    public static NamedTextColor resolveScoreboardColor(
            Profile profile,
            String playerName,
            NamedTextColor fallback
    ) {
        Component name = resolveName(
                profile,
                playerName
        );

        TextColor color = findFirstExplicitColor(name);

        if (color instanceof NamedTextColor named) {
            return named;
        }

        if (color != null) {
            return NamedTextColor.nearestTo(color);
        }

        return fallback != null
                ? fallback
                : NamedTextColor.GRAY;
    }
}
