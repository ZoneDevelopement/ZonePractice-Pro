package dev.nandi0813.practice.util;

import dev.nandi0813.practice.manager.backend.LanguageManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public enum StringUtil {
    ;

    public static String CC(String string) {
        return ChatColor.translateAlternateColorCodes('&', string);
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
                    .replaceAll("%seconds%", String.valueOf(NumberUtil.doubleToInt(seconds)))
                    .replaceAll("%secondName%", (seconds < 2 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC")));
        } else {
            return string
                    .replaceAll("%seconds%", String.valueOf(seconds))
                    .replaceAll("%secondName%", (seconds < 2 ? LanguageManager.getString("SECOND-NAME.1SEC") : LanguageManager.getString("SECOND-NAME.1<SEC")));
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

    public static Color translateChatColorToColor(ChatColor chatColor) {
        return switch (chatColor) {
            case AQUA -> Color.AQUA;
            case BLACK -> Color.BLACK;
            case BLUE -> Color.BLUE;
            case DARK_AQUA -> Color.TEAL;
            case DARK_BLUE -> Color.NAVY;
            case DARK_GRAY -> Color.GRAY;
            case DARK_GREEN -> Color.GREEN;
            case DARK_PURPLE -> Color.PURPLE;
            case DARK_RED -> Color.MAROON;
            case GOLD -> Color.ORANGE;
            case GRAY -> Color.SILVER;
            case GREEN -> Color.OLIVE;
            case LIGHT_PURPLE -> Color.FUCHSIA;
            case RED -> Color.RED;
            case WHITE -> Color.WHITE;
            case YELLOW -> Color.YELLOW;
            default -> null;
        };
    }

    public static String legacyColorToMiniMessage(String string) {
        return string
                .replaceAll("&0", "<black>")
                .replaceAll("&1", "<dark_blue>")
                .replaceAll("&2", "<dark_green>")
                .replaceAll("&3", "<dark_aqua>")
                .replaceAll("&4", "<dark_red>")
                .replaceAll("&5", "<dark_purple>")
                .replaceAll("&6", "<gold>")
                .replaceAll("&7", "<gray>")
                .replaceAll("&8", "<dark_gray>")
                .replaceAll("&9", "<blue>")
                .replaceAll("&a", "<green>")
                .replaceAll("&b", "<aqua>")
                .replaceAll("&c", "<red>")
                .replaceAll("&d", "<light_purple>")
                .replaceAll("&e", "<yellow>")
                .replaceAll("&f", "<white>")
                .replaceAll("&k", "<obf>")
                .replaceAll("&l", "<bold>")
                .replaceAll("&m", "<st>")
                .replaceAll("&n", "<u>")
                .replaceAll("&o", "<i>")
                .replaceAll("&r", "<reset>")
                .replaceAll("§0", "<black>")
                .replaceAll("§1", "<dark_blue>")
                .replaceAll("§2", "<dark_green>")
                .replaceAll("§3", "<dark_aqua>")
                .replaceAll("§4", "<dark_red>")
                .replaceAll("§5", "<dark_purple>")
                .replaceAll("§6", "<gold>")
                .replaceAll("§7", "<gray>")
                .replaceAll("§8", "<dark_gray>")
                .replaceAll("§9", "<blue>")
                .replaceAll("§a", "<green>")
                .replaceAll("§b", "<aqua>")
                .replaceAll("§c", "<red>")
                .replaceAll("§d", "<light_purple>")
                .replaceAll("§e", "<yellow>")
                .replaceAll("§f", "<white>")
                .replaceAll("§k", "<obf>")
                .replaceAll("§l", "<bold>")
                .replaceAll("§m", "<st>")
                .replaceAll("§n", "<u>")
                .replaceAll("§o", "<i>")
                .replaceAll("§r", "<reset>");
    }

}