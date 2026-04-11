package dev.nandi0813.practice.util;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public enum StringUtil {
    ;

    public static String CC(String string) {
        if (string == null) {
            return "";
        }
        return LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(string));
    }

    public static List<String> CC(List<String> stringlist) {
        List<String> list = new ArrayList<>();
        for (String string : stringlist) {
            list.add(CC(string));
        }
        return list;
    }

    public static String replaceSecondString(String string, double seconds) {
        if ((seconds == Math.floor(seconds)) && !Double.isInfinite(seconds)) {
            return string
                    .replace("%seconds%", String.valueOf(NumberUtil.doubleToInt(seconds)))
                    .replace("%secondName%", (seconds < 2 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC")));
        } else {
            return string
                    .replace("%seconds%", String.valueOf(seconds))
                    .replace("%secondName%", (seconds < 2 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC")));
        }
    }

    public static String getDate(long timeMilis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date resultdate = new Date(timeMilis);
        return sdf.format(resultdate);
    }

    public static String formatMillisecondsToMinutes(long l) {
        int h1 = (int) (l / 1000L) % 60;
        int h2 = (int) (l / 60000L % 60L);
        return String.format("%02d:%02d", h2, h1);
    }

    public static boolean isNotInteger(String s) {
        return !isInteger(s, 10);
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }

    public static String getNormalizedName(String name) {
        return StringUtils.capitalize(name.replace("_", " ").toLowerCase());
    }


    /**
     * Converts a string that may contain legacy color codes (both & and § prefix)
     * as well as hex color codes in the formats &#RRGGBB, &x&R&R&G&G&B&B,
     * §x§R§R§G§G§B§B, and #RRGGBB into their MiniMessage equivalents.
     *
     * This is the correct entry-point for strings returned by PlaceholderAPI,
     * since PAPI expansions (e.g. LuckPerms) typically produce legacy-coded strings.
     */
    public static String translateColorsToMiniMessage(String string) {
        if (string == null || string.isEmpty()) return string;

        // ── 1. §x§R§R§G§G§B§B  (Bungeecord/vanilla hex, section-prefixed) ──
        // Pattern: §x followed by six §<char> pairs
        string = string.replaceAll(
                "§x§([0-9A-Fa-f])§([0-9A-Fa-f])§([0-9A-Fa-f])§([0-9A-Fa-f])§([0-9A-Fa-f])§([0-9A-Fa-f])",
                "<#$1$2$3$4$5$6>"
        );

        // ── 2. &x&R&R&G&G&B&B  (same but ampersand-prefixed) ──
        string = string.replaceAll(
                "&x&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])&([0-9A-Fa-f])",
                "<#$1$2$3$4$5$6>"
        );

        // ── 3. &#RRGGBB  (common shorthand used by many plugins) ──
        string = string.replaceAll("&#([0-9A-Fa-f]{6})", "<#$1>");

        // ── 4. §#RRGGBB  (section variant of the shorthand) ──
        string = string.replaceAll("§#([0-9A-Fa-f]{6})", "<#$1>");

        // ── 5. Standalone #RRGGBB at word boundary (bare hex, used by some expansions) ──
        string = string.replaceAll("(?<![&§<])#([0-9A-Fa-f]{6})(?![0-9A-Fa-f>])", "<#$1>");

        // ── 6. Standard legacy & and § codes ──
        return legacyColorToMiniMessage(string);
    }

    public static String legacyColorToMiniMessage(String string) {
        return string
                .replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&k", "<obf>")
                .replace("&l", "<bold>")
                .replace("&m", "<st>")
                .replace("&n", "<u>")
                .replace("&o", "<i>")
                .replace("&r", "<reset>")
                .replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§k", "<obf>")
                .replace("§l", "<bold>")
                .replace("§m", "<st>")
                .replace("§n", "<u>")
                .replace("§o", "<i>")
                .replace("§r", "<reset>");
    }

}