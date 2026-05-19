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
}