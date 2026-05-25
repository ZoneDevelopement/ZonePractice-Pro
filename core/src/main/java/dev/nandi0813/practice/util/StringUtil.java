package dev.nandi0813.practice.util;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public final class StringUtil {

    private StringUtil() {}

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

    private static final Map<Character, String> LEGACY_TO_MM = Map.ofEntries(
            Map.entry('0', "black"),
            Map.entry('1', "dark_blue"),
            Map.entry('2', "dark_green"),
            Map.entry('3', "dark_aqua"),
            Map.entry('4', "dark_red"),
            Map.entry('5', "dark_purple"),
            Map.entry('6', "gold"),
            Map.entry('7', "gray"),
            Map.entry('8', "dark_gray"),
            Map.entry('9', "blue"),
            Map.entry('a', "green"),
            Map.entry('b', "aqua"),
            Map.entry('c', "red"),
            Map.entry('d', "light_purple"),
            Map.entry('e', "yellow"),
            Map.entry('f', "white"),
            Map.entry('k', "obfuscated"),
            Map.entry('l', "bold"),
            Map.entry('m', "strikethrough"),
            Map.entry('n', "underlined"),
            Map.entry('o', "italic"),
            Map.entry('r', "reset")
    );

    public static String legacyToMiniMessage(String text) {
        if (text == null || text.isEmpty()) return text;

        StringBuilder out = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if ((c == '&' || c == '§') && i + 1 < text.length()) {
                char code = Character.toLowerCase(text.charAt(++i));

                // hex
                if (code == '#' && i + 6 < text.length()) {
                    String hex = text.substring(i + 1, i + 7);

                    if (hex.matches("[0-9a-fA-F]{6}")) {
                        out.append("<#").append(hex).append(">");
                        i += 6;
                        continue;
                    }
                }

                // legacy
                String tag = LEGACY_TO_MM.get(code);
                if (tag != null) {
                    out.append('<').append(tag).append('>');
                    continue;
                }

                out.append(c).append(code);
                continue;
            }

            out.append(c);
        }

        return out.toString();
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
}